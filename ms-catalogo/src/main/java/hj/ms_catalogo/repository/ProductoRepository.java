package hj.ms_catalogo.repository;

import hj.ms_catalogo.entity.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("select p from Producto p where p.destacado = true and (p.activo is null or p.activo = true) order by p.id")
    List<Producto> findDestacadosActivos();

    @Query("select count(p) from Producto p where p.destacado = true and (p.activo is null or p.activo = true)")
    long contarDestacadosActivos();
}

