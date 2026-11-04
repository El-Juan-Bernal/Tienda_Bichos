import { useState } from 'react';
import type { Vista } from '../../types/vista';
import Logo from '../common/Logo';
import { IconoCarrito, IconoCerrar, IconoMenu } from '../common/Iconos';

interface Props {
  vista: Vista;
  onCambiar: (vista: Vista) => void;
  hayCuenta: boolean;
  isAdmin: boolean;
  totalCarrito: number;
  oculto: boolean;
}

export default function MenuPrincipal({
  vista,
  onCambiar,
  hayCuenta,
  isAdmin,
  totalCarrito,
  oculto,
}: Readonly<Props>) {
  const [abierto, setAbierto] = useState(false);

  const items: { vista: Vista; etiqueta: string; mostrar: boolean; deshabilitado?: boolean }[] = [
    { vista: 'inicio', etiqueta: 'Inicio', mostrar: true },
    { vista: 'catalogo', etiqueta: 'Catálogo', mostrar: true, deshabilitado: !hayCuenta },
    { vista: 'pedidos', etiqueta: 'Mis pedidos', mostrar: hayCuenta },
    { vista: 'admin', etiqueta: 'Panel Admin', mostrar: isAdmin },
  ];

  function ir(destino: Vista) {
    onCambiar(destino);
    setAbierto(false);
  }

  const escondido = oculto && !abierto;

  return (
    <header className={escondido ? 'menu-principal menu-oculto' : 'menu-principal'}>
      <div className="menu-interior">
        <button type="button" className="menu-logo" aria-label="Ir al inicio" onClick={() => ir('inicio')}>
          <Logo />
        </button>

        <nav className={abierto ? 'menu-nav abierto' : 'menu-nav'} aria-label="Principal">
          {items
            .filter((i) => i.mostrar)
            .map((i) => (
              <button
                key={i.vista}
                type="button"
                className={vista === i.vista ? 'menu-link activo' : 'menu-link'}
                disabled={i.deshabilitado}
                onClick={() => ir(i.vista)}
              >
                {i.etiqueta}
              </button>
            ))}
        </nav>

        <div className="menu-acciones">
          {hayCuenta && (
            <button
              type="button"
              className={vista === 'carrito' ? 'menu-carrito activo' : 'menu-carrito'}
              onClick={() => ir('carrito')}
            >
              <IconoCarrito />
              <span className="carrito-texto">Carrito</span>
              <span className="carrito-contador">{totalCarrito}</span>
            </button>
          )}
          <button
            type="button"
            className="menu-hamburguesa"
            aria-label={abierto ? 'Cerrar menú' : 'Abrir menú'}
            aria-expanded={abierto}
            onClick={() => setAbierto(!abierto)}
          >
            {abierto ? <IconoCerrar /> : <IconoMenu />}
          </button>
        </div>
      </div>
    </header>
  );
}

