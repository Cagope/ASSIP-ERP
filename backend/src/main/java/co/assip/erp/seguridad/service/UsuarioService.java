package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ============================================================
    // LISTAR DTO
    // ============================================================
    public List<Map<String, Object>> listar() {
        return usuarioRepository.listarConRolYAgencia();
    }

    // ============================================================
    // BUSCAR DTO PARA EDICIÓN
    // ============================================================
    public Map<String, Object> buscarDtoPorId(Integer idUsuario) {
        return usuarioRepository.buscarDtoPorId(idUsuario);
    }

    // ============================================================
    // ENTIDAD
    // ============================================================
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Optional<Usuario> buscarPorId(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    // ============================================================
    // GUARDAR / ACTUALIZAR
    // ============================================================
    public Usuario guardar(Usuario usuario) {

        // ------------------------------
        // VALIDACIÓN DE ROL
        // ------------------------------
        if (usuario.getIdRol() == null || usuario.getIdRol() == 0) {
            throw new RuntimeException("Debe seleccionar un rol válido");
        }

        Optional<Usuario> existente = usuarioRepository.findByUsername(usuario.getUsername());

        if (existente.isPresent()) {
            Usuario actual = existente.get();

            // Clave
            if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
                actual.setPassword(passwordEncoder.encode(usuario.getPassword()));
                actual.setPasswordUltimoCambio(LocalDateTime.now());
                actual.setPasswordExpira(LocalDateTime.now().plusDays(90));
                actual.setRequiereCambioPassword(false);
                actual.setPasswordIntentos(0);
            }

            // Campos básicos
            actual.setNombreCompleto(usuario.getNombreCompleto());
            actual.setEmail(usuario.getEmail());
            actual.setActivo(usuario.getActivo());
            actual.setIdAgenciaPrincipal(usuario.getIdAgenciaPrincipal());

            // 🔥 GRABA EL ROL DIRECTAMENTE
            actual.setIdRol(usuario.getIdRol());

            actual.setFechaActualizacion(LocalDateTime.now());
            actual.setUsuarioActualizacion(usuario.getUsuarioActualizacion());

            return usuarioRepository.save(actual);
        }

        // ------------------------------
        // NUEVO USUARIO
        // ------------------------------
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setPasswordUltimoCambio(LocalDateTime.now());
        usuario.setPasswordExpira(LocalDateTime.now().plusDays(90));
        usuario.setPasswordIntentos(0);
        usuario.setBloqueado(false);
        usuario.setRequiereCambioPassword(false);

        return usuarioRepository.save(usuario);
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    public void eliminar(Integer id) {
        usuarioRepository.deleteById(id);
    }

    // ============================================================
    // USUARIO ACTUAL
    // ============================================================
    // ============================================================
// USUARIO ACTUAL (desde TOKEN, no BD)
// ============================================================
    public Usuario getUsuarioActual(HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        // Claims del token
        Object credentials = auth.getCredentials();
        if (!(credentials instanceof io.jsonwebtoken.Claims claims)) {
            throw new RuntimeException("Token inválido en el contexto");
        }

        // Crear un usuario liviano SOLO con los datos del token
        Usuario u = new Usuario();
        u.setIdUsuario(claims.get("idUsuario", Integer.class));  // ← ID real
        u.setIdRol(claims.get("rol", Integer.class));            // ← ROL REAL
        u.setUsername(auth.getName());

        return u;
    }

    // ============================================================
    // RESET PASSWORD
    // ============================================================
    public String generarTokenRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Correo no registrado"));

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(token);
        usuario.setTokenExpira(LocalDateTime.now().plusHours(2));
        usuarioRepository.save(usuario);

        return token;
    }

    public void restaurarPassword(String token, String nuevaClave) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacion(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (usuario.getTokenExpira().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaClave));
        usuario.setPasswordUltimoCambio(LocalDateTime.now());
        usuario.setPasswordExpira(LocalDateTime.now().plusDays(90));
        usuario.setPasswordIntentos(0);
        usuario.setRequiereCambioPassword(false);

        usuario.setTokenRecuperacion(null);
        usuario.setTokenExpira(null);

        usuarioRepository.save(usuario);
    }

    // ============================================================
    // BLOQUEO AUTOMÁTICO
    // ============================================================
    public void registrarIntentoFallido(String username) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElse(null);

        if (usuario == null) return;

        usuario.setPasswordIntentos(
                (usuario.getPasswordIntentos() == null ? 0 : usuario.getPasswordIntentos()) + 1
        );

        usuario.setUltimoIntentoLogin(LocalDateTime.now());

        if (usuario.getPasswordIntentos() >= 5) {
            usuario.setBloqueado(true);
            usuario.setFechaBloqueo(LocalDateTime.now());
        }

        usuarioRepository.save(usuario);
    }

    // ============================================================
    // LOGIN EXITOSO
    // ============================================================
    public void registrarLoginExitoso(String username, String ip) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElse(null);

        if (usuario == null) return;

        usuario.setUltimoLogin(LocalDateTime.now());
        usuario.setIpUltimoLogin(ip);
        usuario.setPasswordIntentos(0);

        usuarioRepository.save(usuario);
    }
}
