package co.assip.erp.cartera.evaluacion.criterios.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EvaluacionCriterioDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idEvaluacionCriterio;

    // =========================================================
    // Datos del criterio
    // =========================================================

    private String codigoProceso;

    private String codigoCriterio;

    private String nombreCriterio;

    private String descripcionCriterio;

    /**
     * A = Asociado
     * C = Crédito
     */
    private String nivelAplicacion;

    /**
     * R = Comparación por rango
     * C = Comparación por código o valor
     */
    private String tipoComparacion;

    private Integer ordenEvaluacion;

    private BigDecimal puntajeMaximo;

    private Boolean aplica;

    private Boolean activo;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;
}