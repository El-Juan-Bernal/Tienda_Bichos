interface Props {
  titulo: string;
  valor: string;
  detalle?: string;
}

export default function TarjetaKpi({ titulo, valor, detalle }: Readonly<Props>) {
  return (
    <article className="rep-kpi">
      <p className="rep-kpi-titulo">{titulo}</p>
      <p className="rep-kpi-valor">{valor}</p>
      {detalle && <p className="rep-kpi-detalle">{detalle}</p>}
    </article>
  );
}

