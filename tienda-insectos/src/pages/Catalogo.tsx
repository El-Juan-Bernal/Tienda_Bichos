import { useEffect, useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useCatalogo } from '../hooks/useCatalogo';
import type { Producto } from '../types/producto';
import Paginacion from '../components/common/Paginacion';
import FiltrosCategoria from '../components/productos/FiltrosCategoria';
import type { Filtro } from '../components/productos/FiltrosCategoria';
import TarjetaProducto from '../components/productos/TarjetaProducto';
import ModalDetalle from '../components/productos/ModalDetalle';

const POR_PAGINA = 20; // 4 por fila x 5 filas

interface Props {
  onAgregar: (producto: Producto, cantidad: number) => boolean;
}

export default function Catalogo({ onAgregar }: Readonly<Props>) {
  const { instance, account } = useAuth();
  const { productos, cargando, error, cargar } = useCatalogo(instance, account);

  const [filtro, setFiltro] = useState<Filtro>('TODOS');
  const [pagina, setPagina] = useState(1);
  const [seleccionado, setSeleccionado] = useState<Producto | null>(null);

  // Cada vez que se entra al catálogo se consulta de nuevo,
  // así los cambios hechos en el Panel Admin se ven sin refrescar la página.
  useEffect(() => {
    void cargar();
  }, [cargar]);

  const filtrados = filtro === 'TODOS' ? productos : productos.filter((p) => p.categoria === filtro);
  const totalPaginas = Math.max(1, Math.ceil(filtrados.length / POR_PAGINA));
  const paginaActual = Math.min(pagina, totalPaginas);
  const visibles = filtrados.slice((paginaActual - 1) * POR_PAGINA, paginaActual * POR_PAGINA);

  function cambiarFiltro(nuevo: Filtro) {
    setFiltro(nuevo);
    setPagina(1);
  }

  function cambiarPagina(nueva: number) {
    setPagina(nueva);
    window.scrollTo({ top: 0 });
  }

  return (
    <section className="contenedor catalogo">
      <header className="catalogo-cabecera">
        <p className="etiqueta">Tienda online</p>
        <h1>Catálogo</h1>
      </header>

      <FiltrosCategoria actual={filtro} onCambiar={cambiarFiltro} />

      {cargando && productos.length === 0 && <p className="catalogo-aviso">Cargando catálogo...</p>}
      {error && <p className="catalogo-aviso catalogo-error">{error}</p>}
      {!cargando && !error && filtrados.length === 0 && (
        <p className="catalogo-aviso">No hay ejemplares en esta categoría por ahora.</p>
      )}

      <div className="catalogo-grilla">
        {visibles.map((p) => (
          <TarjetaProducto key={p.id} producto={p} onVerDetalle={setSeleccionado} onAgregar={onAgregar} />
        ))}
      </div>

      <Paginacion pagina={paginaActual} total={totalPaginas} onCambiar={cambiarPagina} />

      {seleccionado && (
        <ModalDetalle
          producto={seleccionado}
          onCerrar={() => setSeleccionado(null)}
          onAgregar={onAgregar}
        />
      )}
    </section>
  );
}

