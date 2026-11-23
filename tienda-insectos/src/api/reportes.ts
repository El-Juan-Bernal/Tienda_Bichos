import type { Kpis, RangoReporte, TopProducto } from '../types/reporte';
import { pedirJson } from './http';
import type { Sesion } from './http';

export function obtenerKpis(sesion: Sesion, rango: RangoReporte): Promise<Kpis> {
  return pedirJson<Kpis>(sesion, `/api/report/kpis?range=${rango}`);
}

export function obtenerTopProductos(
  sesion: Sesion,
  rango: RangoReporte,
  limite = 10,
): Promise<TopProducto[]> {
  return pedirJson<TopProducto[]>(sesion, `/api/report/top-products?range=${rango}&limit=${limite}`);
}

