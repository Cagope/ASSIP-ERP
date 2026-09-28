package co.assip.erp.seguridad.autenticacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maneja los intentos de acceso a recursos protegidos
 * cuando el usuario no se encuentra autenticado.
 *
 * Devuelve una respuesta HTTP 401 en formato JSON
 * sin exponer detalles internos de Spring Security.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Genera la respuesta estándar cuando Spring Security
     * rechaza una solicitud por falta de autenticación.
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now().toString()
        );

        body.put(
                "status",
                HttpServletResponse.SC_UNAUTHORIZED
        );

        body.put(
                "error",
                "No autorizado"
        );

        body.put(
                "message",
                "Se requiere autenticación para acceder al recurso."
        );

        body.put(
                "path",
                request.getRequestURI()
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                body
        );
    }
}