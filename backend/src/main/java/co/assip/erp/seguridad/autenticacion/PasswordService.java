package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.general.parametro.Parametro;
import co.assip.erp.general.parametro.ParametroService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio responsable de las credenciales de contraseña
 * de los usuarios del ERP.
 *
 * Centraliza:
 * - verificación de contraseñas;
 * - establecimiento de contraseña durante la activación;
 * - recuperación de contraseña;
 * - cambio voluntario de contraseña;
 * - validación de la política de seguridad;
 * - actualización de fechas y controles asociados.
 *
 * No administra usuarios, roles, agencias, JWT ni tokens
 * de activación o recuperación.
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

    private static final int CODIGO_PARAMETRO_DIAS_VIGENCIA_PASSWORD = 700;
    private static final int CODIGO_PARAMETRO_LONGITUD_MINIMA_PASSWORD = 705;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParametroService parametroService;


    // =========================================================
    // VERIFICACIÓN
    // =========================================================

    /**
     * Verifica si una contraseña corresponde al hash
     * almacenado para el usuario.
     *
     * Una cuenta que todavía no posee contraseña
     * nunca puede superar esta validación.
     */
    public boolean verificarPassword(
            Usuario usuario,
            String password
    ) {

        if (usuario == null
                || password == null
                || password.isBlank()
                || usuario.getPassword() == null
                || usuario.getPassword().isBlank()) {

            return false;
        }

        return passwordEncoder.matches(
                password,
                usuario.getPassword()
        );
    }


    // =========================================================
    // ACTIVACIÓN
    // =========================================================

    /**
     * Establece la contraseña definitiva de una cuenta
     * que se encuentra pendiente de activación.
     *
     * La validación del código de activación pertenece
     * a UsuarioTokenService y debe realizarse antes de
     * ejecutar esta operación.
     */
    @Transactional
    public void establecerPasswordActivacion(
            Integer idUsuario,
            String passwordNueva
    ) {

        validarIdUsuario(
                idUsuario
        );

        Usuario usuario =
                buscarUsuario(
                        idUsuario
                );

        validarPasswordNueva(
                usuario,
                passwordNueva
        );

        if (usuario.getPassword() != null
                && !usuario.getPassword().isBlank()) {

            throw new IllegalStateException(
                    "La cuenta ya tiene una contraseña establecida."
            );
        }

        if (!Boolean.TRUE.equals(
                usuario.getRequiereCambioPassword()
        )) {

            throw new IllegalStateException(
                    "La cuenta no se encuentra pendiente de activación."
            );
        }

        actualizarPassword(
                usuario,
                passwordNueva
        );

        usuarioRepository.save(
                usuario
        );
    }


    // =========================================================
    // CAMBIO VOLUNTARIO
    // =========================================================

    /**
     * Cambia la contraseña de un usuario que ya posee
     * una credencial definitiva.
     *
     * Requiere comprobar previamente la contraseña actual.
     */
    @Transactional
    public void cambiarPassword(
            Integer idUsuario,
            String passwordActual,
            String passwordNueva
    ) {

        validarIdUsuario(
                idUsuario
        );

        if (passwordActual == null
                || passwordActual.isBlank()) {

            throw new IllegalArgumentException(
                    "La contraseña actual es obligatoria."
            );
        }

        Usuario usuario =
                buscarUsuario(
                        idUsuario
                );

        validarPasswordNueva(
                usuario,
                passwordNueva
        );

        if (usuario.getPassword() == null
                || usuario.getPassword().isBlank()) {

            throw new IllegalStateException(
                    "La cuenta todavía no tiene una contraseña establecida."
            );
        }

        if (!passwordEncoder.matches(
                passwordActual,
                usuario.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "La contraseña actual es incorrecta."
            );
        }

        if (passwordEncoder.matches(
                passwordNueva,
                usuario.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente de la actual."
            );
        }

        actualizarPassword(
                usuario,
                passwordNueva
        );

        usuarioRepository.save(
                usuario
        );
    }


    // =========================================================
    // RECUPERACIÓN
    // =========================================================

    /**
     * Establece una nueva contraseña después de que
     * autenticación haya validado correctamente un
     * proceso de recuperación.
     *
     * No valida códigos ni tokens de recuperación.
     */
    @Transactional
    public void establecerPasswordRecuperacion(
            Integer idUsuario,
            String passwordNueva
    ) {

        validarIdUsuario(
                idUsuario
        );

        Usuario usuario =
                buscarUsuario(
                        idUsuario
                );

        validarPasswordNueva(
                usuario,
                passwordNueva
        );

        /*
         * Si existe una contraseña anterior,
         * la nueva no puede ser exactamente la misma.
         */
        if (usuario.getPassword() != null
                && !usuario.getPassword().isBlank()
                && passwordEncoder.matches(
                passwordNueva,
                usuario.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente de la actual."
            );
        }

        actualizarPassword(
                usuario,
                passwordNueva
        );

        usuarioRepository.save(
                usuario
        );
    }


    // =========================================================
    // USUARIO
    // =========================================================

    /**
     * Busca la cuenta sobre la que se realizará
     * una operación de contraseña.
     */
    private Usuario buscarUsuario(
            Integer idUsuario
    ) {

        return usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado."
                        )
                );
    }


    /**
     * Valida el identificador de usuario.
     */
    private void validarIdUsuario(
            Integer idUsuario
    ) {

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }
    }


    // =========================================================
    // POLÍTICA DE CONTRASEÑA
    // =========================================================

    /**
     * Valida la política de seguridad de una nueva contraseña.
     *
     * Requisitos:
     * - longitud mínima parametrizada por agencia;
     * - al menos una letra mayúscula;
     * - al menos una letra minúscula;
     * - al menos un número;
     * - al menos un carácter especial;
     * - no permite espacios.
     */
    private void validarPasswordNueva(
            Usuario usuario,
            String passwordNueva
    ) {

        if (passwordNueva == null
                || passwordNueva.isBlank()) {

            throw new IllegalArgumentException(
                    "La nueva contraseña es obligatoria."
            );
        }

        int longitudMinima =
                obtenerLongitudMinimaPassword(
                        usuario
                );

        if (passwordNueva.length() < longitudMinima) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener mínimo "
                            + longitudMinima
                            + " caracteres."
            );
        }

        if (!passwordNueva.matches(".*[A-Z].*")) {

            throw new IllegalArgumentException(
                    "La contraseña debe contener al menos una letra mayúscula."
            );
        }

        if (!passwordNueva.matches(".*[a-z].*")) {

            throw new IllegalArgumentException(
                    "La contraseña debe contener al menos una letra minúscula."
            );
        }

        if (!passwordNueva.matches(".*[0-9].*")) {

            throw new IllegalArgumentException(
                    "La contraseña debe contener al menos un número."
            );
        }

        if (!passwordNueva.matches(".*[^A-Za-z0-9\\s].*")) {

            throw new IllegalArgumentException(
                    "La contraseña debe contener al menos un carácter especial."
            );
        }

        if (passwordNueva.matches(".*\\s.*")) {

            throw new IllegalArgumentException(
                    "La contraseña no puede contener espacios."
            );
        }
    }


    // =========================================================
    // ACTUALIZACIÓN DE CONTRASEÑA
    // =========================================================

    /**
     * Codifica y persiste en la entidad los controles
     * asociados al establecimiento de una contraseña.
     */
    private void actualizarPassword(
            Usuario usuario,
            String passwordNueva
    ) {

        LocalDateTime ahora =
                LocalDateTime.now();

        usuario.setPassword(
                passwordEncoder.encode(
                        passwordNueva
                )
        );

        usuario.setPasswordUltimoCambio(
                ahora
        );

        int diasVigenciaPassword =
                obtenerDiasVigenciaPassword(
                        usuario
                );

        usuario.setPasswordExpira(
                ahora.plusDays(
                        diasVigenciaPassword
                )
        );

        usuario.setPasswordIntentos(0);

        usuario.setRequiereCambioPassword(
                false
        );

        /*
         * Una contraseña establecida correctamente
         * deja limpia la condición de bloqueo asociada
         * a intentos anteriores.
         */
        usuario.setBloqueado(false);
        usuario.setFechaBloqueo(null);
        usuario.setBloqueadoHasta(null);

        usuario.setFechaActualizacion(
                ahora
        );

        usuario.setFechaEdicion(
                ahora
        );
    }


    // =========================================================
    // PARÁMETROS
    // =========================================================

    /**
     * Obtiene los días de vigencia de la contraseña
     * según la agencia principal del usuario.
     */
    private int obtenerDiasVigenciaPassword(
            Usuario usuario
    ) {

        if (usuario.getIdAgenciaPrincipal() == null) {

            throw new IllegalStateException(
                    "El usuario no tiene una agencia principal configurada."
            );
        }

        Parametro parametro =
                parametroService
                        .obtenerPorAgenciaYCodigo(
                                usuario.getIdAgenciaPrincipal(),
                                CODIGO_PARAMETRO_DIAS_VIGENCIA_PASSWORD
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No está configurado el parámetro "
                                                + CODIGO_PARAMETRO_DIAS_VIGENCIA_PASSWORD
                                                + " para la agencia "
                                                + usuario.getIdAgenciaPrincipal()
                                                + "."
                                )
                        );

        if (parametro.getValorParametro() == null) {

            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_DIAS_VIGENCIA_PASSWORD
                            + " no tiene un valor configurado."
            );
        }

        int dias;

        try {

            dias =
                    parametro
                            .getValorParametro()
                            .intValueExact();

        } catch (ArithmeticException ex) {

            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_DIAS_VIGENCIA_PASSWORD
                            + " debe contener un número entero de días."
            );
        }

        if (dias <= 0) {

            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_DIAS_VIGENCIA_PASSWORD
                            + " debe ser mayor que cero."
            );
        }

        return dias;
    }


    /**
     * Obtiene la longitud mínima permitida para una contraseña
     * según la agencia principal del usuario.
     */
    public int obtenerLongitudMinimaPassword(
            Usuario usuario
    ) {

        if (usuario.getIdAgenciaPrincipal() == null) {

            throw new IllegalStateException(
                    "El usuario no tiene una agencia principal configurada."
            );
        }

        Parametro parametro =
                parametroService
                        .obtenerPorAgenciaYCodigo(
                                usuario.getIdAgenciaPrincipal(),
                                CODIGO_PARAMETRO_LONGITUD_MINIMA_PASSWORD
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No está configurado el parámetro "
                                                + CODIGO_PARAMETRO_LONGITUD_MINIMA_PASSWORD
                                                + " para la agencia "
                                                + usuario.getIdAgenciaPrincipal()
                                                + "."
                                )
                        );

        if (parametro.getValorParametro() == null) {

            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_LONGITUD_MINIMA_PASSWORD
                            + " no tiene un valor configurado."
            );
        }

        int longitud;

        try {

            longitud =
                    parametro
                            .getValorParametro()
                            .intValueExact();

        } catch (ArithmeticException ex) {

            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_LONGITUD_MINIMA_PASSWORD
                            + " debe contener un número entero."
            );
        }

        if (longitud <= 0) {

            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_LONGITUD_MINIMA_PASSWORD
                            + " debe ser mayor que cero."
            );
        }

        return longitud;
    }
}