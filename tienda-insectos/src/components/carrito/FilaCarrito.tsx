import type { ItemCarrito } from '../../types/carrito';
import { formatoPrecio } from '../../utils/formato';
import { precioUnitario } from '../../utils/precio';
import { IconoBasura } from '../common/Iconos';

interface Props {
  item: ItemCarrito;
  onCambiar: (id: number, delta: number) => void;
  onEliminar: (id: number) => void;
}

export default function FilaCarrito({ item, onCambiar, onEliminar }: Readonly<Props>) {
  const { producto, cantidad } = item;
  const unitario = precioUnitario(producto);

  return (
    <div className="fila-carrito">
      <img className="fila-foto" src={producto.imagenUrl} alt={producto.nombre} />

      <div className="fila-info">
        <h3>{producto.nombre}</h3>
        <p>Precio unitario: {formatoPrecio.format(unitario)}</p>
        <p className="fila-stock">Stock disponible: {producto.stock ?? 0}</p>
      </div>

      <div className="cantidad-control">
        <button type="button" aria-label="Quitar una unidad" onClick={() => onCambiar(producto.id, -1)}>
          −
        </button>
        <span>{cantidad}</span>
        <button type="button" aria-label="Sumar una unidad" onClick={() => onCambiar(producto.id, 1)}>
          +
        </button>
      </div>

      <p className="fila-subtotal">{formatoPrecio.format(unitario * cantidad)}</p>

      <button
        type="button"
        className="fila-basura"
        aria-label={`Eliminar ${producto.nombre} del carrito`}
        onClick={() => onEliminar(producto.id)}
      >
        <IconoBasura />
      </button>
    </div>
  );
}

