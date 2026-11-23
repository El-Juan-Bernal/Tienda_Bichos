import type { EventoAuditoria, FiltrosEventos, PaginaEventos } from '../types/evento';
import { pedirJson } from './http';
import type { Sesion } from './http';

export const FILTROS_VACIOS: FiltrosEventos = { usuario: '', tipo: '', desde: '', hasta: '' };

// ms-auditoria recibe las fechas como instantes ISO (UTC). Si cambia el formato, se ajusta solo aquí.
function inicioDelDia(fecha: string): string {
  return new Date(`${fecha}T00:00:00`).toISOString();
}

function finDelDia(fecha: string): string {
  return new Date(`${fecha}T23:59:59.999`).toISOString();
}

export function listarEventos(
  sesion: Sesion,
  filtros: FiltrosEventos,
  pagina: number,
  tamano: number,
): Promise<PaginaEventos> {
  const params = new URLSearchParams();
  if (filtros.usuario.trim()) params.set('usuario', filtros.usuario.trim());
  if (filtros.tipo) params.set('tipo', filtros.tipo);
  if (filtros.desde) params.set('desde', inicioDelDia(filtros.desde));
  if (filtros.hasta) params.set('hasta', finDelDia(filtros.hasta));
  params.set('pagina', String(pagina));
  params.set('tamano', String(tamano));
  return pedirJson<PaginaEventos>(sesion, `/api/audit/events?${params.toString()}`);
}

/** Acepta tanto una lista directa como un objeto que traiga la lista dentro. */
function extraerLista(dato: unknown): EventoAuditoria[] {
  if (Array.isArray(dato)) return dato as EventoAuditoria[];
  if (dato && typeof dato === 'object') {
    const lista = Object.values(dato).find(Array.isArray);
    if (lista) return lista as EventoAuditoria[];
  }
  return [];
}

export async function obtenerTimeline(sesion: Sesion, pedidoId: number): Promise<EventoAuditoria[]> {
  const dato = await pedirJson<unknown>(sesion, `/api/audit/orders/${pedidoId}/timeline`);
  return extraerLista(dato);
}

