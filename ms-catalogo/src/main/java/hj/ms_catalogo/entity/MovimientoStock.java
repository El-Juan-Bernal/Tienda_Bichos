package hj.ms_catalogo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** Registro de reposiciones aplicadas. La clave única evita reponer dos veces el mismo pedido. */
@Entity
@Table(name = "movimientos_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "pedido_id", "producto_id"}))
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    protected MovimientoStock() {}

    public MovimientoStock(String tipo, Long pedidoId, Long productoId, Integer cantidad) {
        this.tipo = tipo;
        this.pedidoId = pedidoId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.fecha = LocalDateTime.now(ZoneId.of("America/Santiago"));
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public Long getPedidoId() { return pedidoId; }
    public Long getProductoId() { return productoId; }
    public Integer getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
}

