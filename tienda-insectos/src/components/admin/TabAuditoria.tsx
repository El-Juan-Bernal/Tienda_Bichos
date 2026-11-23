import { EVENTOS_POR_PAGINA, useAuditoria } from '../../hooks/useAuditoria';
import type { MostrarToast } from '../../types/toast';
import Paginacion from '../common/Paginacion';
import FormularioFiltros from './FormularioFiltros';
import PanelTimeline from './PanelTimeline';
import TablaEventos from './TablaEventos';

export default function TabAuditoria({ mostrarToast }: Readonly<{ mostrarToast: MostrarToast }>) {
  const auditoria = useAuditoria(mostrarToast);
  const { resultado, cargando, timeline, pagina } = auditoria;

  const eventos = resultado?.contenido ?? [];
  const total = resultado?.total ?? 0;
  const totalPaginas = Math.max(1, Math.ceil(total / EVENTOS_POR_PAGINA));

  return (
    <div>
      <header className="rep-cabecera">
        <div>
          <h2 className="admin-subtitulo">Auditoría</h2>
          <p className="aud-resumen">
            Registro de solo lectura de lo que ocurre con los pedidos: quién, qué, cuándo y desde dónde.
          </p>
        </div>
        <p className="aud-resumen">
          Eventos registrados: <strong>{total}</strong>
        </p>
      </header>

      <FormularioFiltros
        aplicados={auditoria.filtros}
        onBuscar={auditoria.buscar}
        onAviso={(mensaje) => mostrarToast('aviso', 'Revisa los filtros', mensaje)}
      />

      {timeline && <PanelTimeline timeline={timeline} onCerrar={auditoria.cerrarTimeline} />}

      {cargando && <p className="catalogo-aviso">Cargando eventos...</p>}
      {!cargando && eventos.length === 0 && (
        <p className="catalogo-aviso">No hay eventos con estos filtros.</p>
      )}

      {eventos.length > 0 && (
        <TablaEventos eventos={eventos} onVerPedido={(id) => void auditoria.verTimeline(id)} />
      )}

      <Paginacion pagina={pagina} total={totalPaginas} onCambiar={auditoria.irAPagina} />
    </div>
  );
}

