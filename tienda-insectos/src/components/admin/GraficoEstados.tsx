import { ESTADOS_EN_ORDEN, etiquetaEstado } from '../../utils/etiquetasAuditoria';

export default function GraficoEstados({ datos }: Readonly<{ datos: Record<string, number> }>) {
  const maximo = Math.max(1, ...ESTADOS_EN_ORDEN.map((estado) => datos[estado] ?? 0));

  return (
    <div className="rep-barras-h">
      {ESTADOS_EN_ORDEN.map((estado) => {
        const cantidad = datos[estado] ?? 0;
        return (
          <div key={estado} className="rep-fila-h">
            <span className="rep-fila-h-nombre">{etiquetaEstado(estado)}</span>
            <div className="rep-fila-h-pista">
              <div
                className={`rep-fila-h-relleno est-${estado}`}
                style={{ width: `${(cantidad / maximo) * 100}%` }}
              />
            </div>
            <span className="rep-fila-h-valor">
              <strong>{cantidad}</strong>
            </span>
          </div>
        );
      })}
    </div>
  );
}

