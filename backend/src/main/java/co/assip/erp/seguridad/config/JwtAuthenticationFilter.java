package co.assip.erp.seguridad.config;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import co.assip.erp.seguridad.service.JwtService;
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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String path = request.getServletPath();

        // 🔓 Rutas públicas
        if (path.startsWith("/auth") ||
                path.startsWith("/api/v1/auth") ||
                path.startsWith("/error")) {

            filterChain.doFilter(request, response);
            return;
        }

        final String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);

        Claims claims;
        try {
            claims = jwtService.extraerTodo(token);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expirado");
            return;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido");
            return;
        }

        String username = claims.getSubject();

        // Si no está autenticado aún
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            var usuarioOpt = usuarioRepository.findByUsername(username);

            if (usuarioOpt.isPresent() &&
                    jwtService.validarToken(token)) {

                Usuario usuario = usuarioOpt.get();

                // =============================================
                // ✔ Leer idRol como Integer desde el token
                // =============================================
                Integer rolId = claims.get("rol", Integer.class);
                if (rolId == null) {
                    rolId = 0; // rol por defecto sin permisos
                }

                // Crear autoridad con formato ROLE_{id}
                var authority = new SimpleGrantedAuthority("ROLE_" + rolId);

                var authToken = new UsernamePasswordAuthenticationToken(
                        usuario.getUsername(),
                        claims,
                        List.of(authority)
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
