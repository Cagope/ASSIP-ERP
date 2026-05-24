package co.assip.erp.shared.cuentas_ahorro.extracto.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExtractoCuentaSharedResumenDTO {

    private Integer idCuentaAhorro;

    private String codigoCuenta;

    private String documento;
    private String nombreCompleto;

    private String direccion;

    private String codigoForma;
    private String nombreForma;

    private String nombreAgencia;

    private java.math.BigDecimal saldoInicial;
    private java.math.BigDecimal totalCreditos;
    private java.math.BigDecimal totalDebitos;
    private java.math.BigDecimal saldoFinal;
}