package co.assip.erp.sarlaft.evaluacion.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SarlaftAlertaResultadoDTO {

    private boolean alerta;

    private String severidad;

    private String descripcion;

    private Long idAlerta;

    private String nombreRegla;

    private Boolean bloqueaOperacion;

    private String accionRecomendada;
}