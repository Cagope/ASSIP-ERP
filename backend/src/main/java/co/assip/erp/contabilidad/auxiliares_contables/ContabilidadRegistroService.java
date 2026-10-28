
package co.assip.erp.contabilidad.auxiliares_contables;

import co.assip.erp.contabilidad.conceptos_contables.ConceptosContablesRepository;
import co.assip.erp.contabilidad.origen_comprobantes.OrigenComprobantesRepository;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuentaRepository;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio global de registro contable.
 *
 * Registra cabecera, origen, auxiliares y controles documentales.
 * Conserva el registro convencional y la creación estricta.
 *
 * La modalidad manual admite controles tipificados sin modificar
 * el contrato contable utilizado por los demás módulos del ERP.
 */
@Service
public class ContabilidadRegistroService {

    private final ConceptosContablesRepository conceptosRepo;
    private final OrigenComprobantesRepository origenRepo;
    private final AuxiliaresContablesRepository auxiliaresRepo;
    private final PlanCuentaRepository planCuentaRepository;
    private final ControlesAuxiliaresRepository controlesRepo;

    public ContabilidadRegistroService(
            ConceptosContablesRepository conceptosRepo,
            OrigenComprobantesRepository origenRepo,
            AuxiliaresContablesRepository auxiliaresRepo,
            PlanCuentaRepository planCuentaRepository,
            ControlesAuxiliaresRepository controlesRepo
    ) {
        this.conceptosRepo = conceptosRepo;
        this.origenRepo = origenRepo;
        this.auxiliaresRepo = auxiliaresRepo;
        this.planCuentaRepository = planCuentaRepository;
        this.controlesRepo = controlesRepo;
    }

    /**
     * Asocia un movimiento contable con su tipo de control.
     *
     * El documento soporte permanece dentro de MovimientoContableDTO.
     * No se permite una asociación independiente por posición.
     */
    public record MovimientoManual(
            MovimientoContableDTO movimiento,
            Integer idTiposControl
    ) {
    }

    /**
     * Registra comprobantes mediante el comportamiento existente.
     * Conserva la compatibilidad con los módulos automáticos del ERP.
     */
    @Transactional
    public void registrarComprobante(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String conceptoComprobante,
            OrigenComprobanteDTO origen,
            List<MovimientoContableDTO> movimientos,
            Integer idUsuario
    ) {
        registrarInternamente(
                idAgencia,
                tipoComprobante,
                numeroComprobante,
                conceptoComprobante,
                origen,
                movimientos,
                idUsuario,
                false
        );
    }

    /**
     * Registra exclusivamente un comprobante nuevo.
     * Rechaza una cabecera existente sin modificarla.
     */
    @Transactional
    public void registrarComprobanteNuevo(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String conceptoComprobante,
            OrigenComprobanteDTO origen,
            List<MovimientoContableDTO> movimientos,
            Integer idUsuario
    ) {
        registrarInternamente(
                idAgencia,
                tipoComprobante,
                numeroComprobante,
                conceptoComprobante,
                origen,
                movimientos,
                idUsuario,
                true
        );
    }

    /**
     * Registra un comprobante manual nuevo con controles tipificados.
     *
     * Cada movimiento conserva su propio tipo de control.
     * La cabecera, los auxiliares y sus controles se registran
     * dentro de una única transacción.
     */
    @Transactional
    public void registrarComprobanteManualNuevo(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String conceptoComprobante,
            OrigenComprobanteDTO origen,
            List<MovimientoManual> movimientosManuales,
            Integer idUsuario
    ) {

        if (movimientosManuales == null
                || movimientosManuales.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe existir al menos un movimiento contable."
            );
        }

        for (MovimientoManual item : movimientosManuales) {
            if (item == null || item.movimiento() == null) {
                throw new IllegalArgumentException(
                        "El comprobante contiene un movimiento vacío."
                );
            }
        }

        List<MovimientoContableDTO> movimientos =
                movimientosManuales.stream()
                        .map(MovimientoManual::movimiento)
                        .toList();

