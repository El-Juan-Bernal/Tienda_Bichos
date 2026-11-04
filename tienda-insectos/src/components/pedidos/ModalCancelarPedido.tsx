import type { Pedido } from '../../types/pedido';
import Modal from '../common/Modal';
import { formatoPrecio } from '../../utils/formato';

interface Props {
  pedido: Pedido;
  procesando: boolean;
  onConfirmar: () => void;
  onCerrar: () => void;
}

export default function ModalCancelarPedido({ pedido, procesando, onConfirmar, onCerrar }: Readonly<Props>) {
  return (
    <Modal titulo="Cancelar pedido" onCerrar={onCerrar} ancho="angosto">
      <div className="modal-centrado">
        <h2>¿Cancelar el pedido #{pedido.id}?</h2>
        <p className="modal-subtitulo">
          Te devolveremos <strong>{formatoPrecio.format(pedido.montoTotal ?? 0)}</strong>
          {pedido.pasarelaSeleccionada && ` por ${pedido.pasarelaSeleccionada}`}. Esta acción no se puede deshacer.
        </p>
        <div className="modal-acciones">
          <button type="button" className="btn btn-peligro" disabled={procesando} onClick={onConfirmar}>
            {procesando ? 'Cancelando...' : 'Sí, cancelar pedido'}
          </button>
          <button type="button" className="btn btn-borde" disabled={procesando} onClick={onCerrar}>
            Volver
          </button>
        </div>
      </div>
    </Modal>
  );
}

