package co.assip.erp.sarlaft.evaluacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EvaluacionResultado {

    private boolean alerta;

    private String severidad;

    private String descripcion;

    private Long idAlerta;

    private String nombreRegla;

    private Boolean bloqueaOperacion;

    private String accionRecomendada;

    private List<SarlaftAlertaResultadoDTO> alertas = new ArrayList<>();
}