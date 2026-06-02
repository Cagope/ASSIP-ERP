package co.assip.erp.cajas.cierre_recaudos_convenios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CierreRecaudosConveniosRequestDTO {

    private Long idCaja;
    private LocalDate fechaContable;

    private Long idConvenio;

    private String documentoSoporte;
}