package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Filtro encargado de validar el JWT recibido en cada petición
 * autenticada y establecer la identidad del usuario en el
 * contexto de Spring Security.
 *
 * Los roles y permisos no se obtienen del JWT.
 * La autorización funcional se resuelve posteriormente desde
 * seguridad.usuarios_roles y seguridad.rol_permisos.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Procesa el token Bearer recibido en la petición.
     *
     * Además de validar el JWT, verifica que la cuenta continúe
     * activa, vigente y sin bloqueo administrativo.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorization.substring(7);

        if (!jwtService.validarToken(token)) {

            filterChain.doFilter(request, response);
            return;
        }

        Claims claims;

        try {
            claims = jwtService.extraerTodo(token);
        } catch (Exception ex) {
            filterChain.doFilter(request, response);
            return;
        }

        String username =
                claims.getSubject();

        Integer idUsuario =
                claims.get(
                        "idUsuario",
                        Integer.class
                );

        if (username == null
                || username.isBlank()
                || idUsuario == null) {

            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Evita reconstruir la autenticación cuando Spring Security
         * ya estableció un usuario para la petición actual.
         */
        if (SecurityContextHolder
                .getContext()
                .getAuthentication() != null) {

            filterChain.doFilter(request, response);
            return;
        }

        Usuario usuario =
                usuarioRepository
                        .findById(idUsuario)
                        .orElse(null);

        if (usuario == null
                || !username.equals(usuario.getUsername())
                || !usuarioDisponible(usuario)) {

            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Spring Security solamente necesita reconocer que existe
         * una identidad autenticada.
         *
         * Los permisos funcionales no se convierten aquí en
         * GrantedAuthority porque AccessValidator los resuelve
         * desde el estado vigente de la base de datos.
         */
        List<SimpleGrantedAuthority> authorities =
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_AUTHENTICATED"
                        )
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        claims,
                        authorities
                );

        authentication.setDetails(
                usuario.getIdUsuario()
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica que la cuenta pueda continuar utilizando
     * una sesión previamente emitida.
     */
    private boolean usuarioDisponible(
            Usuario usuario
    ) {

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return false;
        }

        LocalDateTime ahora =
                LocalDateTime.now();

        if (usuario.getFechaInicioVigencia() != null
                && usuario.getFechaInicioVigencia().isAfter(ahora)) {
            return false;
        }

        if (usuario.getFechaFinVigencia() != null
                && usuario.getFechaFinVigencia().isBefore(ahora)) {
            return false;
        }

        /*
         * Mientras implementamos formalmente el desbloqueo
         * temporal, un usuario marcado como bloqueado continúa
         * considerándose bloqueado.
         */
        return !Boolean.TRUE.equals(
                usuario.getBloqueado()
        );
    }
}