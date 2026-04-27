package co.assip.erp.nomina.conceptos_nomina.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoNominaDTO {

    private String codigoConcepto;
    private String nombreConcepto;
    private String tipoConcepto;

    private Boolean esFijo;
    private Boolean activo;

    private String tipoCalculo;
    private String baseCalculo;
    private BigDecimal multiplicador;

    private Boolean afectaIbc;
    private Boolean afectaBaseCesantias;
    private Boolean afectaBasePrimaLegal;
    private Boolean afectaBaseVacaciones;
    private Boolean afectaBasePrimaSemestral;
    private Boolean afectaBaseArl;
    private Boolean afectaBaseParafiscales;

    private BigDecimal smmlvDesde;
    private BigDecimal smmlvHasta;
}