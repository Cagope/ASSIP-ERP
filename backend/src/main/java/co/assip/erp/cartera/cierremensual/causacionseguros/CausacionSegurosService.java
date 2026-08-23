package co.assip.erp.cartera.cierremensual.causacionseguros;

import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CausacionSegurosService {

    private final CausacionSegurosRepository repository;
    private final CierreMensualRepository cierreRepository;
    private final UsuarioSesionService usuarioSesionService;

    public CausacionSegurosService(
            CausacionSegurosRepository repository,
            CierreMensualRepository cierreRepository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.cierreRepository = cierreRepository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // EJECUTAR CAUSACION DE SEGUROS
    //
    // 1. valida id del cierre
    // 2. recupera el cierre
    // 3. valida estado C
    // 4. obtiene usuario de sesión
    // 5. valida seguros duplicados
    // 6. valida causaciones ya contabilizadas
    // 7. elimina causación automática anterior no contabilizada
    // 8. genera causación de seguros
    // 9. consolida valor del mes y saldo acumulado
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

        validarEstadoCerrado(
                cierre
        );

        // =====================================================
        // 3. USUARIO
        // =====================================================

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // 4. VALIDAR MAS DE UN SEGURO ACTIVO POR CREDITO
        // =====================================================

        int duplicados =
                repository
                        .contarCreditosConMultiplesSegurosActivos(
                                idCierreCartera
                        );

        if (duplicados > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + duplicados
                            + " créditos con más de un seguro activo "
                            + "aplicable al cierre."
            );
        }

        // =====================================================
        // 5. VALIDAR MOVIMIENTOS CONTABILIZADOS
        // =====================================================

        int contabilizados =
                repository
                        .contarMovimientosContabilizados(
                                idCierreCartera
                        );

        if (contabilizados > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + contabilizados
                            + " causaciones de seguros del cierre "
                            + "que ya tienen comprobante. "
                            + "No se puede recalcular."
            );
        }

        // =====================================================
        // 6. LIMPIAR CAUSACION AUTOMATICA ANTERIOR
        // =====================================================

        repository
                .limpiarCausacionNoContabilizada(
                        idCierreCartera
                );

        // =====================================================
        // 7. GENERAR CAUSACION
        // =====================================================

        int cantidad =
                repository
                        .causarSeguros(
                                idCierreCartera,
                                idUsuario
                        );

        // =====================================================
        // 8. CONSOLIDAR RESULTADOS DEL CIERRE
        //
        // valor_seguros_mes
        // saldo_seguros
        // =====================================================

        repository
                .consolidarResultadosSeguros(
                        idCierreCartera,
                        idUsuario
                );

        return cantidad;
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
// VALIDAR CIERRE CERRADO
//
// C = fotografía cerrada en firme.
// Solamente desde este estado se permiten los cálculos
// definitivos del cierre.
// =========================================================

    private void validarEstadoCerrado(
            CierreMensualDTO cierre
    ) {

        if (
                cierre.getEstadoCierre() == null
                        || !"C".equalsIgnoreCase(
                        cierre.getEstadoCierre().trim()
                )
        ) {

            throw new IllegalStateException(
                    "El cierre "
                            + cierre.getIdCierreCartera()
                            + " debe estar cerrado en firme "
                            + "para ejecutar la causación de seguros."
            );
        }
    }
}