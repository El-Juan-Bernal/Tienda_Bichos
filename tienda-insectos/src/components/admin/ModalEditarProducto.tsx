import { useState } from 'react';
import type { Producto } from '../../types/producto';
import { aProductoNuevo, formDesdeProducto, validarForm } from '../../utils/formProducto';
import type { FormProducto } from '../../utils/formProducto';
import Modal from '../common/Modal';
import CamposProducto from './CamposProducto';

interface Props {
  producto: Producto;
  onGuardar: (producto: Producto) => Promise<boolean>;
  onAviso: (mensaje: string) => void;
  onCerrar: () => void;
}

export default function ModalEditarProducto({ producto, onGuardar, onAviso, onCerrar }: Readonly<Props>) {
  const [datos, setDatos] = useState<FormProducto>(() => formDesdeProducto(producto));
  const [guardando, setGuardando] = useState(false);

  async function guardar() {
    const error = validarForm(datos);
    if (error) {
      onAviso(error);
      return;
    }
    setGuardando(true);
    // El descuento y el estado se mantienen: se cambian desde la lista.
    const ok = await onGuardar({
      ...producto,
      ...aProductoNuevo(datos),
      porcentajeDescuento: producto.porcentajeDescuento,
      activo: producto.activo,
    });
    setGuardando(false);
    if (ok) onCerrar();
  }

  return (
    <Modal titulo={`Editar ${producto.nombre}`} onCerrar={onCerrar}>
      <h2 className="admin-subtitulo">Editando: {producto.nombre}</h2>
      <CamposProducto
        datos={datos}
        prefijo={`editar-${producto.id}`}
        onCambiar={(parcial) => setDatos((prev) => ({ ...prev, ...parcial }))}
      />
      <div className="admin-acciones">
        <button type="button" className="btn btn-primario" disabled={guardando} onClick={() => void guardar()}>
          {guardando ? 'Guardando...' : 'Guardar cambios'}
        </button>
        <button type="button" className="btn btn-borde" disabled={guardando} onClick={onCerrar}>
          Cancelar
        </button>
      </div>
    </Modal>
  );
}

