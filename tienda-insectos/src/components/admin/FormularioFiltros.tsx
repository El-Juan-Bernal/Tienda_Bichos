import { useState } from 'react';
import { FILTROS_VACIOS } from '../../api/auditoria';
import type { FiltrosEventos } from '../../types/evento';
import { TIPOS_EVENTO } from '../../utils/etiquetasAuditoria';

interface Props {
  aplicados: FiltrosEventos;
  onBuscar: (filtros: FiltrosEventos) => void;
  onAviso: (mensaje: string) => void;
}

export default function FormularioFiltros({ aplicados, onBuscar, onAviso }: Readonly<Props>) {
  const [borrador, setBorrador] = useState<FiltrosEventos>(aplicados);
  const hoy = new Date().toLocaleDateString('en-CA'); // AAAA-MM-DD en hora local

  function cambiar(campo: keyof FiltrosEventos, valor: string) {
    setBorrador((previo) => ({ ...previo, [campo]: valor }));
  }

  function enviar() {
    if (borrador.desde && borrador.hasta && borrador.desde > borrador.hasta) {
      onAviso('La fecha "Desde" no puede ser posterior a la fecha "Hasta".');
      return;
    }
    onBuscar(borrador);
  }

  function limpiar() {
    setBorrador(FILTROS_VACIOS);
    onBuscar(FILTROS_VACIOS);
  }

  return (
    <form
      className="aud-filtros"
      onSubmit={(e) => {
        e.preventDefault();
        enviar();
      }}
    >
      <div className="campo">
        <label htmlFor="aud-usuario">Usuario</label>
        <input
          id="aud-usuario"
          className="entrada"
          type="text"
          placeholder="Correo del usuario"
          value={borrador.usuario}
          onChange={(e) => cambiar('usuario', e.target.value)}
        />
      </div>

      <div className="campo">
        <label htmlFor="aud-tipo">Tipo de evento</label>
        <select
          id="aud-tipo"
          className="entrada"
          value={borrador.tipo}
          onChange={(e) => cambiar('tipo', e.target.value)}
        >
          <option value="">Todos</option>
          {TIPOS_EVENTO.map((t) => (
            <option key={t.valor} value={t.valor}>
              {t.etiqueta}
            </option>
          ))}
        </select>
      </div>

      <div className="campo">
        <label htmlFor="aud-desde">Desde</label>
        <input
          id="aud-desde"
          className="entrada"
          type="date"
          max={hoy}
          value={borrador.desde}
          onChange={(e) => cambiar('desde', e.target.value)}
        />
      </div>

      <div className="campo">
        <label htmlFor="aud-hasta">Hasta</label>
        <input
          id="aud-hasta"
          className="entrada"
          type="date"
          max={hoy}
          value={borrador.hasta}
          onChange={(e) => cambiar('hasta', e.target.value)}
        />
      </div>

      <div className="aud-filtros-acciones">
        <button type="submit" className="rep-boton">
          Buscar
        </button>
        <button type="button" className="rep-boton secundario" onClick={limpiar}>
          Limpiar
        </button>
      </div>
    </form>
  );
}

