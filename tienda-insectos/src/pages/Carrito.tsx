import { useEffect, useState } from 'react';
import type { CarritoApi } from '../hooks/useCarrito';
import { useCheckout } from '../hooks/useCheckout';
import type { MostrarToast } from '../types/toast';
import { IconoBasura } from '../components/common/Iconos';
import FilaCarrito from '../components/carrito/FilaCarrito';
import ResumenCompra from '../components/carrito/ResumenCompra';
import ModalPasarelas from '../components/carrito/ModalPasarelas';
import ModalVaciar from '../components/carrito/ModalVaciar';
import ProcesandoPago from '../components/common/ProcesandoPago';

interface Props {
  carrito: CarritoApi;
  mostrarToast: MostrarToast;
  onIrCatalogo: () => void;
}

export default function Carrito({ carrito, mostrarToast, onIrCatalogo }: Readonly<Props>) {
  const { procesando, sincronizar, pagar } = useCheckout(carrito, mostrarToast);
  const [verPasarelas, setVerPasarelas] = useState(false);
  const [verVaciar, setVerVaciar] = useState(false);

  // Al abrir el carrito se revisa contra el catálogo actual (stock, precios, deshabilitados).
  useEffect(() => {
    if (carrito.items.length === 0) return;
    sincronizar(false).catch((e) => console.error('No se pudo revisar el carrito:', e));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

    async function elegirPasarela(pasarela: string) {
    setVerPasarelas(false); // el recuadro desaparece de inmediato
    await pagar(pasarela);
  }

  function confirmarVaciar() {
    carrito.vaciar();
    setVerVaciar(false);
  }

  return (
    <section className="contenedor pagina-carrito">
      <header className="carrito-cabecera">
        <div>
          <p className="etiqueta">Tu compra</p>
          <h1>Carrito de compras</h1>
        </div>
        {carrito.items.length > 0 && (
          <button type="button" className="btn-vaciar" onClick={() => setVerVaciar(true)}>
            <IconoBasura />
            Vaciar carrito
          </button>
        )}
      </header>

      {carrito.items.length === 0 ? (
        <div className="carrito-vacio">
          <p>Tu carrito está vacío. ¡Explora el catálogo y añade ejemplares!</p>
          <button type="button" className="btn btn-primario" onClick={onIrCatalogo}>
            Ver catálogo
          </button>
        </div>
      ) : (
        <div className="carrito-layout">
          <div className="carrito-lista">
            {carrito.items.map((item) => (
              <FilaCarrito
                key={item.producto.id}
                item={item}
                onCambiar={carrito.cambiarCantidad}
                onEliminar={carrito.eliminar}
              />
            ))}
          </div>
          <ResumenCompra
            unidades={carrito.totalUnidades}
            monto={carrito.montoTotal}
            procesando={procesando}
            onPagar={() => setVerPasarelas(true)}
          />
        </div>
      )}

      {verPasarelas && (
        <ModalPasarelas
          monto={carrito.montoTotal}
          onElegir={(p) => void elegirPasarela(p)}
          onCerrar={() => setVerPasarelas(false)}
        />
      )}
      {verVaciar && (
        <ModalVaciar
          unidades={carrito.totalUnidades}
          onConfirmar={confirmarVaciar}
          onCerrar={() => setVerVaciar(false)}
        />
      )}
      {procesando && <ProcesandoPago />}
    </section>
  );
}

