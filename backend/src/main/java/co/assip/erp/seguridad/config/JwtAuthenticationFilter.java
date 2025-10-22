package co.assip.erp.seguridad.config;

import co.assip.erp.seguridad.service.JwtService;
import co.assip.erp.seguridad.repository.UsuarioRepository;
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
 * Filtro de autenticación JWT.
 * Intercepta todas las solicitudes HTTP y valida el token JWT en el encabezado Authorization.
 * Excluye automáticamente las rutas públicas: /api/v1/auth/**
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 🔹 Excluir rutas públicas (login y register)
        String path = request.getServletPath();
        if (path.startsWith("/api/v1/auth/") || path.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 🔹 Si no hay encabezado Authorization o no empieza con Bearer, continúa sin validar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            username = jwtService.extraerUsername(jwt);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expirado");
            return;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido");
            return;
        }

        // 🔹 Si el usuario existe y no está autenticado aún, establecer autenticación en contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var usuarioOpt = usuarioRepository.findByUsername(username);
            if (usuarioOpt.isPresent() && jwtService.validarToken(jwt)) {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER")); // ✅ Autoridad básica
                var authToken = new UsernamePasswordAuthenticationToken(
                        usuarioOpt.get(), null, authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 🔹 Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
