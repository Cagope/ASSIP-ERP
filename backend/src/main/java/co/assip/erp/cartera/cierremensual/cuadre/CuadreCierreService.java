package co.assip.erp.cartera.cierremensual.cuadre;

import co.assip.erp.cartera.cierremensual.cuadre.dto.CuadreCierreDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CuadreCierreService {

    private final CuadreCierreRepository repository;


    // =========================================================
    // CUADRAR CIERRE
    //
    // Este proceso:
    //
    // - NO modifica datos.
    // - NO recalcula cartera.
    // - NO modifica resultados.
    // - NO modifica PE.
    // - NO genera comprobantes.
    //
    // Solamente consolida y valida:
    //
    // 1. TOTAL = A1 + PE
    // 2. PE general = PE detalle
    //
    // =========================================================

    @Transactional(readOnly = true)
    public CuadreCierreDTO cuadrar(
            Integer idCierreCartera
    ) {

        // =====================================================
        // 1. VALIDAR ID
        // =====================================================

        validarIdCierre(
                idCierreCartera
        );


        // =====================================================
        // 2. VALIDAR EXISTENCIA DEL CIERRE
        // =====================================================

        if (!repository.existeCierre(
                idCierreCartera
        )) {

            throw new IllegalArgumentException(
                    "No existe el cierre de cartera: "
                            + idCierreCartera
            );
        }


        // =====================================================
        // 3. RECUPERAR CONSOLIDADO GENERAL
        // =====================================================

        CuadreCierreRepository.ConsolidadoResultados general =
                repository.obtenerConsolidadoResultados(
                        idCierreCartera
                );


        // =====================================================
        // 4. RECUPERAR CONSOLIDADO PE DETALLE
        // =====================================================

        CuadreCierreRepository.ConsolidadoPeDetalle peDetalle =
                repository.obtenerConsolidadoPeDetalle(
                        idCierreCartera
                );


        // =====================================================
        // 5. DIFERENCIAS A1 + PE = TOTAL
        // =====================================================

        BigDecimal diferenciaSaldoActual =
                diferencia(
                        general.saldoActualTotal(),
                        general.saldoActualA1(),
                        general.saldoActualPe()
                );

        BigDecimal diferenciaInteresesCausados =
                diferencia(
                        general.saldoInteresesCausadosTotal(),
                        general.saldoInteresesCausadosA1(),
                        general.saldoInteresesCausadosPe()
                );

        BigDecimal diferenciaInteresesContingentes =
                diferencia(
                        general.saldoInteresesContingentesTotal(),
                        general.saldoInteresesContingentesA1(),
                        general.saldoInteresesContingentesPe()
                );

        BigDecimal diferenciaSeguros =
                diferencia(
                        general.saldoSegurosTotal(),
                        general.saldoSegurosA1(),
                        general.saldoSegurosPe()
                );

        BigDecimal diferenciaAlivios =
                diferencia(
                        general.saldoAliviosTotal(),
                        general.saldoAliviosA1(),
                        general.saldoAliviosPe()
                );

        BigDecimal diferenciaCostasJudiciales =
                diferencia(
                        general.valorCostasJudicialesTotal(),
                        general.valorCostasJudicialesA1(),
                        general.valorCostasJudicialesPe()
                );

        BigDecimal diferenciaAportes =
                diferencia(
                        general.valorAportesCreditoTotal(),
                        general.valorAportesCreditoA1(),
                        general.valorAportesCreditoPe()
                );

        BigDecimal diferenciaGarantias =
                diferencia(
                        general.valorGarantiasCreditoTotal(),
                        general.valorGarantiasCreditoA1(),
                        general.valorGarantiasCreditoPe()
                );

        BigDecimal diferenciaFondosGarantias =
                diferencia(
                        general.valorFondosGarantiasTotal(),
                        general.valorFondosGarantiasA1(),
                        general.valorFondosGarantiasPe()
                );

        BigDecimal diferenciaOtrosConceptos =
                diferencia(
                        general.valorOtrosConceptosTotal(),
                        general.valorOtrosConceptosA1(),
                        general.valorOtrosConceptosPe()
                );

        BigDecimal diferenciaDeterioroCapital =
                diferencia(
                        general.deterioroCapitalTotal(),
                        general.deterioroCapitalA1(),
                        general.deterioroCapitalPe()
                );

        BigDecimal diferenciaDeterioroIntereses =
                diferencia(
                        general.deterioroInteresesTotal(),
                        general.deterioroInteresesA1(),
                        general.deterioroInteresesPe()
                );

        BigDecimal diferenciaDeterioroOtros =
                diferencia(
                        general.deterioroOtrosTotal(),
                        general.deterioroOtrosA1(),
                        general.deterioroOtrosPe()
                );

        BigDecimal diferenciaPerdidaEsperada =
                diferencia(
                        general.perdidaEsperadaTotal(),
                        general.perdidaEsperadaA1(),
                        general.perdidaEsperadaPe()
                );


        // =====================================================
        // 6. DIFERENCIAS PE GENERAL VS PE DETALLE
        // =====================================================

        BigDecimal diferenciaPerdidaEsperadaPe =
                diferenciaDos(
                        general.perdidaEsperadaPe(),
                        peDetalle.perdidaEsperada()
                );

        BigDecimal diferenciaDeterioroCapitalPe =
                diferenciaDos(
                        general.deterioroCapitalPe(),
                        peDetalle.deterioroCapitalPe()
                );

        BigDecimal diferenciaDeterioroInteresesPe =
                diferenciaDos(
                        general.deterioroInteresesPe(),
                        peDetalle.deterioroInteresesPe()
                );

        BigDecimal diferenciaDeterioroOtrosPe =
                diferenciaDos(
                        general.deterioroOtrosPe(),
                        peDetalle.deterioroOtrosPe()
                );


        // =====================================================
        // 7. VALIDAR CANTIDADES
        // =====================================================

        boolean cantidadesCuadradas =

                general.cantidadCreditosTotal().intValue()
                        ==
                        general.cantidadCreditosA1().intValue()
                                + general.cantidadCreditosPe().intValue()

                        &&

                        general.cantidadCreditosPe().intValue()
                                ==
                                peDetalle.cantidadPe().intValue();

        // =====================================================
        // 8. VALIDAR DIFERENCIAS A1 + PE = TOTAL
        // =====================================================

        boolean diferenciasGeneralesCero =

                esCero(
                        diferenciaSaldoActual
                )

                        && esCero(
                        diferenciaInteresesCausados
                )

                        && esCero(
                        diferenciaInteresesContingentes
                )

                        && esCero(
                        diferenciaSeguros
                )

                        && esCero(
                        diferenciaAlivios
                )

                        && esCero(
                        diferenciaCostasJudiciales
                )

                        && esCero(
                        diferenciaAportes
                )

                        && esCero(
                        diferenciaGarantias
                )

                        && esCero(
                        diferenciaFondosGarantias
                )

                        && esCero(
                        diferenciaOtrosConceptos
                )

                        && esCero(
                        diferenciaDeterioroCapital
                )

                        && esCero(
                        diferenciaDeterioroIntereses
                )

                        && esCero(
                        diferenciaDeterioroOtros
                )

                        && esCero(
                        diferenciaPerdidaEsperada
                );


        // =====================================================
        // 9. VALIDAR DIFERENCIAS PE
        // =====================================================

        boolean diferenciasPeCero =

                esCero(
                        diferenciaPerdidaEsperadaPe
                )

                        && esCero(
                        diferenciaDeterioroCapitalPe
                )

                        && esCero(
                        diferenciaDeterioroInteresesPe
                )

                        && esCero(
                        diferenciaDeterioroOtrosPe
                );


        // =====================================================
        // 10. VALIDAR PE INTERNO
        //
        // En pe_resultados:
        //
        // deterioro_total_pe
        // =
        // deterioro_capital_pe
        // + deterioro_intereses_pe
        // + deterioro_otros_pe
        //
        // y además:
        //
        // deterioro_total_pe
        // =
        // perdida_esperada
        //
        // =====================================================

        BigDecimal sumaComponentesPe =
                nvl(
                        peDetalle.deterioroCapitalPe()
                )
                        .add(
                                nvl(
                                        peDetalle.deterioroInteresesPe()
                                )
                        )
                        .add(
                                nvl(
                                        peDetalle.deterioroOtrosPe()
                                )
                        );

        boolean peInternoCuadrado =

                esCero(
                        nvl(
                                peDetalle.deterioroTotalPe()
                        )
                                .subtract(
                                        sumaComponentesPe
                                )
                )

                        &&

                        esCero(
                                nvl(
                                        peDetalle.deterioroTotalPe()
                                )
                                        .subtract(
                                                nvl(
                                                        peDetalle.perdidaEsperada()
                                                )
                                        )
                        );


        // =====================================================
        // 11. RESULTADO FINAL
        // =====================================================

        boolean cuadrado =

                cantidadesCuadradas
                        && diferenciasGeneralesCero
                        && diferenciasPeCero
                        && peInternoCuadrado;


        String estado =
                cuadrado
                        ? "OK"
                        : "ERROR";


        String mensaje =
                cuadrado
                        ? "El cuadre consolidado del cierre es correcto."
                        : "El cuadre consolidado del cierre presenta diferencias.";


        // =====================================================
        // 12. CONSTRUIR DTO
        // =====================================================

        return CuadreCierreDTO.builder()

                // -------------------------------------------------
                // IDENTIFICACIÓN
                // -------------------------------------------------

                .idCierreCartera(
                        idCierreCartera
                )

                .estado(
                        estado
                )

                .cuadrado(
                        cuadrado
                )


                // -------------------------------------------------
                // CANTIDADES
                // -------------------------------------------------

                .cantidadCreditosTotal(
                        general.cantidadCreditosTotal()
                )

                .cantidadCreditosA1(
                        general.cantidadCreditosA1()
                )

                .cantidadCreditosPe(
                        general.cantidadCreditosPe()
                )


                // -------------------------------------------------
                // SALDOS
                // -------------------------------------------------

                .saldoActualTotal(
                        general.saldoActualTotal()
                )

                .saldoActualA1(
                        general.saldoActualA1()
                )

                .saldoActualPe(
                        general.saldoActualPe()
                )


                // -------------------------------------------------
                // INTERESES CAUSADOS
                // -------------------------------------------------

                .saldoInteresesCausadosTotal(
                        general.saldoInteresesCausadosTotal()
                )

                .saldoInteresesCausadosA1(
                        general.saldoInteresesCausadosA1()
                )

                .saldoInteresesCausadosPe(
                        general.saldoInteresesCausadosPe()
                )

                .valorInteresesCausadosMesTotal(
                        general.valorInteresesCausadosMesTotal()
                )

                .valorInteresesCausadosMesA1(
                        general.valorInteresesCausadosMesA1()
                )

                .valorInteresesCausadosMesPe(
                        general.valorInteresesCausadosMesPe()
                )


                // -------------------------------------------------
                // INTERESES CONTINGENTES
                // -------------------------------------------------

                .saldoInteresesContingentesTotal(
                        general.saldoInteresesContingentesTotal()
                )

                .saldoInteresesContingentesA1(
                        general.saldoInteresesContingentesA1()
                )

                .saldoInteresesContingentesPe(
                        general.saldoInteresesContingentesPe()
                )

                .valorInteresesContingentesMesTotal(
                        general.valorInteresesContingentesMesTotal()
                )

                .valorInteresesContingentesMesA1(
                        general.valorInteresesContingentesMesA1()
                )

                .valorInteresesContingentesMesPe(
                        general.valorInteresesContingentesMesPe()
                )


                // -------------------------------------------------
                // SEGUROS
                // -------------------------------------------------

                .saldoSegurosTotal(
                        general.saldoSegurosTotal()
                )

                .saldoSegurosA1(
                        general.saldoSegurosA1()
                )

                .saldoSegurosPe(
                        general.saldoSegurosPe()
                )

                .valorSegurosMesTotal(
                        general.valorSegurosMesTotal()
                )

                .valorSegurosMesA1(
                        general.valorSegurosMesA1()
                )

                .valorSegurosMesPe(
                        general.valorSegurosMesPe()
                )


                // -------------------------------------------------
                // ALIVIOS
                // -------------------------------------------------

                .saldoAliviosTotal(
                        general.saldoAliviosTotal()
                )

                .saldoAliviosA1(
                        general.saldoAliviosA1()
                )

                .saldoAliviosPe(
                        general.saldoAliviosPe()
                )

                .valorAliviosMesTotal(
                        general.valorAliviosMesTotal()
                )

                .valorAliviosMesA1(
                        general.valorAliviosMesA1()
                )

                .valorAliviosMesPe(
                        general.valorAliviosMesPe()
                )


                // -------------------------------------------------
                // COSTAS JUDICIALES
                // -------------------------------------------------

                .valorCostasJudicialesTotal(
                        general.valorCostasJudicialesTotal()
                )

                .valorCostasJudicialesA1(
                        general.valorCostasJudicialesA1()
                )

                .valorCostasJudicialesPe(
                        general.valorCostasJudicialesPe()
                )


                // -------------------------------------------------
                // APORTES
                // -------------------------------------------------

                .saldoAportesFechaCorteTotal(
                        general.saldoAportesFechaCorteTotal()
                )

                .saldoAportesFechaCorteA1(
                        general.saldoAportesFechaCorteA1()
                )

                .saldoAportesFechaCortePe(
                        general.saldoAportesFechaCortePe()
                )

                .valorAportesCreditoTotal(
                        general.valorAportesCreditoTotal()
                )

                .valorAportesCreditoA1(
                        general.valorAportesCreditoA1()
                )

                .valorAportesCreditoPe(
                        general.valorAportesCreditoPe()
                )


                // -------------------------------------------------
                // GARANTÍAS
                // -------------------------------------------------

                .valorGarantiasTotal(
                        general.valorGarantiasTotal()
                )

                .valorGarantiasA1(
                        general.valorGarantiasA1()
                )

                .valorGarantiasPe(
                        general.valorGarantiasPe()
                )

                .valorGarantiasCreditoTotal(
                        general.valorGarantiasCreditoTotal()
                )

                .valorGarantiasCreditoA1(
                        general.valorGarantiasCreditoA1()
                )

                .valorGarantiasCreditoPe(
                        general.valorGarantiasCreditoPe()
                )


                // -------------------------------------------------
                // FONDOS GARANTÍAS
                // -------------------------------------------------

                .valorFondosGarantiasTotal(
                        general.valorFondosGarantiasTotal()
                )

                .valorFondosGarantiasA1(
                        general.valorFondosGarantiasA1()
                )

                .valorFondosGarantiasPe(
                        general.valorFondosGarantiasPe()
                )


                // -------------------------------------------------
                // OTROS
                // -------------------------------------------------

                .valorOtrosConceptosTotal(
                        general.valorOtrosConceptosTotal()
                )

                .valorOtrosConceptosA1(
                        general.valorOtrosConceptosA1()
                )

                .valorOtrosConceptosPe(
                        general.valorOtrosConceptosPe()
                )


                // -------------------------------------------------
                // DETERIORO CAPITAL
                // -------------------------------------------------

                .deterioroCapitalTotal(
                        general.deterioroCapitalTotal()
                )

                .deterioroCapitalA1(
                        general.deterioroCapitalA1()
                )

                .deterioroCapitalPe(
                        general.deterioroCapitalPe()
                )


                // -------------------------------------------------
                // DETERIORO INTERESES
                // -------------------------------------------------

                .deterioroInteresesTotal(
                        general.deterioroInteresesTotal()
                )

                .deterioroInteresesA1(
                        general.deterioroInteresesA1()
                )

                .deterioroInteresesPe(
                        general.deterioroInteresesPe()
                )


                // -------------------------------------------------
                // DETERIORO OTROS
                // -------------------------------------------------

                .deterioroOtrosTotal(
                        general.deterioroOtrosTotal()
                )

                .deterioroOtrosA1(
                        general.deterioroOtrosA1()
                )

                .deterioroOtrosPe(
                        general.deterioroOtrosPe()
                )


                // -------------------------------------------------
                // PÉRDIDA ESPERADA
                // -------------------------------------------------

                .perdidaEsperadaTotal(
                        general.perdidaEsperadaTotal()
                )

                .perdidaEsperadaA1(
                        general.perdidaEsperadaA1()
                )

                .perdidaEsperadaPe(
                        general.perdidaEsperadaPe()
                )


                // -------------------------------------------------
                // DIFERENCIAS GENERALES
                // -------------------------------------------------

                .diferenciaSaldoActual(
                        diferenciaSaldoActual
                )

                .diferenciaInteresesCausados(
                        diferenciaInteresesCausados
                )

                .diferenciaInteresesContingentes(
                        diferenciaInteresesContingentes
                )

                .diferenciaSeguros(
                        diferenciaSeguros
                )

                .diferenciaAlivios(
                        diferenciaAlivios
                )

                .diferenciaCostasJudiciales(
                        diferenciaCostasJudiciales
                )

                .diferenciaAportes(
                        diferenciaAportes
                )

                .diferenciaGarantias(
                        diferenciaGarantias
                )

                .diferenciaFondosGarantias(
                        diferenciaFondosGarantias
                )

                .diferenciaOtrosConceptos(
                        diferenciaOtrosConceptos
                )

                .diferenciaDeterioroCapital(
                        diferenciaDeterioroCapital
                )

                .diferenciaDeterioroIntereses(
                        diferenciaDeterioroIntereses
                )

                .diferenciaDeterioroOtros(
                        diferenciaDeterioroOtros
                )

                .diferenciaPerdidaEsperada(
                        diferenciaPerdidaEsperada
                )


                // -------------------------------------------------
                // PE DETALLE
                // -------------------------------------------------

                .perdidaEsperadaPeDetalle(
                        peDetalle.perdidaEsperada()
                )

                .deterioroCapitalPeDetalle(
                        peDetalle.deterioroCapitalPe()
                )

                .deterioroInteresesPeDetalle(
                        peDetalle.deterioroInteresesPe()
                )

                .deterioroOtrosPeDetalle(
                        peDetalle.deterioroOtrosPe()
                )

                .deterioroTotalPeDetalle(
                        peDetalle.deterioroTotalPe()
                )


                // -------------------------------------------------
                // DIFERENCIAS PE
                // -------------------------------------------------

                .diferenciaPerdidaEsperadaPe(
                        diferenciaPerdidaEsperadaPe
                )

                .diferenciaDeterioroCapitalPe(
                        diferenciaDeterioroCapitalPe
                )

                .diferenciaDeterioroInteresesPe(
                        diferenciaDeterioroInteresesPe
                )

                .diferenciaDeterioroOtrosPe(
                        diferenciaDeterioroOtrosPe
                )


                // -------------------------------------------------
                // RESULTADO
                // -------------------------------------------------

                .mensaje(
                        mensaje
                )

                .build();
    }


    // =========================================================
    // DIFERENCIA:
    //
    // TOTAL - A1 - PE
    // =========================================================

    private BigDecimal diferencia(
            BigDecimal total,
            BigDecimal a1,
            BigDecimal pe
    ) {

        return nvl(
                total
        )
                .subtract(
                        nvl(
                                a1
                        )
                )
                .subtract(
                        nvl(
                                pe
                        )
                );
    }


    // =========================================================
    // DIFERENCIA ENTRE DOS VALORES
    // =========================================================

    private BigDecimal diferenciaDos(
            BigDecimal valor1,
            BigDecimal valor2
    ) {

        return nvl(
                valor1
        )
                .subtract(
                        nvl(
                                valor2
                        )
                );
    }


    // =========================================================
    // VALIDAR CERO
    //
    // compareTo evita problemas por escala:
    //
    // 0
    // 0.00
    // 0.000000
    //
    // se consideran iguales.
    // =========================================================

    private boolean esCero(
            BigDecimal valor
    ) {

        return nvl(
                valor
        ).compareTo(
                BigDecimal.ZERO
        ) == 0;
    }


    // =========================================================
    // NULL -> ZERO
    // =========================================================

    private BigDecimal nvl(
            BigDecimal valor
    ) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }


    // =========================================================
    // VALIDAR ID CIERRE
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