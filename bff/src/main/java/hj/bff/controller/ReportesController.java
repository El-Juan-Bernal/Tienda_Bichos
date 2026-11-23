package hj.bff.controller;

import hj.bff.service.Reenviador;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

// Solo lectura. Solo Admin (lo define SecurityConfig).
@RestController
@RequestMapping("/api/report")
public class ReportesController {

  private final Reenviador reenviador;

  public ReportesController(
      RestClient.Builder restClientBuilder,
      @Value("${microservices.reportes.base-url}") String baseUrl) {
    this.reenviador = new Reenviador(restClientBuilder, baseUrl, "El servicio de reportes");
  }

  @GetMapping("/**")
  public ResponseEntity<byte[]> reenviar(HttpServletRequest request) {
    return reenviador.reenviar(request, null);
  }
}
