import { useEffect, useState } from 'react';
import type { ItemCarrito } from '../types/carrito';
import type { Producto } from '../types/producto';
import type { MostrarToast } from '../types/toast';
import { precioUnitario } from '../utils/precio';

const PREFIJO = 'carrito_insectos_';

function leerGuardado(usuarioId: string | undefined): ItemCarrito[] {
  if (!usuarioId) return [];
  try {
    const guardado = localStorage.getItem(PREFIJO + usuarioId);
    return guardado ? JSON.parse(guardado) : [];
  } catch {
    return [];
  }
}

export function useCarrito(usuarioId: string | undefined, mostrarToast: MostrarToast) {
  const [items, setItems] = useState<ItemCarrito[]>(() => leerGuardado(usuarioId));
  const [duenoActual, setDuenoActual] = useState(usuarioId);

  // Si cambia el usuario (inicia o cierra sesión), se carga el carrito de ese usuario.
  // Se ajusta durante el render para no guardar el carrito de uno bajo la clave del otro.
  if (duenoActual !== usuarioId) {
    setDuenoActual(usuarioId);
    setItems(leerGuardado(usuarioId));
  }

  useEffect(() => {
    if (!usuarioId || duenoActual !== usuarioId) return;
    localStorage.setItem(PREFIJO + usuarioId, JSON.stringify(items));
  }, [items, usuarioId, duenoActual]);

  const totalUnidades = items.reduce((acc, i) => acc + i.cantidad, 0);
  const montoTotal = items.reduce((acc, i) => acc + precioUnitario(i.producto) * i.cantidad, 0);

  /** Devuelve true si se agregó; false si el stock no alcanza. */
  function agregar(producto: Producto, cantidad: number): boolean {
    const existente = items.find((i) => i.producto.id === producto.id);
    const stock = producto.stock ?? 0;

    if ((existente?.cantidad ?? 0) + cantidad > stock) {
      mostrarToast(
        'aviso',
        'Stock insuficiente',
        `No puedes agregar más unidades. El stock máximo disponible para este ejemplar es de ${stock} unidades.`,
      );
      return false;
    }

    setItems((prev) =>
      existente
        ? prev.map((i) =>
            i.producto.id === producto.id ? { ...i, cantidad: i.cantidad + cantidad } : i,
          )
        : [...prev, { producto, cantidad }],
    );
    mostrarToast('exito', 'Agregado al carrito', `${cantidad} unidad(es) de "${producto.nombre}".`);
    return true;
  }

  function eliminar(id: number) {
    const item = items.find((i) => i.producto.id === id);
    setItems((prev) => prev.filter((i) => i.producto.id !== id));
    if (item) {
      mostrarToast('info', 'Producto eliminado', `"${item.producto.nombre}" salió del carrito.`);
    }
  }

  function cambiarCantidad(id: number, delta: number) {
    const actual = items.find((i) => i.producto.id === id);
    if (!actual) return;

    const nueva = actual.cantidad + delta;
    const stock = actual.producto.stock ?? 0;

    if (nueva > stock) {
      mostrarToast('aviso', 'Stock máximo alcanzado', `Hay ${stock} unidades disponibles.`);
      return;
    }
    if (nueva <= 0) {
      eliminar(id);
      return;
    }
    setItems((prev) => prev.map((i) => (i.producto.id === id ? { ...i, cantidad: nueva } : i)));
  }

  function vaciar() {
    setItems([]);
    mostrarToast('info', 'Carrito vaciado', 'Quitaste todos los productos del carrito.');
  }

  /** Vacía sin avisar (después de pagar). */
  function limpiar() {
    setItems([]);
  }

  return {
    items,
    totalUnidades,
    montoTotal,
    agregar,
    eliminar,
    cambiarCantidad,
    vaciar,
    limpiar,
    reemplazar: setItems,
  };
}

export type CarritoApi = ReturnType<typeof useCarrito>;

