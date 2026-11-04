import { useEffect, useState } from 'react';
import type { Producto } from '../types/producto';
import { obtenerDestacados } from '../api/destacados';
import { MAXIMO_DESTACADOS } from '../config/limites';

export function useDestacados() {
  const [destacados, setDestacados] = useState<Producto[]>([]);

  useEffect(() => {
    let activo = true;
    obtenerDestacados()
      .then((datos) => {
        if (activo) setDestacados(datos.slice(0, MAXIMO_DESTACADOS));
      })
      .catch(() => {
        // Sin endpoint o sin conexión: se muestran los recomendados fijos.
      });
    return () => {
      activo = false;
    };
  }, []);

  return { destacados };
}

