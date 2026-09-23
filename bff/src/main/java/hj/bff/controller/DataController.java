package hj.bff.controller;

import hj.bff.dto.ClienteResponse;
import hj.bff.service.DataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
