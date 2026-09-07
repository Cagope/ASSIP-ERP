package co.assip.erp.depositos.analisis.concentracion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ConcentracionDepositosAgenciaDTO {

    private Integer idAgencia;

    private String codigoAgencia;

    private String nombreAgencia;

    private Integer cantidadCuentas;

    private Integer cantidadAsociados;

    private BigDecimal saldo;

    private BigDecimal porcentajeParticipacion;

}