package co.assip.erp.cartera.cierremensual.anexo1;

import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class Anexo1Service {

    private final Anexo1Repository repository;
    private final CierreMensualRepository cierreRepository;

    // =========================================================
    // SOLO EDAD CONTABLE
    //
    // Requiere:
    // - cierre existente
    // - cierre en estado C
    // =========================================================

    @Transactional
    public int calcularEdadContable(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        validarParametros(
                idCierreCartera,
                idUsuario
        );

        validarCierreCerrado(
                idCierreCartera
        );

        return repository.calcularEdadContablePorArrastre(
                idCierreCartera,
                idUsuario
        );
    }


    // =========================================================
    // PROCESAR ANEXO 1 COMPLETO
    //
    // IMPORTANTE:
    //
    // - solamente sobre cierre C
    // - procesa todos los créditos de resultados
    // - deja inicialmente codigo_metodo_calculo = A1
    // - posteriormente Anexo 2 recalcula su población PE
    // =========================================================

    @Transactional
    public ResultadoAnexo1 procesarAnexo1(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        validarParametros(
                idCierreCartera,
                idUsuario
        );

        validarCierreCerrado(
                idCierreCartera
        );

        // =====================================================
        // 1. EDAD CONTABLE / ALINEAMIENTO
        // =====================================================

        int edades =
                repository.calcularEdadContablePorArrastre(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 2. DETERIORO DE CAPITAL
        // =====================================================

        int capital =
                repository.calcularDeterioroCapital(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 3. DETERIORO DE INTERESES
        // =====================================================

        int intereses =
                repository.calcularDeterioroIntereses(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 4. RESULTADO
        // =====================================================

        return new ResultadoAnexo1(
                edades,
                capital,
                intereses
        );
    }


    // =========================================================
    // VALIDAR CIERRE CERRADO
    //
    // C = fotografía cerrada en firme.
    //
    // Anexo 1 solamente puede ejecutarse después de cerrar
    // definitivamente la fotografía del cierre.
    // =========================================================

    private void validarCierreCerrado(
            Integer idCierreCartera
    ) {

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

        String estado =
                cierre.getEstadoCierre();

        if (estado == null
                || !"C".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " debe estar cerrado en firme "
                            + "para ejecutar el Anexo 1."
            );
        }
    }


    // =========================================================
    // VALIDAR PARÁMETROS
    // =========================================================

    private void validarParametros(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        if (idCierreCartera == null
                || idCierreCartera <= 0) {

            throw new IllegalArgumentException(
                    "El id del cierre de cartera es obligatorio."
            );
        }

        if (idUsuario == null
                || idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El id del usuario es obligatorio."
            );
        }
    }


    // =========================================================
    // RESULTADO
    // =========================================================

    public record ResultadoAnexo1(
            int edadesContables,
            int deteriorosCapital,
            int deteriorosIntereses
    ) {
    }
}