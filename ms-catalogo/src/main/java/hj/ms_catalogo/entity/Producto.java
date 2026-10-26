package hj.ms_catalogo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    
    @Column(length = 1000)
    private String descripcion;
    
    private Long precio;
    private Integer stock;
    private String imagenUrl;
    private String categoria;

    @Column(columnDefinition = "integer default 0")
    private Integer porcentajeDescuento = 0;

    @Column(columnDefinition = "boolean default true")
    private Boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Long getPrecio() { return precio; }
    public void setPrecio(Long precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Integer getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(Integer porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}

