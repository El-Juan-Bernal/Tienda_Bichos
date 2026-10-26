package hj.ms_pedidos.repository;

import hj.ms_pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioEmail(String usuarioEmail);
}

