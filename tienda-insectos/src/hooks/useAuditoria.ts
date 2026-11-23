import { useCallback, useEffect, useState } from 'react';
import { useAuth } from './useAuth';
import type { FiltrosEventos, PaginaEventos, TimelinePedido } from '../types/evento';
import type { MostrarToast } from '../types/toast';
import { FILTROS_VACIOS, listarEventos, obtenerTimeline } from '../api/auditoria';
import { textoErrorApi } from '../utils/errorApi';

export const EVENTOS_POR_PAGINA = 15;

export function useAuditoria(mostrarToast: MostrarToast) {
  const { instance, account } = useAuth();
  const [filtros, setFiltros] = useState<FiltrosEventos>(FILTROS_VACIOS);
  const [pagina, setPagina] = useState(1);
  const [resultado, setResultado] = useState<PaginaEventos | null>(null);
  const [cargando, setCargando] = useState(true);
  const [timeline, setTimeline] = useState<TimelinePedido | null>(null);

  // Se usa .then() y no async/await: así los setState quedan dentro de callbacks
  // y la regla react-hooks/set-state-in-effect no los confunde con llamadas directas.
  const cargar = useCallback(
    (f: FiltrosEventos, p: number): Promise<void> => {
      if (!account) return Promise.resolve();
      return listarEventos({ instance, account }, f, p - 1, EVENTOS_POR_PAGINA)
        .then(setResultado, (e: unknown) => {
          mostrarToast('error', 'No se pudo cargar la auditoría', textoErrorApi(e));
        })
        .finally(() => setCargando(false));
    },
    [instance, account, mostrarToast],
  );

  useEffect(() => {
    void cargar(FILTROS_VACIOS, 1);
  }, [cargar]);

  function buscar(nuevos: FiltrosEventos) {
    setFiltros(nuevos);
    setPagina(1);
    setCargando(true);
    void cargar(nuevos, 1);
  }

  function irAPagina(numero: number) {
    setPagina(numero);
    setCargando(true);
    void cargar(filtros, numero);
  }

  async function verTimeline(pedidoId: number) {
    if (!account) return;
    setTimeline({ pedidoId, eventos: [], cargando: true });
    try {
      const eventos = await obtenerTimeline({ instance, account }, pedidoId);
      setTimeline({ pedidoId, eventos, cargando: false });
    } catch (e) {
      setTimeline(null);
      mostrarToast('error', `No se pudo cargar el pedido #${pedidoId}`, textoErrorApi(e));
    }
  }

  return {
    filtros,
    pagina,
    resultado,
    cargando,
    timeline,
    buscar,
    irAPagina,
    verTimeline,
    cerrarTimeline: () => setTimeline(null),
  };
}

