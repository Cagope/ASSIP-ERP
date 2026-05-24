package co.assip.erp.depositos.cierre_mensual_depositos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CierreMensualDepositosResumenFormaDTO {

    private Integer idAgencia;

    private Integer idFormaAhorro;

    private String codigoForma;

    private String nombreForma;

    private Integer cantidadCuentas;

    private BigDecimal saldoTotal;

    private BigDecimal totalDebitos;

    private BigDecimal totalCreditos;

    private Integer hombres;

    private Integer mujeres;

    private Integer juridicas;

}