package co.assip.erp.depositos.analisis.concentracion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ConcentracionDepositosTopDTO {

    /*
     * Grupo de concentración:
     * TOP_10, TOP_20, TOP_50 o RESTO.
     */
    private String grupo;

    /*
     * Cantidad de asociados incluidos en el grupo.
     */
    private Integer cantidadAsociados;

    /*
     * Saldo consolidado correspondiente al grupo.
     */
    private BigDecimal saldo;

    /*
     * Participación del grupo sobre el saldo total
     * de la población analizada.
     */
    private BigDecimal porcentajeParticipacion;
}