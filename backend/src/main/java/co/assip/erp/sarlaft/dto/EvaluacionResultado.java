package co.assip.erp.sarlaft.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 📊 EvaluacionResultado
 * ------------------------------------------------------------
 * Resumen de la evaluación SARLAFT para una operación.
 */
@Getter
@Setter
public class EvaluacionResultado {

    private boolean alerta;
    private String severidad;
    private String descripcion;
    private Long idAlerta;

    // ============================
    // PASO 12 — NUEVOS CAMPOS
    // ============================

    /** Código de la regla que disparó la alerta */
    private String nombreRegla;

    /** true → el frontend debe bloquear la operación */
    private Boolean bloqueaOperacion;

    /** Sugerencia para el usuario */
    private String accionRecomendada;
}
