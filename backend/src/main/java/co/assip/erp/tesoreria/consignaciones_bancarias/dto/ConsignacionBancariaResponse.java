package co.assip.erp.tesoreria.consignaciones_bancarias.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ConsignacionBancariaResponse {

    private Integer idAgencia;
    private LocalDate fechaConsignacion;
    private String numeroConsignacion;

    private Long idCuentaBancaria;
    private Integer codigoBanco;
    private String nombreBanco;
    private String numeroCuenta;
    private String nombreCuenta;

    private Integer cantidadCheques;
    private BigDecimal valorCheques;
    private BigDecimal valorEfectivo;
    private BigDecimal valorTotal;

    private String tipoComprobante;
    private String numeroComprobante;
    private String conceptoComprobante;
}
