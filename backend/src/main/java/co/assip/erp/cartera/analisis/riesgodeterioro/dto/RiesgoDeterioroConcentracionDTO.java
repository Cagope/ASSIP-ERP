package co.assip.erp.cartera.analisis.riesgodeterioro.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RiesgoDeterioroConcentracionDTO {

    private Integer posicion;

    private Integer idCarteraCredito;
    private Integer idDatosPersonal;

    private Integer idAgencia;

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private String pagareCartera;

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private BigDecimal saldoCartera;

    private Integer diasMora;
    private String edadContable;

    private BigDecimal vea;
    private BigDecimal pi;
    private BigDecimal pdi;

    private BigDecimal perdidaEsperada;
    private BigDecimal deterioroTotal;
    private BigDecimal exposicionTotal;

    private BigDecimal valorAportesCredito;
    private BigDecimal valorGarantiasCredito;

    private BigDecimal porcentajeSobreTotal;

    private BigDecimal porcentajeAcumulado;
}