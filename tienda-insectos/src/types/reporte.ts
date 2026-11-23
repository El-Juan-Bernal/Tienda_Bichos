export type RangoReporte = 'last24h' | 'last7d' | 'last30d';

export interface VentaHora {
  hora: string; // "2026-11-18T14" (hora de Chile)
  pedidos: number;
  monto: number;
}

export interface Kpis {
  rango: string;
  desde: string;
  pedidosVendidos: number;
  ventasTotales: number;
  ticketPromedio: number;
  pedidosEntregados: number;
  leadTimePromedioSegundos: number;
  pedidosActivos: number;
  pedidosPorEstado: Record<string, number>;
  ventasPorHora: VentaHora[];
}

export interface TopProducto {
  productoId: number;
  nombre: string;
  unidades: number;
  ingresos: number;
}

