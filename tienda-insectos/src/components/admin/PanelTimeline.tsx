import { useEffect, useRef } from 'react';
import type { TimelinePedido } from '../../types/evento';
import { etiquetaEstado, etiquetaEvento, etiquetaOrigen } from '../../utils/etiquetasAuditoria';
import { formatoDuracion, formatoFechaHoraSeg, segundosEntre } from '../../utils/formatoReportes';

interface Props {
  timeline: TimelinePedido;
  onCerrar: () => void;
}

function claseTipo(tipo: string): string {
  if (tipo === 'OrderCancelled') return 'cancelado';
  if (tipo === 'OrderDelivered') return 'entregado';
  return '';
}

export default function PanelTimeline({ timeline, onCerrar }: Readonly<Props>) {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    ref.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }, [timeline.pedidoId]);

  const eventos = [...timeline.eventos].sort((a, b) =>
    (a.ocurridoEn ?? '').localeCompare(b.ocurridoEn ?? ''),
  );
  const primero = eventos[0];
  const ultimo = eventos.at(-1);
  const duracion =
    primero?.tipo === 'OrderCreated' && ultimo?.tipo === 'OrderDelivered'
      ? formatoDuracion(segundosEntre(primero.ocurridoEn, ultimo.ocurridoEn))
      : null;

  return (
    <section className="aud-timeline" ref={ref}>
      <div className="aud-timeline-cabecera">
        <div>
          <h3>Línea de tiempo del pedido #{timeline.pedidoId}</h3>
          {duracion && <p className="aud-paso-detalle">Tiempo total (lead time): {duracion}</p>}
        </div>
        <button type="button" className="rep-boton secundario" onClick={onCerrar}>
          Cerrar
        </button>
      </div>

      {timeline.cargando && <p className="catalogo-aviso">Cargando línea de tiempo...</p>}
      {!timeline.cargando && eventos.length === 0 && (
        <p className="rep-vacio">No hay eventos registrados para este pedido.</p>
      )}

      <ol className="aud-pasos">
        {eventos.map((ev) => (
          <li key={ev.eventId} className={`aud-paso ${claseTipo(ev.tipo)}`}>
            <p className="aud-paso-titulo">{etiquetaEvento(ev.tipo)}</p>
            <p className="aud-paso-detalle">
              {formatoFechaHoraSeg(ev.ocurridoEn)} · {ev.usuario} · {etiquetaOrigen(ev.origen)}
            </p>
            <p className="aud-paso-detalle">
              {etiquetaEstado(ev.estadoAnterior)} → {etiquetaEstado(ev.estadoNuevo)}
            </p>
          </li>
        ))}
      </ol>
    </section>
  );
}

