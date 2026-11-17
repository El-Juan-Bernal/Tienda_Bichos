package hj.ms_auditoria.repository;

import hj.ms_auditoria.entity.EventoAuditoria;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    boolean existsByEventId(String eventId);

    List<EventoAuditoria> findByPedidoIdOrderByOcurridoEnAscIdAsc(Long pedidoId);

    @Query("""
            select e from EventoAuditoria e
            where (:usuario is null or lower(e.usuario) like lower(concat('%', :usuario, '%')))
              and (:tipo is null or e.tipo = :tipo)
              and (:desde is null or e.ocurridoEn >= :desde)
              and (:hasta is null or e.ocurridoEn <= :hasta)
            order by e.ocurridoEn desc, e.id desc
            """)
    Page<EventoAuditoria> buscar(@Param("usuario") String usuario,
                                 @Param("tipo") String tipo,
                                 @Param("desde") Instant desde,
                                 @Param("hasta") Instant hasta,
                                 Pageable pageable);
}

