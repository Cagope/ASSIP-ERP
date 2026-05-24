package co.assip.erp.cdat.causacion_mensual_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CdatCausacionMensualPreviewDTO {

    private Long idCausacionMensualInteres;
    private Long idCierreMensualCdat;

    private Integer idAgencia;
    private LocalDate fechaCorte;
    private LocalDate fechaContable;

    private Integer anio;
    private Integer mes;

    private String tipoComprobante;
    private String numeroComprobante;

    private Integer totalCdats;

    private BigDecimal totalCapital;
    private BigDecimal totalInteres;
    private BigDecimal totalRetencion;
    private BigDecimal totalNeto;

    private Boolean existeCausacion;
    private String estado;

    private List<CdatCausacionMensualItemDTO> items;
}