import { useRef, useState } from 'react';
import type { Producto } from '../../types/producto';
import { aProductoNuevo, formVacio, validarForm } from '../../utils/formProducto';
import type { FormProducto } from '../../utils/formProducto';
import CamposProducto from './CamposProducto';

interface Props {
  onGuardar: (nuevos: Omit<Producto, 'id'>[]) => Promise<boolean>;
  onAviso: (mensaje: string) => void;
}

interface Formulario {
  clave: number;
  datos: FormProducto;
}

export default function TabAgregar({ onGuardar, onAviso }: Readonly<Props>) {
  // Última clave usada. Solo se lee dentro de los clics, nunca durante el render.
  const ultimaClave = useRef(0);

  // El primer formulario tiene la clave 0; los siguientes salen de nuevo()
  const [formularios, setFormularios] = useState<Formulario[]>(() => [{ clave: 0, datos: formVacio() }]);
  const [guardando, setGuardando] = useState(false);

  function nuevo(): Formulario {
    ultimaClave.current += 1;
    return { clave: ultimaClave.current, datos: formVacio() };
  }

  function agregar() {
    const formulario = nuevo();
    setFormularios((prev) => [...prev, formulario]);
  }

  function cambiar(clave: number, parcial: Partial<FormProducto>) {
    setFormularios((prev) =>
      prev.map((f) => (f.clave === clave ? { ...f, datos: { ...f.datos, ...parcial } } : f)),
    );
  }

  async function guardar() {
    const problema = formularios
      .map((f, i) => {
        const error = validarForm(f.datos);
        return error ? `Producto ${i + 1}: ${error}` : null;
      })
      .find(Boolean);
    if (problema) {
      onAviso(problema);
      return;
    }

    setGuardando(true);
    const ok = await onGuardar(formularios.map((f) => aProductoNuevo(f.datos)));
    setGuardando(false);
    if (ok) setFormularios([nuevo()]);
  }

  return (
    <div>
      <h2 className="admin-subtitulo">Carga de productos</h2>

      {formularios.map((f, i) => (
        <section key={f.clave} className="form-caja">
          <header className="form-caja-cabecera">
            <h3>Producto {i + 1}</h3>
            {i > 0 && (
              <button
                type="button"
                className="btn-texto-rojo"
                onClick={() => setFormularios((prev) => prev.filter((x) => x.clave !== f.clave))}
              >
                Quitar formulario
              </button>
            )}
          </header>
          <CamposProducto
            datos={f.datos}
            prefijo={`nuevo-${f.clave}`}
            onCambiar={(parcial) => cambiar(f.clave, parcial)}
          />
        </section>
      ))}

      <div className="admin-acciones">
        <button type="button" className="btn btn-borde" onClick={agregar}>
          + Agregar otro producto
        </button>
        <button type="button" className="btn btn-primario" disabled={guardando} onClick={() => void guardar()}>
          {guardando ? 'Guardando...' : 'Guardar todo'}
        </button>
      </div>
    </div>
  );
}

