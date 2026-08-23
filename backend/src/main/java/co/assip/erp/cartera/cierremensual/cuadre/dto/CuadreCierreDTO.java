package co.assip.erp.cartera.cierremensual.cuadre.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CuadreCierreDTO {

    // =========================================================
    // IDENTIFICACIÓN
    // =========================================================

    private Integer idCierreCartera;

    private String estado;

    private boolean cuadrado;


    // =========================================================
    // CANTIDADES
    // =========================================================

    private Integer cantidadCreditosTotal;

    private Integer cantidadCreditosA1;

    private Integer cantidadCreditosPe;


    // =========================================================
    // SALDOS PRINCIPALES
    // =========================================================

    private BigDecimal saldoActualTotal;

    private BigDecimal saldoActualA1;

    private BigDecimal saldoActualPe;


    // =========================================================
    // INTERESES CAUSADOS
    // =========================================================

    private BigDecimal saldoInteresesCausadosTotal;

    private BigDecimal saldoInteresesCausadosA1;

    private BigDecimal saldoInteresesCausadosPe;

    private BigDecimal valorInteresesCausadosMesTotal;

    private BigDecimal valorInteresesCausadosMesA1;

    private BigDecimal valorInteresesCausadosMesPe;


    // =========================================================
    // INTERESES CONTINGENTES
    // =========================================================

    private BigDecimal saldoInteresesContingentesTotal;

    private BigDecimal saldoInteresesContingentesA1;

    private BigDecimal saldoInteresesContingentesPe;

    private BigDecimal valorInteresesContingentesMesTotal;

    private BigDecimal valorInteresesContingentesMesA1;

    private BigDecimal valorInteresesContingentesMesPe;


    // =========================================================
    // SEGUROS
    // =========================================================

    private BigDecimal saldoSegurosTotal;

    private BigDecimal saldoSegurosA1;

    private BigDecimal saldoSegurosPe;

    private BigDecimal valorSegurosMesTotal;

    private BigDecimal valorSegurosMesA1;

    private BigDecimal valorSegurosMesPe;


    // =========================================================
    // ALIVIOS
    // =========================================================

    private BigDecimal saldoAliviosTotal;

    private BigDecimal saldoAliviosA1;

    private BigDecimal saldoAliviosPe;

    private BigDecimal valorAliviosMesTotal;

    private BigDecimal valorAliviosMesA1;

    private BigDecimal valorAliviosMesPe;


    // =========================================================
    // COSTAS JUDICIALES
    // =========================================================

    private BigDecimal valorCostasJudicialesTotal;

    private BigDecimal valorCostasJudicialesA1;

    private BigDecimal valorCostasJudicialesPe;


    // =========================================================
    // APORTES
    // =========================================================

    private BigDecimal saldoAportesFechaCorteTotal;

    private BigDecimal saldoAportesFechaCorteA1;

    private BigDecimal saldoAportesFechaCortePe;

    private BigDecimal valorAportesCreditoTotal;

    private BigDecimal valorAportesCreditoA1;

    private BigDecimal valorAportesCreditoPe;


    // =========================================================
    // GARANTÍAS
    // =========================================================

    private BigDecimal valorGarantiasTotal;

    private BigDecimal valorGarantiasA1;

    private BigDecimal valorGarantiasPe;

    private BigDecimal valorGarantiasCreditoTotal;

    private BigDecimal valorGarantiasCreditoA1;

    private BigDecimal valorGarantiasCreditoPe;


    // =========================================================
    // OTROS CONCEPTOS
    // =========================================================

    private BigDecimal valorFondosGarantiasTotal;

    private BigDecimal valorFondosGarantiasA1;

    private BigDecimal valorFondosGarantiasPe;

    private BigDecimal valorOtrosConceptosTotal;

    private BigDecimal valorOtrosConceptosA1;

    private BigDecimal valorOtrosConceptosPe;


    // =========================================================
    // DETERIORO CAPITAL
    // =========================================================

    private BigDecimal deterioroCapitalTotal;

    private BigDecimal deterioroCapitalA1;

    private BigDecimal deterioroCapitalPe;


    // =========================================================
    // DETERIORO INTERESES
    // =========================================================

    private BigDecimal deterioroInteresesTotal;

    private BigDecimal deterioroInteresesA1;

    private BigDecimal deterioroInteresesPe;


    // =========================================================
    // DETERIORO OTROS
    // =========================================================

    private BigDecimal deterioroOtrosTotal;

    private BigDecimal deterioroOtrosA1;

    private BigDecimal deterioroOtrosPe;


    // =========================================================
    // PÉRDIDA ESPERADA
    // =========================================================

    private BigDecimal perdidaEsperadaTotal;

    private BigDecimal perdidaEsperadaA1;

    private BigDecimal perdidaEsperadaPe;


    // =========================================================
    // CONTROLES A1 + PE = TOTAL
    // =========================================================

    private BigDecimal diferenciaSaldoActual;

    private BigDecimal diferenciaInteresesCausados;

    private BigDecimal diferenciaInteresesContingentes;

    private BigDecimal diferenciaSeguros;

    private BigDecimal diferenciaAlivios;

    private BigDecimal diferenciaCostasJudiciales;

    private BigDecimal diferenciaAportes;

    private BigDecimal diferenciaGarantias;

    private BigDecimal diferenciaFondosGarantias;

    private BigDecimal diferenciaOtrosConceptos;

    private BigDecimal diferenciaDeterioroCapital;

    private BigDecimal diferenciaDeterioroIntereses;

    private BigDecimal diferenciaDeterioroOtros;

    private BigDecimal diferenciaPerdidaEsperada;


    // =========================================================
    // CONTROLES PE CONTRA pe_resultados
    // =========================================================

    private BigDecimal perdidaEsperadaPeDetalle;

    private BigDecimal deterioroCapitalPeDetalle;

    private BigDecimal deterioroInteresesPeDetalle;

    private BigDecimal deterioroOtrosPeDetalle;

    private BigDecimal deterioroTotalPeDetalle;


    // =========================================================
    // DIFERENCIAS PE
    // =========================================================

    private BigDecimal diferenciaPerdidaEsperadaPe;

    private BigDecimal diferenciaDeterioroCapitalPe;

    private BigDecimal diferenciaDeterioroInteresesPe;

    private BigDecimal diferenciaDeterioroOtrosPe;


    // =========================================================
    // RESULTADO FINAL
    // =========================================================

    private String mensaje;
}