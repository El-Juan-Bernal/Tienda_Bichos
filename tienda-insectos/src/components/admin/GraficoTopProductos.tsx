import type { TopProducto } from '../../types/reporte';
import { formatoPrecio } from '../../utils/formato';

export default function GraficoTopProductos({ datos }: Readonly<{ datos: TopProducto[] }>) {
  if (datos.length === 0) {
    return <p className="rep-vacio">Sin ventas en este rango.</p>;
  }

  const maximo = Math.max(1, ...datos.map((p) => p.unidades));

  return (
    <div className="rep-barras-h">
      {datos.map((p, i) => (
        <div key={p.productoId} className="rep-fila-h">
          <span className="rep-fila-h-nombre" title={p.nombre}>
            {i + 1}. {p.nombre}
          </span>
          <div className="rep-fila-h-pista">
            <div className="rep-fila-h-relleno" style={{ width: `${(p.unidades / maximo) * 100}%` }} />
          </div>
          <span className="rep-fila-h-valor">
            <strong>{p.unidades} u.</strong>
            <small>{formatoPrecio.format(p.ingresos)}</small>
          </span>
        </div>
      ))}
    </div>
  );
}

