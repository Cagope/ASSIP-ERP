package co.assip.erp.cartera.prorrogas_v1.dto;

import java.math.BigDecimal;
import java.util.List;
import co.assip.erp.cartera.pago_cuotas_v1.motor.dto.PagoCuotasMedioRecaudoDTO;

/**
 * Datos solicitados para simular y ejecutar una prórroga.
 * Los valores financieros serán validados por el servicio.
 */
public record ProrrogasV1RequestDTO(
        Integer idCarteraCredito,
        Integer mesesProrroga,
        BigDecimal valorInteresCorriente,
        BigDecimal valorInteresMora,
        BigDecimal valorCapital,
        BigDecimal valorSeguro,
        BigDecimal valorAportes,
        BigDecimal valorFondoGarantia,
        BigDecimal valorOtrosConceptos,
        List<PagoCuotasMedioRecaudoDTO> mediosRecaudo,
        String observacion
) {
}