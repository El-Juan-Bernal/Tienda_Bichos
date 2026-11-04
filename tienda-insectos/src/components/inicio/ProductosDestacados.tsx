import type { Producto } from '../../types/producto';
import { recomendados } from '../../data/inicio';
import { formatoPrecio } from '../../utils/formato';
import { precioUnitario } from '../../utils/precio';

interface Props {
  destacados: Producto[];
  onVerCatalogo: () => void;
}

interface Tarjeta {
  id: number;
  nombre: string;
  imagenUrl: string;
  etiqueta?: string;
  detalle: string;
}

function aTarjetas(destacados: Producto[]): Tarjeta[] {
  if (destacados.length === 0) {
    return recomendados.map((r) => ({
      id: r.id,
      nombre: r.nombre,
      imagenUrl: r.imagenUrl,
      detalle: r.descripcion,
    }));
  }
  return destacados.map((p) => ({
    id: p.id,
    nombre: p.nombre,
    imagenUrl: p.imagenUrl,
    etiqueta: p.categoria,
    detalle: formatoPrecio.format(precioUnitario(p)),
  }));
}

export default function ProductosDestacados({ destacados, onVerCatalogo }: Readonly<Props>) {
  const tarjetas = aTarjetas(destacados);

  return (
    <section className="seccion contenedor">
      <div className="seccion-cabecera">
        <div>
          <p className="etiqueta">Tienda online</p>
          <h2 className="seccion-titulo">Productos destacados</h2>
        </div>
        <button type="button" className="enlace-subrayado" onClick={onVerCatalogo}>
          Ver catálogo
        </button>
      </div>

      <div className="destacados-grilla">
        {tarjetas.map((t) => (
          <article key={t.id} className="destacado">
            <div className="destacado-foto">
              <img src={t.imagenUrl} alt={t.nombre} loading="lazy" />
            </div>
            {t.etiqueta && <p className="etiqueta destacado-etiqueta">{t.etiqueta}</p>}
            <h3 className="destacado-nombre">{t.nombre}</h3>
            <p className="destacado-detalle">{t.detalle}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

