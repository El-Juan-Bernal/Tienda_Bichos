import type { Producto } from '../../types/producto';
import Precio from './Precio';

interface Props {
  producto: Producto;
  onVerDetalle: (producto: Producto) => void;
  onAgregar: (producto: Producto, cantidad: number) => boolean;
}

export default function TarjetaProducto({ producto, onVerDetalle, onAgregar }: Readonly<Props>) {
  const descuento = producto.porcentajeDescuento || 0;
  const agotado = (producto.stock ?? 0) <= 0;

  return (
    <article className="tarjeta">
      {descuento > 0 && <span className="insignia-descuento">-{descuento}%</span>}

      <button type="button" className="tarjeta-enlace" onClick={() => onVerDetalle(producto)}>
        <div className="tarjeta-foto">
          <img src={producto.imagenUrl} alt={producto.nombre} loading="lazy" />
        </div>
        <p className="etiqueta tarjeta-categoria">{producto.categoria}</p>
        <h3 className="tarjeta-nombre">{producto.nombre}</h3>
        <p className="tarjeta-descripcion">{producto.descripcion}</p>
        <Precio producto={producto} />
        <p className="tarjeta-stock">{agotado ? 'Agotado' : `Stock: ${producto.stock}`}</p>
      </button>

      <button
        type="button"
        className="btn btn-primario tarjeta-agregar"
        disabled={agotado}
        onClick={() => onAgregar(producto, 1)}
      >
        Agregar al carrito
      </button>
    </article>
  );
}

