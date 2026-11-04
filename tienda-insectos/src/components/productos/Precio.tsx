import type { Producto } from '../../types/producto';
import { formatoPrecio } from '../../utils/formato';
import { precioUnitario } from '../../utils/precio';

export default function Precio({ producto }: Readonly<{ producto: Producto }>) {
  const tieneDescuento = (producto.porcentajeDescuento || 0) > 0;

  if (!tieneDescuento) {
    return <p className="precio">{formatoPrecio.format(producto.precio)}</p>;
  }

  return (
    <p className="precio">
      <span className="precio-original">{formatoPrecio.format(producto.precio)}</span>
      <span className="precio-oferta">{formatoPrecio.format(precioUnitario(producto))}</span>
    </p>
  );
}

