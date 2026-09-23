import type {
  AccountInfo,
  IPublicClientApplication,
} from '@azure/msal-browser';
import { obtenerToken } from './token';

export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  imagenUrl: string;
  categoria: string;
}

async function llamarBff<T>(
  instance: IPublicClientApplication,
  account: AccountInfo,
  ruta: string,
): Promise<T> {
  const base = import.meta.env.VITE_BFF_BASE_URL?.replace(/\/$/, '');

  if (!base) {
    throw new Error('Complete VITE_BFF_BASE_URL y reinicie Vite');
  }

  const token = await obtenerToken(instance, account);
  const response = await fetch(`${base}${ruta}`, {
    headers: {
      Authorization: `Bearer ${token.accessToken}`,
    },
  });

  const body = await response.text();
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${body}`);
  }

  return JSON.parse(body) as T;
}

export function obtenerProductos(
  instance: IPublicClientApplication,
  account: AccountInfo,
): Promise<Producto[]> {
  return llamarBff<Producto[]>(instance, account, '/api/productos');
}
