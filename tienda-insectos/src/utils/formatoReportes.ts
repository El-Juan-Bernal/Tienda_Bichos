/** Los servidores mandan microsegundos; JavaScript solo entiende hasta milisegundos. */
function aFecha(iso: string): Date {
  return new Date(iso.replace(/(\.\d{3})\d+/, '$1'));
}

/** 131 -> "2 min 11 s" */
export function formatoDuracion(segundos: number): string {
  const total = Math.round(segundos);
  if (total < 60) return `${total} s`;
  const horas = Math.floor(total / 3600);
  const minutos = Math.floor((total % 3600) / 60);
  const resto = total % 60;
  if (horas > 0) return `${horas} h ${minutos} min`;
  return resto > 0 ? `${minutos} min ${resto} s` : `${minutos} min`;
}

/** "2026-11-18T14" -> "18/11 14h" */
export function etiquetaHora(hora: string): string {
  const [fecha, h] = hora.split('T');
  const [, mes, dia] = (fecha ?? '').split('-');
  return mes && dia && h !== undefined ? `${dia}/${mes} ${h}h` : hora;
}

export function segundosEntre(inicio: string, fin: string): number {
  return (aFecha(fin).getTime() - aFecha(inicio).getTime()) / 1000;
}

/** Fecha y hora con segundos, para ver el orden exacto de los eventos. */
export function formatoFechaHoraSeg(iso: string | null | undefined): string {
  if (!iso) return '';
  const fecha = aFecha(iso);
  return Number.isNaN(fecha.getTime())
    ? ''
    : fecha.toLocaleString('es-CL', { dateStyle: 'medium', timeStyle: 'medium' });
}

