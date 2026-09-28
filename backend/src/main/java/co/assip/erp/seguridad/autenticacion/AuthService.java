package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.seguridad.usuariosagencias.UsuarioAgenciaService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import co.assip.erp.seguridad.usuarios.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de autenticar usuarios del ERP.
 *
 * Valida las credenciales y el estado de la cuenta,
 * controla los intentos fallidos y genera el token JWT
 * con el contexto de seguridad del usuario.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final UsuarioAgenciaService usuarioAgenciaService;

    /*
     * Se mantiene local temporalmente mientras se reorganiza
     * la configuración general de seguridad.
     */
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    /**
     * Autentica un usuario y genera su token JWT.
     *
     * Si la contraseña es incorrecta, registra el intento fallido.
     * Si la autenticación es correcta, reinicia el contador de
     * intentos y registra la fecha e IP del último acceso.
     *
     * @param username nombre de acceso
     * @param password contraseña recibida en el login
     * @param ip       dirección IP desde donde se realiza el acceso
     * @return información básica de la sesión y token JWT
     */
    public Map<String, Object> autenticarConUsuario(
            String username,
            String password,
            String ip
    ) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalArgumentException(
                    "Usuario y contraseña son obligatorios."
            );
        }

        String usernameNormalizado = username.trim();

        Usuario usuario = usuarioRepository
                .findByUsername(usernameNormalizado)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario o contraseña incorrectos."
                        )
                );

        /*
         * Una cuenta inactiva no puede autenticarse.
         */
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalStateException(
                    "Usuario inactivo."
            );
        }

        /*
         * Una cuenta bloqueada no puede autenticarse.
         */
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            throw new IllegalStateException(
                    "Usuario bloqueado."
            );
        }

        /*
         * Todo usuario debe tener un rol asignado
         * antes de iniciar sesión.
         */
        if (usuario.getIdRol() == null) {
            throw new IllegalStateException(
                    "El usuario no tiene un rol asignado."
            );
        }

        /*
         * Contraseña incorrecta:
         * registra el intento y permite que UsuarioService
         * aplique el bloqueo cuando alcance el límite definido.
         */
        if (!passwordEncoder.matches(
                password,
                usuario.getPassword()
        )) {

            usuarioService.registrarIntentoFallido(
                    usernameNormalizado
            );

            throw new IllegalArgumentException(
                    "Usuario o contraseña incorrectos."
            );
        }

        /*
         * Autenticación correcta:
         * registra último acceso y reinicia intentos fallidos.
         */
        usuarioService.registrarLoginExitoso(
                usernameNormalizado,
                ip
        );

        /*
         * Obtiene las agencias autorizadas del usuario.
         */
        List<Integer> agencias =
                usuarioAgenciaService
                        .listarAgenciasDelUsuario(
                                usuario.getIdUsuario()
                        )
                        .stream()
                        .map(agencia ->
                                (Integer) agencia.get("id_agencia")
                        )
                        .toList();

        /*
         * Genera el JWT con usuario, rol y agencias.
         */
        String token =
                jwtService.generarTokenConClaims(
                        usuario,
                        agencias
                );

        Map<String, Object> respuesta =
                new HashMap<>();

        respuesta.put(
                "idUsuario",
                usuario.getIdUsuario()
        );

        respuesta.put(
                "username",
                usuario.getUsername()
        );

        respuesta.put(
                "idRol",
                usuario.getIdRol()
        );

        respuesta.put(
                "agencias",
                agencias
        );

        respuesta.put(
                "token",
                token
        );

        return respuesta;
    }

    /**
     * Busca el usuario persistido correspondiente
     * a un username.
     */
    public Usuario buscarUsuarioPorUsername(
            String username
    ) {

        if (username == null || username.isBlank()) {
            return null;
        }

        return usuarioRepository
                .findByUsername(username.trim())
                .orElse(null);
    }
}