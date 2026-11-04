export default function ProcesandoPago() {
  return (
    <div className="procesando-fondo" role="status" aria-live="polite">
      <div className="procesando-tarjeta">
        <span className="procesando-rueda" aria-hidden="true" />
        <p>Procesando su pago...</p>
      </div>
    </div>
  );
}

