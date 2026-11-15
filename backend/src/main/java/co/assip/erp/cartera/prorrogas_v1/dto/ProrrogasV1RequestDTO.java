
package co.assip.erp.cartera.prorrogas_v1.dto;

import co.assip.erp.cartera.pago_cuotas_v1.motor.dto.PagoCuotasMedioRecaudoDTO;
import java.math.BigDecimal;
import java.util.List;

/**
 * Datos solicitados para simular y ejecutar una prórroga.
 * Los importes financieros se validan en el servicio.
 * Seguros, alivios y costas judiciales son recaudos independientes.
 */
public record ProrrogasV1RequestDTO(
        Integer idCarteraCredito,
        Integer mesesProrroga,
        BigDecimal valorInteresCorriente,
        BigDecimal valorInteresMora,
        BigDecimal valorCapital,
        BigDecimal valorSeguro,
        BigDecimal valorAlivios,
        BigDecimal valorCostasJudiciales,
        List<PagoCuotasMedioRecaudoDTO> mediosRecaudo,
        String observacion
) {
}
