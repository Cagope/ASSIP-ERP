package co.assip.erp.cartera.cierremensual.alivios;

import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsolidacionAliviosService {

    private final ConsolidacionAliviosRepository repository;
    private final CierreMensualRepository cierreRepository;
    private final UsuarioSesionService usuarioSesionService;

    public ConsolidacionAliviosService(
            ConsolidacionAliviosRepository repository,
            CierreMensualRepository cierreRepository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.cierreRepository = cierreRepository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // CONSOLIDAR ALIVIOS DEL CIERRE
    //
    // 1. valida id del cierre
    // 2. recupera cierre
    // 3. valida estado C
    // 4. obtiene usuario de sesión
    // 5. consolida:
    //      - valor_alivios_mes
    //      - saldo_alivios
    //
    // No genera movimientos nuevos.
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
        // 4. CONSOLIDAR ALIVIOS
        // =====================================================

        return repository
                .consolidarResultadosAlivios(
                        idCierreCartera,
                        idUsuario
                );
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
                            + "para consolidar los alivios."
            );
        }
    }
}