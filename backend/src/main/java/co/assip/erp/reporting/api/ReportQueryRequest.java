package co.assip.erp.reporting.api;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 📦 ReportQueryRequest
 * ==============================================================
 * DTO de entrada para consultas dinámicas al módulo Reporting.
 * ==============================================================
 */
@Data
public class ReportQueryRequest {

    private String schema;
    private String view;
    private List<String> columns;
    private Map<String, Object> filters;
    private List<String> groupBy;
    private Map<String, String> aggregations;
}
