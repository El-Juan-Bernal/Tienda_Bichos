package hj.ms_cliente.controller;

import hj.ms_cliente.dto.ClienteResponse;
import hj.ms_cliente.service.ClienteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

  private final ClienteService clienteService;

  public ClienteController(ClienteService clienteService) {
    this.clienteService = clienteService;
  }

  @GetMapping("/{id}")
  public ClienteResponse buscarPorId(@PathVariable Long id) {
    return clienteService.buscarPorId(id);
  }
}
