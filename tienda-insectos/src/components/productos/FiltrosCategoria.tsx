export type Filtro = 'TODOS' | 'VOLADOR' | 'TERRESTRE' | 'ACUATICO';

const etiquetas: Record<Filtro, string> = {
  TODOS: 'Todos',
  VOLADOR: 'Voladores',
  TERRESTRE: 'Terrestres',
  ACUATICO: 'Acuáticos',
};

interface Props {
  actual: Filtro;
  onCambiar: (filtro: Filtro) => void;
}

export default function FiltrosCategoria({ actual, onCambiar }: Readonly<Props>) {
  return (
    <div className="filtros">
      {(Object.keys(etiquetas) as Filtro[]).map((f) => (
        <button
          key={f}
          type="button"
          className={actual === f ? 'filtro activo' : 'filtro'}
          onClick={() => onCambiar(f)}
        >
          {etiquetas[f]}
        </button>
      ))}
    </div>
  );
}

