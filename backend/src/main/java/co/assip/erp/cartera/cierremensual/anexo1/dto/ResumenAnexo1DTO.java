package co.assip.erp.cartera.cierremensual.anexo1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenAnexo1DTO(

        // =====================================================
        // CIERRE
        // =====================================================

        Integer idCierreCartera,
        LocalDate fechaCorte,

        // =====================================================
        // POBLACIÓN
        // =====================================================

        Integer cantidadCreditos,

        BigDecimal saldoCapital,

        // =====================================================
        // CAUSACIÓN DE INTERESES
        // =====================================================

        Integer cantidadCreditosConInteresesCausados,
        BigDecimal valorInteresesCausadosMes,
        BigDecimal saldoInteresesCausados,

        Integer cantidadCreditosConInteresesContingentes,
        BigDecimal valorInteresesContingentesMes,
        BigDecimal saldoInteresesContingentes,

        // =====================================================
        // EDAD CONTABLE
        // =====================================================

        Integer cantidadEdadA,
        Integer cantidadEdadB,
        Integer cantidadEdadC,
        Integer cantidadEdadD,
        Integer cantidadEdadE,

        // =====================================================
        // COBERTURAS
        // =====================================================

        BigDecimal valorAportesAplicados,
        BigDecimal valorGarantiasAsignadas,
        BigDecimal valorGarantiasReconocidas,

        // =====================================================
        // DETERIORO
        // =====================================================

        BigDecimal baseDeterioroCapital,
        BigDecimal deterioroCapital,
        BigDecimal deterioroIntereses,
        BigDecimal deterioroTotal

) {
}