package co.assip.erp.reporting.api;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 📊 ReportResult
 * ==============================================================
 * Representa el resultado tabular devuelto por una consulta
 * del módulo Reporting.
 * ==============================================================
 */
@Data
public class ReportResult {

    /** 🔹 Datos crudos (lista de filas con nombre → valor) */
    private List<Map<String, Object>> data;

    /** 🔹 Total de filas devueltas */
    private int total;

    /** ⏱ Tiempo de ejecución en milisegundos */
    private long durationMs;
}
