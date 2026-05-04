package co.assip.erp.cdat.cdats.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CdatListDTO {

    private Long idCuentaCdat;

    private String codigoCdat;

    private Integer idAgencia;

    private Integer idDatosPersonal;
    private String nombreCompleto;

    private LocalDate fechaAperturaCdat;
    private LocalDate fechaVencimientoCdat;

    private Integer plazoMeses;

    private BigDecimal valorAperturaCdat;
    private BigDecimal saldoActualCdat;

    private BigDecimal tasaEfectivaAnual;

    private String estadoCdat;

    private String origenCdat;
}