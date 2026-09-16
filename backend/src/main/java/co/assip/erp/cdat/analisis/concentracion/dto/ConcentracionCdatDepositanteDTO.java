package co.assip.erp.cdat.analisis.concentracion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ConcentracionCdatDepositanteDTO {

    private Integer posicion;

    private Long idDatosPersonal;
    private String documento;
    private String nombreCompleto;

    private Integer cantidadCdats;

    private BigDecimal saldoTotal;

    private BigDecimal participacion;
    private BigDecimal participacionAcumulada;

    private BigDecimal tasaPonderada;
    private BigDecimal plazoPonderadoMeses;
}