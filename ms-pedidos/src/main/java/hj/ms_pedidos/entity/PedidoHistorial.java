package hj.ms_pedidos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "pedido_historial")
@Getter
@Setter
@NoArgsConstructor
public class PedidoHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoAnterior; // null cuando el pedido se crea

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estadoNuevo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // CLIENTE (acción manual) o SISTEMA (avance automático)
    private String origen;

    public PedidoHistorial(Long pedidoId, EstadoPedido estadoAnterior,
                           EstadoPedido estadoNuevo, String origen) {
        this.pedidoId = pedidoId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.origen = origen;
        this.fecha = LocalDateTime.now(ZoneId.of("America/Santiago"));
    }
}

