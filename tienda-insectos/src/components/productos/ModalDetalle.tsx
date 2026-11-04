import { useState } from 'react';
import type { Producto } from '../../types/producto';
import Modal from '../common/Modal';
import Precio from './Precio';

interface Props {
  producto: Producto;
  onCerrar: () => void;
  onAgregar: (producto: Producto, cantidad: number) => boolean;
}

export default function ModalDetalle({ producto, onCerrar, onAgregar }: Readonly<Props>) {
  const [cantidad, setCantidad] = useState(1);
  const stock = producto.stock ?? 0;

  function agregar() {
    if (onAgregar(producto, cantidad)) onCerrar();
  }

  return (
    <Modal titulo={producto.nombre} onCerrar={onCerrar}>
      <div className="detalle">
        <img className="detalle-foto" src={producto.imagenUrl} alt={producto.nombre} />

        <div className="detalle-info">
          <p className="etiqueta">{producto.categoria}</p>
          <h2>{producto.nombre}</h2>
          <p className="detalle-descripcion">{producto.descripcion}</p>
          <Precio producto={producto} />
          <p className="tarjeta-stock">Stock disponible: {stock} unidades</p>

          <div className="cantidad-fila">
            <span>Cantidad:</span>
            <div className="cantidad-control">
              <button
                type="button"
                aria-label="Quitar una unidad"
                disabled={cantidad <= 1}
                onClick={() => setCantidad(cantidad - 1)}
              >
                −
              </button>
              <span>{cantidad}</span>
              <button
                type="button"
                aria-label="Sumar una unidad"
                disabled={cantidad >= stock}
                onClick={() => setCantidad(cantidad + 1)}
              >
                +
              </button>
            </div>
          </div>

          <button type="button" className="btn btn-primario" disabled={stock <= 0} onClick={agregar}>
            Agregar al carrito ({cantidad})
          </button>
        </div>
      </div>
    </Modal>
  );
}

