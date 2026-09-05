package co.assip.erp.cartera.cierremensual.anexo2.dto;

import java.time.LocalDate;

public record MoraAnexo2DTO(

        // =====================================================
        // CIERRE
        // =====================================================

        Integer idCierreCartera,
        LocalDate fechaCorte,

        // =====================================================
        // CRÉDITO
        // =====================================================

        Integer idCarteraCredito,
        Integer idCierreCarteraCredito,

        String pagareCartera,
        String documento,
        String nombreCompleto,

        // =====================================================
        // MODELO PE
        // =====================================================

        Integer idModeloPe,
        String nombreModeloPe,

        // =====================================================
        // VECTOR HISTÓRICO DE MORA
        // =====================================================

        Integer periodo,
        LocalDate fechaReferencia,
        Integer diasMora

) {
}