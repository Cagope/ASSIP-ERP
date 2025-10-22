package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Autentica un usuario y genera un token JWT.
     */
    public String autenticar(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return jwtService.generarToken(usuario.getUsername());
    }

    /**
     * Registra un nuevo usuario cifrando la contraseña.
     */
    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    /**
     * Método auxiliar para recuperar el usuario autenticado.
     * Usado en la auditoría (registro de login en log_evento).
     */
    public Usuario buscarUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    /**
     * ✅ Nuevo método: autentica y devuelve usuario + token
     * Esto permitirá al AuthController registrar el login con el id real del usuario.
     */
    public Map<String, Object> autenticarConUsuario(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtService.generarToken(usuario.getUsername());
        return Map.of("usuario", usuario, "token", token);
    }
}
