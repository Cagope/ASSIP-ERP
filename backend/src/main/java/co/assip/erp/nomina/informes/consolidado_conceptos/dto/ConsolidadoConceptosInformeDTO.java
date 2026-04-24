package co.assip.erp.nomina.informes.consolidado_conceptos.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsolidadoConceptosInformeDTO {

    private String codigoConcepto;
    private String nombreConcepto;
    private String tipoConcepto;

    private Long cantidadRegistros;
    private BigDecimal totalCantidad;
    private BigDecimal totalValor;
}