package hj.ms_auditoria.dto;

/** Registro que se publica en audit.timeline: quién / qué / cuándo / desde dónde. */
public record EntradaTimeline(
        String eventId,
        Long pedidoId,
        String quien,
        String actor,
        String que,
        String estadoAnterior,
        String estadoNuevo,
        String cuando,
        String desdeDonde
) {}

