package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.general.parametro.Parametro;
import co.assip.erp.general.parametro.ParametroService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Gestiona los códigos de seguridad utilizados
 * para activación y recuperación de cuentas.
 *
 * Los códigos:
 * - son numéricos de seis dígitos;
 * - tienen una vigencia definida por parámetros de seguridad;
 * - tienen un máximo de intentos definido por parámetros de seguridad;
 * - son de un solo uso;
 * - se almacenan exclusivamente mediante hash.
 *
 * Los intentos fallidos y las desactivaciones
 * se registran mediante UsuarioTokenIntentosService
 * en transacciones independientes.
 */
@Service
@RequiredArgsConstructor
public class UsuarioTokenService {

    public static final String TIPO_ACTIVACION = "ACTIVACION";
    public static final String TIPO_RECUPERACION = "RECUPERACION";

    /*
     * Constantes técnicas para garantizar
     * un código numérico de seis dígitos.
     */
    private static final int LIMITE_INFERIOR_TOKEN = 100000;
    private static final int CANTIDAD_TOKEN = 900000;

    /*
     * Códigos de parámetros de seguridad.
     */
    private static final int CODIGO_PARAMETRO_DIAS_VIGENCIA_TOKEN_ACTIVACION = 702;
    private static final int CODIGO_PARAMETRO_DIAS_VIGENCIA_TOKEN_RECUPERACION = 703;
    private static final int CODIGO_PARAMETRO_MAXIMO_INTENTOS_TOKEN = 704;

    private final UsuarioTokenRepository usuarioTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioTokenIntentosService usuarioTokenIntentosService;
    private final PasswordEncoder passwordEncoder;
    private final ParametroService parametroService;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Genera un código de activación.
     *
     * Invalida los códigos pendientes anteriores
     * del mismo usuario y tipo.
     */
    @Transactional
    public String generarTokenActivacion(
            Integer idUsuario,
            Integer idUsuarioResponsable
    ) {

        return generarToken(
                idUsuario,
                TIPO_ACTIVACION,
                idUsuarioResponsable
        );
    }

    /**
     * Genera un código de recuperación.
     */
    @Transactional
    public String generarTokenRecuperacion(
            Integer idUsuario,
            Integer idUsuarioResponsable
    ) {

        return generarToken(
                idUsuario,
                TIPO_RECUPERACION,
                idUsuarioResponsable
        );
    }

    /**
     * Valida un código de activación.
     *
     * Una validación correcta no consume el código.
     */
    public UsuarioToken validarTokenActivacion(
            Integer idUsuario,
            String token
    ) {

        return validarToken(
                idUsuario,
                TIPO_ACTIVACION,
                token
        );
    }

    /**
     * Valida un código de recuperación.
     *
     * Una validación correcta no consume el código.
     */
    public UsuarioToken validarTokenRecuperacion(
            Integer idUsuario,
            String token
    ) {

        return validarToken(
                idUsuario,
                TIPO_RECUPERACION,
                token
        );
    }

    /**
     * Marca un código como utilizado.
     *
     * Debe ejecutarse dentro de la transacción
     * del proceso que consume el código.
     */
    @Transactional
    public void marcarUtilizado(
            UsuarioToken usuarioToken,
            Integer idUsuarioResponsable
    ) {

        if (usuarioToken == null
                || usuarioToken.getIdUsuarioToken() == null) {

            throw new IllegalArgumentException(
                    "El token de seguridad es obligatorio."
            );
        }

        /*
         * Se recupera el estado actual del token
         * antes de marcarlo como utilizado.
         */
        UsuarioToken registro = usuarioTokenRepository
                .findByIdForUpdate(
                        usuarioToken.getIdUsuarioToken()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Código de seguridad no encontrado."
                        )
                );

        if (Boolean.TRUE.equals(registro.getUtilizado())
                || !Boolean.TRUE.equals(registro.getActivo())) {

            throw new IllegalStateException(
                    "El token de seguridad ya no está disponible."
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (registro.getFechaExpiracion() == null
                || !registro.getFechaExpiracion().isAfter(ahora)) {

            throw new IllegalStateException(
                    "El código de seguridad ha expirado."
            );
        }

        registro.setUtilizado(true);
        registro.setActivo(false);
        registro.setFechaUtilizacion(ahora);
        registro.setFechaEdicion(ahora);

        if (idUsuarioResponsable != null) {

            Usuario responsable = buscarUsuario(
                    idUsuarioResponsable,
                    "Usuario responsable no encontrado."
            );

            registro.setUsuarioEdicion(responsable);
        }

        usuarioTokenRepository.save(registro);
    }

    /**
     * Genera y persiste un nuevo código.
     */
    private String generarToken(
            Integer idUsuario,
            String tipoToken,
            Integer idUsuarioResponsable
    ) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        /*
         * El bloqueo de usuario serializa
         * la emisión de códigos para la cuenta.
         */
        Usuario usuario = usuarioRepository
                .findByIdForUpdate(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado."
                        )
                );

        Usuario responsable = null;

