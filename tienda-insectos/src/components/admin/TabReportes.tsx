import { useState } from 'react';
import { useReportes } from '../../hooks/useReportes';
import type { RangoReporte } from '../../types/reporte';
import type { MostrarToast } from '../../types/toast';
import { formatoFechaHora } from '../../utils/fechaHora';
import { formatoPrecio } from '../../utils/formato';
import { formatoDuracion } from '../../utils/formatoReportes';
import GraficoEstados from './GraficoEstados';
import GraficoTopProductos from './GraficoTopProductos';
import GraficoVentasHora from './GraficoVentasHora';
import TarjetaKpi from './TarjetaKpi';

const RANGOS: { valor: RangoReporte; etiqueta: string }[] = [
  { valor: 'last24h', etiqueta: 'Últimas 24 horas' },
  { valor: 'last7d', etiqueta: 'Últimos 7 días' },
  { valor: 'last30d', etiqueta: 'Últimos 30 días' },
];

export default function TabReportes({ mostrarToast }: Readonly<{ mostrarToast: MostrarToast }>) {
  const [rango, setRango] = useState<RangoReporte>('last24h');
  const { kpis, top, cargando, error, actualizado, recargar } = useReportes(rango, mostrarToast);

  return (
    <div>
      <header className="rep-cabecera">
        <h2 className="admin-subtitulo">Reportes</h2>
        <div className="rep-controles">
          <label htmlFor="rep-rango" className="rep-rotulo">
            Rango
          </label>
          <select
            id="rep-rango"
            className="entrada"
            value={rango}
            onChange={(e) => setRango(e.target.value as RangoReporte)}
          >
            {RANGOS.map((r) => (
              <option key={r.valor} value={r.valor}>
                {r.etiqueta}
              </option>
            ))}
          </select>
          <button type="button" className="rep-boton" onClick={() => void recargar()}>
            Actualizar
          </button>
        </div>
      </header>

      {cargando && <p className="catalogo-aviso">Cargando reportes...</p>}
      {error && <p className="rep-error">{error}</p>}

      {kpis && (
        <>
          <p className="rep-actualizado">
            Datos desde {formatoFechaHora(kpis.desde)}
            {actualizado && ` · actualizado a las ${actualizado.toLocaleTimeString('es-CL')}`} · se refrescan
            solos cada 10 segundos
          </p>

          <div className="rep-kpis">
            <TarjetaKpi
              titulo="Pedidos vendidos"
              valor={String(kpis.pedidosVendidos)}
              detalle="Aceptados y no cancelados"
            />
            <TarjetaKpi
              titulo="Ventas totales"
              valor={formatoPrecio.format(kpis.ventasTotales)}
              detalle="Después de descuentos"
            />
            <TarjetaKpi
              titulo="Ticket promedio"
              valor={formatoPrecio.format(kpis.ticketPromedio)}
              detalle="Por pedido"
            />
            <TarjetaKpi titulo="Pedidos entregados" valor={String(kpis.pedidosEntregados)} />
            <TarjetaKpi
              titulo="Lead time promedio"
              valor={kpis.pedidosEntregados > 0 ? formatoDuracion(kpis.leadTimePromedioSegundos) : '—'}
              detalle="Desde que se crea hasta que se entrega"
            />
            <TarjetaKpi titulo="Pedidos activos" valor={String(kpis.pedidosActivos)} detalle="En curso ahora" />
          </div>

          <div className="rep-paneles">
            <section className="rep-panel ancho">
              <h3>Ventas por hora</h3>
              <p className="rep-panel-nota">
                Monto de los pedidos aceptados y no cancelados, por hora de Chile.
              </p>
              <GraficoVentasHora datos={kpis.ventasPorHora ?? []} />
            </section>

            <section className="rep-panel">
              <h3>Pedidos por estado</h3>
              <p className="rep-panel-nota">Histórico: incluye todos los pedidos registrados.</p>
              <GraficoEstados datos={kpis.pedidosPorEstado ?? {}} />
            </section>

            <section className="rep-panel">
              <h3>Productos más vendidos</h3>
              <p className="rep-panel-nota">
                Las cancelaciones se descuentan. Los ingresos son brutos (antes de descuentos).
              </p>
              <GraficoTopProductos datos={top} />
            </section>
          </div>
        </>
      )}
    </div>
  );
}

