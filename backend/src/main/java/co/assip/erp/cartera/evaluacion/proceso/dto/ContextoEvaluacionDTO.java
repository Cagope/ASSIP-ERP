package co.assip.erp.cartera.evaluacion.proceso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContextoEvaluacionDTO {

    // =========================================================
    // Evaluación
    // =========================================================

    private EvaluacionCarteraDTO evaluacion;

    // =========================================================
    // Fecha de corte
    // =========================================================

    private LocalDate fechaCorte;

    // =========================================================
    // Créditos disponibles para evaluar
    // =========================================================

    private List<EvaluacionCreditoDTO> creditos;
}