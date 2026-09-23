package hj.bff.dto;

public record ProductoResponse(
    Long id,
    String nombre,
    String descripcion,
    Long precio,
    Integer stock,
    String imagenUrl,
    String categoria
) {
}
