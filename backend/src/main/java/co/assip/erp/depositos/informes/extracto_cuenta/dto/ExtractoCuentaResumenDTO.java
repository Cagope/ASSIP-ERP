package co.assip.erp.depositos.informes.extracto_cuenta.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ExtractoCuentaResumenDTO {

    // EMPRESA
    private String razonSocial;
    private String siglaEmpresa;

    private String documentoEmpresa;
    private String digitoVerificacion;

    private String telefonoEmpresa;
    private String celularEmpresa;

    private String sitioWebEmpresa;

    private String logoUrl;

    // CUENTA
    private Integer idCuentaAhorro;

    private String codigoCuenta;

    // ASOCIADO
    private String documento;

    private String nombreCompleto;

    private String direccion;

    // DEPÓSITO
    private String codigoForma;
    private String nombreForma;

    // AGENCIA
    private String nombreAgencia;

    // RESUMEN
    private BigDecimal saldoInicial;

    private BigDecimal totalCreditos;
    private BigDecimal totalDebitos;

    private BigDecimal saldoFinal;

}