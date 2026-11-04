import { useState } from 'react';
import type { Pedido } from '../../types/pedido';
import { useHistorial } from '../../hooks/useHistorial';
import { ETIQUETAS, ZONA, sePuedeCancelar } from '../../utils/estadoPedido';
import { formatoFechaHora } from '../../utils/fechaHora';
import { formatoPrecio } from '../../utils/formato';
import LineaTiempo from './LineaTiempo';

interface Props {
  pedido: Pedido;
  nombres: Record<number, string>;
  onCancelar: (pedido: Pedido) => void;
}

export default function TarjetaPedido({ pedido, nombres, onCancelar }: Readonly<Props>) {
  const historial = useHistorial(pedido.id, pedido.estado);
  const [mostrarNota, setMostrarNota] = useState(false);

  const cancelable = sePuedeCancelar(pedido.estado);
  const cancelado = pedido.estado === 'CANCELADO';

  function alPresionarCancelar() {
    if (cancelable) onCancelar(pedido);
    else setMostrarNota(true);
  }

  return (
    <article className="pedido">
      <header className="pedido-cabecera">
        <div>
          <h2>Pedido #{pedido.id}</h2>
          <p className="pedido-fecha">{formatoFechaHora(pedido.fechaCreacion)}</p>
        </div>
        <span className={`estado estado-${ZONA[pedido.estado]}`}>{ETIQUETAS[pedido.estado]}</span>
      </header>

      <LineaTiempo estado={pedido.estado} historial={historial} />

      {pedido.items.length === 0 ? (
        <p className="pedido-sin-detalle">Pedido antiguo, sin detalle de productos.</p>
      ) : (
        <ul className="pedido-items">
          {pedido.items.map((i) => (
            <li key={i.productoId}>
              <span>{i.cantidad} ×</span> {nombres[i.productoId] ?? `Producto #${i.productoId}`}
            </li>
          ))}
        </ul>
      )}

      {cancelado && (
        <p className="pedido-reembolso">
          Reembolsado: <strong>{formatoPrecio.format(pedido.montoReembolsado ?? 0)}</strong>
          {pedido.pasarelaSeleccionada && ` con ${pedido.pasarelaSeleccionada}`}
          {pedido.fechaReembolso && ` · ${formatoFechaHora(pedido.fechaReembolso)}`}
        </p>
      )}

      <footer className="pedido-pie">
        <div className="pedido-total">
          <span>Total</span>
          <strong>{pedido.montoTotal == null ? '—' : formatoPrecio.format(pedido.montoTotal)}</strong>
          {pedido.pasarelaSeleccionada && (
            <span className="pedido-pasarela">Pagado con {pedido.pasarelaSeleccionada}</span>
          )}
        </div>

        {!cancelado && (
          <button
            type="button"
            className={cancelable ? 'btn btn-borde btn-cancelar-pedido' : 'btn btn-borde btn-cancelar-pedido atenuado'}
            aria-disabled={!cancelable}
            onClick={alPresionarCancelar}
          >
            Cancelar pedido
          </button>
        )}
      </footer>

      {mostrarNota && !cancelable && !cancelado && (
        <p className="nota-devolucion">
          Ya no puedes cancelar este pedido porque salió de bodega. Puedes pedir una devolución al recibirlo.
        </p>
      )}
    </article>
  );
}

