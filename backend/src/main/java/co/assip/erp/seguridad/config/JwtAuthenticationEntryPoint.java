package co.assip.erp.seguridad.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Manejador centralizado de errores de autenticación JWT.
 * Devuelve respuestas JSON legibles y seguras para el cliente.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String json = String.format("""
                {
                  "timestamp": "%s",
                  "status": 401,
                  "error": "No autorizado",
                  "message": "%s",
                  "path": "%s"
                }
                """,
                LocalDateTime.now(),
                authException.getMessage(),
                request.getRequestURI()
        );

        response.getWriter().write(json);
    }
}