        if (idUsuarioResponsable != null) {

            responsable = buscarUsuario(
                    idUsuarioResponsable,
                    "Usuario responsable no encontrado."
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        /*
         * Invalida los códigos pendientes anteriores.
         */
        usuarioTokenRepository.desactivarTokensPendientes(
                idUsuario,
                tipoToken,
                ahora
        );

        /*
         * La vigencia se obtiene de los parámetros
         * correspondientes a la agencia principal
         * del usuario.
         */
        int diasVigencia =
                obtenerDiasVigenciaToken(
                        usuario,
                        tipoToken
                );

        String token = generarCodigoSeisDigitos();

        UsuarioToken registro = UsuarioToken.builder()
                .usuario(usuario)
                .tipoToken(tipoToken)
                .tokenHash(passwordEncoder.encode(token))
                .fechaExpiracion(
                        ahora.plusDays(
                                diasVigencia
                        )
                )
                .intentos(0)
                .utilizado(false)
                .activo(true)
                .usuarioCreacion(responsable)
                .fechaCreacion(ahora)
                .build();

        usuarioTokenRepository.save(registro);

        return token;
    }

    /**
     * Valida formato, vigencia, intentos
     * y correspondencia del código.
     *
     * Los cambios por validaciones fallidas
     * se confirman en transacciones independientes.
     */
    private UsuarioToken validarToken(
            Integer idUsuario,
            String tipoToken,
            String token
    ) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        if (token == null || !token.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "El código de seguridad debe contener 6 dígitos."
            );
        }

        UsuarioToken registro = usuarioTokenRepository
                .findFirstByUsuario_IdUsuarioAndTipoTokenAndActivoTrueAndUtilizadoFalseOrderByFechaCreacionDesc(
                        idUsuario,
                        tipoToken
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe un código de seguridad vigente."
                        )
                );

        LocalDateTime ahora = LocalDateTime.now();

        /*
         * Código vencido.
         */
        if (registro.getFechaExpiracion() == null
                || !registro.getFechaExpiracion().isAfter(ahora)) {

            usuarioTokenIntentosService.desactivarTokenVencido(
                    registro.getIdUsuarioToken()
            );

            throw new IllegalArgumentException(
                    "El código de seguridad ha expirado."
            );
        }

        int intentosActuales =
                registro.getIntentos() == null
                        ? 0
                        : registro.getIntentos();

        /*
         * El máximo de intentos se obtiene del parámetro 704
         * correspondiente a la agencia principal del usuario.
         */
        Usuario usuario =
                buscarUsuario(
                        idUsuario,
                        "Usuario no encontrado."
                );

        int maximoIntentos =
                obtenerParametroEnteroPositivo(
                        usuario,
                        CODIGO_PARAMETRO_MAXIMO_INTENTOS_TOKEN
                );

        /*
         * Máximo de intentos alcanzado.
         */
        if (intentosActuales >= maximoIntentos) {

            throw new IllegalArgumentException(
                    "El código de seguridad ha superado el número máximo de intentos."
            );
        }

        /*
         * Código incorrecto.
         */
        if (!passwordEncoder.matches(
                token,
                registro.getTokenHash()
        )) {

            usuarioTokenIntentosService.registrarIntentoFallido(
                    registro.getIdUsuarioToken()
            );

            throw new IllegalArgumentException(
                    "El código de seguridad no es válido."
            );
        }

        return registro;
    }

    /**
     * Busca un usuario requerido por una operación
     * de seguridad.
     */
    private Usuario buscarUsuario(
            Integer idUsuario,
            String mensajeError
    ) {

        return usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                mensajeError
                        )
                );
    }

    /**
     * Determina qué parámetro controla la vigencia
     * según el tipo de token.
     */
    private int obtenerDiasVigenciaToken(
            Usuario usuario,
            String tipoToken
    ) {

        int codigoParametro;

        if (TIPO_ACTIVACION.equals(tipoToken)) {

            codigoParametro =
                    CODIGO_PARAMETRO_DIAS_VIGENCIA_TOKEN_ACTIVACION;

        } else if (TIPO_RECUPERACION.equals(tipoToken)) {

            codigoParametro =
                    CODIGO_PARAMETRO_DIAS_VIGENCIA_TOKEN_RECUPERACION;

        } else {

            throw new IllegalArgumentException(
                    "Tipo de token de seguridad no válido."
            );
        }

        return obtenerParametroEnteroPositivo(
                usuario,
                codigoParametro
        );
    }

    /**
     * Obtiene un parámetro entero positivo
     * correspondiente a la agencia principal
     * del usuario.
     */
    private int obtenerParametroEnteroPositivo(
            Usuario usuario,
            int codigoParametro
    ) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        if (usuario.getIdAgenciaPrincipal() == null) {
            throw new IllegalStateException(
                    "El usuario no tiene una agencia principal configurada."
            );
        }

        Parametro parametro =
                parametroService
                        .obtenerPorAgenciaYCodigo(
                                usuario.getIdAgenciaPrincipal(),
                                codigoParametro
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No está configurado el parámetro "
                                                + codigoParametro
                                                + " para la agencia "
                                                + usuario.getIdAgenciaPrincipal()
                                                + "."
                                )
                        );

        if (parametro.getValorParametro() == null) {
            throw new IllegalStateException(
                    "El parámetro "
                            + codigoParametro
                            + " no tiene un valor configurado."
            );
        }

        int valor;

        try {

            valor = parametro
                    .getValorParametro()
                    .intValueExact();

        } catch (ArithmeticException ex) {

            throw new IllegalStateException(
                    "El parámetro "
                            + codigoParametro
                            + " debe contener un valor entero."
            );
        }

        if (valor <= 0) {
            throw new IllegalStateException(
                    "El parámetro "
                            + codigoParametro
                            + " debe ser mayor que cero."
            );
        }

        return valor;
    }

    /**
     * Genera un código numérico entre
     * 100000 y 999999.
     */
    private String generarCodigoSeisDigitos() {

        int numero = LIMITE_INFERIOR_TOKEN
                + secureRandom.nextInt(CANTIDAD_TOKEN);

        return Integer.toString(numero);
    }
}