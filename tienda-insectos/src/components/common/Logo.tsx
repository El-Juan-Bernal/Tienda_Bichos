interface Props {
  /** "claro" para fondos blancos; "oscuro" para el footer. */
  variante?: 'claro' | 'oscuro';
}

export default function Logo({ variante = 'claro' }: Readonly<Props>) {
  return (
    <span className={`logo logo-${variante}`}>
      <img src="/logo-icono.svg" alt="" />
      <span className="logo-texto">
        Insectos <b>H&J</b>
      </span>
    </span>
  );
}

