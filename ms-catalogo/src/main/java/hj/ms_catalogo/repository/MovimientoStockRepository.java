package hj.ms_catalogo.repository;

import hj.ms_catalogo.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    boolean existsByTipoAndPedidoIdAndProductoId(String tipo, Long pedidoId, Long productoId);
}

