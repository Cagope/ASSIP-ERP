package co.assip.erp.depositos.informes.extracto_asociado.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExtractoAsociadoCuentaDTO {

    private Integer idCuentaAhorro;

    private String codigoCuenta;

    private String codigoForma;
    private String nombreForma;

    private String nombreAgencia;

    private BigDecimal saldoInicial;
    private BigDecimal totalCreditos;
    private BigDecimal totalDebitos;
    private BigDecimal saldoFinal;

}