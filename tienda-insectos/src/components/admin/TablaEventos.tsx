import type { EventoAuditoria } from '../../types/evento';
import { etiquetaEstado, etiquetaEvento, etiquetaOrigen } from '../../utils/etiquetasAuditoria';
import { formatoPrecio } from '../../utils/formato';
import { formatoFechaHoraSeg } from '../../utils/formatoReportes';

interface Props {
  eventos: EventoAuditoria[];
  onVerPedido: (pedidoId: number) => void;
}

export default function TablaEventos({ eventos, onVerPedido }: Readonly<Props>) {
  return (
    <div className="aud-tabla-wrap">
      <table className="aud-tabla">
        <thead>
          <tr>
            <th>Fecha y hora</th>
            <th>Evento</th>
            <th>Pedido</th>
            <th>Usuario</th>
            <th>Origen</th>
            <th>Cambio de estado</th>
            <th>Monto</th>
            <th>Kafka</th>
          </tr>
        </thead>
        <tbody>
          {eventos.map((ev) => (
            <tr key={ev.eventId}>
              <td className="aud-fecha">{formatoFechaHoraSeg(ev.ocurridoEn)}</td>
              <td>
                <span className={ev.tipo === 'OrderCancelled' ? 'aud-insignia cancelado' : 'aud-insignia'}>
                  {etiquetaEvento(ev.tipo)}
                </span>
              </td>
              <td>
                <button
                  type="button"
                  className="aud-enlace"
                  title="Ver la línea de tiempo del pedido"
                  onClick={() => onVerPedido(ev.pedidoId)}
                >
                  #{ev.pedidoId}
                </button>
              </td>
              <td>{ev.usuario}</td>
              <td>{etiquetaOrigen(ev.origen)}</td>
              <td className="aud-estados">
                {etiquetaEstado(ev.estadoAnterior)} → {etiquetaEstado(ev.estadoNuevo)}
              </td>
              <td>{ev.montoTotal == null ? '—' : formatoPrecio.format(ev.montoTotal)}</td>
              <td className="aud-kafka" title={ev.kafkaTopico}>
                P{ev.kafkaParticion} · offset {ev.kafkaOffset}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

