package co.assip.erp.nomina.liquidacion.dto;

import java.util.List;

public record LiquidacionPreviewResponseDTO(

        // 🔑 Período realmente usado
        Integer idPeriodoNomina,
        Integer anio,
        Integer mes,
        Integer numeroPeriodo,
        String tipoPeriodo,

        // 🔍 Info de origen
        String origenPeriodo, // "AUTOMATICO"

        // 📦 Resultado actual (sin cambios)
        List<?> contratos

) {}