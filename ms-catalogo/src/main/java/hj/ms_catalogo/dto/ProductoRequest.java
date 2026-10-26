package hj.ms_catalogo.dto;

public record ProductoRequest(
    String nombre,
    String descripcion,
    Long precio,
    Integer stock,
    String imagenUrl,
    String categoria,
    Integer porcentajeDescuento,
    Boolean activo
) {}

