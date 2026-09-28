package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro encargado de autenticar las solicitudes mediante JWT.
 *
 * Valida el token, verifica el estado actual del usuario
 * y registra la autenticación en el SecurityContext de Spring.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Procesa el JWT enviado en el encabezado Authorization
     * y establece el usuario autenticado en el contexto de seguridad.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        if (esRutaPublica(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(7);

        Claims claims;

        try {
            claims = jwtService.extraerTodo(token);

        } catch (ExpiredJwtException ex) {
            responderNoAutorizado(
                    response,
                    "Token expirado"
            );
            return;

        } catch (Exception ex) {
            responderNoAutorizado(
                    response,
                    "Token inválido"
            );
            return;
        }

        String username =
                claims.getSubject();

        if (username == null
                || username.isBlank()
                || SecurityContextHolder
                .getContext()
                .getAuthentication() != null) {

            filterChain.doFilter(request, response);
            return;
        }

        Usuario usuario =
                usuarioRepository
                        .findByUsername(username)
                        .orElse(null);

        if (usuario == null) {
            responderNoAutorizado(
                    response,
                    "Usuario no autorizado"
            );
            return;
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            responderNoAutorizado(
                    response,
                    "Usuario inactivo"
            );
            return;
        }

        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            responderNoAutorizado(
                    response,
                    "Usuario bloqueado"
            );
            return;
        }

        if (!jwtService.validarToken(token)) {
            responderNoAutorizado(
                    response,
                    "Token inválido"
            );
            return;
        }

        Integer idRol =
                obtenerIdRol(claims);

        if (idRol == null) {
            responderNoAutorizado(
                    response,
                    "Usuario sin rol asignado"
            );
            return;
        }

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + idRol
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        usuario.getUsername(),
                        claims,
                        List.of(authority)
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /**
     * Obtiene el identificador del rol almacenado
     * en los claims del JWT.
     */
    private Integer obtenerIdRol(
            Claims claims
    ) {

        Object valor =
                claims.get("rol");

        if (valor instanceof Integer integer) {
            return integer;
        }

        if (valor instanceof Number numero) {
            return numero.intValue();
        }

        return null;
    }

    /**
     * Identifica las rutas que no requieren
     * procesamiento del JWT en este filtro.
     */
    private boolean esRutaPublica(
            String path
    ) {

        return path.startsWith("/auth")
                || path.startsWith("/api/v1/auth")
                || path.startsWith("/error");
    }

    /**
     * Devuelve una respuesta HTTP 401 para
     * solicitudes con autenticación inválida.
     */
    private void responderNoAutorizado(
            HttpServletResponse response,
            String mensaje
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "text/plain;charset=UTF-8"
        );

        response.getWriter().write(mensaje);
    }
}