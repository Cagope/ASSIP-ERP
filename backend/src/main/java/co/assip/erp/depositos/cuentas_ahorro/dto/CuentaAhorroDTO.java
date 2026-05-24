package co.assip.erp.depositos.cuentas_ahorro.dto;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class CuentaAhorroDTO {

    private Integer idCuentaAhorro;
    private String codigoCuenta;

    private Integer idAgencia;      // ⭐ NUEVO: requerido por el Repository
    private String nombreAgencia;

    private String nombreTitular;   // ⭐ NUEVO: requerido por el Repository

    private BigDecimal saldoActual;     // ⭐ NUEVO: requerido por el Repository

    private String nombreForma;     // ya existía
    private String fechaApertura;   // ya existía
}
