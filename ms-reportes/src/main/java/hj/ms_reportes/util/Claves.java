package hj.ms_reportes.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Claves de los stores (hora y día en horario de Chile). Ordenan bien como texto. */
public final class Claves {

    public static final ZoneId ZONA = ZoneId.of("America/Santiago");
    public static final String OMITIDO = "OMITIDO";

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Claves() {
    }

    public static String hora(Instant instante) {
        return HORA.format(instante.atZone(ZONA));
    }

    public static String dia(Instant instante) {
        return DIA.format(instante.atZone(ZONA));
    }
}

