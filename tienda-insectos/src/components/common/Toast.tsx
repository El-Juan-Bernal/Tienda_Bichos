import { useEffect } from 'react';
import type { TipoToast, ToastData } from '../../types/toast';

const iconos: Record<TipoToast, string> = {
  exito: '✓',
  error: '✕',
  aviso: '!',
  info: 'i',
};

function ToastItem({
  toast,
  onCerrar,
}: Readonly<{ toast: ToastData; onCerrar: (id: number) => void }>) {
  useEffect(() => {
    const timer = setTimeout(() => onCerrar(toast.id), toast.duracion);
    return () => clearTimeout(timer);
  }, [toast.id, toast.duracion, onCerrar]);

  return (
    <div
      className={`toast toast-${toast.tipo}`}
      role={toast.tipo === 'error' ? 'alert' : undefined}
      style={{ ['--toast-duracion' as string]: `${toast.duracion}ms` }}
    >
      <span className="toast-icono" aria-hidden="true">
        {iconos[toast.tipo]}
      </span>
      <div className="toast-texto">
        <p className="toast-titulo">{toast.titulo}</p>
        {toast.mensaje && <p className="toast-mensaje">{toast.mensaje}</p>}
      </div>
      <button
        type="button"
        className="toast-cerrar"
        aria-label="Cerrar notificación"
        onClick={() => onCerrar(toast.id)}
      >
        ×
      </button>
      <span className="toast-progreso" aria-hidden="true" />
    </div>
  );
}

export function ToastContainer({
  toasts,
  onCerrar,
}: Readonly<{ toasts: ToastData[]; onCerrar: (id: number) => void }>) {
  return (
    <div className="toast-contenedor" aria-live="polite">
      {toasts.map((t) => (
        <ToastItem key={t.id} toast={t} onCerrar={onCerrar} />
      ))}
    </div>
  );
}

