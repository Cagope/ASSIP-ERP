package co.assip.erp.tesoreria.operaciones_pendientes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Gestiona la publicación de operaciones financieras que otros
 * módulos dejan pendientes para su atención en Tesorería.
 *
 * No realiza cálculos financieros ni modifica los productos de origen.
 */
@Service
@RequiredArgsConstructor
public class TesoreriaOperacionesPendientesService {

    private final TesoreriaOperacionesPendientesRepository repository;

    /**
     * Publica una operación pendiente dentro de la transacción
     * del módulo de origen.
     *
     * Si ya existe la referencia, verifica que sus datos coincidan
     * para evitar reutilizarla con una obligación diferente.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long publicar(
            Integer idAgencia,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            Integer idDatosPersonal,
            String naturaleza,
            LocalDate fechaDocumento,
            String concepto,
            BigDecimal valorOriginal,
            Integer idUsuario
    ) {
        validar(
                idAgencia,
                moduloOrigen,
                procesoOrigen,
                tablaOrigen,
                idOrigen,
                naturaleza,
                fechaDocumento,
                concepto,
                valorOriginal,
                idUsuario
        );

        Long idOperacion = repository.publicar(
                idAgencia,
                moduloOrigen.trim(),
                procesoOrigen.trim(),
                tablaOrigen.trim(),
                idOrigen,
                idDatosPersonal,
                naturaleza.trim().toUpperCase(),
                fechaDocumento,
                concepto.trim(),
                valorOriginal,
                idUsuario
        );

        if (idOperacion == null) {
            throw new IllegalStateException(
                    "No fue posible identificar la operación pendiente."
            );
        }

        return idOperacion;
    }

    /**
     * Consulta las operaciones pendientes o parcialmente ejecutadas
     * de una agencia.
     *
     * El controlador deberá aplicar los permisos y el alcance
     * de agencias del usuario autenticado.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarDisponibles(
            Integer idAgencia
    ) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }

        return repository.listarDisponibles(idAgencia);
    }


    /**
     * Consulta las cuentas bancarias habilitadas para
     * realizar pagos desde una agencia.
     *
     * El repositorio filtra las cuentas activas,
     * autorizadas para pagar y asociadas a la agencia.
     *
     * El controlador debe validar los permisos y
     * el acceso del usuario a la agencia solicitada.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarCuentasParaPago(
            Integer idAgencia
    ) {

        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }

        return repository.listarCuentasParaPago(idAgencia);
    }


    /**
     * Valida los datos mínimos requeridos para publicar una
     * operación financiera pendiente.
     */
    private void validar(
            Integer idAgencia,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            String naturaleza,
            LocalDate fechaDocumento,
            String concepto,
            BigDecimal valorOriginal,
            Integer idUsuario
    ) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }

        if (idOrigen == null || idOrigen <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de origen es obligatorio."
            );
        }

        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El usuario de creación es obligatorio."
            );
        }

        if (esVacio(moduloOrigen)
                || esVacio(procesoOrigen)
                || esVacio(tablaOrigen)) {
            throw new IllegalArgumentException(
                    "La referencia del módulo de origen está incompleta."
            );
        }

        if (moduloOrigen.trim().length() > 30
                || procesoOrigen.trim().length() > 60
                || tablaOrigen.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "La referencia de origen supera la longitud permitida."
            );
        }

        if (fechaDocumento == null) {
            throw new IllegalArgumentException(
                    "La fecha del documento es obligatoria."
            );
        }

        if (esVacio(concepto) || concepto.trim().length() > 500) {
            throw new IllegalArgumentException(
                    "El concepto es obligatorio y no puede superar 500 caracteres."
            );
        }

        if (!Objects.equals(naturalezaNormalizada(naturaleza), "ENTRADA")
                && !Objects.equals(naturalezaNormalizada(naturaleza), "SALIDA")) {
            throw new IllegalArgumentException(
                    "La naturaleza debe ser ENTRADA o SALIDA."
            );
        }

        if (valorOriginal == null
                || valorOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El valor de la operación debe ser mayor que cero."
            );
        }

        if (valorOriginal.scale() > 2) {
            throw new IllegalArgumentException(
                    "El valor de la operación no puede superar dos decimales."
            );
        }
    }

    /** Normaliza la naturaleza para las validaciones y el registro. */
    private String naturalezaNormalizada(String naturaleza) {
        return naturaleza == null
                ? ""
                : naturaleza.trim().toUpperCase();
    }

    /** Identifica cadenas nulas o sin contenido. */
    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}