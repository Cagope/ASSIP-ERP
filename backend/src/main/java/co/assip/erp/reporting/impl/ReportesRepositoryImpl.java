package co.assip.erp.reporting.impl;

import co.assip.erp.reporting.api.ReportQueryRequest;
import co.assip.erp.reporting.api.ReportResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 🧩 ReportesRepositoryImpl — Búsquedas detalladas seguras
 * ==============================================================
 * Ejecuta consultas dinámicas sobre vistas de cualquier esquema
 * permitido (por defecto "reporting", pero configurable).
 *
 * 🔒 Seguridad:
 * - Sanitiza nombres de esquema y vista
 * - Construye WHERE dinámico con parámetros seguros
 * - Aplica solo filtros explícitos enviados
 * - Limita los resultados (máx. 2000 filas)
 *
 * 🧮 Filtros admitidos:
 *   id_datos_personal, documento, nombres, primer_apellido,
 *   segundo_apellido, codigo_forma, codigo_cuenta, fecha_inicial, fecha_final
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
     * 🔹 Ejecuta una consulta segura y dinámica sobre una vista especificada.
     */
    public ReportResult ejecutarConsultaSegura(ReportQueryRequest req) {
        if (req == null || req.getView() == null) {
            throw new IllegalArgumentException("Debe especificar la vista de reporte.");
        }

        // ✅ Sanitizar nombre de esquema
        String schema = (req.getSchema() != null) ? req.getSchema() : "reporting";
        if (!schema.matches("^[a-zA-Z0-9_]+$")) {
            throw new SecurityException("Nombre de esquema no válido.");
        }

        // ✅ Sanitizar nombre de vista
        String vista = req.getView().replaceAll("[^a-zA-Z0-9_]", "");

        StringBuilder sql = new StringBuilder("SELECT * FROM " + schema + "." + vista);
        List<Object> params = new ArrayList<>();

        // ======================================================
        // 🧠 Filtros dinámicos — modo detallado (AND)
        // ======================================================
        Map<String, Object> filters = req.getFilters();
        if (filters != null && !filters.isEmpty()) {
            StringBuilder where = new StringBuilder();
            int count = 0;

            // 🔹 Filtros por datos personales
            // 🔹 Filtros por datos personales
            if (filters.get("id_datos_personal") != null && !filters.get("id_datos_personal").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("id_datos_personal = ?");
                params.add(Integer.parseInt(filters.get("id_datos_personal").toString().trim()));
            }

            if (filters.get("documento") != null && !filters.get("documento").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("CAST(documento AS TEXT) ILIKE ?");
                params.add("%" + filters.get("documento").toString().trim() + "%");
            }

// 🔹 Nuevo filtro por forma de ahorro (texto)
            if (filters.get("forma_ahorro") != null && !filters.get("forma_ahorro").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("LOWER(forma_ahorro) ILIKE LOWER(?)");
                params.add("%" + filters.get("forma_ahorro").toString().trim() + "%");
            }

// 🔹 Filtro por código de cuenta
            if (filters.get("codigo_cuenta") != null && !filters.get("codigo_cuenta").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("CAST(codigo_cuenta AS TEXT) ILIKE ?");
                params.add("%" + filters.get("codigo_cuenta").toString().trim() + "%");
            }


            // ======================================================
            // 🔸 BLOQUE ACTUALIZADO — ORDEN CORRECTO
            // ======================================================

            // 🔹 1️⃣ Filtro por código de forma
            if (filters.get("codigo_forma") != null && !filters.get("codigo_forma").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("CAST(codigo_forma AS TEXT) ILIKE ?");
                params.add("%" + filters.get("codigo_forma").toString().trim() + "%");
            }

            // 🔹 2️⃣ Filtro por código de cuenta
            if (filters.get("codigo_cuenta") != null && !filters.get("codigo_cuenta").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("CAST(codigo_cuenta AS TEXT) ILIKE ?");
                params.add("%" + filters.get("codigo_cuenta").toString().trim() + "%");
            }

            // 🔹 3️⃣ Filtro por fecha inicial
            if (filters.get("fecha_inicial") != null && !filters.get("fecha_inicial").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("fecha_movimiento >= TO_DATE(?, 'YYYY-MM-DD')");
                params.add(filters.get("fecha_inicial").toString().trim());
            }

            // 🔹 4️⃣ Filtro por fecha final
            if (filters.get("fecha_final") != null && !filters.get("fecha_final").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("fecha_movimiento <= TO_DATE(?, 'YYYY-MM-DD')");
                params.add(filters.get("fecha_final").toString().trim());
            }

            // ======================================================
            sql.append(where);
        }

        // ======================================================
        // 🔹 Orden y límite de seguridad — adaptado por esquema
        // ======================================================
        String orderClause;

        if (vista.toLowerCase().contains("extracto")) {
            // 🕓 Para vistas de extractos: orden cronológico
            orderClause = " ORDER BY fecha_movimiento ASC, hora_movimiento ASC LIMIT 2000";
        } else if (schema.equalsIgnoreCase("reporting") || schema.equalsIgnoreCase("hoja_vida")) {
            orderClause = " ORDER BY fecha_actualizacion DESC LIMIT 2000";
        } else if (schema.equalsIgnoreCase("depositos")) {
            orderClause = " ORDER BY nombres ASC LIMIT 2000";
        } else {
            orderClause = " ORDER BY 1 DESC LIMIT 2000";
        }

        sql.append(orderClause);

        // ======================================================
        // ⚡ Ejecución segura
        // ======================================================
        long start = System.currentTimeMillis();
        List<Map<String, Object>> data = jdbc.queryForList(sql.toString(), params.toArray());
        long duration = System.currentTimeMillis() - start;

        // ======================================================
        // 📦 Resultado estructurado
        // ======================================================
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
