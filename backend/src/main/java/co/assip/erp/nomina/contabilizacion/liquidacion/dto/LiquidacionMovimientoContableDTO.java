package co.assip.erp.nomina.contabilizacion.liquidacion.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidacionMovimientoContableDTO {

    private Integer idAgencia;

    private Integer idCatalogoCuenta;
    private String codigoCuenta;
    private String nombreCuenta;

    private Integer idTercero;
    private String nombreTercero;

    // Para auditoría: empleado origen del valor
    private Integer idEmpleadoReferencia;
    private String nombreEmpleadoReferencia;
    private String documentoTercero;
    private BigDecimal debito;
    private BigDecimal credito;
    private BigDecimal valorBase;
}