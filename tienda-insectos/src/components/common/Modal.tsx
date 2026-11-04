import { useEffect } from 'react';

interface Props {
  titulo: string;
  onCerrar: () => void;
  ancho?: 'normal' | 'angosto';
  children: React.ReactNode;
}

/** Ventana flotante reutilizable: se cierra con Escape o haciendo clic fuera. */
export default function Modal({ titulo, onCerrar, ancho = 'normal', children }: Readonly<Props>) {
  useEffect(() => {
    const alTeclear = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onCerrar();
    };
    document.addEventListener('keydown', alTeclear);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', alTeclear);
      document.body.style.overflow = '';
    };
  }, [onCerrar]);

  return (
    <div
      className="modal-fondo"
      role="presentation"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onCerrar();
      }}
    >
      <div
        className={ancho === 'angosto' ? 'modal modal-angosto' : 'modal'}
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
      >
        <button type="button" className="modal-cerrar" aria-label="Cerrar" onClick={onCerrar}>
          ×
        </button>
        {children}
      </div>
    </div>
  );
}

