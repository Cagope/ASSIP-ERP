package co.assip.erp.cajas.cierre.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CajaCierreResumenDTO {

    private Long idProvision;
    private Long idCaja;
    private Integer idAgencia;

    private String codigoCaja;
    private String descripcionCaja;
    private String tipoCaja;

    private LocalDate fechaContable;
    private String estado;

    private BigDecimal efectivoInicio;
    private BigDecimal chequesInicio;

    private CajaCierreMovimientosDTO movimientos;

    private List<CajaCierreMovimientoResumenDTO> resumenMovimientos;

    private List<CajaCierreComprobantesResumenDTO> resumenComprobantes;

    private CajaCierreVentanillaDTO ventanilla;

    private BigDecimal efectivoFinCalculado;
    private BigDecimal chequesFinCalculado;
    private BigDecimal totalFisicoFinal;

    private Integer movimientosPendientes;
}