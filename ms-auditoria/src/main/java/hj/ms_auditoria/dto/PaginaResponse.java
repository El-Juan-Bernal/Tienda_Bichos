package hj.ms_auditoria.dto;

import java.util.List;

public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long total) {}

