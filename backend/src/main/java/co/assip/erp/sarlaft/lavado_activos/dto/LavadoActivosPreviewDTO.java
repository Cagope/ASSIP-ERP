package co.assip.erp.sarlaft.lavado_activos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LavadoActivosPreviewDTO {

    private Integer idAgencia;
    private BigDecimal valorTransaccion;

    private BigDecimal montoControl;

    private Boolean requiereFormato;

    private String mensaje;
}