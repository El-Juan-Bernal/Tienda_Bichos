import type { Producto } from '../types/producto';

/** Precio final de una unidad, con el descuento aplicado si lo tiene. */
export function precioUnitario(p: Producto): number {
  const descuento = p.porcentajeDescuento || 0;
  return descuento > 0 ? Math.round(p.precio * (1 - descuento / 100)) : p.precio;
}

