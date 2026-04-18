package co.assip.erp.nomina.liquidacion_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidacionV2DetalleDTO {

    /**
     * Tipo de movimiento:
     * DEVENGADO | DEDUCCION | PROVISION
     */
    private String tipo;

    /**
     * Código del concepto de nómina
     */
    private String codigoConcepto;

    /**
     * Nombre del concepto (opcional para UI/reportes)
     */
    private String nombreConcepto;

    /**
     * Cantidad (días, horas, unidades)
     */
    private BigDecimal cantidad;

    /**
     * Valor unitario
     */
    private BigDecimal valorUnitario;

    /**
     * Valor total
     */
    private BigDecimal valorTotal;

    /**
     * Base de cálculo utilizada
     */
    private BigDecimal baseCalculo;

    /**
     * Indica si el concepto proviene de novedad o es automático
     */
    private String origen; // AUTOMATICO | NOVEDAD

    /**
     * Id de la novedad asociada (si aplica)
     */
    private Integer idNovedad;
}