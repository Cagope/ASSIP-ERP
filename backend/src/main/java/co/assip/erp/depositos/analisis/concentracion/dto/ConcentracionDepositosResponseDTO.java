package co.assip.erp.depositos.analisis.concentracion.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ConcentracionDepositosResponseDTO {

    /*
     * Resumen general del análisis.
     */
    private ConcentracionDepositosResumenDTO resumen;

    /*
     * Distribución por agencia.
     */
    private List<ConcentracionDepositosAgenciaDTO> agencias;

    /*
     * Distribución por forma de ahorro.
     */
    private List<ConcentracionDepositosFormaDTO> formas;

    /*
     * Indicadores de concentración:
     * TOP_10, TOP_20, TOP_50 y RESTO.
     */
    private List<ConcentracionDepositosTopDTO> concentracion;

    /*
     * Ranking completo de asociados,
     * ordenado de mayor a menor saldo.
     */
    private List<ConcentracionDepositosAsociadoDTO> asociados;
}