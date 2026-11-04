interface PropsIcono {
  className?: string;
}

function Svg({ className = 'icono', children }: Readonly<PropsIcono & { children: React.ReactNode }>) {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {children}
    </svg>
  );
}

export function IconoCarrito(props: Readonly<PropsIcono>) {
  return (
    <Svg {...props}>
      <circle cx="9" cy="21" r="1" />
      <circle cx="20" cy="21" r="1" />
      <path d="M1 1h4l2.7 13.4a2 2 0 0 0 2 1.6h9.7a2 2 0 0 0 2-1.6L23 6H6" />
    </Svg>
  );
}

export function IconoBasura(props: Readonly<PropsIcono>) {
  return (
    <Svg {...props}>
      <path d="M3 6h18" />
      <path d="M8 6V4h8v2" />
      <path d="M19 6l-1 14H6L5 6" />
      <path d="M10 11v6" />
      <path d="M14 11v6" />
    </Svg>
  );
}

export function IconoMenu(props: Readonly<PropsIcono>) {
  return (
    <Svg {...props}>
      <path d="M3 6h18" />
      <path d="M3 12h18" />
      <path d="M3 18h18" />
    </Svg>
  );
}

export function IconoCerrar(props: Readonly<PropsIcono>) {
  return (
    <Svg {...props}>
      <path d="M6 6l12 12" />
      <path d="M18 6L6 18" />
    </Svg>
  );
}

export function IconoEnvio(props: Readonly<PropsIcono>) {
  return (
    <Svg {...props}>
      <path d="M1 4h13v11H1z" />
      <path d="M14 8h4l3 3v4h-7" />
      <circle cx="6" cy="17" r="2" />
      <circle cx="17" cy="17" r="2" />
    </Svg>
  );
}

export function IconoHoja(props: Readonly<PropsIcono>) {
  return (
    <Svg {...props}>
      <path d="M5 21c0-9 5-15 15-16 0 10-6 15-15 16z" />
      <path d="M5 21c3-5 6-8 10-10" />
    </Svg>
  );
}

export function IconoDevolucion(props: Readonly<PropsIcono>) {
  return (
    <Svg {...props}>
      <path d="M3 12a9 9 0 0 1 15-6.7L21 8" />
      <path d="M21 3v5h-5" />
      <path d="M21 12a9 9 0 0 1-15 6.7L3 16" />
      <path d="M3 21v-5h5" />
    </Svg>
  );
}

export function IconoSoporte(props: Readonly<PropsIcono>) {
  return (
    <Svg {...props}>
      <path d="M4 14v-2a8 8 0 0 1 16 0v2" />
      <rect x="2" y="14" width="4" height="6" rx="1" />
      <rect x="18" y="14" width="4" height="6" rx="1" />
    </Svg>
  );
}

export function IconoEstrella({ relleno = false, ...props }: Readonly<PropsIcono & { relleno?: boolean }>) {
  return (
    <Svg {...props}>
      <polygon
        fill={relleno ? 'currentColor' : 'none'}
        points="12 2 15.1 8.6 22 9.3 16.8 14 18.2 21 12 17.5 5.8 21 7.2 14 2 9.3 8.9 8.6 12 2"
      />
    </Svg>
  );
}

export function IconoLapiz(props: Readonly<PropsIcono>) {
  return (
    <Svg {...props}>
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z" />
    </Svg>
  );
}

