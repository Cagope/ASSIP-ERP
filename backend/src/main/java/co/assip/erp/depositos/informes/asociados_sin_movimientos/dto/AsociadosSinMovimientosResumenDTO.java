package co.assip.erp.depositos.informes.asociados_sin_movimientos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AsociadosSinMovimientosResumenDTO {

    private Integer totalCuentas;

    private Integer totalConSaldo;
    private Integer totalSinSaldo;

    private BigDecimal saldoTotal;

}