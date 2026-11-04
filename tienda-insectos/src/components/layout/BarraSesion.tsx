interface Props {
  usuario?: string;
  bloqueado: boolean;
  onEntrar: () => void | Promise<void>;
  onSalir: () => void | Promise<void>;
}

export default function BarraSesion({ usuario, bloqueado, onEntrar, onSalir }: Readonly<Props>) {
  return (
    <div className="barra-sesion">
      <div className="barra-sesion-interior">
        {usuario ? (
          <>
            <span className="barra-sesion-usuario">{usuario}</span>
            <button type="button" className="btn-sesion" disabled={bloqueado} onClick={() => void onSalir()}>
              Cerrar sesión
            </button>
          </>
        ) : (
          <button type="button" className="btn-sesion" disabled={bloqueado} onClick={() => void onEntrar()}>
            Iniciar sesión
          </button>
        )}
      </div>
    </div>
  );
}

