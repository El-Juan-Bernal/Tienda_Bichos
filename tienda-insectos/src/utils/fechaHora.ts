/** "2026-11-04T02:26:42.898932" -> "4 nov 2026, 2:26 a. m." */
export function formatoFechaHora(iso: string | null | undefined): string {
  if (!iso) return '';
  const fecha = new Date(iso.replace(/(\.\d{3})\d+/, '$1')); // el servidor manda microsegundos
  return Number.isNaN(fecha.getTime())
    ? ''
    : fecha.toLocaleString('es-CL', { dateStyle: 'medium', timeStyle: 'short' });
}

