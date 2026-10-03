package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.general.parametro.Parametro;
import co.assip.erp.general.parametro.ParametroService;
import co.assip.erp.seguridad.usuarios.Usuario;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Gestiona los intentos fallidos de códigos de seguridad.
 *
 * Aplica a:
 * - activación inicial de cuentas;
 * - recuperación de contraseñas.
 *
 * Los cambios se confirman mediante una transacción
 * independiente para evitar que una excepción
 * del proceso principal revierta:
 *
 * - el incremento de intentos;
 * - la desactivación por vencimiento;
 * - la desactivación por máximo de intentos.
 *
 * No genera códigos, no valida contraseñas
 * y no consume códigos utilizados correctamente.
 */
@Service
@RequiredArgsConstructor
public class UsuarioTokenIntentosService {

    /*
     * Código del parámetro que define el máximo
     * de intentos permitidos para un token.
     */
    private static final int CODIGO_PARAMETRO_MAXIMO_INTENTOS_TOKEN = 704;

    private final UsuarioTokenRepository usuarioTokenRepository;
    private final ParametroService parametroService;

    /**
     * Registra un intento fallido sobre un código.
     *
     * El registro se obtiene nuevamente dentro de
     * una transacción independiente.
     *
     * Si alcanza el máximo permitido, el código
     * queda desactivado.
     *
     * @param idUsuarioToken identificador del registro
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void registrarIntentoFallido(
            Long idUsuarioToken
    ) {

        if (idUsuarioToken == null) {
            throw new IllegalArgumentException(
                    "El identificador del código es obligatorio."
            );
        }

        UsuarioToken registro = usuarioTokenRepository
                .findByIdForUpdate(idUsuarioToken)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Código de seguridad no encontrado."
                        )
                );

        /*
         * No se incrementan intentos sobre códigos
         * utilizados o previamente desactivados.
         */
        if (!Boolean.TRUE.equals(registro.getActivo())
                || Boolean.TRUE.equals(registro.getUtilizado())) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();

        /*
         * Un código vencido se desactiva
         * sin incrementar intentos.
         */
        if (registro.getFechaExpiracion() == null
                || !registro.getFechaExpiracion().isAfter(ahora)) {

            registro.setActivo(false);
            registro.setFechaEdicion(ahora);

            usuarioTokenRepository.save(registro);
            return;
        }

        /*
         * El máximo de intentos se obtiene del parámetro
         * 704 correspondiente a la agencia principal
         * del usuario propietario del token.
         */
        int maximoIntentos =
                obtenerMaximoIntentosToken(
                        registro
                );

        int intentosActuales =
                registro.getIntentos() == null
                        ? 0
                        : registro.getIntentos();

        int nuevosIntentos =
                intentosActuales + 1;

        registro.setIntentos(
                nuevosIntentos
        );

        registro.setFechaEdicion(
                ahora
        );

        if (nuevosIntentos >= maximoIntentos) {
            registro.setActivo(false);
        }

        usuarioTokenRepository.save(
                registro
        );
    }

    /**
     * Desactiva un código de seguridad vencido.
     *
     * Conserva el registro histórico y no
     * modifica su número de intentos.
     *
     * @param idUsuarioToken identificador del registro
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void desactivarTokenVencido(
            Long idUsuarioToken
    ) {

        if (idUsuarioToken == null) {
            throw new IllegalArgumentException(
                    "El identificador del código es obligatorio."
            );
        }

        UsuarioToken registro = usuarioTokenRepository
                .findByIdForUpdate(idUsuarioToken)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Código de seguridad no encontrado."
                        )
                );

        if (!Boolean.TRUE.equals(registro.getActivo())
                || Boolean.TRUE.equals(registro.getUtilizado())) {
            return;
        }

        LocalDateTime ahora =
                LocalDateTime.now();

        /*
         * Solamente se desactiva cuando
         * realmente ha vencido.
         */
        if (registro.getFechaExpiracion() != null
                && registro.getFechaExpiracion().isAfter(ahora)) {
            return;
        }

        registro.setActivo(false);
        registro.setFechaEdicion(ahora);

        usuarioTokenRepository.save(
                registro
        );
    }

    /**
     * Obtiene el máximo de intentos permitido
     * para el token desde el parámetro 704.
     */
    private int obtenerMaximoIntentosToken(
            UsuarioToken registro
    ) {

        if (registro.getUsuario() == null) {
            throw new IllegalStateException(
                    "El token de seguridad no tiene un usuario asociado."
            );
        }

        Usuario usuario =
                registro.getUsuario();

        if (usuario.getIdAgenciaPrincipal() == null) {
            throw new IllegalStateException(
                    "El usuario no tiene una agencia principal configurada."
            );
        }

        Parametro parametro =
                parametroService
                        .obtenerPorAgenciaYCodigo(
                                usuario.getIdAgenciaPrincipal(),
                                CODIGO_PARAMETRO_MAXIMO_INTENTOS_TOKEN
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No está configurado el parámetro "
                                                + CODIGO_PARAMETRO_MAXIMO_INTENTOS_TOKEN
                                                + " para la agencia "
                                                + usuario.getIdAgenciaPrincipal()
                                                + "."
                                )
                        );

        if (parametro.getValorParametro() == null) {
            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_MAXIMO_INTENTOS_TOKEN
                            + " no tiene un valor configurado."
            );
        }

        int maximoIntentos;

        try {

            maximoIntentos =
                    parametro
                            .getValorParametro()
                            .intValueExact();

        } catch (ArithmeticException ex) {

            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_MAXIMO_INTENTOS_TOKEN
                            + " debe contener un valor entero."
            );
        }

        if (maximoIntentos <= 0) {
            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_MAXIMO_INTENTOS_TOKEN
                            + " debe ser mayor que cero."
            );
        }

        return maximoIntentos;
    }
}