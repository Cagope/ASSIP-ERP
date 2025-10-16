package co.assip.erp.shared.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorPayload(
            Instant timestamp,
            int status,
            String error,
            String message,
            List<String> details,
            String path
    ) {
        public ErrorPayload(int status, String error, String message, List<String> details, String path) {
            this(Instant.now(), status, error, message, details, path);
        }
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorPayload> handleRSE(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        ErrorPayload body = new ErrorPayload(
                ex.getStatusCode().value(),
                status != null ? status.getReasonPhrase() : "Error",
                ex.getReason(),
                null,
                null
        );
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorPayload> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream().map(err -> err.getField() + ": " + err.getDefaultMessage()).toList();
        ErrorPayload body = new ErrorPayload(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validación",
                details,
                null
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorPayload> handleAny(Exception ex) {
        log.error("Error no controlado", ex);
        ErrorPayload body = new ErrorPayload(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                null,
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
