import type { Producto } from '../types/producto';

export interface FormProducto {
  nombre: string;
  descripcion: string;
  categoria: string;
  precio: number | '';
  stock: number | '';
  imagenUrl: string;
}

export const MAX_DESCRIPCION = 1000;

export function formVacio(): FormProducto {
  return { nombre: '', descripcion: '', categoria: '', precio: '', stock: '', imagenUrl: '' };
}

export function formDesdeProducto(p: Producto): FormProducto {
  return {
    nombre: p.nombre,
    descripcion: p.descripcion ?? '',
    categoria: p.categoria,
    precio: p.precio,
    stock: p.stock,
    imagenUrl: p.imagenUrl ?? '',
  };
}

/** Devuelve el texto del problema, o null si el formulario está bien. */
export function validarForm(f: FormProducto): string | null {
  if (!f.nombre.trim() || !f.categoria || f.precio === '' || f.stock === '') {
    return 'completa el nombre, la categoría, el precio y el stock.';
  }
  if (f.descripcion.length > MAX_DESCRIPCION) {
    return `la descripción excede los ${MAX_DESCRIPCION} caracteres permitidos.`;
  }
  return null;
}

export function aProductoNuevo(f: FormProducto): Omit<Producto, 'id'> {
  return {
    nombre: f.nombre.trim(),
    descripcion: f.descripcion.trim(),
    categoria: f.categoria,
    precio: Number(f.precio),
    stock: Number(f.stock),
    imagenUrl: f.imagenUrl.trim(),
    porcentajeDescuento: 0,
    activo: true,
  };
}

