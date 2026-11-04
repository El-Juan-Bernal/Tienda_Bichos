import { useCallback, useState } from 'react';
import { useAuth } from './useAuth';
import type { Producto } from '../types/producto';
import type { MostrarToast } from '../types/toast';
import type { Sesion } from '../api/http';
import {
  actualizarProducto,
  cambiarDestacadoProducto,
  cambiarEstadoProducto,
  crearProductosMasivo,
  obtenerProductos,
} from '../api/productos';
import { mensajeDeError } from '../utils/mensajeError';

export function useAdminProductos(mostrarToast: MostrarToast) {
  const { instance, account } = useAuth();
  const [productos, setProductos] = useState<Producto[]>([]);
  const [cargando, setCargando] = useState(true);

  const cargar = useCallback(async () => {
    if (!account) return;
    try {
      setProductos(await obtenerProductos({ instance, account }));
    } catch (e) {
      mostrarToast('error', 'No se pudo cargar el inventario', e instanceof Error ? e.message : String(e));
    } finally {
      setCargando(false);
    }
  }, [instance, account, mostrarToast]);

  /** Ejecuta la llamada; si no hay conexión avisa y devuelve null. */
  async function llamar(accion: (sesion: Sesion) => Promise<Response>): Promise<Response | null> {
    if (!account) return null;
    try {
      return await accion({ instance, account });
    } catch (e) {
      console.error('Error de red:', e);
      mostrarToast('error', 'Error de conexión', 'No pudimos comunicarnos con el servidor. Inténtalo de nuevo.');
      return null;
    }
  }

  async function avisarFallo(r: Response, titulo: string, tipo: 'error' | 'aviso' = 'error') {
    mostrarToast(tipo, titulo, mensajeDeError(await r.text()));
  }

  async function crear(nuevos: Omit<Producto, 'id'>[]): Promise<boolean> {
    const r = await llamar((s) => crearProductosMasivo(s, nuevos));
    if (!r) return false;
    if (!r.ok) {
      await avisarFallo(r, 'No se pudo guardar en la base de datos');
      return false;
    }
    mostrarToast('exito', 'Productos guardados', 'Los productos se guardaron correctamente.');
    await cargar();
    return true;
  }

  async function guardarEdicion(producto: Producto): Promise<boolean> {
    const r = await llamar((s) => actualizarProducto(s, producto));
    if (!r) return false;
    if (!r.ok) {
      await avisarFallo(r, 'No se pudo actualizar el producto');
      return false;
    }
    mostrarToast('exito', 'Producto actualizado', `"${producto.nombre}" se actualizó correctamente.`);
    await cargar();
    return true;
  }

  async function aplicarDescuento(producto: Producto, porcentaje: number) {
    const r = await llamar((s) => actualizarProducto(s, { ...producto, porcentajeDescuento: porcentaje }));
    if (!r) return;
    if (!r.ok) {
      await avisarFallo(r, 'El servidor rechazó el cambio');
      return;
    }
    mostrarToast(
      'exito',
      porcentaje > 0 ? 'Descuento aplicado' : 'Descuento eliminado',
      porcentaje > 0
        ? `Se guardó un ${porcentaje}% de descuento para "${producto.nombre}".`
        : `"${producto.nombre}" vuelve a su precio base.`,
    );
    await cargar();
  }

  async function cambiarActivo(producto: Producto) {
    const habilitar = producto.activo === false;
    const r = await llamar((s) => cambiarEstadoProducto(s, producto.id, habilitar));
    if (!r) return;
    if (!r.ok) {
      await avisarFallo(r, 'No se pudo cambiar el estado');
      return;
    }
    if (!habilitar && producto.destacado) {
      mostrarToast('info', 'Producto deshabilitado', `"${producto.nombre}" dejó de estar destacado.`);
    }
    await cargar();
  }

  async function cambiarDestacado(producto: Producto) {
    const r = await llamar((s) => cambiarDestacadoProducto(s, producto.id, !producto.destacado));
    if (!r) return;
    if (!r.ok) {
      // 409: ya hay 8 destacados o el producto está deshabilitado
      await avisarFallo(r, 'No se pudo cambiar el destacado', r.status === 409 ? 'aviso' : 'error');
      return;
    }
    await cargar();
  }

  return { productos, cargando, cargar, crear, guardarEdicion, aplicarDescuento, cambiarActivo, cambiarDestacado };
}

export type AdminApi = ReturnType<typeof useAdminProductos>;

