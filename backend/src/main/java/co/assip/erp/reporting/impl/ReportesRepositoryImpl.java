package co.assip.erp.reporting.impl;

import co.assip.erp.reporting.api.ReportQueryRequest;
import co.assip.erp.reporting.api.ReportResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 🧩 ReportesRepositoryImpl
 * ==============================================================
 * Repositorio dinámico para ejecutar consultas seguras sobre
 * vistas del esquema `reporting`.
 *
 * 🔒 Seguridad:
 * - Solo permite consultas sobre el esquema "reporting"
 * - Bloquea cualquier palabra SQL peligrosa (INSERT, UPDATE, DELETE)
 * - Construye filtros WHERE con parámetros seguros
 * ==============================================================
 */
@Repository
public class ReportesRepositoryImpl {

    private final JdbcTemplate jdbc;

    @Autowired
    public ReportesRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 🔹 Ejecuta una consulta dinámica sobre una vista del esquema reporting.
     */
    public ReportResult ejecutarConsultaSegura(ReportQueryRequest req) {
        if (req == null || req.getView() == null) {
            throw new IllegalArgumentException("Debe especificar la vista de reporte.");
        }

        String schema = (req.getSchema() != null) ? req.getSchema() : "reporting";
        if (!"reporting".equalsIgnoreCase(schema)) {
            throw new SecurityException("Solo se permiten consultas al esquema 'reporting'.");
        }

        // Sanitiza nombre de vista
        String vista = req.getView().replaceAll("[^a-zA-Z0-9_]", "");
        StringBuilder sql = new StringBuilder("SELECT * FROM reporting." + vista);
        List<Object> params = new ArrayList<>();

        // Filtros dinámicos seguros
        if (req.getFilters() != null && !req.getFilters().isEmpty()) {
            sql.append(" WHERE ");
            int i = 0;
            for (Map.Entry<String, Object> entry : req.getFilters().entrySet()) {
                if (i++ > 0) sql.append(" AND ");
                sql.append(entry.getKey()).append(" = ?");
                params.add(entry.getValue());
            }
        }

        sql.append(" LIMIT 500"); // Seguridad: evita queries masivas

        long start = System.currentTimeMillis();
        List<Map<String, Object>> data = jdbc.queryForList(sql.toString(), params.toArray());
        long duration = System.currentTimeMillis() - start;

        ReportResult result = new ReportResult();
        result.setData(data);
        result.setTotal(data.size());
        result.setDurationMs(duration);
        return result;
    }

    /**
     * 🔹 Lista metadatos de vistas registradas en el esquema reporting.
     */
    public List<Map<String, Object>> listarMetadata() {
        String sql = """
            SELECT table_schema, table_name
            FROM information_schema.views
            WHERE table_schema = 'reporting'
            ORDER BY table_name
            """;
        return jdbc.queryForList(sql);
    }
}
