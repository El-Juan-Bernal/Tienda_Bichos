import type { Producto } from './producto';

export interface ItemCarrito {
  producto: Producto;
  cantidad: number;
}

