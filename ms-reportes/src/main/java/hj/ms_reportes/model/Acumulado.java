package hj.ms_reportes.model;

/** Acumulador genérico: cantidad, monto y segundos (según el KPI). Admite sumar y restar. */
public record Acumulado(String etiqueta, long cantidad, double monto, double segundos) {

    public static Acumulado vacio() {
        return new Acumulado(null, 0, 0, 0);
    }

    public Acumulado sumar(Acumulado otro) {
        return new Acumulado(otro.etiqueta != null ? otro.etiqueta : etiqueta,
                cantidad + otro.cantidad, monto + otro.monto, segundos + otro.segundos);
    }

    public Acumulado restar(Acumulado otro) {
        return new Acumulado(etiqueta, cantidad - otro.cantidad, monto - otro.monto, segundos - otro.segundos);
    }
}

