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
 * Servicio GLOBAL para registrar comprobantes contables en:
 * ✅ conceptos_contables (cabecera lógica)
 * ✅ origen_comprobantes (trazabilidad)
 * ✅ auxiliares_contables (movimientos)
 * ✅ controles_auxiliares (solo si la cuenta lo exige)
 *
 * Reutilizable por TODO el proyecto.
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

    // =========================================================
    // ✅ REGISTRO COMPLETO DEL COMPROBANTE (GLOBAL)
    // =========================================================
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

        // =========================
        // 1) Validaciones mínimas
        // =========================
        if (idAgencia == null) {
            throw new IllegalArgumentException("idAgencia es obligatorio");
        }

        if (tipoComprobante == null || tipoComprobante.isBlank()) {
            throw new IllegalArgumentException("tipoComprobante es obligatorio");
        }

        if (numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new IllegalArgumentException("numeroComprobante es obligatorio");
        }

        if (conceptoComprobante == null || conceptoComprobante.isBlank()) {
            throw new IllegalArgumentException("conceptoComprobante es obligatorio");
        }

        if (movimientos == null || movimientos.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos 1 movimiento contable");
        }

        // =========================
        // 2) Validar cuadres
        // =========================
        BigDecimal totalDebito = movimientos.stream()
                .map(m -> m.getValorDebito() == null ? BigDecimal.ZERO : m.getValorDebito())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredito = movimientos.stream()
                .map(m -> m.getValorCredito() == null ? BigDecimal.ZERO : m.getValorCredito())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebito.compareTo(totalCredito) != 0) {
            throw new IllegalStateException(
                    "Comprobante descuadrado. Débito=" + totalDebito + " Crédito=" + totalCredito
            );
        }

        // =========================
        // 3) Insertar concepto contable
        // =========================
        conceptosRepo.insertarConcepto(
                idAgencia,
                tipoComprobante,
                numeroComprobante,
                conceptoComprobante,
                idUsuario
        );

        // =========================
        // 4) Insertar origen del comprobante
        // =========================
        if (origen != null) {

            origen.setIdAgencia(idAgencia);
            origen.setTipoComprobante(tipoComprobante);
            origen.setNumeroComprobante(numeroComprobante);

            origenRepo.insertarOrigen(origen, idUsuario);
        }

        // =========================
        // 5) Insertar auxiliares (y controles si aplica)
        // =========================
        for (MovimientoContableDTO mov : movimientos) {

            // completar llaves si vienen vacías
            mov.setIdAgencia(idAgencia);
            mov.setTipoComprobante(tipoComprobante);
            mov.setNumeroComprobante(numeroComprobante);

            // =========================
            // NUEVO: Trazabilidad ERP
            // =========================
            if (origen != null) {

                mov.setOrigenModulo(origen.getModuloOrigen());
                mov.setDocumentoOrigen(origen.getProcesoOrigen());

            } else {

                // fallback seguro
                mov.setOrigenModulo("CONTABILIDAD");
                mov.setDocumentoOrigen(tipoComprobante + "-" + numeroComprobante);
            }

            // =========================
            // Validaciones mínimas
            // =========================
            if (mov.getIdCatalogoCuenta() == null) {
                throw new IllegalArgumentException("Movimiento sin idCatalogoCuenta");
            }

            if (mov.getFechaAuxiliar() == null) {
                throw new IllegalArgumentException("Movimiento sin fechaAuxiliar");
            }

            if (mov.getDetalleMovimiento() == null || mov.getDetalleMovimiento().isBlank()) {
                throw new IllegalArgumentException("Movimiento sin detalleMovimiento");
            }

            if (mov.getIdDatosPersonal() == null) {
                throw new IllegalArgumentException("Movimiento sin idDatosPersonal (tercero contable)");
            }

            Integer idAuxiliar = auxiliaresRepo.insertarAuxiliar(mov, idUsuario);

            // =========================
            // Control de documento soporte
            // =========================
            Boolean requiereControl =
                    planCuentaRepository.requiereControlEntradaSalida(mov.getIdCatalogoCuenta());

            if (Boolean.TRUE.equals(requiereControl)) {

                String doc = mov.getDocumentoSoporte();

                if (doc == null || doc.isBlank()) {
                    throw new IllegalStateException(
                            "La cuenta " + mov.getIdCatalogoCuenta() +
                                    " exige documento soporte (control_entrada_salida=true)"
                    );
                }

                if (doc.length() > 20) {
                    throw new IllegalStateException(
                            "El documento soporte supera 20 caracteres. Cuenta=" +
                                    mov.getIdCatalogoCuenta()
                    );
                }

                controlesRepo.insertarDocumentoSoporte(
                        idAuxiliar,
                        doc,
                        idUsuario
                );
            }
        }
    }
}