/** Saca el texto legible de una respuesta de error del BFF (JSON {"error": "..."} o texto plano). */
export function mensajeDeError(texto: string): string {
  try {
    const dato = JSON.parse(texto);
    if (typeof dato === 'string') return dato;
    return dato.error ?? dato.message ?? texto;
  } catch {
    return texto || 'Error desconocido.';
  }
}

