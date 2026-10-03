package co.assip.erp.seguridad.autorizacion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo global de errores de autorización funcional.
 *
 * SecurityConfig controla la autenticación JWT.
 * AccessValidator controla los permisos funcionales.
 *
 * Cuando un usuario autenticado intenta ejecutar una operación
 * para la cual no posee permiso, la API responde HTTP 403.
 */
@RestControllerAdvice
public class AccessExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> manejarAccesoDenegado(
            SecurityException ex
    ) {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put(
                "timestamp",
                LocalDateTime.now()
        );

        respuesta.put(
                "status",
                HttpStatus.FORBIDDEN.value()
        );

        respuesta.put(
                "error",
                "Forbidden"
        );

        respuesta.put(
                "message",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(respuesta);
    }
}