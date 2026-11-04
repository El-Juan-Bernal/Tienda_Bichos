import { useEffect, useState } from 'react';

const ALTURA_MINIMA = 120; // no se oculta cerca del tope de la página
const MOVIMIENTO_MINIMO = 8; // ignora micro-movimientos

/** true cuando el usuario baja; vuelve a false apenas sube un poco. */
export function useOcultarAlScroll(): boolean {
  const [oculto, setOculto] = useState(false);

  useEffect(() => {
    let ultimo = window.scrollY;

    const alScroll = () => {
      const y = window.scrollY;
      if (Math.abs(y - ultimo) < MOVIMIENTO_MINIMO) return;
      setOculto(y > ultimo && y > ALTURA_MINIMA);
      ultimo = y;
    };

    window.addEventListener('scroll', alScroll, { passive: true });
    return () => window.removeEventListener('scroll', alScroll);
  }, []);

  return oculto;
}

