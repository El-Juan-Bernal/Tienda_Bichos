package hj.bff.repository;

import hj.bff.dto.ProductoResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
}
