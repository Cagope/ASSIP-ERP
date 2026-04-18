package co.assip.erp.nomina.liquidacion_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidacionV2ResponseDTO {

    /**
     * Período procesado
     */
    private Integer idPeriodoNomina;

    /**
     * Agencia procesada
     */
    private Integer fkAgencia;

    /**
     * Cantidad de contratos procesados
     */
    private Integer totalContratos;

    /**
     * Mensaje del proceso
     */
    private String mensaje;
}