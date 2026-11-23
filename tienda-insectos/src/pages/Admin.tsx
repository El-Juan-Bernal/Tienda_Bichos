import { useEffect, useState } from 'react';
import { useAdminProductos } from '../hooks/useAdminProductos';
import type { MostrarToast } from '../types/toast';
import TabAgregar from '../components/admin/TabAgregar';
import TabAuditoria from '../components/admin/TabAuditoria.tsx';
import TabInventario from '../components/admin/TabInventario';
import TabReportes from '../components/admin/TabReportes';
import '../styles/admin-analitica.css';

type Pestana = 'inventario' | 'agregar' | 'reportes' | 'auditoria';

const PESTANAS: { id: Pestana; texto: string }[] = [
  { id: 'inventario', texto: 'Inventario y ofertas' },
  { id: 'agregar', texto: 'Agregar producto' },
  { id: 'reportes', texto: 'Reportes' },
  { id: 'auditoria', texto: 'Auditoría' },
];

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
        {PESTANAS.map((p) => (
          <button
            key={p.id}
            type="button"
            role="tab"
            aria-selected={pestana === p.id}
            className={pestana === p.id ? 'admin-pestana activa' : 'admin-pestana'}
            onClick={() => setPestana(p.id)}
          >
            {p.texto}
          </button>
        ))}
      </div>

      {pestana === 'inventario' && <TabInventario api={api} onAviso={avisar} />}
      {pestana === 'agregar' && <TabAgregar onGuardar={api.crear} onAviso={avisar} />}
      {pestana === 'reportes' && <TabReportes mostrarToast={mostrarToast} />}
      {pestana === 'auditoria' && <TabAuditoria mostrarToast={mostrarToast} />}
    </section>
  );
}

