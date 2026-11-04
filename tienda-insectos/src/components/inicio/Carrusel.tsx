import { useEffect, useState } from 'react';
import { slidesInicio } from '../../data/inicio';

const INTERVALO_MS = 6000;

export default function Carrusel() {
  const [actual, setActual] = useState(0);

  // Al depender de "actual", elegir un punto a mano reinicia la cuenta.
  useEffect(() => {
    const timer = setTimeout(() => setActual((a) => (a + 1) % slidesInicio.length), INTERVALO_MS);
    return () => clearTimeout(timer);
  }, [actual]);

  return (
    <section className="carrusel" aria-roledescription="carrusel">
      {slidesInicio.map((s, i) => (
        <div
          key={s.titulo}
          className={i === actual ? 'slide activo' : 'slide'}
          style={{ backgroundImage: `url(${s.imagenUrl})` }}
          aria-hidden={i !== actual}
        >
          <div className="slide-velo" />
          <div className="slide-texto">
            <p className="etiqueta etiqueta-clara">{s.etiqueta}</p>
            <h1>{s.titulo}</h1>
            <p className="slide-bajada">{s.texto}</p>
          </div>
        </div>
      ))}

      <div className="carrusel-puntos">
        {slidesInicio.map((s, i) => (
          <button
            key={s.titulo}
            type="button"
            className={i === actual ? 'punto activo' : 'punto'}
            aria-label={`Ir a la diapositiva ${i + 1}`}
            onClick={() => setActual(i)}
          />
        ))}
      </div>
    </section>
  );
}

