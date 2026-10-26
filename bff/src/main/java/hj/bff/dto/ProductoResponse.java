package hj.bff.dto;

public record ProductoResponse(
    Long id,
    String nombre,
    String descripcion,
    Long precio,
    Integer stock,
    Integer porcentajeDescuento,
    String imagenUrl,
    String categoria,
    Boolean activo
) {
}

