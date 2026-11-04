import type { AccountInfo, IPublicClientApplication } from '@azure/msal-browser';
import { BFF_BASE_URL } from '../config/env';
import { obtenerToken } from './token';

/** Lo mínimo para hablar con el BFF: la instancia de MSAL y la cuenta. */
export interface Sesion {
  instance: IPublicClientApplication;
  account: AccountInfo;
}

/** fetch al BFF con el token puesto. Devuelve la Response tal cual. */
export async function fetchBff(
  sesion: Sesion,
  ruta: string,
  opciones: RequestInit = {},
): Promise<Response> {
  const { accessToken } = await obtenerToken(sesion.instance, sesion.account);
  const headers = new Headers(opciones.headers);
  headers.set('Authorization', `Bearer ${accessToken}`);
  if (opciones.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  return fetch(`${BFF_BASE_URL}${ruta}`, { ...opciones, headers });
}

/** Igual que fetchBff, pero lanza Error si no es 2xx y devuelve el JSON. */
export async function pedirJson<T>(
  sesion: Sesion,
  ruta: string,
  opciones: RequestInit = {},
): Promise<T> {
  const respuesta = await fetchBff(sesion, ruta, opciones);
  const cuerpo = await respuesta.text();
  if (!respuesta.ok) {
    throw new Error(`HTTP ${respuesta.status}: ${cuerpo}`);
  }
  return (cuerpo ? JSON.parse(cuerpo) : undefined) as T;
}

/** fetch sin token, para endpoints públicos (por ejemplo, los destacados). */
export function fetchPublico(ruta: string, opciones: RequestInit = {}) {
  return fetch(`${BFF_BASE_URL}${ruta}`, opciones);
}

