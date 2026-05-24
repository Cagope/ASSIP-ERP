package co.assip.erp.cdat.cierre_mensual_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CdatCierreMensualPreviewDTO {

    private Long idCierreMensualCdat;

    private Integer idAgencia;

    private LocalDate fechaCorte;

    private Integer anio;
    private Integer mes;

    private Integer totalCdats;

    private BigDecimal totalValorApertura;
    private BigDecimal totalCapital;

    private BigDecimal totalCapitalAportes;
    private BigDecimal totalCapitalAhorros;

    private Boolean existeCierre;

    private String estado;

    private String observacion;

    private List<CdatCierreMensualItemDTO> items;
}