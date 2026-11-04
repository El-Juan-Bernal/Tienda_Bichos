import { useState } from 'react';
import { usePedidos } from '../hooks/usePedidos';
import type { Pedido } from '../types/pedido';
import type { MostrarToast } from '../types/toast';
import Paginacion from '../components/common/Paginacion';
import TarjetaPedido from '../components/pedidos/TarjetaPedido';
import ModalCancelarPedido from '../components/pedidos/ModalCancelarPedido';

const POR_PAGINA = 5;

interface Props {
  mostrarToast: MostrarToast;
  onIrCatalogo: () => void;
}

export default function Pedidos({ mostrarToast, onIrCatalogo }: Readonly<Props>) {
  const { pedidos, nombres, cargando, error, cancelando, cancelar } = usePedidos(mostrarToast);
  const [pagina, setPagina] = useState(1);
  const [aCancelar, setACancelar] = useState<Pedido | null>(null);

  const totalPaginas = Math.max(1, Math.ceil(pedidos.length / POR_PAGINA));
  const paginaActual = Math.min(pagina, totalPaginas);
  const visibles = pedidos.slice((paginaActual - 1) * POR_PAGINA, paginaActual * POR_PAGINA);

  async function confirmarCancelacion() {
    if (!aCancelar) return;
    await cancelar(aCancelar);
    setACancelar(null);
  }

  return (
    <section className="contenedor pagina-pedidos">
      <header className="pedidos-cabecera">
        <p className="etiqueta">Tu cuenta</p>
        <h1>Mis pedidos</h1>
        <p className="pedidos-ayuda">Se actualizan solos cada pocos segundos.</p>
      </header>

      {cargando && <p className="catalogo-aviso">Cargando tus pedidos...</p>}
      {error && pedidos.length === 0 && <p className="catalogo-aviso catalogo-error">{error}</p>}

      {!cargando && !error && pedidos.length === 0 && (
        <div className="carrito-vacio">
          <p>Aún no tienes pedidos. ¡Explora el catálogo y haz tu primera compra!</p>
          <button type="button" className="btn btn-primario" onClick={onIrCatalogo}>
            Ver catálogo
          </button>
        </div>
      )}

      <div className="pedidos-lista">
        {visibles.map((p) => (
          <TarjetaPedido key={p.id} pedido={p} nombres={nombres} onCancelar={setACancelar} />
        ))}
      </div>

      <Paginacion
        pagina={paginaActual}
        total={totalPaginas}
        onCambiar={(n) => {
          setPagina(n);
          window.scrollTo({ top: 0 });
        }}
      />

      {aCancelar && (
        <ModalCancelarPedido
          pedido={aCancelar}
          procesando={cancelando}
          onConfirmar={() => void confirmarCancelacion()}
          onCerrar={() => {
            if (!cancelando) setACancelar(null);
          }}
        />
      )}
    </section>
  );
}

