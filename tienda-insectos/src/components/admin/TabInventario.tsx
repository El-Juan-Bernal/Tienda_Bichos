import { useState } from 'react';
import type { AdminApi } from '../../hooks/useAdminProductos';
import type { Producto } from '../../types/producto';
import { MAXIMO_DESTACADOS } from '../../config/limites';
import Paginacion from '../common/Paginacion';
import FilaInventario from './FilaInventario';
import ModalEditarProducto from './ModalEditarProducto';

const POR_PAGINA = 12;

interface Props {
  api: AdminApi;
  onAviso: (mensaje: string) => void;
}

export default function TabInventario({ api, onAviso }: Readonly<Props>) {
  const { productos, cargando } = api;
  const [pagina, setPagina] = useState(1);
  const [editando, setEditando] = useState<Producto | null>(null);

  const totalPaginas = Math.max(1, Math.ceil(productos.length / POR_PAGINA));
  const paginaActual = Math.min(pagina, totalPaginas);
  const visibles = productos.slice((paginaActual - 1) * POR_PAGINA, paginaActual * POR_PAGINA);
  const destacados = productos.filter((p) => p.destacado && p.activo !== false).length;

  return (
    <div>
      <header className="inv-cabecera">
        <h2 className="admin-subtitulo">Inventario y ofertas</h2>
        <p className="inv-contador">
          Destacados: <strong>{destacados}</strong> de {MAXIMO_DESTACADOS}
        </p>
      </header>

      {cargando && <p className="catalogo-aviso">Cargando inventario...</p>}
      {!cargando && productos.length === 0 && <p className="catalogo-aviso">Aún no hay productos cargados.</p>}

      <div className="inv-lista">
        {visibles.map((p) => (
          <FilaInventario
            key={`${p.id}-${p.porcentajeDescuento ?? 0}`}
            producto={p}
            onActivo={(x) => void api.cambiarActivo(x)}
            onDestacado={(x) => void api.cambiarDestacado(x)}
            onDescuento={(x, n) => void api.aplicarDescuento(x, n)}
            onEditar={setEditando}
          />
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

      {editando && (
        <ModalEditarProducto
          producto={editando}
          onGuardar={api.guardarEdicion}
          onAviso={onAviso}
          onCerrar={() => setEditando(null)}
        />
      )}
    </div>
  );
}

