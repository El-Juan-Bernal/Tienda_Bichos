package hj.bff.controller;

import hj.bff.dto.ClienteResponse;
import hj.bff.service.DataService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Resto heredado (llama a un ms-clientes que no existe): oculto en Swagger.
@Hidden
@RestController
@RequestMapping("/api")
public class DataController {

  private final DataService dataService;

  public DataController(DataService dataService) {
    this.dataService = dataService;
  }

  @GetMapping("/data")
  public ClienteResponse data() {
    return dataService.obtenerData();
  }
}
