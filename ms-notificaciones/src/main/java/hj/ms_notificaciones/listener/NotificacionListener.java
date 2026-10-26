package hj.ms_notificaciones.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.html2pdf.HtmlConverter;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionListener {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();
    private final TemplateEngine templateEngine;

    @Value("${SLACK_WEBHOOK_URL}")
    private String slackWebhookUrl;

    // Constantes para evitar duplicidad de literales
    private static final String CAMPO_NOMBRE_PRODUCTO = "nombreProducto";
    private static final String CAMPO_PRECIO_UNITARIO = "precioUnitario";
    private static final String CAMPO_CANTIDAD = "cantidad";
    private static final String CAMPO_SUBTOTAL = "subtotal";
    private static final String CAMPO_SUBTOTAL_NETO = "subtotalNeto";
    private static final String CAMPO_IVA = "iva";
    private static final String CAMPO_TOTAL = "total";

    @RabbitListener(queues = "q.cmd.email")
    public void procesarCorreo(String mensaje) {
        try {
            log.info("📩 Recibido en q.cmd.email. Leyendo datos...");
            
            JsonNode json = objectMapper.readTree(mensaje);
            String destinatario = json.get("usuarioEmail").asText();
            String estado = json.get("estado").asText();
            String pedidoId = json.get("pedidoId").asText();

            // REGLA DE NEGOCIO: Si el estado es CREADO, omitimos el envío del correo
            if ("CREADO".equals(estado)) {
                log.info("🛑 El pedido #{} está en estado CREADO (interno) ", pedidoId);
                return;
            }

            String textoPersonalizado = obtenerMensajePorEstado(estado);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Actualización de Pedido #" + pedidoId + " - Tienda de insectos H&J");
            helper.setText("Hola,\n\n" + textoPersonalizado + "\n\nSaludos cordiales,\nEl equipo de Tienda de insectos H&J");
            
            // Adjuntar boleta PDF solo si fue ACEPTADO (Lógica extraída a método privado)
            if ("ACEPTADO".equals(estado)) {
                adjuntarBoletaPdf(pedidoId, destinatario, json, helper);
            }
            
            mailSender.send(mimeMessage);
            log.info("✅ Correo enviado exitosamente a {} con estado {}", destinatario, estado);

        } catch (Exception e) {
            log.error("❌ Error enviando el correo: {}", e.getMessage());
            throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("Fallo al enviar correo", e); 
        }
    }

    private void adjuntarBoletaPdf(String pedidoId, String destinatario, JsonNode json, MimeMessageHelper helper) throws Exception {
        Context context = new Context();
        context.setVariable("pedidoId", pedidoId);
        context.setVariable("clienteEmail", destinatario);

        // 1. Extraer la lista de productos (items) del JSON
        List<Map<String, Object>> itemsList = new ArrayList<>();
        JsonNode itemsNode = json.get("items");
        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode item : itemsNode) {
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put(CAMPO_NOMBRE_PRODUCTO, item.has(CAMPO_NOMBRE_PRODUCTO) ? item.get(CAMPO_NOMBRE_PRODUCTO).asText() : "Producto sin nombre");
                mapItem.put(CAMPO_PRECIO_UNITARIO, item.has(CAMPO_PRECIO_UNITARIO) ? item.get(CAMPO_PRECIO_UNITARIO).asInt() : 0);
                mapItem.put(CAMPO_CANTIDAD, item.has(CAMPO_CANTIDAD) ? item.get(CAMPO_CANTIDAD).asInt() : 0);
                mapItem.put(CAMPO_SUBTOTAL, item.has(CAMPO_SUBTOTAL) ? item.get(CAMPO_SUBTOTAL).asInt() : 0);
                itemsList.add(mapItem);
            }
        }
        context.setVariable("items", itemsList);

        // 2. Pasar los totales globales de la boleta
        context.setVariable(CAMPO_SUBTOTAL_NETO, json.has(CAMPO_SUBTOTAL_NETO) ? json.get(CAMPO_SUBTOTAL_NETO).asInt() : 0);
        context.setVariable(CAMPO_IVA, json.has(CAMPO_IVA) ? json.get(CAMPO_IVA).asInt() : 0);
        context.setVariable(CAMPO_TOTAL, json.has(CAMPO_TOTAL) ? json.get(CAMPO_TOTAL).asInt() : 0);

        // 3. Procesar la plantilla HTML
        String htmlBoleta = templateEngine.process("boleta", context);

        // 4. Convertir el HTML a PDF usando iText
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlBoleta, outputStream);
        ByteArrayResource archivoPdf = new ByteArrayResource(outputStream.toByteArray());

        // 5. Adjuntar el PDF real
        helper.addAttachment("Boleta_Pedido_" + pedidoId + ".pdf", archivoPdf);
        log.info("📎 PDF de boleta generado y adjuntado exitosamente.");
    }

    @RabbitListener(queues = "q.cmd.bodega")
    public void procesarTicketBodega(String mensaje) {
        log.info("📦 Recibido en q.cmd.bodega: Generando ticket de despacho...");
    }

    @RabbitListener(queues = "q.cmd.email.dlq")
    public void procesarErrorCorreo(String mensaje) {
        log.warn("🚨 Mensaje capturado en DLQ. Enviando alerta a Slack...");
        String textoSlack = "⚠️ *Alerta Crítica - Tienda de insectos H&J* \nFalló el envío de un correo a un cliente.\n*Detalle:* `" + mensaje + "`";
        
        try {
            restClient.post()
                    .uri(slackWebhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", textoSlack))
                    .retrieve()
                    .toBodilessEntity();
            log.info("✅ Alerta enviada a Slack exitosamente.");
        } catch (Exception e) {
            log.error("❌ Falló la comunicación con Slack: {}", e.getMessage());
        }
    }

    // Opciones de respuestas según el estado
    private String obtenerMensajePorEstado(String estado) {
        return switch (estado) {
            case "ACEPTADO" -> "Su compra ha sido realizado con éxito, en breve será preparada para su despacho. Adjunto en este correo encontrará su boleta. Muchas gracias por su compra.";
            case "EN_PREPARACION" -> "Su pedido está siendo preparado en bodega para su despacho, le avisaremos cuando salga a reparto.";
            case "DESPACHADO" -> "Su pedido acaba de salir de bodega y ya se encuentra en reparto.";
            case "ENTREGADO" -> "Su pedido ha sido entregado exitosamente, esperamos que pueda disfrutar de su nueva mascota. Muchas gracias por elegirnos.\n\nTienda de insectos H&J.\nProyecto DuocUC";
            case "CANCELADO" -> "Su pedido ha sido cancelado.";
            default -> "El estado de su pedido ha sido actualizado a: " + estado + ".";
        };
    }
}

