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
public class LiquidacionV2RequestDTO {

    /**
     * Agencia a liquidar.
     * Obligatoria cuando se ejecuta el proceso.
     */
    private Integer fkAgencia;

    /**
     * Período de nómina a liquidar.
     * Si viene null, el service puede tomar el período operativo abierto.
     */
    private Integer idPeriodoNomina;
}