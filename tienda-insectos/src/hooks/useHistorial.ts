import { useEffect, useState } from 'react';
import { useAuth } from './useAuth';
import type { HistorialPedido } from '../types/pedido';
import { obtenerHistorial } from '../api/pedidos';

/** Historial de un pedido; se vuelve a pedir cada vez que cambia su estado. */
export function useHistorial(pedidoId: number, estado: string): HistorialPedido[] {
  const { instance, account } = useAuth();
  const [historial, setHistorial] = useState<HistorialPedido[]>([]);

  useEffect(() => {
    if (!account) return;
    let activo = true;
    obtenerHistorial({ instance, account }, pedidoId)
      .then((datos) => {
        if (activo) setHistorial(datos);
      })
      .catch(() => {
        // Sin historial la línea se arma igual con el estado actual.
      });
    return () => {
      activo = false;
    };
  }, [instance, account, pedidoId, estado]);

  return historial;
}

