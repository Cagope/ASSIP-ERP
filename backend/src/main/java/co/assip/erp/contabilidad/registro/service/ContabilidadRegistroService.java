package co.assip.erp.contabilidad.registro.service;

import co.assip.erp.contabilidad.registro.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.registro.dto.OrigenComprobanteDTO;
import co.assip.erp.contabilidad.registro.repository.*;
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
    private final CatalogoCuentasRepository catalogoCuentasRepo;
    private final ControlesAuxiliaresRepository controlesRepo;

    public ContabilidadRegistroService(
            ConceptosContablesRepository conceptosRepo,
            OrigenComprobantesRepository origenRepo,
            AuxiliaresContablesRepository auxiliaresRepo,
            CatalogoCuentasRepository catalogoCuentasRepo,
            ControlesAuxiliaresRepository controlesRepo
    ) {
        this.conceptosRepo = conceptosRepo;
        this.origenRepo = origenRepo;
        this.auxiliaresRepo = auxiliaresRepo;
        this.catalogoCuentasRepo = catalogoCuentasRepo;
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
        if (idAgencia == null) throw new IllegalArgumentException("idAgencia es obligatorio");
        if (tipoComprobante == null || tipoComprobante.isBlank()) throw new IllegalArgumentException("tipoComprobante es obligatorio");
        if (numeroComprobante == null || numeroComprobante.isBlank()) throw new IllegalArgumentException("numeroComprobante es obligatorio");
        if (conceptoComprobante == null || conceptoComprobante.isBlank()) throw new IllegalArgumentException("conceptoComprobante es obligatorio");

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
            // asegurar llaves
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

            // validaciones mínimas por movimiento
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

            // ✅ Si la cuenta exige control → insertar documento soporte
            boolean requiereControl = catalogoCuentasRepo.requiereControlEntradaSalida(mov.getIdCatalogoCuenta());

            if (requiereControl) {

                String doc = mov.getDocumentoSoporte();

                if (doc == null || doc.isBlank()) {
                    throw new IllegalStateException(
                            "La cuenta " + mov.getIdCatalogoCuenta() +
                                    " exige documento soporte (control_entrada_salida=true)"
                    );
                }

                // ✅ numero_documento = VARCHAR(20)
                if (doc.length() > 20) {
                    throw new IllegalStateException(
                            "El documento soporte supera 20 caracteres. Cuenta=" + mov.getIdCatalogoCuenta()
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
