package co.assip.erp.contabilidad.informes.especiales.comprobante_resumen.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Resultado consolidado del comprobante resumen. */
public record ComprobanteResumenResponseDTO(
        Integer idAgencia,
        LocalDate fechaInicial,
        LocalDate fechaFinal,
        List<ComprobanteResumenFilaDTO> filas,
        int cantidadCuentas,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos
) {}
