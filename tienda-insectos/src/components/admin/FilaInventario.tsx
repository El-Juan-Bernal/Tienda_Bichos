import { useState } from 'react';
import type { Producto } from '../../types/producto';
import { formatoPrecio } from '../../utils/formato';
import { precioUnitario } from '../../utils/precio';
import { IconoEstrella, IconoLapiz } from '../common/Iconos';

interface Props {
  producto: Producto;
  onActivo: (producto: Producto) => void;
  onDestacado: (producto: Producto) => void;
  onDescuento: (producto: Producto, porcentaje: number) => void;
  onEditar: (producto: Producto) => void;
}

export default function FilaInventario({ producto, onActivo, onDestacado, onDescuento, onEditar }: Readonly<Props>) {
  const activo = producto.activo !== false;
  const destacado = producto.destacado === true;
  const descuentoGuardado = producto.porcentajeDescuento || 0;

  const [descuento, setDescuento] = useState<number | ''>(descuentoGuardado || '');
  const cambioPendiente = (descuento || 0) !== descuentoGuardado;

  function escribirDescuento(valor: string) {
    setDescuento(valor === '' ? '' : Math.min(100, Math.max(0, Number(valor))));
  }

  return (
    <article className={activo ? 'inv-fila' : 'inv-fila inactivo'}>
      <img
        className="inv-foto"
        src={producto.imagenUrl}
        alt={producto.nombre}
        onError={(e) => {
          e.currentTarget.style.visibility = 'hidden';
        }}
      />

      <div className="inv-info">
        <h3>{producto.nombre}</h3>
        <p className="inv-categoria">Categoría: {producto.categoria}</p>
        <p className="inv-datos">
          <span>
            Precio: {formatoPrecio.format(producto.precio)}
            {descuentoGuardado > 0 && (
              <strong className="inv-oferta"> → {formatoPrecio.format(precioUnitario(producto))}</strong>
            )}
          </span>
          <span>Stock: {producto.stock}</span>
        </p>
        {!activo && <p className="inv-aviso">Deshabilitado: no se muestra en el catálogo</p>}
      </div>

      <div className="inv-controles">
        <label className="inv-control">
          <span className="inv-rotulo">Habilitado</span>
          <span className="interruptor">
            <input type="checkbox" checked={activo} onChange={() => onActivo(producto)} />
            <span className="interruptor-pista" />
          </span>
        </label>

        <div className="inv-control">
          <span className="inv-rotulo">Destacado</span>
          <button
            type="button"
            className={destacado ? 'estrella activa' : 'estrella'}
            disabled={!activo}
            aria-pressed={destacado}
            aria-label={destacado ? 'Quitar de destacados' : 'Destacar en la página de inicio'}
            title={activo ? 'Destacar en la página de inicio' : 'Habilita el producto para poder destacarlo'}
            onClick={() => onDestacado(producto)}
          >
            <IconoEstrella relleno={destacado} className="icono icono-estrella" />
          </button>
        </div>

        <div className="inv-control">
          <label className="inv-rotulo" htmlFor={`descuento-${producto.id}`}>
            Descuento (%)
          </label>
          <div className="inv-descuento">
            <input
              id={`descuento-${producto.id}`}
              className="entrada entrada-chica"
              type="number"
              min="0"
              max="100"
              placeholder="0"
              value={descuento}
              onChange={(e) => escribirDescuento(e.target.value)}
            />
            <button
              type="button"
              className="btn btn-primario btn-chico"
              disabled={!cambioPendiente}
              onClick={() => onDescuento(producto, descuento || 0)}
            >
              Aplicar
            </button>
          </div>
        </div>

        <button type="button" className="btn-editar" onClick={() => onEditar(producto)}>
          <IconoLapiz className="icono" />
          Editar
        </button>
      </div>
    </article>
  );
}

