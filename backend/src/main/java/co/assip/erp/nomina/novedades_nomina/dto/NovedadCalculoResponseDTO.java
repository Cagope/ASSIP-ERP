package co.assip.erp.nomina.novedades_nomina.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovedadCalculoResponseDTO {

    private BigDecimal valorCalculado;

}
