package co.assip.erp.cartera.cierremensual.causacionintereses;

import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CausacionInteresesService {

    private final CausacionInteresesRepository repository;
    private final CierreMensualRepository cierreRepository;
    private final UsuarioSesionService usuarioSesionService;

    public CausacionInteresesService(
            CausacionInteresesRepository repository,
            CierreMensualRepository cierreRepository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.cierreRepository = cierreRepository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // EJECUTAR CAUSACIÓN DE INTERESES
    //
    // 1. valida cierre
    // 2. valida estado P
    // 3. obtiene usuario de sesión
    // 4. valida que no existan movimientos contabilizados
    // 5. limpia causaciones automáticas previas del cierre
    // 6. genera intereses causados
    // 7. genera intereses contingentes
    // 8. consolida resultados del cierre:
    //
    //    - valor_intereses_causados_mes
    //    - saldo_intereses_causados
    //    - valor_intereses_contingentes_mes
    //    - saldo_intereses_contingentes
    //
    // Todavía NO:
    // - genera comprobante contable
    // =========================================================

    public int ejecutar(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

        // =====================================================
        // 1. RECUPERAR CIERRE
        // =====================================================

        CierreMensualDTO cierre =
                cierreRepository
                        .buscarPorId(
                                idCierreCartera
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el cierre de cartera: "
                                                + idCierreCartera
                                )
                        );

        // =====================================================
        // 2. VALIDAR ESTADO
        // =====================================================

        validarEstadoEnProceso(
                cierre
        );

        // =====================================================
        // 3. USUARIO
        // =====================================================

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // 4. VALIDAR MOVIMIENTOS CONTABILIZADOS
        //
        // Si alguna causación automática del cierre ya tiene
        // comprobante, no permitimos eliminarla y recalcularla.
        // =====================================================

        int movimientosContabilizados =
                repository.contarMovimientosContabilizados(
                        idCierreCartera
                );

        if (movimientosContabilizados > 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " tiene "
                            + movimientosContabilizados
                            + " movimientos de intereses "
                            + "con comprobante contable. "
                            + "No se puede recalcular automáticamente."
            );
        }

        // =====================================================
        // 5. LIMPIAR REEJECUCIÓN DEL CIERRE
        //
        // Solo elimina movimientos automáticos del mismo cierre
        // sin comprobante.
        //
        // NO elimina:
        // - pagos
        // - ajustes
        // - anulaciones
        // - movimientos contabilizados
        // =====================================================

        repository.limpiarCausacionCierre(
                idCierreCartera
        );

        repository.limpiarContingentesCierre(
                idCierreCartera
        );

        // =====================================================
        // 6. INTERESES CAUSADOS
        // =====================================================

        int causados =
                repository.causarIntereses(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 7. INTERESES CONTINGENTES
        // =====================================================

        repository.causarInteresesContingentes(
                idCierreCartera,
                idUsuario
        );

        // =====================================================
        // 8. CONSOLIDAR EN RESULTADOS DEL CIERRE
        // =====================================================

        repository.consolidarResultadosIntereses(
                idCierreCartera,
                idUsuario
        );

        return causados;
    }

    // =========================================================
    // VALIDAR ID
    // =========================================================

    private void validarIdCierre(
            Integer idCierreCartera
    ) {

        if (
                idCierreCartera == null
                        || idCierreCartera <= 0
        ) {

            throw new IllegalArgumentException(
                    "El id del cierre de cartera es obligatorio."
            );
        }
    }

    // =========================================================
    // VALIDAR ESTADO P
    // =========================================================

    private void validarEstadoEnProceso(
            CierreMensualDTO cierre
    ) {

        if (
                cierre.getEstadoCierre() == null
                        || !"P".equalsIgnoreCase(
                        cierre.getEstadoCierre()
                )
        ) {

            throw new IllegalStateException(
                    "El cierre "
                            + cierre.getIdCierreCartera()
                            + " no se encuentra en estado P."
            );
        }
    }
}