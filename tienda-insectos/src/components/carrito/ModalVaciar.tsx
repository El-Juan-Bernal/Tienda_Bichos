import Modal from '../common/Modal';

interface Props {
  unidades: number;
  onConfirmar: () => void;
  onCerrar: () => void;
}

export default function ModalVaciar({ unidades, onConfirmar, onCerrar }: Readonly<Props>) {
  return (
    <Modal titulo="Vaciar el carrito" onCerrar={onCerrar} ancho="angosto">
      <div className="modal-centrado">
        <h2>¿Vaciar el carrito?</h2>
        <p className="modal-subtitulo">
          Se quitarán todos los productos ({unidades} unidades) de tu carrito.
        </p>
        <div className="modal-acciones">
          <button type="button" className="btn btn-peligro" onClick={onConfirmar}>
            Sí, vaciar
          </button>
          <button type="button" className="btn btn-borde" onClick={onCerrar}>
            Cancelar
          </button>
        </div>
      </div>
    </Modal>
  );
}

