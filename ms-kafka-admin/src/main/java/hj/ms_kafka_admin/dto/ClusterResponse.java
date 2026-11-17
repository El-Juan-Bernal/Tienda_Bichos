package hj.ms_kafka_admin.dto;

import java.util.List;

public record ClusterResponse(String clusterId, int controlador, List<String> brokers) {}

