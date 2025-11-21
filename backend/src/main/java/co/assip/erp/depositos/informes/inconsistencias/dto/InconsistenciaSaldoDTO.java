package co.assip.erp.depositos.informes.inconsistencias.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class InconsistenciaSaldoDTO {

    private String codigoCuenta;
    private String codigoAgencia;
    private String nombreAgencia;

    private String codigoForma;

    private String documento;
    private String nombreCompleto;

    // ✅ Cambiados a BigDecimal
    private BigDecimal saldoTabla;
    private BigDecimal saldoMov;
    private BigDecimal diferencia;

    private Boolean negativo;
}
