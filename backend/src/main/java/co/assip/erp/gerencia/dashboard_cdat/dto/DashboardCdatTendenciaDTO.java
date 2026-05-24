package co.assip.erp.gerencia.dashboard_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardCdatTendenciaDTO {

    private String periodo;

    private Integer aperturas;

    private Integer cancelaciones;

    private Integer renovaciones;

    private BigDecimal captacionNeta;

}