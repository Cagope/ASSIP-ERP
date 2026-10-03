
package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Gestiona el registro de intentos fallidos de autenticación.
 *
 * Cada intento se registra en una transacción independiente
 * para evitar que las excepciones del proceso de login
 * reviertan el contador o el bloqueo de la cuenta.
 *
 * No administra contraseñas, tokens ni sesiones JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthIntentosService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;

    private final UsuarioRepository usuarioRepository;

    /**
     * Registra un intento fallido de autenticación.
     *
     * La operación se ejecuta mediante REQUIRES_NEW
     * para garantizar que sus cambios se confirmen
     * independientemente de la transacción del login.
     *
     * Al alcanzar cinco intentos fallidos,
     * la cuenta queda bloqueada.
     *
     * @param idUsuario identificador del usuario
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void registrarIntentoFallido(
            Integer idUsuario
    ) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        /*
         * Se consulta nuevamente el usuario dentro
         * de la transacción independiente.
         *
         * No se recibe una entidad administrada
         * por la transacción principal.
         */
        Usuario usuario = usuarioRepository
                .findByIdForUpdate(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado."
                        )
                );

        /*
         * No se incrementan intentos sobre
         * una cuenta que ya está bloqueada.
         */
        if (Boolean.TRUE.equals(
                usuario.getBloqueado()
        )) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();

        int intentosActuales =
                usuario.getPasswordIntentos() == null
                        ? 0
                        : usuario.getPasswordIntentos();

        int nuevosIntentos = intentosActuales + 1;

        usuario.setPasswordIntentos(
                nuevosIntentos
        );

        usuario.setUltimoIntentoLogin(
                ahora
        );

        /*
         * Bloqueo automático al alcanzar
         * el máximo de intentos permitidos.
         */
        if (nuevosIntentos >= MAX_INTENTOS_FALLIDOS) {

            usuario.setBloqueado(
                    true
            );

            usuario.setFechaBloqueo(
                    ahora
            );
        }

        /*
         * Conserva las fechas de actualización
         * utilizadas por el modelo actual.
         */
        usuario.setFechaActualizacion(
                ahora
        );

        usuario.setFechaEdicion(
                ahora
        );

        usuarioRepository.save(
                usuario
        );
    }

    /**
     * Registra un inicio de sesión exitoso.
     *
     * Reinicia el contador de intentos y
     * actualiza la información del último acceso.
     *
     * No desbloquea cuentas bloqueadas.
     *
     * @param idUsuario identificador del usuario
     * @param ip dirección IP del acceso
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void registrarLoginExitoso(
            Integer idUsuario,
            String ip
    ) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        Usuario usuario = usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado."
                        )
                );

        /*
         * Una cuenta bloqueada no puede
         * registrar un acceso exitoso.
         */
        if (Boolean.TRUE.equals(
                usuario.getBloqueado()
        )) {
            throw new IllegalStateException(
                    "Usuario bloqueado."
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        usuario.setUltimoLogin(
                ahora
        );

        usuario.setIpUltimoLogin(
                ip
        );

        usuario.setPasswordIntentos(
                0
        );

        usuario.setUltimoIntentoLogin(
                ahora
        );

        usuario.setFechaActualizacion(
                ahora
        );

        usuario.setFechaEdicion(
                ahora
        );

        usuarioRepository.save(
                usuario
        );
    }
}
