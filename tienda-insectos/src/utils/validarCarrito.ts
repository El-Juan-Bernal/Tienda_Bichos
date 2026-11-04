import type { ItemCarrito } from '../types/carrito';
import type { Producto } from '../types/producto';
import { formatoPrecio } from './formato';
import { precioUnitario } from './precio';

/**
 * Compara el carrito guardado con el catálogo actual:
 * - quita productos que ya no existen, están deshabilitados o agotados
 * - ajusta la cantidad si supera el stock
 * - actualiza los datos del producto y avisa si el precio cambió
 */
export function validarContraCatalogo(carrito: ItemCarrito[], catalogo: Producto[]) {
  const mensajes: string[] = [];
  const carritoValidado: ItemCarrito[] = [];

  for (const item of carrito) {
    const actual = catalogo.find((p) => p.id === item.producto.id);
    const nombre = item.producto.nombre;

    if (!actual || actual.activo === false) {
      mensajes.push(`"${nombre}" ya no está disponible y se quitó del carrito.`);
      continue;
    }

    const stock = actual.stock ?? 0;
    if (stock <= 0) {
      mensajes.push(`"${nombre}" está agotado y se quitó del carrito.`);
      continue;
    }

    let cantidad = item.cantidad;
    if (cantidad > stock) {
      cantidad = stock;
      mensajes.push(`Ajustamos "${nombre}" a ${stock} unidad(es), que es el stock disponible.`);
    }

    const precioAntes = precioUnitario(item.producto);
    const precioAhora = precioUnitario(actual);
    if (precioAntes !== precioAhora) {
      mensajes.push(
        `El precio de "${nombre}" cambió de ${formatoPrecio.format(precioAntes)} a ${formatoPrecio.format(precioAhora)}.`,
      );
    }

    carritoValidado.push({ producto: actual, cantidad });
  }

  return { carritoValidado, mensajes };
}

