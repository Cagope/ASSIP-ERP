package co.assip.erp.seguridad.usuarios;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de administración y seguridad básica de usuarios.
 *
 * Gestiona los datos del usuario, contraseña, bloqueo,
 * recuperación de acceso y seguimiento de autenticación.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int DIAS_VIGENCIA_PASSWORD = 90;
    private static final int HORAS_VIGENCIA_TOKEN = 2;

    private final UsuarioRepository usuarioRepository;

    /*
     * Se mantiene local temporalmente mientras reorganizamos
     * la configuración de seguridad.
     */
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    /**
     * Obtiene el listado administrativo de usuarios.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar() {
        return usuarioRepository.listarConRolYAgencia();
    }

    /**
     * Obtiene la información utilizada para editar un usuario.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> buscarDtoPorId(Integer idUsuario) {

        Map<String, Object> usuario =
                usuarioRepository.buscarDtoPorId(idUsuario);

        if (usuario == null || usuario.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existe el usuario con id " + idUsuario
            );
        }

        return usuario;
    }

    /**
     * Busca un usuario por su nombre de acceso.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Busca un usuario por su identificador.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    /**
     * Crea o actualiza un usuario.
     *
     * Por compatibilidad con el proceso actual, la existencia
     * del username determina si se trata de creación o actualización.
     */
    @Transactional
    public Usuario guardar(Usuario usuario) {

        validarDatosBasicos(usuario);

        Optional<Usuario> existente =
                usuarioRepository.findByUsername(usuario.getUsername());

        if (existente.isPresent()) {
            return actualizar(existente.get(), usuario);
        }

        return crear(usuario);
    }

    /**
     * Actualiza los datos administrativos de un usuario existente.
     */
    private Usuario actualizar(
            Usuario actual,
            Usuario cambios
    ) {

        if (cambios.getPassword() != null
                && !cambios.getPassword().isBlank()) {

            actualizarPassword(actual, cambios.getPassword(), false);
        }

        actual.setNombreCompleto(cambios.getNombreCompleto());
        actual.setEmail(cambios.getEmail());
        actual.setActivo(cambios.getActivo());
        actual.setIdRol(cambios.getIdRol());
        actual.setIdAgenciaPrincipal(cambios.getIdAgenciaPrincipal());

        actual.setFechaActualizacion(LocalDateTime.now());
        actual.setUsuarioActualizacion(
                cambios.getUsuarioActualizacion()
        );

        return usuarioRepository.save(actual);
    }

    /**
     * Inicializa y persiste un nuevo usuario.
     */
    private Usuario crear(Usuario usuario) {

        if (usuario.getPassword() == null
                || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria para crear el usuario."
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        usuario.setIdUsuario(null);
        usuario.setUsername(usuario.getUsername().trim());

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        usuario.setActivo(true);
        usuario.setBloqueado(false);
        usuario.setPasswordIntentos(0);

        usuario.setFechaCreacion(ahora);
        usuario.setPasswordUltimoCambio(ahora);
        usuario.setPasswordExpira(
                ahora.plusDays(DIAS_VIGENCIA_PASSWORD)
        );

        usuario.setRequiereCambioPassword(false);

        return usuarioRepository.save(usuario);
    }

    /**
     * Valida los datos mínimos requeridos para administrar un usuario.
     */
    private void validarDatosBasicos(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios."
            );
        }

        if (usuario.getUsername() == null
                || usuario.getUsername().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        if (usuario.getIdRol() == null
                || usuario.getIdRol() <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un rol válido."
            );
        }
    }

    /**
     * Obtiene el usuario autenticado a partir de los claims
     * almacenados en el contexto de Spring Security.
     *
     * Se construye un usuario liviano; no realiza consulta
     * adicional a la base de datos.
     */
    public Usuario getUsuarioActual(HttpServletRequest request) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                    "No hay usuario autenticado."
            );
        }

        Object credentials = auth.getCredentials();

        if (!(credentials instanceof Claims claims)) {
            throw new IllegalStateException(
                    "Token inválido en el contexto de seguridad."
            );
        }

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(
                claims.get("idUsuario", Integer.class)
        );

        usuario.setIdRol(
                claims.get("rol", Integer.class)
        );

        usuario.setUsername(auth.getName());

        return usuario;
    }

    /**
     * Genera un token temporal para recuperación de contraseña.
     */
    @Transactional
    public String generarTokenRecuperacion(String email) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Correo no registrado."
                        )
                );

        String token = UUID.randomUUID().toString();

        usuario.setTokenRecuperacion(token);
        usuario.setTokenExpira(
                LocalDateTime.now()
                        .plusHours(HORAS_VIGENCIA_TOKEN)
        );

        usuarioRepository.save(usuario);

        return token;
    }

    /**
     * Establece una nueva contraseña mediante un token
     * de recuperación vigente.
     */
    @Transactional
    public void restaurarPassword(
            String token,
            String nuevaClave
    ) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "El token de recuperación es obligatorio."
            );
        }

        if (nuevaClave == null || nuevaClave.isBlank()) {
            throw new IllegalArgumentException(
                    "La nueva contraseña es obligatoria."
            );
        }

        Usuario usuario =
                usuarioRepository.findByTokenRecuperacion(token)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Token inválido."
                                )
                        );

        if (usuario.getTokenExpira() == null
                || usuario.getTokenExpira()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "El token ha expirado."
            );
        }

        actualizarPassword(usuario, nuevaClave, false);

        usuario.setTokenRecuperacion(null);
        usuario.setTokenExpira(null);

        usuarioRepository.save(usuario);
    }

    /**
     * Registra un intento de autenticación fallido.
     *
     * Al alcanzar el máximo permitido, bloquea la cuenta.
     */
    @Transactional
    public void registrarIntentoFallido(String username) {

        Usuario usuario =
                usuarioRepository.findByUsername(username)
                        .orElse(null);

        if (usuario == null) {
            return;
        }

        int intentos =
                usuario.getPasswordIntentos() == null
                        ? 0
                        : usuario.getPasswordIntentos();

        intentos++;

        usuario.setPasswordIntentos(intentos);
        usuario.setUltimoIntentoLogin(LocalDateTime.now());

        if (intentos >= MAX_INTENTOS_FALLIDOS) {
            usuario.setBloqueado(true);
            usuario.setFechaBloqueo(LocalDateTime.now());
        }

        usuarioRepository.save(usuario);
    }

    /**
     * Registra una autenticación exitosa y reinicia
     * el contador de intentos fallidos.
     */
    @Transactional
    public void registrarLoginExitoso(
            String username,
            String ip
    ) {

        Usuario usuario =
                usuarioRepository.findByUsername(username)
                        .orElse(null);

        if (usuario == null) {
            return;
        }

        usuario.setUltimoLogin(LocalDateTime.now());
        usuario.setIpUltimoLogin(ip);
        usuario.setPasswordIntentos(0);

        usuarioRepository.save(usuario);
    }

    /**
     * Actualiza la contraseña y sus controles asociados.
     */
    private void actualizarPassword(
            Usuario usuario,
            String nuevaClave,
            boolean requiereCambio
    ) {

        LocalDateTime ahora = LocalDateTime.now();

        usuario.setPassword(
                passwordEncoder.encode(nuevaClave)
        );

        usuario.setPasswordUltimoCambio(ahora);
        usuario.setPasswordExpira(
                ahora.plusDays(DIAS_VIGENCIA_PASSWORD)
        );

        usuario.setPasswordIntentos(0);
        usuario.setRequiereCambioPassword(requiereCambio);
    }
}