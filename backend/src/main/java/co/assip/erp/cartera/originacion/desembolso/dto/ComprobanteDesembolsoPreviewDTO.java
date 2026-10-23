
package co.assip.erp.cartera.originacion.desembolso.dto;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;

import java.math.BigDecimal;
import java.util.List;

public record ComprobanteDesembolsoPreviewDTO(
        String tipoComprobante,
        String concepto,
        List<MovimientoContablePreviewDTO> movimientos,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia,
        boolean cuadrado
) {
}
