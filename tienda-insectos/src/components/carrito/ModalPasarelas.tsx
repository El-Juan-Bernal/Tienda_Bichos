import Modal from '../common/Modal';
import { formatoPrecio } from '../../utils/formato';

const pasarelas = [
  { nombre: 'Webpay Plus', imagen: '/webpay.png' },
  { nombre: 'Mercado Pago', imagen: '/mercadopago.png' },
  { nombre: 'Khipu', imagen: '/khipu.png' },
];

interface Props {
  monto: number;
  onElegir: (pasarela: string) => void;
  onCerrar: () => void;
}

export default function ModalPasarelas({ monto, onElegir, onCerrar }: Readonly<Props>) {
  return (
    <Modal titulo="Selecciona tu pasarela de pago" onCerrar={onCerrar}>
      <div className="modal-centrado">
        <h2>Selecciona tu pasarela de pago</h2>
        <p className="modal-subtitulo">
          Monto total a cancelar: <strong>{formatoPrecio.format(monto)}</strong>
        </p>

        <div className="pasarelas">
          {pasarelas.map((p) => (
            <button key={p.nombre} type="button" className="pasarela" onClick={() => onElegir(p.nombre)}>
              <img src={p.imagen} alt="" />
              <span>{p.nombre}</span>
            </button>
          ))}
        </div>

        <button type="button" className="btn btn-borde" onClick={onCerrar}>
          Cancelar
        </button>
      </div>
    </Modal>
  );
}

