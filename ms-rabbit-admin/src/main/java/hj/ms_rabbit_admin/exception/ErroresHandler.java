package hj.ms_rabbit_admin.exception;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Todos los errores salen como {"error": "mensaje"}, igual que en el resto del proyecto. */
@RestControllerAdvice
public class ErroresHandler {

    private static final String CLAVE_ERROR = "error";

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> alResponder(ResponseStatusException e) {
        String mensaje = e.getReason() == null ? "Error" : e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of(CLAVE_ERROR, mensaje));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> alValidar(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of(CLAVE_ERROR, detalle));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> alLeerCuerpo(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of(CLAVE_ERROR, "El cuerpo de la petición no es un JSON válido."));
    }
}
