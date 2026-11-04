import { useEffect, useState } from 'react';
import { useAdminProductos } from '../hooks/useAdminProductos';
import type { MostrarToast } from '../types/toast';
import TabAgregar from '../components/admin/TabAgregar';
import TabInventario from '../components/admin/TabInventario';

type Pestana = 'agregar' | 'inventario';

export default function Admin({ mostrarToast }: Readonly<{ mostrarToast: MostrarToast }>) {
  const api = useAdminProductos(mostrarToast);
  const { cargar } = api;
  const [pestana, setPestana] = useState<Pestana>('inventario');

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const avisar = (mensaje: string) => mostrarToast('aviso', 'Revisa el formulario', mensaje);

  return (
    <section className="contenedor pagina-admin">
      <header>
        <p className="etiqueta">Administración</p>
        <h1>Panel Admin</h1>
      </header>

      <div className="admin-pestanas" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={pestana === 'inventario'}
          className={pestana === 'inventario' ? 'admin-pestana activa' : 'admin-pestana'}
          onClick={() => setPestana('inventario')}
        >
          Inventario y ofertas
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={pestana === 'agregar'}
          className={pestana === 'agregar' ? 'admin-pestana activa' : 'admin-pestana'}
          onClick={() => setPestana('agregar')}
        >
          Agregar producto
        </button>
      </div>

      {pestana === 'inventario' ? (
        <TabInventario api={api} onAviso={avisar} />
      ) : (
        <TabAgregar onGuardar={api.crear} onAviso={avisar} />
      )}
    </section>
  );
}

