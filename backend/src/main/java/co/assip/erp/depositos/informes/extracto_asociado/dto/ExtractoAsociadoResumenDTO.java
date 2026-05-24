package co.assip.erp.depositos.informes.extracto_asociado.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ExtractoAsociadoResumenDTO {

    // EMPRESA
    private String razonSocial;
    private String siglaEmpresa;
    private String documentoEmpresa;
    private String digitoVerificacion;
    private String telefonoEmpresa;
    private String celularEmpresa;
    private String sitioWebEmpresa;
    private String logoUrl;

    // ASOCIADO
    private Integer idDatosPersonal;

    private String documento;
    private String nombreCompleto;
    private String direccion;

    // RESUMEN CONSOLIDADO
    private Integer totalCuentas;

    private BigDecimal saldoInicial;
    private BigDecimal totalCreditos;
    private BigDecimal totalDebitos;
    private BigDecimal saldoFinal;

}