import type { HistorialPedido, Pedido } from '../types/pedido';
import { fetchBff, pedirJson } from './http';
import type { Sesion } from './http';

export interface PayloadPedido {
  usuario?: string;
  pasarelaSeleccionada: string;
  items: { productoId: number; cantidad: number }[];
  montoTotal: number;
}

export function crearPedido(sesion: Sesion, payload: PayloadPedido) {
  return fetchBff(sesion, '/api/pedidos', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function actualizarEstadoPedido(sesion: Sesion, id: number, nuevoEstado: string) {
  return fetchBff(sesion, `/api/pedidos/${id}/estado`, {
    method: 'PATCH',
    body: JSON.stringify({ nuevoEstado }),
  });
}

/** El BFF devuelve solo los pedidos del usuario del token. */
export function listarPedidos(sesion: Sesion): Promise<Pedido[]> {
  return pedirJson<Pedido[]>(sesion, '/api/pedidos');
}

export function obtenerHistorial(sesion: Sesion, id: number): Promise<HistorialPedido[]> {
  return pedirJson<HistorialPedido[]>(sesion, `/api/pedidos/${id}/historial`);
}

export function cancelarPedido(sesion: Sesion, id: number) {
  return fetchBff(sesion, `/api/pedidos/${id}/cancelar`, { method: 'PATCH' });
}

