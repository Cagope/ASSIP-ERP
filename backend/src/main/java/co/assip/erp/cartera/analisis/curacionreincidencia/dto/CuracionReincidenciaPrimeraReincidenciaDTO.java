package co.assip.erp.cartera.analisis.curacionreincidencia.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CuracionReincidenciaPrimeraReincidenciaDTO {

    private Integer mes;
    private Integer reincidentes;
    private BigDecimal porcentajeSobreCurasMaduras;
}
