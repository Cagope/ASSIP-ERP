package co.assip.erp.cartera.calculosprevios.dto;

import java.math.BigDecimal;

public record ResumenAportesGarantiasDTO(

        // =====================================================
        // APORTES
        // =====================================================

        Integer creditosConAportes,

        Integer creditosSinAportes,

        BigDecimal saldoAportesDisponible,

        BigDecimal valorAportesProrrateado,


        // =====================================================
        // GARANTÍAS
        // =====================================================

        Integer creditosConGarantia,

        Integer creditosSinGarantia,

        Integer cantidadBienesGarantia,

        BigDecimal valorBienesGarantia,

        BigDecimal valorGarantiasAsignado

) {
}