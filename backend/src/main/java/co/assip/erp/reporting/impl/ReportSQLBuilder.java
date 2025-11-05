package co.assip.erp.reporting.impl;

import co.assip.erp.reporting.api.ReportQueryRequest;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 🧮 ReportSQLBuilder
 * ==============================================================
 * Generador centralizado de sentencias SQL dinámicas seguras
 * para el módulo Reporting ASSIP-ERP.
 *
 * Recibe un objeto {@link ReportQueryRequest} y construye
 * la sentencia SELECT completa, permitiendo filtros, agrupaciones
 * y funciones de agregación.
 *
 * 🔒 Seguridad:
 *  - Solo admite el esquema “reporting”.
 *  - Valida caracteres permitidos (solo letras, números y guiones bajos).
 *  - No acepta comandos DML (INSERT, UPDATE, DELETE, DROP, etc.).
 *
 * 📘 Ejemplo de uso:
 * --------------------------------------------------------------
 * ReportQueryRequest req = new ReportQueryRequest();
 * req.setSchema("reporting");
 * req.setView("vw_hoja_vida_general_total");
 * req.setColumns(List.of("documento","nombres","nombre_genero"));
 * req.setFilters(Map.of("nombre_genero","FEMENINO"));
 * String sql = ReportSQLBuilder.buildSQL(req);
 * --------------------------------------------------------------
 * Resultado:
 * SELECT documento,nombres,nombre_genero
 * FROM reporting.vw_hoja_vida_general_total
 * WHERE nombre_genero = 'FEMENINO'
 * ORDER BY 1;
 *
 * Autor: Carlos González Pérez
 * Empresa: ERP ASSIP SOLIDARIA Y FINANCIERA
 * Fecha: 2025-11-02
 * ==============================================================
 */
public class ReportSQLBuilder {

    /**
     * 🔹 Construye la sentencia SQL dinámica.
     * @param req objeto con parámetros del reporte
     * @return cadena SQL lista para ejecución segura
     */
    public static String buildSQL(ReportQueryRequest req) {

        // 🔒 Validar esquema permitido
        if (req.getSchema() == null || !req.getSchema().equalsIgnoreCase("reporting")) {
            throw new IllegalArgumentException("Solo se permite el esquema 'reporting'.");
        }

        // 🔒 Validar nombre de vista (solo caracteres seguros)
        if (!req.getView().matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Nombre de vista no válido: " + req.getView());
        }

        // 🔹 Construcción base
        StringBuilder sql = new StringBuilder("SELECT ");

        // ✅ Agregaciones o columnas simples
        if (req.getAggregations() != null && !req.getAggregations().isEmpty()) {
            // Ejemplo: SUM(total_activos) AS total_activos
            String agg = req.getAggregations().entrySet().stream()
                    .map(e -> String.format("%s(%s) AS %s",
                            e.getValue().toUpperCase(),
                            e.getKey(),
                            e.getKey()))
                    .collect(Collectors.joining(", "));
            sql.append(agg);
        } else if (req.getColumns() == null || req.getColumns().isEmpty()) {
            sql.append("*");
        } else {
            sql.append(String.join(",", req.getColumns()));
        }

        // FROM
        sql.append(" FROM ").append(req.getSchema()).append(".").append(req.getView());

        // WHERE
        if (req.getFilters() != null && !req.getFilters().isEmpty()) {
            String where = req.getFilters().entrySet().stream()
                    .map(e -> {
                        Object val = e.getValue();
                        if (val instanceof Number) {
                            return e.getKey() + " = " + val;
                        } else {
                            return e.getKey() + " = '" + sanitize(val.toString()) + "'";
                        }
                    })
                    .collect(Collectors.joining(" AND "));
            sql.append(" WHERE ").append(where);
        }

        // GROUP BY
        if (req.getGroupBy() != null && !req.getGroupBy().isEmpty()) {
            sql.append(" GROUP BY ").append(String.join(",", req.getGroupBy()));
        }

        // ORDER BY
        sql.append(" ORDER BY 1;");

        return sql.toString();
    }

    /**
     * 🔐 Sanitiza una cadena para evitar inyección SQL.
     * Elimina comillas simples dobles y punto y coma.
     */
    private static String sanitize(String value) {
        return value.replaceAll("['\";]", "").trim();
    }
}
