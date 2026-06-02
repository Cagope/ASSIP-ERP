package co.assip.erp.cajas.recaudos_convenios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecaudoConvenioRequestDTO {

    private Long idProvision;
    private Long idConvenio;
    private String documentoSoporte;
    private BigDecimal valorRecaudo;
}