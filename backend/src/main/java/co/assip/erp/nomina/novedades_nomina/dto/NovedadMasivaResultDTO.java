package co.assip.erp.nomina.novedades_nomina.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovedadMasivaResultDTO {

    private Integer totalContratos;
    private Integer insertados;
    private Integer omitidos;
}
