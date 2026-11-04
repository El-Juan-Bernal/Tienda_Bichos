import { useState } from 'react';
import { useAuth } from './useAuth';
import type { CarritoApi } from './useCarrito';
import type { MostrarToast } from '../types/toast';
import { obtenerProductos } from '../api/productos';
import { actualizarEstadoPedido, crearPedido } from '../api/pedidos';
import { validarContraCatalogo } from '../utils/validarCarrito';

const DURACION_AVISO_LARGO_MS = 15000;
/** Tiempo mínimo que se ve la tarjeta "Procesando su pago...". */
const MINIMO_PROCESANDO_MS = 3000;

const esperar = (ms: number) => new Promise<void>((r) => setTimeout(r, ms));

export function useCheckout(carrito: CarritoApi, mostrarToast: MostrarToast) {
  const { instance, account } = useAuth();
  const [procesando, setProcesando] = useState(false);

  /** Devuelve true si el carrito estaba correcto; false si hubo que corregirlo. */
  async function sincronizar(
    antesDePagar: boolean,
    avisar: MostrarToast = mostrarToast,
  ): Promise<boolean> {
    if (!account) return false;
    const catalogo = await obtenerProductos({ instance, account });
    const { carritoValidado, mensajes } = validarContraCatalogo(carrito.items, catalogo);
    if (mensajes.length === 0) return true;

    carrito.reemplazar(carritoValidado);
    const detalle = antesDePagar
      ? [...mensajes, '', 'No se procesó el pago. Revisa tu carrito y vuelve a intentarlo.']
      : mensajes;
    avisar('aviso', 'Actualizamos tu carrito', detalle.join('\n'), DURACION_AVISO_LARGO_MS);
    return false;
  }

  /** Devuelve true si el pedido quedó creado y aceptado. */
  async function pagar(pasarela: string): Promise<boolean> {
    if (!account) return false;
    const sesion = { instance, account };
    const inicio = Date.now();

    // Los avisos esperan a que la tarjeta cumpla su tiempo mínimo.
    const avisar: MostrarToast = (tipo, titulo, mensaje, duracion) => {
      const falta = MINIMO_PROCESANDO_MS - (Date.now() - inicio);
      void esperar(Math.max(0, falta)).then(() => mostrarToast(tipo, titulo, mensaje, duracion));
    };

    setProcesando(true);
    try {
      if (!(await sincronizar(true, avisar))) return false;

      // El dueño del pedido lo toma el BFF desde el token, no se envía desde aquí.
      const respuesta = await crearPedido(sesion, {
        pasarelaSeleccionada: pasarela,
        items: carrito.items.map((i) => ({ productoId: i.producto.id, cantidad: i.cantidad })),
        montoTotal: carrito.montoTotal,
      });
      if (!respuesta.ok) {
        avisar('error', 'No se pudo completar el pedido', await respuesta.text());
        return false;
      }

      const pedido = await respuesta.json();
      const aceptado = await actualizarEstadoPedido(sesion, pedido.id, 'ACEPTADO');
      if (!aceptado.ok) {
        avisar(
          'aviso',
          'Pedido creado, pago sin confirmar',
          'El pedido se creó, pero hubo un error al confirmar el pago en el servidor.',
        );
        return false;
      }

      avisar(
        'exito',
        `¡Pago aprobado con ${pasarela}!`,
        'Tu compra se realizó con éxito.\nTu boleta será enviada a tu correo.',
      );
      carrito.limpiar();
      return true;
    } catch (error) {
      console.error('Error al procesar pago:', error);
      avisar(
        'error',
        'Error de conexión',
        'No pudimos comunicarnos con el servidor para procesar el pago. Inténtalo de nuevo.',
      );
      return false;
    } finally {
      const falta = MINIMO_PROCESANDO_MS - (Date.now() - inicio);
      if (falta > 0) await esperar(falta);
      setProcesando(false);
    }
  }

  return { procesando, sincronizar, pagar };
}

