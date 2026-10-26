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

  public ProductoResponse crear(ProductoResponse request) {
    return productoRepository.crear(request);
  }

  public List<ProductoResponse> crearMasivo(List<ProductoResponse> requests) {
    return productoRepository.crearMasivo(requests);
  }

  public ProductoResponse actualizar(Long id, ProductoResponse request) {
    return productoRepository.actualizar(id, request);
  }

  public void cambiarEstado(Long id, Boolean activo) {
    productoRepository.cambiarEstado(id, activo);
  }
}

