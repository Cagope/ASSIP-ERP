package co.assip.erp.cartera.cierremensual.validacion;

import co.assip.erp.cartera.cierremensual.cuadre.CuadreCierreService;
import co.assip.erp.cartera.cierremensual.cuadre.dto.CuadreCierreDTO;
import co.assip.erp.cartera.cierremensual.validacion.dto.ResultadoValidacionCierreDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidacionCierreService {

    private final ValidacionCierreRepository repository;
    private final CuadreCierreService cuadreCierreService;


    // =========================================================
    // EJECUTAR VALIDACIONES FINALES DEL CIERRE
    //
    // Este proceso:
    //
    // - NO modifica información.
    // - NO recalcula cartera.
    // - NO finaliza el cierre.
    // - NO genera comprobantes.
    // - NO genera informes.
    //
    // Valida:
    //
    // 1. Integridad de población.
    // 2. Métodos A1 / PE.
    // 3. Trazabilidad PE.
    // 4. Edades.
    // 5. Valores monetarios.
    // 6. PI / PDI.
    // 7. Cuadre PE.
    // 8. Estado del proceso PE.
    // 9. Cuadre consolidado final.
    //
    // =========================================================

    @Transactional(readOnly = true)
    public ResultadoValidacionCierreDTO validar(
            Integer idCierreCartera
    ) {

        // =====================================================
        // 1. VALIDAR ID
        // =====================================================

        validarIdCierre(
                idCierreCartera
        );


        // =====================================================
        // 2. CANTIDADES PRINCIPALES
        // =====================================================

        int cantidadCreditosCabecera =
                repository.cantidadCreditosCabecera(
                        idCierreCartera
                );

        int cantidadCreditosFotografia =
                repository.cantidadCreditosFotografia(
                        idCierreCartera
                );

        int cantidadResultados =
                repository.cantidadResultados(
                        idCierreCartera
                );

        int cantidadResultadosA1 =
                repository.cantidadResultadosA1(
                        idCierreCartera
                );

        int cantidadResultadosPe =
                repository.cantidadResultadosPe(
                        idCierreCartera
                );

        int cantidadPePersistidos =
                repository.cantidadPePersistidos(
                        idCierreCartera
                );


        // =====================================================
        // 3. INTEGRIDAD DE RESULTADOS
        // =====================================================

        int creditosSinResultado =
                repository.cantidadCreditosSinResultado(
                        idCierreCartera
                );

        int creditosConResultadoDuplicado =
                repository.cantidadCreditosConResultadoDuplicado(
                        idCierreCartera
                );

        int metodosInvalidos =
                repository.cantidadMetodosInvalidos(
                        idCierreCartera
                );


        // =====================================================
        // 4. INTEGRIDAD PE
        // =====================================================

        int peSinResultadoPe =
                repository.cantidadPeSinResultadoPe(
                        idCierreCartera
                );

        int peDuplicados =
                repository.cantidadPeDuplicados(
                        idCierreCartera
                );

        int peHuerfanos =
                repository.cantidadPeHuerfanos(
                        idCierreCartera
                );


        // =====================================================
        // 5. EDADES
        // =====================================================

        int edadesNulas =
                repository.cantidadEdadesNulas(
                        idCierreCartera
                );

        int edadesInvalidas =
                repository.cantidadEdadesInvalidas(
                        idCierreCartera
                );

        int edadesPeNulas =
                repository.cantidadEdadesPeNulas(
                        idCierreCartera
                );

        int edadesPeInvalidas =
                repository.cantidadEdadesPeInvalidas(
                        idCierreCartera
                );


        // =====================================================
        // 6. VALIDACIONES MONETARIAS
        // =====================================================

        int valoresNegativos =
                repository.cantidadValoresNegativos(
                        idCierreCartera
                );

        int valoresPeNegativos =
                repository.cantidadValoresPeNegativos(
                        idCierreCartera
                );

        int piPdiFueraRango =
                repository.cantidadPiPdiFueraRango(
                        idCierreCartera
                );

        int piPdiPeFueraRango =
                repository.cantidadPiPdiPeFueraRango(
                        idCierreCartera
                );

        int peDescuadrados =
                repository.cantidadPeDescuadrados(
                        idCierreCartera
                );


        // =====================================================
        // 7. ESTADO DEL PROCESO PE
        // =====================================================

        boolean procesoPeFinalizado =
                repository.procesoPeFinalizado(
                        idCierreCartera
                );


        // =====================================================
        // 8. CUADRE CONSOLIDADO FINAL
        //
        // Este servicio es únicamente de lectura.
        //
        // Valida:
        //
        // TOTAL = A1 + PE
        //
        // y:
        //
        // PE general = pe_resultados
        //
        // =====================================================

        CuadreCierreDTO cuadre =
                cuadreCierreService.cuadrar(
                        idCierreCartera
                );

        boolean cuadreConsolidadoOk =
                cuadre.isCuadrado();

        String estadoCuadreConsolidado =
                cuadre.getEstado();


        // =====================================================
        // 9. ERRORES BLOQUEANTES
        // =====================================================

        List<String> errores =
                new ArrayList<>();


        // -----------------------------------------------------
        // CABECERA VS FOTOGRAFÍA
        // -----------------------------------------------------

        if (cantidadCreditosCabecera
                != cantidadCreditosFotografia) {

            errores.add(
                    "La cantidad de créditos de la cabecera ("
                            + cantidadCreditosCabecera
                            + ") no coincide con la fotografía con saldo ("
                            + cantidadCreditosFotografia
                            + ")."
            );
        }


        // -----------------------------------------------------
        // FOTOGRAFÍA VS RESULTADOS
        // -----------------------------------------------------

        if (cantidadCreditosFotografia
                != cantidadResultados) {

            errores.add(
                    "La cantidad de créditos de la fotografía con saldo ("
                            + cantidadCreditosFotografia
                            + ") no coincide con los resultados ("
                            + cantidadResultados
                            + ")."
            );
        }


        // -----------------------------------------------------
        // A1 + PE = TOTAL RESULTADOS
        // -----------------------------------------------------

        int totalMetodos =
                cantidadResultadosA1
                        + cantidadResultadosPe;

        if (totalMetodos
                != cantidadResultados) {

            errores.add(
                    "La suma de resultados A1 ("
                            + cantidadResultadosA1
                            + ") + PE ("
                            + cantidadResultadosPe
                            + ") no coincide con el total de resultados ("
                            + cantidadResultados
                            + ")."
            );
        }


        // -----------------------------------------------------
        // CRÉDITOS SIN RESULTADO
        // -----------------------------------------------------

        if (creditosSinResultado > 0) {

            errores.add(
                    "Existen "
                            + creditosSinResultado
                            + " créditos de la fotografía sin resultado."
            );
        }


        // -----------------------------------------------------
        // RESULTADOS DUPLICADOS
        // -----------------------------------------------------

        if (creditosConResultadoDuplicado > 0) {

            errores.add(
                    "Existen "
                            + creditosConResultadoDuplicado
                            + " créditos con resultados duplicados."
            );
        }


        // -----------------------------------------------------
        // MÉTODOS INVÁLIDOS
        // -----------------------------------------------------

        if (metodosInvalidos > 0) {

            errores.add(
                    "Existen "
                            + metodosInvalidos
                            + " resultados con método de cálculo inválido."
            );
        }


        // -----------------------------------------------------
        // RESULTADOS PE VS pe_resultados
        // -----------------------------------------------------

        if (cantidadResultadosPe
                != cantidadPePersistidos) {

            errores.add(
                    "La cantidad de resultados PE ("
                            + cantidadResultadosPe
                            + ") no coincide con los registros persistidos "
                            + "en cartera.pe_resultados ("
                            + cantidadPePersistidos
                            + ")."
            );
        }


        // -----------------------------------------------------
        // PE SIN TRAZABILIDAD
        // -----------------------------------------------------

        if (peSinResultadoPe > 0) {

            errores.add(
                    "Existen "
                            + peSinResultadoPe
                            + " resultados PE sin trazabilidad "
                            + "en cartera.pe_resultados."
            );
        }


        // -----------------------------------------------------
        // PE DUPLICADOS
        // -----------------------------------------------------

        if (peDuplicados > 0) {

            errores.add(
                    "Existen "
                            + peDuplicados
                            + " créditos con registros PE duplicados."
            );
        }


        // -----------------------------------------------------
        // PE HUÉRFANOS
        // -----------------------------------------------------

        if (peHuerfanos > 0) {

            errores.add(
                    "Existen "
                            + peHuerfanos
                            + " registros en cartera.pe_resultados "
                            + "sin resultado final marcado como PE."
            );
        }


        // -----------------------------------------------------
        // EDADES NULAS
        // -----------------------------------------------------

        if (edadesNulas > 0) {

            errores.add(
                    "Existen "
                            + edadesNulas
                            + " resultados con edades obligatorias nulas."
            );
        }


        // -----------------------------------------------------
        // EDADES INVÁLIDAS
        // -----------------------------------------------------

        if (edadesInvalidas > 0) {

            errores.add(
                    "Existen "
                            + edadesInvalidas
                            + " resultados con edades fuera del rango A-E."
            );
        }


        // -----------------------------------------------------
        // EDADES PE NULAS
        // -----------------------------------------------------

        if (edadesPeNulas > 0) {

            errores.add(
                    "Existen "
                            + edadesPeNulas
                            + " registros PE con edades o calificaciones nulas."
            );
        }


        // -----------------------------------------------------
        // EDADES PE INVÁLIDAS
        // -----------------------------------------------------

        if (edadesPeInvalidas > 0) {

            errores.add(
                    "Existen "
                            + edadesPeInvalidas
                            + " registros PE con edades o calificaciones "
                            + "fuera del rango A-E."
            );
        }


        // -----------------------------------------------------
        // VALORES NEGATIVOS
        // -----------------------------------------------------

        if (valoresNegativos > 0) {

            errores.add(
                    "Existen "
                            + valoresNegativos
                            + " resultados generales con valores "
                            + "monetarios negativos no permitidos."
            );
        }


        // -----------------------------------------------------
        // VALORES NEGATIVOS PE
        // -----------------------------------------------------

        if (valoresPeNegativos > 0) {

            errores.add(
                    "Existen "
                            + valoresPeNegativos
                            + " registros PE con valores negativos "
                            + "no permitidos."
            );
        }


        // -----------------------------------------------------
        // PI / PDI FUERA DE RANGO
        // -----------------------------------------------------

        if (piPdiFueraRango > 0) {

            errores.add(
                    "Existen "
                            + piPdiFueraRango
                            + " resultados generales con PI/PDI "
                            + "fuera del rango 0-100."
            );
        }


        // -----------------------------------------------------
        // PI / PDI PE FUERA DE RANGO
        // -----------------------------------------------------

        if (piPdiPeFueraRango > 0) {

            errores.add(
                    "Existen "
                            + piPdiPeFueraRango
                            + " registros PE con PI/PDI "
                            + "fuera del rango 0-100."
            );
        }


        // -----------------------------------------------------
        // PÉRDIDA ESPERADA VS DETERIORO TOTAL
        // -----------------------------------------------------

        if (peDescuadrados > 0) {

            errores.add(
                    "Existen "
                            + peDescuadrados
                            + " registros PE donde el deterioro total "
                            + "no coincide con la pérdida esperada."
            );
        }


        // -----------------------------------------------------
        // PROCESO PE
        // -----------------------------------------------------

        if (!procesoPeFinalizado) {

            errores.add(
                    "El proceso PE del cierre no se encuentra FINALIZADO."
            );
        }


        // -----------------------------------------------------
        // CUADRE CONSOLIDADO FINAL
        // -----------------------------------------------------

        if (!cuadreConsolidadoOk) {

            errores.add(
                    "El cuadre consolidado final del cierre "
                            + idCierreCartera
                            + " presenta diferencias."
            );
        }


        // =====================================================
        // 10. ADVERTENCIAS
        //
        // El cuadre consolidado ya no es pendiente.
        //
        // Por ahora no existen advertencias no bloqueantes.
        // =====================================================

        List<String> advertencias =
                new ArrayList<>();


        // =====================================================
        // 11. RESULTADO GENERAL
        // =====================================================

        boolean valido =
                errores.isEmpty();

        String estado =
                valido
                        ? "OK"
                        : "ERROR";

        String mensaje =
                valido
                        ? "El cierre cumple las validaciones finales implementadas."
                        : "El cierre presenta inconsistencias que impiden "
                        + "finalizar el procesamiento.";


        // =====================================================
        // 12. CONSTRUIR DTO
        // =====================================================

        return ResultadoValidacionCierreDTO.builder()

                .idCierreCartera(
                        idCierreCartera
                )

                .valido(
                        valido
                )

                .estado(
                        estado
                )

                .mensaje(
                        mensaje
                )

                .cantidadCreditosCabecera(
                        cantidadCreditosCabecera
                )

                .cantidadCreditosFotografia(
                        cantidadCreditosFotografia
                )

                .cantidadResultados(
                        cantidadResultados
                )

                .cantidadResultadosA1(
                        cantidadResultadosA1
                )

                .cantidadResultadosPe(
                        cantidadResultadosPe
                )

                .cantidadPePersistidos(
                        cantidadPePersistidos
                )

                .creditosSinResultado(
                        creditosSinResultado
                )

                .creditosConResultadoDuplicado(
                        creditosConResultadoDuplicado
                )

                .metodosInvalidos(
                        metodosInvalidos
                )

                .peSinResultadoPe(
                        peSinResultadoPe
                )

                .peDuplicados(
                        peDuplicados
                )

                .peHuerfanos(
                        peHuerfanos
                )

                .edadesNulas(
                        edadesNulas
                )

                .edadesInvalidas(
                        edadesInvalidas
                )

                .edadesPeNulas(
                        edadesPeNulas
                )

                .edadesPeInvalidas(
                        edadesPeInvalidas
                )

                .valoresNegativos(
                        valoresNegativos
                )

                .valoresPeNegativos(
                        valoresPeNegativos
                )

                .piPdiFueraRango(
                        piPdiFueraRango
                )

                .piPdiPeFueraRango(
                        piPdiPeFueraRango
                )

                .peDescuadrados(
                        peDescuadrados
                )

                .procesoPeFinalizado(
                        procesoPeFinalizado
                )

                .cuadreConsolidadoOk(
                        cuadreConsolidadoOk
                )

                .estadoCuadreConsolidado(
                        estadoCuadreConsolidado
                )

                .errores(
                        errores
                )

                .advertencias(
                        advertencias
                )

                .build();
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
}