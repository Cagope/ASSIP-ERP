package co.assip.erp.depositos.informes.promedios.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PromediosFormaDTO {

    private String codigoForma;

    private String nombreForma;

    private Integer totalCuentas;

    private BigDecimal totalSaldos;

    private BigDecimal saldoPromedio;

    private BigDecimal saldoMayor;

    private BigDecimal saldoMenor;

}