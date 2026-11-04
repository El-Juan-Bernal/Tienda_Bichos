package hj.ms_pedidos.repository;

import hj.ms_pedidos.entity.PedidoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoHistorialRepository extends JpaRepository<PedidoHistorial, Long> {
    List<PedidoHistorial> findByPedidoIdOrderByFechaAsc(Long pedidoId);
}

