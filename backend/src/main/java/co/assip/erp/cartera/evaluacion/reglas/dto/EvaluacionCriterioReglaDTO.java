package co.assip.erp.cartera.evaluacion.reglas.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EvaluacionCriterioReglaDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idEvaluacionCriterioRegla;

    private Integer idEvaluacionCriterio;

    // =========================================================
    // Datos informativos del criterio
    // =========================================================

    private String codigoCriterio;

    private String nombreCriterio;

    // =========================================================
    // Configuración de la regla
    // =========================================================

    private String codigoRegla;

    private String nombreRegla;

    /**
     * C = Comparación por valor o código.
     * R = Comparación por rango.
     */
    private String tipoRegla;

    /**
     * 1 = Persona natural.
     * 2 = Persona jurídica.
     * NULL = No aplica.
     */
    private Short idTipoPersona;

    private BigDecimal valorComparacion;

    private BigDecimal valorDesde;

    private BigDecimal valorHasta;

    private BigDecimal puntaje;

    private Integer orden;

    private Boolean aplica;

    private Boolean activo;

    private String observaciones;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;
}