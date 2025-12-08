package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final UsuarioAgenciaService usuarioAgenciaService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 🔐 Autenticar y generar token con claims (agencias + idUsuario)
     */
    public Map<String, Object> autenticarConUsuario(String username, String password) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("PASSWORD_FAIL");
        }

        // 🔹 Traer agencias asignadas
        List<Integer> agencias = usuarioAgenciaService.listarAgenciasDelUsuario(usuario.getIdUsuario())
                .stream()
                .map(a -> (Integer) a.get("id_agencia"))
                .toList();

        // 🔹 Generar token con claims (firma original)
        String token = jwtService.generarTokenConClaims(usuario, agencias);

        // 🔹 Respuesta
        Map<String, Object> resp = new HashMap<>();
        resp.put("idUsuario", usuario.getIdUsuario());
        resp.put("username", usuario.getUsername());
        resp.put("idRol", usuario.getIdRol());
        resp.put("agencias", agencias);
        resp.put("token", token);

        return resp;
    }

    // =====================================================
    // Métodos originales (NO SE TOCAN)
    // =====================================================

    public String autenticar(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return jwtService.generarToken(usuario.getUsername());
    }

    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}
