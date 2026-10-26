package hj.bff.repository;

import hj.bff.dto.ProductoResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

@Repository
public class ProductoRepository {

  private final RestClient restClient;

  public ProductoRepository(
      RestClient.Builder restClientBuilder,
      @Value("${microservices.catalogo.base-url}")
      String baseUrl) {
    this.restClient = restClientBuilder
        .baseUrl(baseUrl)
        .build();
  }

  public List<ProductoResponse> listar() {
    return restClient
        .get()
        .uri("/api/productos")
        .retrieve()
        .body(new ParameterizedTypeReference<List<ProductoResponse>>() {});
  }

  public ProductoResponse buscarPorId(Long id) {
    return restClient
        .get()
        .uri("/api/productos/{id}", id)
        .retrieve()
        .body(ProductoResponse.class);
  }

  public ProductoResponse crear(ProductoResponse request) {
    return restClient
        .post()
        .uri("/api/productos")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(ProductoResponse.class);
  }

  public List<ProductoResponse> crearMasivo(List<ProductoResponse> requests) {
    return restClient
        .post()
        .uri("/api/productos/masivo")
        .contentType(MediaType.APPLICATION_JSON)
        .body(requests)
        .retrieve()
        .body(new ParameterizedTypeReference<List<ProductoResponse>>() {});
  }

  public ProductoResponse actualizar(Long id, ProductoResponse request) {
    return restClient
        .put()
        .uri("/api/productos/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(ProductoResponse.class);
  }

  public void cambiarEstado(Long id, Boolean activo) {
    restClient
        .put()
        .uri("/api/productos/{id}/estado", id)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("activo", activo))
        .retrieve()
        .toBodilessEntity();
  }
}

