import type { FormProducto } from '../../utils/formProducto';
import { MAX_DESCRIPCION } from '../../utils/formProducto';

interface Props {
  datos: FormProducto;
  prefijo: string;
  onCambiar: (parcial: Partial<FormProducto>) => void;
}

const numero = (valor: string): number | '' => (valor === '' ? '' : Math.max(0, Number(valor)));

function bloquearSignos(e: React.KeyboardEvent<HTMLInputElement>) {
  if (['-', '+', 'e', 'E'].includes(e.key)) e.preventDefault();
}

/** Campos del producto, compartidos por "Agregar" y por la ventana de edición. */
export default function CamposProducto({ datos, prefijo, onCambiar }: Readonly<Props>) {
  const id = (campo: string) => `${prefijo}-${campo}`;

  return (
    <>
      <div className="campo">
        <label htmlFor={id('nombre')}>Nombre del insecto</label>
        <input
          id={id('nombre')}
          className="entrada"
          type="text"
          placeholder="Ej: Araña pollito"
          value={datos.nombre}
          onChange={(e) => onCambiar({ nombre: e.target.value })}
        />
      </div>

      <div className="campo">
        <div className="campo-cabecera">
          <label htmlFor={id('descripcion')}>Descripción</label>
          <span className={datos.descripcion.length >= MAX_DESCRIPCION ? 'contador exceso' : 'contador'}>
            {datos.descripcion.length}/{MAX_DESCRIPCION}
          </span>
        </div>
        <textarea
          id={id('descripcion')}
          className="entrada"
          rows={4}
          maxLength={MAX_DESCRIPCION}
          placeholder="Descripción detallada del ejemplar"
          value={datos.descripcion}
          onChange={(e) => onCambiar({ descripcion: e.target.value })}
        />
      </div>

      <div className="campo">
        <label htmlFor={id('categoria')}>Categoría</label>
        <select
          id={id('categoria')}
          className="entrada"
          value={datos.categoria}
          onChange={(e) => onCambiar({ categoria: e.target.value })}
        >
          <option value="">Seleccione categoría</option>
          <option value="TERRESTRE">Terrestre</option>
          <option value="VOLADOR">Volador</option>
          <option value="ACUATICO">Acuático</option>
        </select>
      </div>

      <div className="campos-fila">
        <div className="campo">
          <label htmlFor={id('precio')}>Precio ($ CLP)</label>
          <input
            id={id('precio')}
            className="entrada"
            type="number"
            min="0"
            placeholder="Ej: 15000"
            onKeyDown={bloquearSignos}
            value={datos.precio}
            onChange={(e) => onCambiar({ precio: numero(e.target.value) })}
          />
        </div>
        <div className="campo">
          <label htmlFor={id('stock')}>Stock disponible</label>
          <input
            id={id('stock')}
            className="entrada"
            type="number"
            min="0"
            placeholder="Ej: 10"
            onKeyDown={bloquearSignos}
            value={datos.stock}
            onChange={(e) => onCambiar({ stock: numero(e.target.value) })}
          />
        </div>
      </div>

      <div className="campo">
        <label htmlFor={id('imagen')}>URL de la imagen</label>
        <input
          id={id('imagen')}
          className="entrada"
          type="text"
          placeholder="https://..."
          value={datos.imagenUrl}
          onChange={(e) => onCambiar({ imagenUrl: e.target.value })}
        />
      </div>
    </>
  );
}

