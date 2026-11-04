import type { Producto } from '../types/producto';
import { fetchPublico } from './http';

/** Endpoint público del BFF (no pide token). Se crea en la parte de backend. */
export async function obtenerDestacados(): Promise<Producto[]> {
  const respuesta = await fetchPublico('/api/productos/destacados');
  if (!respuesta.ok) {
    throw new Error(`HTTP ${respuesta.status}`);
  }
  return respuesta.json();
}

