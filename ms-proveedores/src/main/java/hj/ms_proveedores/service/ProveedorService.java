package hj.ms_proveedores.service;

import com.itextpdf.html2pdf.HtmlConverter;
import hj.ms_proveedores.dto.OrdenCompraDto;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ProveedorService {

    private static final Logger log = LoggerFactory.getLogger(ProveedorService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${proveedores.terrestres}")
    private String emailTerrestres;

    @Value("${proveedores.voladores}")
    private String emailVoladores;

    @Value("${proveedores.acuaticos}")
    private String emailAcuaticos;

    public ProveedorService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void procesarOrden(OrdenCompraDto orden) {
        String emailDestino = obtenerEmailPorCategoria(orden.getCategoria());
        String numeroOrden = "OC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        log.info("Generando Orden de Compra {} ({} unidades) para: {}", numeroOrden, orden.getCantidadRequerida(), orden.getProductoNombre());
        
        try {
            Context context = new Context();
            context.setVariable("numeroOrden", numeroOrden);
            context.setVariable("fechaEmision", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("categoriaProveedor", orden.getCategoria());
            context.setVariable("emailProveedor", emailDestino);
            context.setVariable("productoId", orden.getProductoId());
            context.setVariable("productoNombre", orden.getProductoNombre());

            String htmlProcess = templateEngine.process("orden-compra", context);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlProcess, outputStream);
            ByteArrayResource archivoPdf = new ByteArrayResource(outputStream.toByteArray());

            enviarCorreoProveedor(emailDestino, numeroOrden, htmlProcess, archivoPdf);
            
            log.info("Correo con PDF enviado exitosamente al proveedor: {}", emailDestino);

                } catch (Exception e) {
            log.error("Error al procesar la orden de compra para el producto ID {}: {}", orden.getProductoId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void enviarCorreoProveedor(String destinatario, String numeroOrden, String htmlCuerpo, ByteArrayResource pdfAdjunto) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(destinatario);
        helper.setSubject("Nueva Orden de Compra - Tienda de Insectos H&J SpA [" + numeroOrden + "]");
        helper.setText(htmlCuerpo, true); 
        
        if (pdfAdjunto != null) {
            helper.addAttachment("Orden_Compra_" + numeroOrden + ".pdf", pdfAdjunto);
        }
        
        mailSender.send(message);
    }

    private String obtenerEmailPorCategoria(String categoria) {
        if (categoria == null) return emailTerrestres;
        return switch (categoria.toLowerCase()) {
            case "voladores" -> emailVoladores;
            case "acuaticos" -> emailAcuaticos;
            default -> emailTerrestres;
        };
    }
}

