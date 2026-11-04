import { Fragment } from 'react';
import type { EstadoPedido, HistorialPedido } from '../../types/pedido';
import { ETIQUETAS, FLUJO, ZONA } from '../../utils/estadoPedido';
import type { Zona } from '../../utils/estadoPedido';
import { formatoFechaHora } from '../../utils/fechaHora';

interface Props {
  estado: EstadoPedido;
  historial: HistorialPedido[];
}

interface Paso {
  estado: EstadoPedido;
  zona: Zona;
  alcanzado: boolean;
  actual: boolean;
}

function armarPasos(estado: EstadoPedido, historial: HistorialPedido[]): Paso[] {
  if (estado === 'CANCELADO') {
    // La línea llega hasta donde estaba el pedido y termina en un punto rojo.
    const anterior = historial.find((h) => h.estadoNuevo === 'CANCELADO')?.estadoAnterior;
    const indice = anterior ? FLUJO.indexOf(anterior) : -1;
    const hasta = indice >= 0 ? indice : 1;
    const estados: EstadoPedido[] = [...FLUJO.slice(0, hasta + 1), 'CANCELADO'];
    return estados.map((e, i) => ({
      estado: e,
      zona: ZONA[e],
      alcanzado: true,
      actual: i === estados.length - 1,
    }));
  }

  const actual = FLUJO.indexOf(estado);
  return FLUJO.map((e, i) => ({
    estado: e,
    zona: ZONA[e],
    alcanzado: i <= actual,
    actual: i === actual,
  }));
}

export default function LineaTiempo({ estado, historial }: Readonly<Props>) {
  const pasos = armarPasos(estado, historial);

  function titulo(p: Paso): string {
    const fila = historial.find((h) => h.estadoNuevo === p.estado);
    const fecha = p.alcanzado ? formatoFechaHora(fila?.fecha) : '';
    return fecha ? `${ETIQUETAS[p.estado]} · ${fecha}` : ETIQUETAS[p.estado];
  }

  return (
    <div className="tiempo">
      <div className="linea" role="img" aria-label={`Avance del pedido: ${ETIQUETAS[estado]}`}>
        {pasos.map((p, i) => (
          <Fragment key={p.estado}>
            {i > 0 && (
              <span
                className={`tramo zona-${pasos[i - 1].zona}${p.alcanzado ? ' alcanzado' : ''}`}
                aria-hidden="true"
              />
            )}
            <span
              className={`punto zona-${p.zona}${p.alcanzado ? ' alcanzado' : ''}${p.actual ? ' actual' : ''}`}
              title={titulo(p)}
            />
          </Fragment>
        ))}
      </div>

      {estado !== 'CANCELADO' && (
        <div className="leyenda">
          <span>
            <i className="muestra zona-cancelacion" /> Puedes cancelar
          </span>
          <span>
            <i className="muestra zona-devolucion" /> Solo devolución
          </span>
        </div>
      )}
    </div>
  );
}

