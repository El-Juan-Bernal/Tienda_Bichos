import { mensajeDeError } from './mensajeError';

/** Convierte el Error de pedirJson ("HTTP 403: ...") en un texto para mostrar al usuario. */
export function textoErrorApi(e: unknown): string {
  const texto = e instanceof Error ? e.message : String(e);
  const coincidencia = /^HTTP (\d{3}): ?([\s\S]*)$/.exec(texto);
  if (!coincidencia) {
    return e instanceof TypeError ? 'No se pudo conectar con el servidor.' : texto;
  }
  const [, codigo, cuerpo] = coincidencia;
  if (codigo === '403') {
    return 'Tu cuenta no tiene permisos de administrador para ver esta información.';
  }
  if (cuerpo.trim() === '') {
    return `El servidor respondió con el error ${codigo}.`;
  }
  return mensajeDeError(cuerpo);
}

