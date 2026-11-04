import { IconoDevolucion, IconoEnvio, IconoHoja, IconoSoporte } from '../common/Iconos';

const beneficios = [
  { Icono: IconoEnvio, titulo: 'Envío a todo Chile', texto: 'Despacho seguro a cualquier región' },
  { Icono: IconoHoja, titulo: 'Ejemplares sanos', texto: 'Seleccionados y revisados con cuidado' },
  { Icono: IconoDevolucion, titulo: 'Devoluciones', texto: 'Proceso simple y sin complicaciones' },
  { Icono: IconoSoporte, titulo: 'Soporte', texto: 'Atención personalizada por email' },
];

export default function FranjaBeneficios() {
  return (
    <section className="beneficios">
      <div className="contenedor beneficios-grilla">
        {beneficios.map(({ Icono, titulo, texto }) => (
          <div key={titulo} className="beneficio">
            <Icono className="icono icono-grande" />
            <h3>{titulo}</h3>
            <p>{texto}</p>
          </div>
        ))}
      </div>
    </section>
  );
}

