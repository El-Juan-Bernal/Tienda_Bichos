import { useCallback, useEffect, useState } from 'react';
import { useAuth } from './useAuth';
import type { Pedido } from '../types/pedido';
import type { MostrarToast } from '../types/toast';
import { cancelarPedido, listarPedidos } from '../api/pedidos';
import { obtenerProductos } from '../api/productos';
import { formatoPrecio } from '../utils/formato';
import { mensajeDeError } from '../utils/mensajeError';

const REFRESCO_MS = 5000;

export function usePedidos(mostrarToast: MostrarToast) {
  const { instance, account } = useAuth();
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [nombres, setNombres] = useState<Record<number, string>>({});
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [cancelando, setCancelando] = useState(false);

  const cargar = useCallback(async () => {
    if (!account) return;
    try {
      const datos = await listarPedidos({ instance, account });
      // Un pedido sin pagar (CREADO) todavía no es un pedido para el cliente.
      setPedidos(datos.filter((p) => p.estado !== 'CREADO').sort((a, b) => b.id - a.id));
      setError('');
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setCargando(false);
    }
  }, [instance, account]);

  // Carga inicial y refresco automático (se pausa si la pestaña no está a la vista).
  useEffect(() => {
    void cargar();
    const timer = setInterval(() => {
      if (!document.hidden) void cargar();
    }, REFRESCO_MS);
    return () => clearInterval(timer);
  }, [cargar]);

  // Los pedidos solo traen ids de producto: se cruzan con el catálogo para mostrar nombres.
  useEffect(() => {
    if (!account) return;
    obtenerProductos({ instance, account })
      .then((lista) => setNombres(Object.fromEntries(lista.map((p) => [p.id, p.nombre]))))
      .catch(() => {
        // Sin catálogo se muestra "Producto #id".
      });
  }, [instance, account]);

  const cancelar = useCallback(
    async (pedido: Pedido) => {
      if (!account) return;
      setCancelando(true);
      try {
        const respuesta = await cancelarPedido({ instance, account }, pedido.id);
        if (respuesta.ok) {
          mostrarToast(
            'exito',
            'Pedido cancelado',
            `Te devolveremos ${formatoPrecio.format(pedido.montoTotal ?? 0)} por ${
              pedido.pasarelaSeleccionada ?? 'la pasarela elegida'
            }.\nRecibirás el detalle en tu correo.`,
          );
        } else {
          mostrarToast('error', 'No se pudo cancelar el pedido', mensajeDeError(await respuesta.text()));
        }
      } catch (e) {
        console.error('Error al cancelar:', e);
        mostrarToast('error', 'Error de conexión', 'No pudimos comunicarnos con el servidor. Inténtalo de nuevo.');
      } finally {
        setCancelando(false);
        await cargar();
      }
    },
    [instance, account, mostrarToast, cargar],
  );

  return { pedidos, nombres, cargando, error, cancelando, cancelar };
}

