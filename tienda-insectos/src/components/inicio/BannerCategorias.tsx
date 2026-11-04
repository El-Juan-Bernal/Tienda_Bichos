import { categoriasBanner } from '../../data/inicio';

/** Solo visual: no es clicable porque el catálogo exige iniciar sesión. */
export default function BannerCategorias() {
  return (
    <section className="banner-categorias">
      <div className="banner-velo" />
      <div className="contenedor banner-contenido">
        <p className="etiqueta etiqueta-clara">Nuestras categorías</p>
        <h2>Un mundo en cada terrario</h2>
        <div className="banner-columnas">
          {categoriasBanner.map((c) => (
            <div key={c.nombre} className="banner-columna">
              <h3>{c.nombre}</h3>
              <p>{c.texto}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

