package co.assip.erp.depositos.informes.saldos_rangos_edad.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SaldosRangosEdadItemDTO {

    private Integer idAgencia;
    private String nombreAgencia;

    private String documento;
    private String nombreCompleto;

    private Integer edad;
    private String nombreRango;

    private String ocupacion;
    private String sectorEconomico;

    private BigDecimal salario;
    private BigDecimal otrosIngresos;
    private BigDecimal totalIngresos;

    private String ciudad;
    private String celular;
    private String correo;

    private BigDecimal saldoAportes;

}