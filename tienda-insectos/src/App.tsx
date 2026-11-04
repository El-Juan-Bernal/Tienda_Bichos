import { useEffect, useState } from 'react';
import { useAuth } from './hooks/useAuth';
import { useCarrito } from './hooks/useCarrito';
import { useToasts } from './hooks/useToasts';
import { useOcultarAlScroll } from './hooks/useOcultarAlScroll';
import type { Vista } from './types/vista';
import BarraSesion from './components/layout/BarraSesion';
import MenuPrincipal from './components/layout/MenuPrincipal';
import Footer from './components/layout/Footer';
import { ToastContainer } from './components/common/Toast';
import Admin from './pages/Admin';
import Inicio from './pages/Inicio';
import Catalogo from './pages/Catalogo';
import Carrito from './pages/Carrito';
import Pedidos from './pages/Pedidos';

const VISTAS = new Set<string>(['inicio', 'catalogo', 'carrito', 'pedidos', 'admin']);

function vistaGuardada(): Vista {
  const guardada = sessionStorage.getItem('vista_actual');
  return guardada && VISTAS.has(guardada) ? (guardada as Vista) : 'inicio';
}

export default function App() {
  const { account, bloqueado, isAdmin, entrar, salir } = useAuth();
  const { toasts, mostrarToast, cerrarToast } = useToasts();
  const carrito = useCarrito(account?.homeAccountId, mostrarToast);
  const menuOculto = useOcultarAlScroll();

  const [vista, setVista] = useState<Vista>(vistaGuardada);

  // Sin sesión solo se puede ver el inicio; el panel exige rol Admin.
  const vistaActiva: Vista =
    vista === 'inicio' || (account && (vista !== 'admin' || isAdmin)) ? vista : 'inicio';

  useEffect(() => {
    sessionStorage.setItem('vista_actual', vistaActiva);
    window.scrollTo(0, 0);
  }, [vistaActiva]);

    function verCatalogo() {
    if (account) setVista('catalogo');
    else void entrar();
  }
  
  return (
    <div className="pagina">
      <BarraSesion
        usuario={account?.username}
        bloqueado={bloqueado}
        onEntrar={entrar}
        onSalir={salir}
      />
      <MenuPrincipal
        vista={vistaActiva}
        onCambiar={setVista}
        hayCuenta={Boolean(account)}
        isAdmin={isAdmin}
        totalCarrito={carrito.totalUnidades}
        oculto={menuOculto}
      />

      <main className="contenido">
        {vistaActiva === 'admin' && <Admin mostrarToast={mostrarToast} />}
        {vistaActiva === 'inicio' && <Inicio onVerCatalogo={verCatalogo} />}
        {vistaActiva === 'catalogo' && <Catalogo onAgregar={carrito.agregar} />}
        {vistaActiva === 'carrito' && (
          <Carrito
            carrito={carrito}
            mostrarToast={mostrarToast}
            onIrCatalogo={() => setVista('catalogo')}
          />
        )}
        {vistaActiva === 'pedidos' && (
          <Pedidos mostrarToast={mostrarToast} onIrCatalogo={() => setVista('catalogo')} />
        )}
      </main>

      <Footer onCambiar={setVista} hayCuenta={Boolean(account)} />
      <ToastContainer toasts={toasts} onCerrar={cerrarToast} />
    </div>
  );
}

