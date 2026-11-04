export type Categoria = 'VOLADOR' | 'TERRESTRE' | 'ACUATICO';

export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  imagenUrl: string;
  categoria: string;
  porcentajeDescuento?: number;
  activo?: boolean;
  destacado?: boolean;
}

