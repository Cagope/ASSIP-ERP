package co.assip.erp.cartera.analisis.curacionreincidencia.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CuracionReincidenciaSegmentoDTO {

    private Integer id;
    private String codigo;
    private String descripcion;

    private Integer episodios;
    private Integer episodiosCurados;
    private Integer episodiosAbiertos;

    private BigDecimal tasaCuraObservada;
    private BigDecimal mesesPromedioCura;

    private Integer curasSeguimiento6m;
    private Integer reincidentes6m;
    private BigDecimal tasaReincidencia6m;
    private BigDecimal mesesPromedioReincidencia;
}
