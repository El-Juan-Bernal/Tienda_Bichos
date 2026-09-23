package hj.bff.service;

import hj.bff.dto.ClienteResponse;
import hj.bff.repository.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class DataService {

  private final ClienteRepository clienteRepository;

  public DataService(ClienteRepository clienteRepository) {
    this.clienteRepository = clienteRepository;
  }

  public ClienteResponse obtenerData() {
    return clienteRepository.buscarPorId(1L);
  }
}
