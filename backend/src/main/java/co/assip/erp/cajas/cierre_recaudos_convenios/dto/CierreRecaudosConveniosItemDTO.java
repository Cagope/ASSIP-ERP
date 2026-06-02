package co.assip.erp.cajas.cierre_recaudos_convenios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CierreRecaudosConveniosItemDTO {

    private Long idRecaudoConvenio;
    private LocalDate fechaRecaudo;
    private String documentoSoporte;
    private BigDecimal valorRecaudo;
}