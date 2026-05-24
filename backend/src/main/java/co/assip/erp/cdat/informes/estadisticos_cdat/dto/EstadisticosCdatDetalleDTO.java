package co.assip.erp.cdat.informes.estadisticos_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class EstadisticosCdatDetalleDTO {

    private Long idCuentaCdat;

    private String codigoCdat;

    private String documento;

    private String nombreCompleto;

    private String agencia;

    private LocalDate fechaAperturaCdat;

    private LocalDate fechaVencimientoCdat;

    private Integer plazoMeses;

    private BigDecimal tasaNominalAnual;

    private BigDecimal saldoActualCdat;

    private String estadoCdat;
}