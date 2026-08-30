package co.assip.erp.cartera.analisis.moratemprana.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MoraTempranaPrimeraMoraDTO {

    private String evento;
    private Integer mob;

    private Integer cantidadCreditos;
    private BigDecimal porcentajeCreditos;
}