        registrarInternamente(
                idAgencia,
                tipoComprobante,
                numeroComprobante,
                conceptoComprobante,
                origen,
                movimientos,
                idUsuario,
                true,
                movimientosManuales
        );
    }

    /**
     * Ejecuta el registro contable convencional.
     *
     * Mantiene el comportamiento original de los procesos
     * automáticos y de la creación estricta existente.
     */
    private void registrarInternamente(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String conceptoComprobante,
            OrigenComprobanteDTO origen,
            List<MovimientoContableDTO> movimientos,
            Integer idUsuario,
            boolean creacionEstricta
    ) {
        registrarInternamente(
                idAgencia,
                tipoComprobante,
                numeroComprobante,
                conceptoComprobante,
                origen,
                movimientos,
                idUsuario,
                creacionEstricta,
                null
        );
    }

    /**
     * Ejecuta el registro contable completo.
     *
     * Si movimientosManuales es null, conserva el registro
     * documental anterior.
     *
     * En modalidad manual registra el tipo de control
     * asociado al auxiliar que acaba de insertarse.
     */
    private void registrarInternamente(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String conceptoComprobante,
            OrigenComprobanteDTO origen,
            List<MovimientoContableDTO> movimientos,
            Integer idUsuario,
            boolean creacionEstricta,
            List<MovimientoManual> movimientosManuales
    ) {

        // 1. Validaciones generales.

        if (idAgencia == null) {
            throw new IllegalArgumentException(
                    "idAgencia es obligatorio"
            );
        }

        if (tipoComprobante == null || tipoComprobante.isBlank()) {
            throw new IllegalArgumentException(
                    "tipoComprobante es obligatorio"
            );
        }

        if (numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new IllegalArgumentException(
                    "numeroComprobante es obligatorio"
            );
        }

        if (conceptoComprobante == null
                || conceptoComprobante.isBlank()) {
            throw new IllegalArgumentException(
                    "conceptoComprobante es obligatorio"
            );
        }

        if (movimientos == null || movimientos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe existir al menos 1 movimiento contable"
            );
        }

        // 2. Validar equilibrio contable.

        BigDecimal totalDebito = movimientos.stream()
                .map(m -> m.getValorDebito() == null
                        ? BigDecimal.ZERO
                        : m.getValorDebito())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredito = movimientos.stream()
                .map(m -> m.getValorCredito() == null
                        ? BigDecimal.ZERO
                        : m.getValorCredito())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebito.compareTo(totalCredito) != 0) {
            throw new IllegalStateException(
                    "Comprobante descuadrado. Débito="
                            + totalDebito
                            + " Crédito="
                            + totalCredito
            );
        }

        // 3. Registrar cabecera.

        if (creacionEstricta) {
            conceptosRepo.insertarConceptoNuevo(
                    idAgencia,
                    tipoComprobante,
                    numeroComprobante,
                    conceptoComprobante,
                    idUsuario
            );
        } else {
            conceptosRepo.insertarConcepto(
                    idAgencia,
                    tipoComprobante,
                    numeroComprobante,
                    conceptoComprobante,
                    idUsuario
            );
        }

        // 4. Registrar origen.

        if (origen != null) {

            origen.setIdAgencia(idAgencia);
            origen.setTipoComprobante(tipoComprobante);
            origen.setNumeroComprobante(numeroComprobante);

            origenRepo.insertarOrigen(origen, idUsuario);
        }

        // 5. Registrar auxiliares y controles.

        int indiceMovimiento = 0;

        for (MovimientoContableDTO mov : movimientos) {

            mov.setIdAgencia(idAgencia);
            mov.setTipoComprobante(tipoComprobante);
            mov.setNumeroComprobante(numeroComprobante);

            if (origen != null) {
                mov.setOrigenModulo(origen.getModuloOrigen());
                mov.setDocumentoOrigen(origen.getProcesoOrigen());
            } else {
                mov.setOrigenModulo("CONTABILIDAD");
                mov.setDocumentoOrigen(
                        tipoComprobante + "-" + numeroComprobante
                );
            }

            if (mov.getIdCatalogoCuenta() == null) {
                throw new IllegalArgumentException(
                        "Movimiento sin idCatalogoCuenta"
                );
            }

            if (mov.getFechaAuxiliar() == null) {
                throw new IllegalArgumentException(
                        "Movimiento sin fechaAuxiliar"
                );
            }

            if (mov.getDetalleMovimiento() == null
                    || mov.getDetalleMovimiento().isBlank()) {
                throw new IllegalArgumentException(
                        "Movimiento sin detalleMovimiento"
                );
            }

            if (mov.getIdDatosPersonal() == null) {
                throw new IllegalArgumentException(
                        "Movimiento sin idDatosPersonal (tercero contable)"
                );
            }

            Integer idAuxiliar = auxiliaresRepo.insertarAuxiliar(
                    mov,
                    idUsuario
            );

            Boolean requiereControl =
                    planCuentaRepository.requiereControlEntradaSalida(
                            mov.getIdCatalogoCuenta()
                    );

            if (movimientosManuales == null) {

                // Comportamiento original de los demás módulos.

                if (Boolean.TRUE.equals(requiereControl)) {

                    String doc = mov.getDocumentoSoporte();

                    if (doc == null || doc.isBlank()) {
                        throw new IllegalStateException(
                                "La cuenta "
                                        + mov.getIdCatalogoCuenta()
                                        + " exige documento soporte "
                                        + "(control_entrada_salida=true)"
                        );
                    }

                    if (doc.length() > 20) {
                        throw new IllegalStateException(
                                "El documento soporte supera 20 caracteres. Cuenta="
                                        + mov.getIdCatalogoCuenta()
                        );
                    }

                    controlesRepo.insertarDocumentoSoporte(
                            idAuxiliar,
                            doc,
                            idUsuario
                    );
                }

            } else {

                // Modalidad específica de comprobantes manuales.

                MovimientoManual item =
                        movimientosManuales.get(indiceMovimiento);

                Integer idTiposControl = item.idTiposControl();

                if (Boolean.TRUE.equals(requiereControl)) {

                    controlesRepo.insertarControlComprobanteManual(
                            idAuxiliar,
                            idTiposControl,
                            mov.getDocumentoSoporte(),
                            idUsuario
                    );

                } else if (idTiposControl != null) {

                    throw new IllegalArgumentException(
                            "La cuenta "
                                    + mov.getIdCatalogoCuenta()
                                    + " no requiere control documental."
                    );
                }
            }

            indiceMovimiento++;
        }
    }
}
