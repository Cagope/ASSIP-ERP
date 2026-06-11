package co.assip.erp.reporting.api;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 📦 ReportQueryRequest
 * ==============================================================
 * DTO de entrada para consultas dinámicas al módulo Reporting.
 * --------------------------------------------------------------
 * Permite especificar:
 *  - schema y vista base
 *  - columnas específicas
 *  - filtros (mapa clave:valor)
 *  - agrupaciones y agregaciones
 *  - joins dinámicos opcionales (para combinar vistas)
 * ==============================================================
 */
@Data
public class ReportQueryRequest {

    private String schema;
    private String view;

    /**
     * USUARIO = aplica filtro de agencias permitidas
     * GLOBAL  = consulta sin restricción automática de agencias
     */
    private String scope;

    private List<String> columns;
    private Map<String, Object> filters;
    private List<String> groupBy;
    private Map<String, String> aggregations;

    // 🔹 definición de joins dinámicos opcionales
    private List<JoinDefinition> joins;

    /**
     * Representa un LEFT JOIN dinámico entre vistas.
     * Ejemplo:
     *   schema = "reporting"
     *   view = "vw_hoja_vida_ubicaciones_total"
     *   alias = "ub"
     *   on = "ca.id_datos_personal = ub.id_datos_personal"
     */
    @Data
    public static class JoinDefinition {

        private String schema;
        private String view;
        private String alias;
        private String on;
    }
}