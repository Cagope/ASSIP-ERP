package co.assip.erp.gerencia.dashboard_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DashboardCdatTendenciaDTO {

    private LocalDate fechaCorte;

    private String periodo;

    private Integer cantidadCdats;

    private BigDecimal valorCaptado;
}