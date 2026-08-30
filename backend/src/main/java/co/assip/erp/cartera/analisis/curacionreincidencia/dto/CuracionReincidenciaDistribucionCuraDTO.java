package co.assip.erp.cartera.analisis.curacionreincidencia.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CuracionReincidenciaDistribucionCuraDTO {

    private Integer orden;
    private String codigo;
    private String descripcion;

    private Integer episodios;
    private BigDecimal porcentajeEpisodios;
}
