package hj.ms_proveedores.dto;

public class OrdenCompraDto {
    private Long productoId;
    private String productoNombre;
    private String categoria;
    private Integer cantidadRequerida = 50;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Integer getCantidadRequerida() { return cantidadRequerida; }
    public void setCantidadRequerida(Integer cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
}

