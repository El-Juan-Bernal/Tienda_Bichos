import { formatoPrecio } from '../../utils/formato';

interface Props {
  unidades: number;
  monto: number;
  procesando: boolean;
  onPagar: () => void;
}

export default function ResumenCompra({ unidades, monto, procesando, onPagar }: Readonly<Props>) {
  return (
    <aside className="resumen">
      <h2>Resumen de compra</h2>
      <div className="resumen-linea">
        <span>Total productos</span>
        <span>{unidades} unidades</span>
      </div>
      <div className="resumen-linea resumen-total">
        <span>Total a pagar</span>
        <span>{formatoPrecio.format(monto)}</span>
      </div>
      <button type="button" className="btn btn-primario resumen-boton" disabled={procesando} onClick={onPagar}>
        {procesando ? 'Procesando...' : 'Proceder al pago'}
      </button>
    </aside>
  );
}

