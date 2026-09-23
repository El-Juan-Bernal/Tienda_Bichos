package hj.bff.service;

import hj.bff.dto.ProductoResponse;
import hj.bff.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

  private final ProductoRepository productoRepository;

  public ProductoService(ProductoRepository productoRepository) {
    this.productoRepository = productoRepository;
  }

  public List<ProductoResponse> obtenerTodos() {
    return productoRepository.listar();
  }

  public ProductoResponse obtenerPorId(Long id) {
    return productoRepository.buscarPorId(id);
  }
}
