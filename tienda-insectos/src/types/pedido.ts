export type EstadoPedido =
  | 'CREADO'
  | 'ACEPTADO'
  | 'EN_PREPARACION'
  | 'DESPACHADO'
  | 'ENTREGADO'
  | 'CANCELADO';

export interface ItemPedido {
  productoId: number;
  cantidad: number;
}

export interface Pedido {
  id: number;
  estado: EstadoPedido;
  fechaCreacion: string;
  fechaCancelacion: string | null;
  fechaReembolso: string | null;
  montoTotal: number | null;
  montoReembolsado: number | null;
  pasarelaSeleccionada: string | null;
  items: ItemPedido[];
  usuarioEmail: string;
}

export interface HistorialPedido {
  id: number;
  pedidoId: number;
  estadoAnterior: EstadoPedido | null;
  estadoNuevo: EstadoPedido;
  origen: 'CLIENTE' | 'SISTEMA';
  fecha: string;
}

