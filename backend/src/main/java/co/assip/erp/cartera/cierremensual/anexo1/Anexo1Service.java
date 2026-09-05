package co.assip.erp.cartera.cierremensual.anexo1;

import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.anexo1.dto.DetalleAnexo1DTO;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.assip.erp.cartera.cierremensual.anexo1.dto.ResumenAnexo1DTO;
import co.assip.erp.cartera.cierremensual.causacionintereses.CausacionInteresesRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Anexo1Service {

    private final Anexo1Repository repository;
    private final Anexo1ConsultaRepository consultaRepository;
    private final CausacionInteresesRepository causacionInteresesRepository;
    private final CierreMensualRepository cierreRepository;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // SOLO EDAD CONTABLE
    //
    // Método de soporte / pruebas.
    //
    // Requiere:
    // - cierre existente
    // - cálculos cerrados en firme
    // - Anexo 1 no cerrado en firme
    // =========================================================

    @Transactional
    public int calcularEdadContable(
            Integer idCierreCartera
    ) {

        validarParametros(
                idCierreCartera
        );

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarCalculosEnFirme(
                cierre
        );

        validarAnexo1NoCerrado(
                cierre
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return repository.calcularEdadContablePorArrastre(
                idCierreCartera,
                idUsuario
        );
    }


    // =========================================================
    // PROCESAR ANEXO 1 COMPLETO
    //
    // REQUISITOS:
    //
    // - fotografía cerrada en firme
    // - cálculos cerrados en firme
    // - Anexo 1 no cerrado en firme
    //
    // ESTADOS:
    //
    // P = Pendiente
    // E = En proceso
    // C = Cerrado en firme
    //
    // IMPORTANTE:
    //
    // - procesa todos los créditos de resultados
    // - deja inicialmente codigo_metodo_calculo = A1
    // - posteriormente Anexo 2 recalcula su población PE
    // =========================================================

    @Transactional
    public ResultadoAnexo1 procesarAnexo1(
            Integer idCierreCartera
    ) {

        validarParametros(idCierreCartera);

        CierreMensualDTO cierre =
                obtenerCierre(idCierreCartera);

        // =====================================================
        // 1. VALIDAR DEPENDENCIAS
        // =====================================================

        validarFotografiaEnFirme(cierre);
        validarCalculosEnFirme(cierre);
        validarAnexo1NoCerrado(cierre);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // 2. VALIDAR CAUSACIONES YA CONTABILIZADAS
        //
        // Si existen movimientos automáticos de este cierre
        // que ya tienen comprobante, no se permite recalcular.
        // =====================================================

        int movimientosContabilizados =
                causacionInteresesRepository
                        .contarMovimientosContabilizados(
                                idCierreCartera
                        );

        if (movimientosContabilizados > 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " tiene "
                            + movimientosContabilizados
                            + " movimiento(s) de causación "
                            + "ya contabilizado(s). "
                            + "No es posible recalcular el Anexo 1."
            );
        }

        // =====================================================
        // 3. INICIAR ETAPA ANEXO 1
        // =====================================================

        int etapaIniciada =
                cierreRepository.iniciarAnexo1(
                        idCierreCartera,
                        idUsuario
                );

        if (etapaIniciada != 1) {

            throw new IllegalStateException(
                    "No fue posible iniciar el Anexo 1 "
                            + "del cierre "
                            + idCierreCartera
                            + "."
            );
        }

        // =====================================================
        // 4. EDAD CONTABLE / LEY DEL ARRASTRE
        // =====================================================

        int edades =
                repository.calcularEdadContablePorArrastre(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 5. LIMPIAR CAUSACIÓN AUTOMÁTICA DEL MISMO CIERRE
        //
        // Solo elimina movimientos automáticos no contabilizados
        // generados para la fecha de este cierre.
        // =====================================================

        causacionInteresesRepository
                .limpiarCausacionCierre(
                        idCierreCartera
                );

        causacionInteresesRepository
                .limpiarContingentesCierre(
                        idCierreCartera
                );

        // =====================================================
        // 6. CAUSAR INTERESES
        //
        // Procesa créditos vigentes y vencidos aplicando:
        // - fechas de causación
        // - saldo base
        // - tasa
        // - convención 30/360
        // - clasificación
        // - categoría
        // - tipo de garantía
        // - tipo de cuota
        // =====================================================

        int causaciones =
                causacionInteresesRepository
                        .causarIntereses(
                                idCierreCartera,
                                idUsuario
                        );

        // =====================================================
        // 7. CAUSAR INTERESES CONTINGENTES
        // =====================================================

        int contingentes =
                causacionInteresesRepository
                        .causarInteresesContingentes(
                                idCierreCartera,
                                idUsuario
                        );

        // =====================================================
        // 8. CONSOLIDAR INTERESES EN RESULTADOS
        //
        // Actualiza:
        // - valor_intereses_causados_mes
        // - saldo_intereses_causados
        // - valor_intereses_contingentes_mes
        // - saldo_intereses_contingentes
        // =====================================================

        int interesesConsolidados =
                causacionInteresesRepository
                        .consolidarResultadosIntereses(
                                idCierreCartera,
                                idUsuario
                        );

        // =====================================================
        // 9. DETERIORO DE CAPITAL
        // =====================================================

        int capital =
                repository.calcularDeterioroCapital(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 10. DETERIORO DE INTERESES
        //
        // Ahora saldo_intereses_causados ya está consolidado.
        // =====================================================

        int intereses =
                repository.calcularDeterioroIntereses(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 11. RESULTADO
        // =====================================================

        return new ResultadoAnexo1(
                edades,
                causaciones,
                contingentes,
                interesesConsolidados,
                capital,
                intereses
        );
    }


    // =========================================================
    // CERRAR ANEXO 1 EN FIRME
    //
    // REQUISITOS:
    //
    // - cálculos cerrados en firme
    // - Anexo 1 en estado E
    //
    // RESULTADO:
    //
    // estado_anexo1 = C
    // fecha_anexo1_firme = CURRENT_TIMESTAMP
    // =========================================================

    @Transactional
    public CierreMensualDTO cerrarAnexo1(
            Integer idCierreCartera
    ) {

        validarParametros(
                idCierreCartera
        );

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarCalculosEnFirme(
                cierre
        );

        String estadoAnexo1 =
                cierre.getEstadoAnexo1();

        if (estadoAnexo1 != null
                && "C".equalsIgnoreCase(
                estadoAnexo1.trim()
        )) {

            throw new IllegalStateException(
                    "El Anexo 1 del cierre "
                            + idCierreCartera
                            + " ya se encuentra cerrado en firme."
            );
        }

        if (estadoAnexo1 == null
                || !"E".equalsIgnoreCase(
                estadoAnexo1.trim()
        )) {

            throw new IllegalStateException(
                    "El Anexo 1 del cierre "
                            + idCierreCartera
                            + " debe procesarse antes de "
                            + "cerrarlo en firme."
            );
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int actualizados =
                cierreRepository.cerrarAnexo1(
                        idCierreCartera,
                        idUsuario
                );

        if (actualizados != 1) {

            throw new IllegalStateException(
                    "No fue posible cerrar en firme "
                            + "el Anexo 1 del cierre "
                            + idCierreCartera
                            + "."
            );
        }

        return obtenerCierre(
                idCierreCartera
        );
    }


    // =========================================================
    // CONSULTAR DETALLE ANEXO 1
    //
    // CONSULTA EXCLUSIVAMENTE DE LECTURA.
    //
    // No ejecuta cálculos.
    // No modifica resultados.
    // No modifica la fotografía.
    //
    // Requiere:
    // - cierre existente
    // - cálculos cerrados en firme
    // =========================================================

    @Transactional(readOnly = true)
    public List<DetalleAnexo1DTO> obtenerDetalleAnexo1(
            Integer idCierreCartera
    ) {

        validarParametros(
                idCierreCartera
        );

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarCalculosEnFirme(
                cierre
        );

        return consultaRepository.obtenerDetalleAnexo1(
                idCierreCartera
        );
    }

    // =========================================================
// CONSULTAR RESUMEN ANEXO 1
//
// CONSULTA EXCLUSIVAMENTE DE LECTURA.
//
// No ejecuta cálculos.
// No modifica resultados.
// No modifica la fotografía.
//
// Requiere:
// - cierre existente
// - cálculos cerrados en firme
// =========================================================

    @Transactional(readOnly = true)
    public ResumenAnexo1DTO obtenerResumenAnexo1(
            Integer idCierreCartera
    ) {

        validarParametros(
                idCierreCartera
        );

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarCalculosEnFirme(
                cierre
        );

        return consultaRepository.obtenerResumenAnexo1(
                idCierreCartera
        );
    }

    // =========================================================
    // OBTENER CIERRE
    // =========================================================

    private CierreMensualDTO obtenerCierre(
            Integer idCierreCartera
    ) {

        return cierreRepository
                .buscarPorId(
                        idCierreCartera
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el cierre de cartera: "
                                        + idCierreCartera
                        )
                );
    }


    // =========================================================
    // VALIDAR FOTOGRAFÍA EN FIRME
    // =========================================================

    private void validarFotografiaEnFirme(
            CierreMensualDTO cierre
    ) {

        String estado =
                cierre.getEstadoFotografia();

        if (estado == null
                || !"C".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "La fotografía del cierre "
                            + cierre.getIdCierreCartera()
                            + " debe estar cerrada en firme "
                            + "para ejecutar el Anexo 1."
            );
        }
    }


    // =========================================================
    // VALIDAR CÁLCULOS EN FIRME
    // =========================================================

    private void validarCalculosEnFirme(
            CierreMensualDTO cierre
    ) {

        String estado =
                cierre.getEstadoCalculos();

        if (estado == null
                || !"C".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "Los cálculos del cierre "
                            + cierre.getIdCierreCartera()
                            + " deben estar cerrados en firme "
                            + "antes de ejecutar el Anexo 1."
            );
        }
    }


    // =========================================================
    // VALIDAR ANEXO 1 NO CERRADO
    // =========================================================

    private void validarAnexo1NoCerrado(
            CierreMensualDTO cierre
    ) {

        String estado =
                cierre.getEstadoAnexo1();

        if (estado != null
                && "C".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "El Anexo 1 del cierre "
                            + cierre.getIdCierreCartera()
                            + " ya se encuentra cerrado en firme."
            );
        }
    }


    // =========================================================
    // VALIDAR PARÁMETROS
    // =========================================================

    private void validarParametros(
            Integer idCierreCartera
    ) {

        if (idCierreCartera == null
                || idCierreCartera <= 0) {

            throw new IllegalArgumentException(
                    "El id del cierre de cartera es obligatorio."
            );
        }
    }

    // =========================================================
    // RESULTADO
    // =========================================================

    public record ResultadoAnexo1(
            int edadesContables,
            int causacionesIntereses,
            int causacionesContingentes,
            int interesesConsolidados,
            int deteriorosCapital,
            int deteriorosIntereses
    ) {
    }
}