package co.assip.erp.cartera.evaluacion.proceso.motor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionEdadRiesgoResultadoDTO {

    // =========================================================
    // Edad calculada por puntaje
    // =========================================================

    private String edadRiesgoCalculada;

    // =========================================================
    // Edad resultante después de aplicar condiciones
    // individuales como mora y crédito evaluado
    // =========================================================

    private String edadRiesgoArrastre;

    // =========================================================
    // Comentario técnico del cálculo individual
    // =========================================================

    private String comentarioEvaluacion;
}