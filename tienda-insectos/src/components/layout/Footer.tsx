import type { Vista } from '../../types/vista';
import Logo from '../common/Logo';

interface Props {
  onCambiar: (vista: Vista) => void;
  hayCuenta: boolean;
}

export default function Footer({ onCambiar, hayCuenta }: Readonly<Props>) {
  return (
    <footer className="pie">
      <div className="pie-interior">
        <div>
          <Logo variante="oscuro" />
          <p className="pie-descripcion">
            Ejemplares cuidadosamente seleccionados para coleccionistas y curiosos. Envíos a todo Chile.
          </p>
        </div>

        <div>
          <h4 className="pie-titulo">Tienda</h4>
          <ul className="pie-lista">
            <li>
              <button type="button" className="pie-link" onClick={() => onCambiar('inicio')}>
                Inicio
              </button>
            </li>
            <li>
              <button
                type="button"
                className="pie-link"
                disabled={!hayCuenta}
                onClick={() => onCambiar('catalogo')}
              >
                Catálogo
              </button>
            </li>
          </ul>
        </div>

        <div>
          <h4 className="pie-titulo">Proyecto académico</h4>
          <ul className="pie-lista">
            <li>Cloud Native · Sede Concepción</li>
            <li>Profesor: Alejandro Sepúlveda</li>
            <li>Autores: Hileok Hernández y Juan Bernal</li>
          </ul>
          <img src="/logo_duocuc.png" alt="Logo Duoc UC" className="pie-logo-duoc" />
        </div>
      </div>

      <div className="pie-base">
        <div className="pie-base-interior">
          <span>&copy; {new Date().getFullYear()} Insectos H&J. Todos los derechos reservados.</span>
          <span>Chile</span>
        </div>
      </div>
    </footer>
  );
}

