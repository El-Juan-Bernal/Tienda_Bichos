package hj.ms_cliente.service;

import hj.ms_cliente.dto.ClienteResponse;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

  public ClienteResponse buscarPorId(Long id) {
    return new ClienteResponse(
        id,
        "Wacoldo Soto",
        "waco.soto@duocuc.cl"
    );
  }
}
