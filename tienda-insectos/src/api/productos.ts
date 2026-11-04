import type { Producto } from '../types/producto';
import { fetchBff, pedirJson } from './http';
import type { Sesion } from './http';

export type ProductoNuevo = Omit<Producto, 'id'>;

export function obtenerProductos(sesion: Sesion): Promise<Producto[]> {
  return pedirJson<Producto[]>(sesion, '/api/productos');
}

export function crearProducto(sesion: Sesion, producto: ProductoNuevo) {
  return fetchBff(sesion, '/api/productos', {
    method: 'POST',
    body: JSON.stringify(producto),
  });
}

export function crearProductosMasivo(sesion: Sesion, productos: ProductoNuevo[]) {
  return fetchBff(sesion, '/api/productos/masivo', {
    method: 'POST',
    body: JSON.stringify(productos),
  });
}

export function actualizarProducto(sesion: Sesion, producto: Producto) {
  return fetchBff(sesion, `/api/productos/${producto.id}`, {
    method: 'PUT',
    body: JSON.stringify(producto),
  });
}

export function cambiarEstadoProducto(sesion: Sesion, id: number, activo: boolean) {
  return fetchBff(sesion, `/api/productos/${id}/estado`, {
    method: 'PUT',
    body: JSON.stringify({ activo }),
  });
}

export function cambiarDestacadoProducto(sesion: Sesion, id: number, destacado: boolean) {
  return fetchBff(sesion, `/api/productos/${id}/destacado`, {
    method: 'PUT',
    body: JSON.stringify({ destacado }),
  });
}

