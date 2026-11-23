package hj.bff.controller;

import hj.bff.service.Reenviador;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

// Administración de Kafka (tópicos): GET, POST, PUT y DELETE. Solo Admin (lo define SecurityConfig).
@RestController
@RequestMapping("/api/kafka-admin")
public class KafkaAdminController {

  private final Reenviador reenviador;

  public KafkaAdminController(
      RestClient.Builder restClientBuilder,
      @Value("${microservices.kafka-admin.base-url}") String baseUrl) {
    this.reenviador = new Reenviador(restClientBuilder, baseUrl, "El administrador de Kafka");
  }

  @RequestMapping(
      path = "/**",
      method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
  public ResponseEntity<byte[]> reenviar(
      HttpServletRequest request, @RequestBody(required = false) byte[] cuerpo) {
    return reenviador.reenviar(request, cuerpo);
  }
}
