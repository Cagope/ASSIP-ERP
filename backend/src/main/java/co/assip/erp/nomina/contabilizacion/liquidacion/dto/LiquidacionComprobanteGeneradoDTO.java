package co.assip.erp.nomina.contabilizacion.liquidacion.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidacionComprobanteGeneradoDTO {

    private Integer idAgencia;
    private String tipoComprobante;
    private String numeroComprobante;
    private String conceptoComprobante;
    private LocalDate fechaComprobante;

    private Integer cantidadMovimientos;
    private BigDecimal totalDebito;
    private BigDecimal totalCredito;
}