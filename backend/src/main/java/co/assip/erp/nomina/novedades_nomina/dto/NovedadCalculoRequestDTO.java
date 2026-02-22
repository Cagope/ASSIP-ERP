package co.assip.erp.nomina.novedades_nomina.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovedadCalculoRequestDTO {

    private Integer idPeriodo;
    private Integer idContrato;
    private String codigoConcepto;
    private BigDecimal cantidad;

}
