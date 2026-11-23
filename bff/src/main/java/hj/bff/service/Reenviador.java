package hj.bff.service;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

// Reenvía una petición tal cual (método, ruta, query y cuerpo) a un microservicio
// y devuelve su respuesta. Las rutas del BFF y del microservicio son las mismas.
public class Reenviador {

  private final RestClient restClient;
  private final String baseUrl;
  private final String nombreServicio;

  public Reenviador(RestClient.Builder builder, String baseUrl, String nombreServicio) {
    this.restClient = builder.build();
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.nombreServicio = nombreServicio;
  }

  public ResponseEntity<byte[]> reenviar(HttpServletRequest request, byte[] cuerpo) {
    try {
      RestClient.RequestBodySpec solicitud = restClient
          .method(HttpMethod.valueOf(request.getMethod()))
          .uri(destino(request));
      if (cuerpo != null && cuerpo.length > 0) {
        solicitud.contentType(MediaType.APPLICATION_JSON).body(cuerpo);
      }
      ResponseEntity<byte[]> respuesta = solicitud.retrieve().toEntity(byte[].class);
      return enJson(respuesta.getStatusCode(), respuesta.getBody());
    } catch (RestClientResponseException e) {
      // El microservicio respondió con error (4xx/5xx): se reenvía su estado y mensaje
      return enJson(e.getStatusCode(), e.getResponseBodyAsByteArray());
    } catch (ResourceAccessException e) {
      String json = "{\"error\":\"" + nombreServicio + " no está disponible.\"}";
      return enJson(HttpStatus.BAD_GATEWAY, json.getBytes(StandardCharsets.UTF_8));
    }
  }

  private URI destino(HttpServletRequest request) {
    String consulta = request.getQueryString();
    return URI.create(baseUrl + request.getRequestURI() + (consulta == null ? "" : "?" + consulta));
  }

  private ResponseEntity<byte[]> enJson(HttpStatusCode estado, byte[] cuerpo) {
    return ResponseEntity.status(estado)
        .contentType(MediaType.APPLICATION_JSON)
        .body(cuerpo);
  }
}
