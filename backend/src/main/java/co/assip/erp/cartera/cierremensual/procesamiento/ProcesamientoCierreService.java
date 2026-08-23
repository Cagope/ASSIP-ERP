package co.assip.erp.cartera.cierremensual.procesamiento;

import co.assip.erp.cartera.calculosprevios.CalculosPreviosService;
import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.alivios.ConsolidacionAliviosService;
import co.assip.erp.cartera.cierremensual.anexo1.Anexo1Service;
import co.assip.erp.cartera.cierremensual.anexo2.Anexo2Service;
import co.assip.erp.cartera.cierremensual.causacionintereses.CausacionInteresesService;
import co.assip.erp.cartera.cierremensual.causacionseguros.CausacionSegurosService;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.cartera.cierremensual.validacion.ValidacionCierreService;
import co.assip.erp.cartera.cierremensual.validacion.dto.ResultadoValidacionCierreDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesamientoCierreService {

    private final CierreMensualRepository cierreRepository;

    private final CausacionInteresesService causacionInteresesService;
    private final CausacionSegurosService causacionSegurosService;
    private final ConsolidacionAliviosService consolidacionAliviosService;
    private final CalculosPreviosService calculosPreviosService;
    private final Anexo1Service anexo1Service;
    private final Anexo2Service anexo2Service;
    private final ValidacionCierreService validacionCierreService;

    private final UsuarioSesionService usuarioSesionService;


    // =========================================================
    // PROCESAR CÁLCULOS DEL CIERRE
    //
    // REQUISITO:
    //
    // El cierre debe encontrarse en estado:
    //
    // C = fotografía cerrada en firme
    //
    // ORDEN OFICIAL:
    //
    // 1. Intereses
    // 2. Seguros
    // 3. Alivios
    // 4. Cálculos previos
    // 5. Anexo 1
    // 6. Anexo 2 / PE
    // 7. Validaciones finales
    //
    // IMPORTANTE:
    //
    // - NO modifica la fotografía.
    // - NO cambia el estado general del cierre.
    // - El cierre permanece en C.
    // - Cada subproceso puede limpiar únicamente sus propios
    //   rastros regenerables del mismo cierre.
    // - Si existe información que no pueda recalcularse
    //   de forma segura, el proceso debe fallar.
    // - Las validaciones finales son BLOQUEANTES.
    //
    // Toda la ejecución participa en una misma transacción.
    //
    // Si alguna etapa falla, incluyendo las validaciones
    // finales, se revierte el procesamiento completo de
    // esta ejecución.
    //
    // =========================================================

    @Transactional
    public ResultadoProcesamientoCierre procesar(
            Integer idCierreCartera
    ) {

        // =====================================================
        // 1. VALIDAR ID
        // =====================================================

        validarIdCierre(
                idCierreCartera
        );


        // =====================================================
        // 2. RECUPERAR CIERRE
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
        // 3. VALIDAR CIERRE CERRADO EN FIRME
        // =====================================================

        validarEstadoCerrado(
                cierre
        );


        // =====================================================
        // 4. OBTENER USUARIO AUTENTICADO
        //
        // No se recibe idUsuario desde frontend.
        // =====================================================

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        if (idUsuario == null
                || idUsuario <= 0) {

            throw new IllegalStateException(
                    "No fue posible identificar el usuario de sesión."
            );
        }


        // =====================================================
        // 5. CAUSACIÓN DE INTERESES
        // =====================================================

        int intereses =
                causacionInteresesService.ejecutar(
                        idCierreCartera
                );


        // =====================================================
        // 6. CAUSACIÓN DE SEGUROS
        // =====================================================

        int seguros =
                causacionSegurosService.ejecutar(
                        idCierreCartera
                );


        // =====================================================
        // 7. CONSOLIDACIÓN DE ALIVIOS
        // =====================================================

        int alivios =
                consolidacionAliviosService.ejecutar(
                        idCierreCartera
                );


        // =====================================================
        // 8. CÁLCULOS PREVIOS
        //
        // - mora
        // - edades
        // - reestructuración
        // - aportes
        // - garantías
        // - costas judiciales
        // =====================================================

        int calculosPrevios =
                calculosPreviosService.ejecutar(
                        idCierreCartera
                );


        // =====================================================
        // 9. ANEXO 1
        //
        // Procesa inicialmente toda la población.
        //
        // codigo_metodo_calculo = A1
        //
        // Posteriormente Anexo 2 sobrescribe solamente
        // los créditos que aplican PE.
        // =====================================================

        Anexo1Service.ResultadoAnexo1 anexo1 =
                anexo1Service.procesarAnexo1(
                        idCierreCartera,
                        idUsuario
                );


        // =====================================================
        // 10. VALIDAR ANEXO 1
        //
        // Los tres cálculos de Anexo 1 deben trabajar sobre
        // exactamente la misma población de cálculos previos.
        // =====================================================

        if (anexo1.edadesContables()
                != calculosPrevios) {

            throw new IllegalStateException(
                    "La cantidad de edades contables del Anexo 1 "
                            + "no coincide con la población del cierre. "
                            + "Esperados: "
                            + calculosPrevios
                            + ", encontrados: "
                            + anexo1.edadesContables()
                            + "."
            );
        }


        if (anexo1.deteriorosCapital()
                != calculosPrevios) {

            throw new IllegalStateException(
                    "La cantidad de deterioros de capital del Anexo 1 "
                            + "no coincide con la población del cierre. "
                            + "Esperados: "
                            + calculosPrevios
                            + ", encontrados: "
                            + anexo1.deteriorosCapital()
                            + "."
            );
        }


        if (anexo1.deteriorosIntereses()
                != calculosPrevios) {

            throw new IllegalStateException(
                    "La cantidad de deterioros de intereses del Anexo 1 "
                            + "no coincide con la población del cierre. "
                            + "Esperados: "
                            + calculosPrevios
                            + ", encontrados: "
                            + anexo1.deteriorosIntereses()
                            + "."
            );
        }


        // =====================================================
        // 11. ANEXO 2 / PÉRDIDA ESPERADA
        //
        // Recalcula únicamente la población que aplica PE.
        //
        // codigo_metodo_calculo:
        //
        // A1 -> permanece para créditos no PE
        // PE -> sobrescribe la población Anexo 2
        // =====================================================

        Anexo2Service.ResultadoPreparacionAnexo2 anexo2 =
                anexo2Service.preparar(
                        idCierreCartera
                );


        // =====================================================
        // 12. VALIDACIÓN BÁSICA DEL ANEXO 2
        //
        // Anexo2Service realiza sus propias validaciones.
        //
        // Aquí hacemos un último control simple antes de
        // ejecutar las validaciones finales integrales.
        // =====================================================

        if (!anexo2.poblacion()
                .equals(
                        anexo2.resultadosPersistidos()
                )) {

            throw new IllegalStateException(
                    "La población del Anexo 2 no coincide con "
                            + "los resultados PE persistidos. "
                            + "Población: "
                            + anexo2.poblacion()
                            + ", persistidos: "
                            + anexo2.resultadosPersistidos()
                            + "."
            );
        }


        // =====================================================
        // 13. VALIDACIONES FINALES DEL CIERRE
        //
        // Validaciones implementadas:
        //
        // - cabecera = fotografía con saldo
        // - fotografía con saldo = resultados
        // - A1 + PE = resultados
        // - sin créditos sin resultado
        // - sin resultados duplicados
        // - métodos A1 / PE válidos
        // - todo PE tiene pe_resultados
        // - sin PE duplicados
        // - sin PE huérfanos
        // - edades completas
        // - edades válidas A-E
        // - valores monetarios válidos
        // - PI/PDI entre 0 y 100
        // - pérdida PE = deterioro total PE
        // - proceso PE FINALIZADO
        //
        // Si resultado.valido = false:
        //
        // EL PROCESAMIENTO COMPLETO FALLA.
        //
        // =====================================================

        ResultadoValidacionCierreDTO validacionFinal =
                validacionCierreService.validar(
                        idCierreCartera
                );


        // =====================================================
        // 14. VALIDACIONES FINALES BLOQUEANTES
        // =====================================================

        if (!validacionFinal.isValido()) {

            String detalleErrores;

            if (validacionFinal.getErrores() == null
                    || validacionFinal.getErrores().isEmpty()) {

                detalleErrores =
                        "No se recibió el detalle de las inconsistencias.";

            } else {

                detalleErrores =
                        String.join(
                                " | ",
                                validacionFinal.getErrores()
                        );
            }

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " no superó las validaciones finales. "
                            + detalleErrores
            );
        }


        // =====================================================
        // 15. RESULTADO GENERAL
        //
        // IMPORTANTE:
        //
        // El procesamiento terminó correctamente.
        //
        // El estado general del cierre NO se modifica todavía.
        // Permanece en C.
        //
        // El cierre definitivo y los comprobantes contables
        // serán etapas posteriores.
        // =====================================================

        return new ResultadoProcesamientoCierre(
                idCierreCartera,
                cierre.getFechaCorte(),
                cierre.getEstadoCierre(),

                intereses,
                seguros,
                alivios,
                calculosPrevios,

                anexo1.edadesContables(),
                anexo1.deteriorosCapital(),
                anexo1.deteriorosIntereses(),

                anexo2.poblacion(),
                anexo2.resultadosPersistidos(),

                validacionFinal.isValido(),
                validacionFinal.getEstado(),
                validacionFinal.getCantidadCreditosCabecera(),
                validacionFinal.getCantidadCreditosFotografia(),
                validacionFinal.getCantidadResultados(),
                validacionFinal.getCantidadResultadosA1(),
                validacionFinal.getCantidadResultadosPe(),
                validacionFinal.getCantidadPePersistidos(),
                validacionFinal.isProcesoPeFinalizado(),
                validacionFinal.getAdvertencias()
        );
    }


    // =========================================================
    // VALIDAR ID
    // =========================================================

    private void validarIdCierre(
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
    // VALIDAR ESTADO C
    //
    // C = fotografía cerrada en firme.
    // =========================================================

    private void validarEstadoCerrado(
            CierreMensualDTO cierre
    ) {

        String estado =
                cierre.getEstadoCierre();

        if (estado == null
                || !"C".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "El cierre "
                            + cierre.getIdCierreCartera()
                            + " debe estar cerrado en firme "
                            + "antes de ejecutar los cálculos de cierre."
            );
        }
    }


    // =========================================================
    // RESULTADO GENERAL DEL PROCESAMIENTO
    // =========================================================

    public record ResultadoProcesamientoCierre(

            Integer idCierreCartera,
            java.time.LocalDate fechaCorte,
            String estadoCierre,

            int interesesCausados,
            int segurosCausados,
            int aliviosConsolidados,
            int calculosPrevios,

            int anexo1EdadesContables,
            int anexo1DeteriorosCapital,
            int anexo1DeteriorosIntereses,

            int poblacionAnexo2,
            int resultadosAnexo2Persistidos,

            boolean validacionesFinalesOk,
            String estadoValidacionFinal,

            int cantidadCreditosCabecera,
            int cantidadCreditosFotografia,
            int cantidadResultados,
            int cantidadResultadosA1,
            int cantidadResultadosPe,
            int cantidadPePersistidos,

            boolean procesoPeFinalizado,

            List<String> advertencias

    ) {
    }
}