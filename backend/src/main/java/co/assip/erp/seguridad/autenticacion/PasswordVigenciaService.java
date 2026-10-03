package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.general.parametro.Parametro;
import co.assip.erp.general.parametro.ParametroService;
import co.assip.erp.seguridad.usuarios.Usuario;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Evalúa la vigencia de la contraseña de un usuario.
 *
 * La evaluación se realiza por fecha calendario y no
 * por hora exacta.
 *
 * Ejemplo:
 *
 * Si passwordExpira corresponde al 2026-10-05,
 * la contraseña continúa vigente durante todo ese día
 * y se considera caducada a partir del 2026-10-06.
 *
 * Este servicio no autentica usuarios, no modifica
 * contraseñas, no genera tokens y no modifica el
 * estado de las cuentas.
 */
@Service
@RequiredArgsConstructor
public class PasswordVigenciaService {

    /*
     * Código del parámetro que define cuántos días
     * antes del vencimiento debe mostrarse el aviso.
     */
    private static final int CODIGO_PARAMETRO_DIAS_AVISO_VENCIMIENTO_PASSWORD = 701;

    private final ParametroService parametroService;

    /**
     * Estados posibles de vigencia de una contraseña.
     */
    public enum EstadoVigenciaPassword {

        VIGENTE,
        PROXIMA_VENCER,
        VENCE_HOY,
        CADUCADA
    }

    /**
     * Resultado de la evaluación de vigencia.
     */
    @Getter
    @RequiredArgsConstructor
    public static class ResultadoVigenciaPassword {

        private final EstadoVigenciaPassword estado;
        private final long diasRestantes;
        private final LocalDate fechaVencimiento;
    }

    /**
     * Evalúa la vigencia de la contraseña del usuario
     * utilizando la fecha de expiración almacenada
     * y el parámetro 701 de su agencia principal.
     */
    public ResultadoVigenciaPassword evaluar(
            Usuario usuario
    ) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        if (usuario.getPasswordExpira() == null) {
            throw new IllegalStateException(
                    "El usuario no tiene fecha de vencimiento de contraseña."
            );
        }

        LocalDate fechaActual =
                LocalDate.now();

        LocalDate fechaVencimiento =
                usuario
                        .getPasswordExpira()
                        .toLocalDate();

        long diasRestantes =
                ChronoUnit.DAYS.between(
                        fechaActual,
                        fechaVencimiento
                );

        /*
         * Una fecha anterior a hoy significa que
         * la contraseña ya se encuentra caducada.
         */
        if (diasRestantes < 0) {

            return new ResultadoVigenciaPassword(
                    EstadoVigenciaPassword.CADUCADA,
                    diasRestantes,
                    fechaVencimiento
            );
        }

        /*
         * La contraseña continúa siendo válida durante
         * todo el día de su fecha de vencimiento.
         */
        if (diasRestantes == 0) {

            return new ResultadoVigenciaPassword(
                    EstadoVigenciaPassword.VENCE_HOY,
                    0,
                    fechaVencimiento
            );
        }

        int diasAviso =
                obtenerDiasAvisoVencimiento(
                        usuario
                );

        /*
         * Si se encuentra dentro de la ventana definida
         * por el parámetro 701, continúa siendo válida
         * pero debe informarse al usuario.
         */
        if (diasRestantes <= diasAviso) {

            return new ResultadoVigenciaPassword(
                    EstadoVigenciaPassword.PROXIMA_VENCER,
                    diasRestantes,
                    fechaVencimiento
            );
        }

        return new ResultadoVigenciaPassword(
                EstadoVigenciaPassword.VIGENTE,
                diasRestantes,
                fechaVencimiento
        );
    }

    /**
     * Obtiene el número de días de anticipación
     * configurado para el aviso de vencimiento.
     */
    private int obtenerDiasAvisoVencimiento(
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
                                CODIGO_PARAMETRO_DIAS_AVISO_VENCIMIENTO_PASSWORD
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No está configurado el parámetro "
                                                + CODIGO_PARAMETRO_DIAS_AVISO_VENCIMIENTO_PASSWORD
                                                + " para la agencia "
                                                + usuario.getIdAgenciaPrincipal()
                                                + "."
                                )
                        );

        if (parametro.getValorParametro() == null) {
            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_DIAS_AVISO_VENCIMIENTO_PASSWORD
                            + " no tiene un valor configurado."
            );
        }

        int diasAviso;

        try {

            diasAviso =
                    parametro
                            .getValorParametro()
                            .intValueExact();

        } catch (ArithmeticException ex) {

            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_DIAS_AVISO_VENCIMIENTO_PASSWORD
                            + " debe contener un número entero de días."
            );
        }

        if (diasAviso < 0) {
            throw new IllegalStateException(
                    "El parámetro "
                            + CODIGO_PARAMETRO_DIAS_AVISO_VENCIMIENTO_PASSWORD
                            + " no puede ser negativo."
            );
        }

        return diasAviso;
    }
}