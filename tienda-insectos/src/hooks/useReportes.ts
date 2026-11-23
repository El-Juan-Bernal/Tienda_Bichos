import { useCallback, useEffect, useRef, useState } from 'react';
import { useAuth } from './useAuth';
import type { Kpis, RangoReporte, TopProducto } from '../types/reporte';
import type { MostrarToast } from '../types/toast';
import { obtenerKpis, obtenerTopProductos } from '../api/reportes';
import { textoErrorApi } from '../utils/errorApi';

const REFRESCO_MS = 10_000;

interface Datos {
  rango: RangoReporte;
  kpis: Kpis;
  top: TopProducto[];
}

interface Fallo {
  rango: RangoReporte;
  mensaje: string;
}

/** Carga los KPIs y el top de productos, y los refresca solos cada 10 segundos. */
export function useReportes(rango: RangoReporte, mostrarToast: MostrarToast) {
  const { instance, account } = useAuth();
  const [datos, setDatos] = useState<Datos | null>(null);
  const [fallo, setFallo] = useState<Fallo | null>(null);
  const [actualizado, setActualizado] = useState<Date | null>(null);
  const falloAvisado = useRef(false);

  // Se usa .then() y no async/await: así los setState quedan dentro de callbacks
  // y la regla react-hooks/set-state-in-effect no los confunde con llamadas directas.
  const consultar = useCallback((): Promise<void> => {
    if (!account) return Promise.resolve();
    const sesion = { instance, account };
    return Promise.all([obtenerKpis(sesion, rango), obtenerTopProductos(sesion, rango)]).then(
      ([kpis, top]) => {
        setDatos({ rango, kpis, top });
        setFallo(null);
        setActualizado(new Date());
        falloAvisado.current = false;
      },
      (e: unknown) => {
        const mensaje = textoErrorApi(e);
        setFallo({ rango, mensaje });
        // Un solo aviso: si el servicio sigue caído no se repite cada 10 segundos
        if (!falloAvisado.current) {
          falloAvisado.current = true;
          mostrarToast('error', 'No se pudieron cargar los reportes', mensaje);
        }
      },
    );
  }, [instance, account, rango, mostrarToast]);

  useEffect(() => {
    void consultar();
    const id = setInterval(() => void consultar(), REFRESCO_MS);
    return () => clearInterval(id);
  }, [consultar]);

  // Solo se muestran datos del rango elegido (al cambiar de rango no se ven los del anterior)
  const vigente = datos?.rango === rango ? datos : null;
  const error = fallo?.rango === rango ? fallo.mensaje : null;

  return {
    kpis: vigente?.kpis ?? null,
    top: vigente?.top ?? [],
    cargando: vigente === null && error === null,
    error,
    actualizado,
    recargar: consultar,
  };
}

