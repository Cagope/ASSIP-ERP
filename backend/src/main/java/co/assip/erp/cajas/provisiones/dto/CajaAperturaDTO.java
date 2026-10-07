package co.assip.erp.cajas.provisiones.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CajaAperturaDTO {

    private Long idCaja;
    private Integer idAgencia;
    private String codigoCaja;
    private String descripcionCaja;

    private LocalDate fechaJornada;

    private Long idProvision;
    private String estado;

    private BigDecimal efectivoInicio;
    private BigDecimal chequesInicio;

    private Boolean tieneProvision;
    private Boolean tieneCierreAnterior;

    private LocalDate fechaUltimoCierre;
}