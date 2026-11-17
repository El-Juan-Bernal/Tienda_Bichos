package hj.ms_kafka_admin.dto;

import java.util.List;
import java.util.Map;

public record TopicoResponse(
    String nombre,
    int particiones,
    int replicas,
    Map<String, String> configuraciones,
    List<ParticionInfo> detalle
) {}

