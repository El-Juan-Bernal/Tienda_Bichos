export interface EventoAuditoria {
  eventId: string;
  pedidoId: number;
  tipo: string;
  usuario: string;
  origen: string;
  estadoAnterior: string | null;
  estadoNuevo: string;
  montoTotal: number | null;
  ocurridoEn: string;
  kafkaTopico: string;
  kafkaParticion: number;
  kafkaOffset: number;
}

export interface PaginaEventos {
  contenido: EventoAuditoria[];
  pagina: number; // desde 0
  tamano: number;
  total: number;
}

/** Fechas en formato AAAA-MM-DD (lo que entrega <input type="date">). */
export interface FiltrosEventos {
  usuario: string;
  tipo: string;
  desde: string;
  hasta: string;
}

export interface TimelinePedido {
  pedidoId: number;
  eventos: EventoAuditoria[];
  cargando: boolean;
}

