package hj.ms_rabbit_admin.dto;

public record BindingResponse(String exchange, String destino, String tipoDestino, String routingKey) {
}
