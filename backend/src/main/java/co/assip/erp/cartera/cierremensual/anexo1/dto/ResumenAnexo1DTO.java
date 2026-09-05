package co.assip.erp.cartera.cierremensual.anexo1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenAnexo1DTO(

        // =====================================================
        // CIERRE
        // =====================================================

        Integer idCierreCartera,
        LocalDate fechaCorte,

        // =====================================================
        // TOTAL GENERAL
        // =====================================================

        Integer cantidadCreditos,
        BigDecimal saldoCapital,

        // =====================================================
        // DETALLE POR EDAD CONTABLE
        // A, B, C, D, E y TOTAL
        // =====================================================

        List<EdadAnexo1DTO> edades

) {

    public record EdadAnexo1DTO(

            // =================================================
            // EDAD
            // =================================================

            String edadContable,

            // =================================================
            // POBLACIÓN
            // =================================================

            Integer cantidadCreditos,
            BigDecimal saldoCapital,

            // =================================================
            // COBERTURAS
            // =================================================

            BigDecimal valorAportesAplicados,
            BigDecimal valorGarantiasAsignadas,
            BigDecimal valorGarantiasReconocidas,

            // =================================================
            // CAUSACIÓN
            // =================================================

            Integer cantidadCreditosConInteresesCausados,
            BigDecimal valorInteresesCausadosMes,

            // =================================================
            // CONTINGENTES
            // =================================================

            Integer cantidadCreditosConInteresesContingentes,
            BigDecimal valorInteresesContingentesMes,

            // =================================================
            // DETERIORO
            // =================================================

            BigDecimal baseDeterioroCapital,
            BigDecimal deterioroCapital,
            BigDecimal deterioroIntereses,
            BigDecimal deterioroTotal

    ) {
    }
}