import type { VentaHora } from '../../types/reporte';
import { formatoPrecio } from '../../utils/formato';
import { etiquetaHora } from '../../utils/formatoReportes';

export default function GraficoVentasHora({ datos }: Readonly<{ datos: VentaHora[] }>) {
  if (datos.length === 0) {
    return <p className="rep-vacio">Sin ventas en este rango.</p>;
  }

  const ordenado = [...datos].sort((a, b) => a.hora.localeCompare(b.hora));
  const maximo = Math.max(1, ...ordenado.map((d) => d.monto));

  return (
    <div className="rep-barras-v">
      {ordenado.map((d) => (
        <div
          key={d.hora}
          className="rep-barra-v"
          title={`${etiquetaHora(d.hora)}: ${formatoPrecio.format(d.monto)} en ${d.pedidos} pedido(s)`}
        >
          <span className="rep-barra-v-valor">{formatoPrecio.format(d.monto)}</span>
          <div className="rep-barra-v-pista">
            <div className="rep-barra-v-relleno" style={{ height: `${(d.monto / maximo) * 100}%` }} />
          </div>
          <span className="rep-barra-v-etiqueta">{etiquetaHora(d.hora)}</span>
        </div>
      ))}
    </div>
  );
}

