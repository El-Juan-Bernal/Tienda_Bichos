export const ESTADOS_EN_ORDEN = [
  'CREADO',
  'ACEPTADO',
  'EN_PREPARACION',
  'DESPACHADO',
  'ENTREGADO',
  'CANCELADO',
] as const;

const ETIQUETA_ESTADO: Record<string, string> = {
  CREADO: 'Creado',
  ACEPTADO: 'Aceptado',
  EN_PREPARACION: 'En preparación',
  DESPACHADO: 'Despachado',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

export function etiquetaEstado(estado: string | null | undefined): string {
  if (!estado) return '—';
  return ETIQUETA_ESTADO[estado] ?? estado;
}

export const TIPOS_EVENTO = [
  { valor: 'OrderCreated', etiqueta: 'Pedido creado' },
  { valor: 'OrderAccepted', etiqueta: 'Pedido aceptado' },
  { valor: 'OrderPreparing', etiqueta: 'En preparación' },
  { valor: 'OrderDispatched', etiqueta: 'Despachado' },
  { valor: 'OrderDelivered', etiqueta: 'Entregado' },
  { valor: 'OrderCancelled', etiqueta: 'Pedido cancelado' },
] as const;

export function etiquetaEvento(tipo: string): string {
  return TIPOS_EVENTO.find((t) => t.valor === tipo)?.etiqueta ?? tipo;
}

export function etiquetaOrigen(origen: string | null | undefined): string {
  if (origen === 'CLIENTE') return 'Cliente';
  if (origen === 'SISTEMA') return 'Sistema (automático)';
  return origen || '—';
}

