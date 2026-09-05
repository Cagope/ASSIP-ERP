package co.assip.erp.cartera.cierremensual.anexo2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenAnexo2DTO(

        // =====================================================
        // CIERRE
        // =====================================================

        Integer idCierreCartera,
        LocalDate fechaCorte,

        // =====================================================
        // MODELO PE
        // =====================================================

        Integer idModeloPe,
        String nombreModeloPe,

        // =====================================================
        // CALIFICACIÓN
        // =====================================================

        String calificacion,

        // =====================================================
        // CANTIDAD
        // =====================================================

        Integer cantidadCreditos,
        BigDecimal porcentajeCantidad,

        // =====================================================
        // SALDO
        // =====================================================

        BigDecimal saldoCapital,
        BigDecimal porcentajeSaldo,

        // =====================================================
        // PÉRDIDA ESPERADA
        // =====================================================

        BigDecimal perdidaEsperada,
        BigDecimal porcentajePerdidaEsperada

) {
}