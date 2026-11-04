import { useCallback, useState } from 'react';
import type { MostrarToast, TipoToast, ToastData } from '../types/toast';

/** Duración por defecto de cada notificación (7 segundos). */
const DURACION_TOAST_MS = 7000;
const MAXIMO_TOASTS = 4;

export function useToasts() {
  const [toasts, setToasts] = useState<ToastData[]>([]);

  const cerrarToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const mostrarToast: MostrarToast = useCallback(
    (tipo: TipoToast, titulo: string, mensaje?: string, duracion = DURACION_TOAST_MS) => {
      const id = Date.now() + Math.random();
      setToasts((prev) => [...prev, { id, tipo, titulo, mensaje, duracion }].slice(-MAXIMO_TOASTS));
    },
    [],
  );

  return { toasts, mostrarToast, cerrarToast };
}

