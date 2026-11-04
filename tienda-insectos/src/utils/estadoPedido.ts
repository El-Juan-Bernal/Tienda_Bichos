import type { EstadoPedido } from '../types/pedido';

/** Orden normal de un pedido, de izquierda a derecha en la línea de tiempo. */
export const FLUJO: EstadoPedido[] = ['CREADO', 'ACEPTADO', 'EN_PREPARACION', 'DESPACHADO', 'ENTREGADO'];

export const ETIQUETAS: Record<EstadoPedido, string> = {
  CREADO: 'Creado',
  ACEPTADO: 'Aceptado',
  EN_PREPARACION: 'En preparación',
  DESPACHADO: 'Despachado',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

/**
 * previa: aún no se puede cancelar · cancelacion: se puede cancelar
 * devolucion: ya salió de bodega, solo devolución · cancelado: pedido cancelado
 */
export type Zona = 'previa' | 'cancelacion' | 'devolucion' | 'cancelado';

export const ZONA: Record<EstadoPedido, Zona> = {
  CREADO: 'cancelacion',
  ACEPTADO: 'cancelacion',
  EN_PREPARACION: 'cancelacion',
  DESPACHADO: 'devolucion',
  ENTREGADO: 'devolucion',
  CANCELADO: 'cancelado',
};

export function sePuedeCancelar(estado: EstadoPedido): boolean {
  return estado === 'ACEPTADO' || estado === 'EN_PREPARACION';
}

