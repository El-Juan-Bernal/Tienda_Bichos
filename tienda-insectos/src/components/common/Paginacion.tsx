interface Props {
  pagina: number;
  total: number;
  onCambiar: (pagina: number) => void;
}

interface Boton {
  clave: string;
  numero: number | null; // null = puntos suspensivos
}

/** Siempre muestra primera, última y la actual con sus vecinas; el resto, con "…". */
function armarBotones(pagina: number, total: number): Boton[] {
  const validos = [...new Set([1, total, pagina - 1, pagina, pagina + 1])]
    .filter((n) => n >= 1 && n <= total)
    .sort((a, b) => a - b);

  const botones: Boton[] = [];
  validos.forEach((n, i) => {
    if (i > 0 && n - validos[i - 1] > 1) {
      botones.push({ clave: `puntos-${n}`, numero: null });
    }
    botones.push({ clave: `pag-${n}`, numero: n });
  });
  return botones;
}

export default function Paginacion({ pagina, total, onCambiar }: Readonly<Props>) {
  if (total <= 1) return null;

  return (
    <nav className="paginacion" aria-label="Paginación">
      <button
        type="button"
        className="pag-boton"
        disabled={pagina === 1}
        onClick={() => onCambiar(pagina - 1)}
      >
        Anterior
      </button>

      {armarBotones(pagina, total).map((b) =>
        b.numero === null ? (
          <span key={b.clave} className="pag-puntos" aria-hidden="true">
            …
          </span>
        ) : (
          <button
            key={b.clave}
            type="button"
            className={b.numero === pagina ? 'pag-boton activo' : 'pag-boton'}
            aria-current={b.numero === pagina ? 'page' : undefined}
            onClick={() => onCambiar(b.numero as number)}
          >
            {b.numero}
          </button>
        ),
      )}

      <button
        type="button"
        className="pag-boton"
        disabled={pagina === total}
        onClick={() => onCambiar(pagina + 1)}
      >
        Siguiente
      </button>
    </nav>
  );
}

