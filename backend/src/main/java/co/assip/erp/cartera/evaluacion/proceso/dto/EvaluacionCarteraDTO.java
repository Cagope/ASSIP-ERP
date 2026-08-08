package co.assip.erp.cartera.evaluacion.proceso.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EvaluacionCarteraDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idEvaluacionCartera;

    // =========================================================
    // Información de la evaluación
    // =========================================================

    private LocalDate fechaCorte;

    // =========================================================
    // Ejecución
    // =========================================================

    private LocalDateTime fechaEjecucion;

    /**
     * P = En proceso.
     * D = Definitiva.
     */
    private String estado;

    private String versionMetodologia;

    // =========================================================
    // Resumen
    // =========================================================

    private Integer cantidadCreditos;

    private Integer cantidadAsociados;

    private BigDecimal saldoTotalEvaluado;

    private Integer cantidadRecalificados;

    private Integer cantidadHabilitados;

    private Integer cantidadMantenidos;

    // =========================================================
    // Comité de Riesgos
    // =========================================================

    private LocalDate fechaComiteRiesgos;

    private String numeroActaRiesgos;

    // =========================================================
    // Consejo de Administración
    // =========================================================

    private LocalDate fechaConsejo;

    private String numeroActaConsejo;

    private String observaciones;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;

}