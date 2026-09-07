package co.assip.erp.gerencia.dashboard_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardCdatResumenDTO {

    private Integer totalCdats;

    private BigDecimal valorTotalCaptado;

    /**
     * Tasa nominal anual promedio,
     * ponderada por saldo actual.
     */
    private BigDecimal promedioTasa;

    /**
     * Plazo promedio expresado en meses.
     */
    private BigDecimal promedioPlazo;

    /**
     * CDAT activos con vencimiento
     * entre hoy y los próximos 30 días.
     */
    private Integer vencen30Dias;

    /**
     * Cantidad de asociados distintos
     * con CDAT activos.
     */
    private Integer totalAsociados;
}