package hj.ms_kafka_admin.dto;

import java.util.List;

public record ParticionInfo(int particion, int lider, List<Integer> replicas, List<Integer> isr) {}

