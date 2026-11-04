import { useDestacados } from '../hooks/useDestacados';
import Carrusel from '../components/inicio/Carrusel';
import ProductosDestacados from '../components/inicio/ProductosDestacados';
import FranjaBeneficios from '../components/inicio/FranjaBeneficios';
import BannerCategorias from '../components/inicio/BannerCategorias';

interface Props {
  onVerCatalogo: () => void;
}

export default function Inicio({ onVerCatalogo }: Readonly<Props>) {
  const { destacados } = useDestacados();

  return (
    <>
      <Carrusel />
      <ProductosDestacados destacados={destacados} onVerCatalogo={onVerCatalogo} />
      <FranjaBeneficios />
      <BannerCategorias />
      <section className="cierre contenedor">
        <p className="etiqueta">Insectos H&J</p>
        <h2>Encuentra tu próximo ejemplar</h2>
        <p>Inicia sesión para explorar el catálogo completo y hacer tu pedido.</p>
        <button type="button" className="btn btn-primario" onClick={onVerCatalogo}>
          Ver catálogo
        </button>
      </section>
    </>
  );
}

