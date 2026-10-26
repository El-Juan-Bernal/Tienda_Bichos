package hj.ms_pedidos.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class PedidoItem {
    private Long productoId;
    private Integer cantidad;
}

