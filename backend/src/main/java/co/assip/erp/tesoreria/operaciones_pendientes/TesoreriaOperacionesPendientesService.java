package co.assip.erp.tesoreria.operaciones_pendientes;

import co.assip.erp.general.operaciones_financieras.TiposOperacionesFinancierasService;
import co.assip.erp.general.operaciones_financieras.dto.TipoOperacionFinancieraDTO;
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
    private final TiposOperacionesFinancierasService tiposOperacionesFinancierasService;

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
        return publicar(idAgencia, moduloOrigen, procesoOrigen, tablaOrigen,
                idOrigen, idDatosPersonal, naturaleza, fechaDocumento,
                concepto, valorOriginal, idUsuario, null, null);
    }

    /** Publica una obligación con referencias al comprobante y CxP históricos. */
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
            Integer idUsuario,
            Integer idOrigenComprobante,
            Integer idCatalogoCuentaCxp
    ) {
        return publicar(idAgencia, moduloOrigen, procesoOrigen, tablaOrigen,
                idOrigen, idDatosPersonal, naturaleza, fechaDocumento,
                concepto, valorOriginal, idUsuario, idOrigenComprobante,
                idCatalogoCuentaCxp, null);
    }

    /**
     * Publica una obligación identificada por su código financiero.
     * Valida el catálogo y conserva su referencia sin modificar el origen.
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
            Integer idUsuario,
            Integer idOrigenComprobante,
            Integer idCatalogoCuentaCxp,
            String codigoOperacionFinanciera
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

        // Mantiene la publicación anterior y habilita la identificación específica.
        TipoOperacionFinancieraDTO configuracion =
                codigoOperacionFinanciera == null
                        ? tiposOperacionesFinancierasService.validarOperacion(
                        moduloOrigen, procesoOrigen, tablaOrigen,
                        "TESORERIA", naturaleza, false)
                        : tiposOperacionesFinancierasService.validarOperacionPorCodigo(
                        moduloOrigen, procesoOrigen, tablaOrigen,
                        codigoOperacionFinanciera, "TESORERIA", naturaleza, false);

        Long idOperacion = repository.publicar(
                idAgencia,
                moduloOrigen.trim(),
                procesoOrigen.trim(),
                tablaOrigen.trim(),
                idOrigen,
                idDatosPersonal,
                naturalezaNormalizada(naturaleza),
                fechaDocumento,
                concepto.trim(),
                valorOriginal,
                idUsuario,
                idOrigenComprobante,
                idCatalogoCuentaCxp,
                configuracion.getIdTipoOperacion()
        );

        if (idOperacion == null) {
            throw new IllegalStateException(
                    "No fue posible identificar la operación pendiente."
            );
        }

        return idOperacion;
    }


    /**
     * Publicación estricta para reversiones: conserva el control único de origen.
     * Las validaciones de banco y de saldos permanecen en el módulo de Cartera.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long publicarEstricto(
            Integer idAgencia, String moduloOrigen, String procesoOrigen,
            String tablaOrigen, Long idOrigen, Integer idDatosPersonal,
            String naturaleza, LocalDate fechaDocumento, String concepto,
            BigDecimal valorOriginal, Integer idUsuario,
            String codigoOperacionFinanciera, String perfilEsperado,
            boolean exigirSinParciales
    ) {
        validar(idAgencia, moduloOrigen, procesoOrigen, tablaOrigen,
                idOrigen, naturaleza, fechaDocumento, concepto, valorOriginal, idUsuario);

        TipoOperacionFinancieraDTO configuracion = codigoOperacionFinanciera == null
                ? tiposOperacionesFinancierasService.validarOperacion(
                        moduloOrigen, procesoOrigen, tablaOrigen,
                        "TESORERIA", naturaleza, false)
                : tiposOperacionesFinancierasService.validarOperacionPorCodigo(
                        moduloOrigen, procesoOrigen, tablaOrigen,
                        codigoOperacionFinanciera, "TESORERIA", naturaleza, false);

        if (!perfilEsperado.equals(configuracion.getPerfilEjecucion())) {
            throw new IllegalStateException(
                    "El perfil financiero de la devolución no corresponde al esperado.");
        }
        if (exigirSinParciales && Boolean.TRUE.equals(configuracion.getPermiteParciales())) {
            throw new IllegalStateException(
                    "La devolución de prórroga no permite ejecuciones parciales.");
        }

        return repository.publicarEstricto(
                idAgencia, moduloOrigen.trim(), procesoOrigen.trim(),
                tablaOrigen.trim(), idOrigen, idDatosPersonal,
                naturalezaNormalizada(naturaleza), fechaDocumento,
                concepto.trim(), valorOriginal, idUsuario,
                configuracion.getIdTipoOperacion());
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
    /** Consulta cuentas de ingreso para la agencia previamente autorizada. */
    public List<Map<String, Object>> listarCuentasParaRecaudo(Integer idAgencia) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException("La agencia es obligatoria.");
        }
        return repository.listarCuentasParaRecaudo(idAgencia);
    }

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