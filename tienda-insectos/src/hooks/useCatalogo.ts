import { useCallback, useState } from 'react';
import type { AccountInfo, IPublicClientApplication } from '@azure/msal-browser';
import type { Producto } from '../types/producto';
import { obtenerProductos } from '../api/productos';

export function useCatalogo(
  instance: IPublicClientApplication,
  account: AccountInfo | undefined,
) {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');

  const cargar = useCallback(async () => {
    if (!account) return;
    setCargando(true);
    setError('');
    try {
      const datos = await obtenerProductos({ instance, account });
      setProductos(datos.filter((p) => p.activo !== false));
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setCargando(false);
    }
  }, [instance, account]);

  return { productos, cargando, error, cargar };
}

