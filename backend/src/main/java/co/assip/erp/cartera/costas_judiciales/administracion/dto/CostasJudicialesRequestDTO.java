
package co.assip.erp.cartera.costas_judiciales.administracion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos para previsualizar y registrar un cargo de costas judiciales.
 * Los identificadores provienen de selecciones del frontend.
 * Las cuentas contables, la agencia y el usuario se resuelven en backend.
 */
public record CostasJudicialesRequestDTO(
        Integer idCarteraCredito,
        LocalDate fechaMovimiento,
        String tipoComprobante,
        Long idDatosPersonalTercero,
        BigDecimal valor,
        String observacion
) {
}
