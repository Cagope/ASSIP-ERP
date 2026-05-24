package co.assip.erp.seguridad.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 🔐 Configuración central de seguridad para ASSIP-ERP.
 *
 * - Permite libremente:
 *   /api/v1/auth/**         → autenticación y login
 *   /api/v1/catalogos/**    → catálogos generales (solo lectura)
 *   /api/v1/public/**       → rutas públicas genéricas
 *   /error                  → manejo de errores Spring
 *
 * - Todo el resto de endpoints exige JWT.
 * - Habilita CORS para peticiones desde Angular (http://localhost:4200).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})  // ✅ habilita CORS antes que CSRF
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",       // 🔓 login, refresh, etc.
                                "/auth/**",              // compatibilidad
                                "/api/v1/catalogos/**",  // 🔓 catálogos públicos
                                "/api/v1/public/**",     // 🔓 endpoints de libre acceso
                                "/error"                 // errores de Spring
                        ).permitAll()
                        .anyRequest().authenticated() // 🔐 todo lo demás requiere token
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 🌐 Configuración global de CORS.
     * Permite acceso desde Angular (http://localhost:4200)
     * con métodos y headers comunes.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",     // entorno local
                "http://127.0.0.1:4200"      // compatibilidad local
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // ✅ permite tokens y cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * ⚙️ Configura el AuthenticationManager para delegar en Spring Boot.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
