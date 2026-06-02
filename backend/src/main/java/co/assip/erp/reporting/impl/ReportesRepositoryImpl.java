package co.assip.erp.reporting.impl;

import co.assip.erp.reporting.api.ReportQueryRequest;
import co.assip.erp.reporting.api.ReportResult;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 🧩 ReportesRepositoryImpl — Búsquedas detalladas seguras
 * ==============================================================
 */
@Repository
public class ReportesRepositoryImpl {

    private final JdbcTemplate jdbc;
    private final UsuarioSesionService usuarioSesionService;

    @Autowired
    public ReportesRepositoryImpl(
            JdbcTemplate jdbc,
            UsuarioSesionService usuarioSesionService
    ) {
        this.jdbc = jdbc;
        this.usuarioSesionService = usuarioSesionService;
    }

    /**
     * 🔹 Ejecuta una consulta segura y dinámica sobre una vista especificada.
     */
    public ReportResult ejecutarConsultaSegura(ReportQueryRequest req) {
        if (req == null || req.getView() == null) {
            throw new IllegalArgumentException("Debe especificar la vista de reporte.");
        }

        // Sanitizar esquema
        String schema = (req.getSchema() != null) ? req.getSchema() : "reporting";
        if (!schema.matches("^[a-zA-Z0-9_]+$")) {
            throw new SecurityException("Nombre de esquema no válido.");
        }

        // Sanitizar vista
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

            // ======================================================
            // 🔹 Filtros de PERSONA
            // ======================================================

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

            if (filters.get("nombres") != null && !filters.get("nombres").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("LOWER(nombres) ILIKE LOWER(?)");
                params.add("%" + filters.get("nombres").toString().trim() + "%");
            }

            if (filters.get("primer_apellido") != null && !filters.get("primer_apellido").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("LOWER(primer_apellido) ILIKE LOWER(?)");
                params.add("%" + filters.get("primer_apellido").toString().trim() + "%");
            }

            if (filters.get("segundo_apellido") != null && !filters.get("segundo_apellido").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("LOWER(segundo_apellido) ILIKE LOWER(?)");
                params.add("%" + filters.get("segundo_apellido").toString().trim() + "%");
            }

            // ======================================================
            // 🔹 Filtros de CUENTA
            // ======================================================

            if (filters.get("id_agencia") != null && !filters.get("id_agencia").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("id_agencia = ?");
                params.add(Integer.parseInt(filters.get("id_agencia").toString().trim()));
            }

            if (filters.get("id_forma_ahorro") != null && !filters.get("id_forma_ahorro").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("id_forma_ahorro = ?");
                params.add(Integer.parseInt(filters.get("id_forma_ahorro").toString().trim()));
            }

            if (filters.get("codigo_cuenta") != null && !filters.get("codigo_cuenta").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("codigo_cuenta = ?");
                params.add(filters.get("codigo_cuenta").toString().trim());
            }

            if (filters.get("codigo_forma") != null && !filters.get("codigo_forma").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("CAST(codigo_forma AS TEXT) ILIKE ?");
                params.add("%" + filters.get("codigo_forma").toString().trim() + "%");
            }

            if (filters.get("forma_ahorro") != null && !filters.get("forma_ahorro").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("LOWER(forma_ahorro) ILIKE LOWER(?)");
                params.add("%" + filters.get("forma_ahorro").toString().trim() + "%");
            }

            // ======================================================
            // 🔹 Filtros de FECHAS
            // ======================================================

            if (filters.get("fecha_inicial") != null && !filters.get("fecha_inicial").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("fecha_movimiento >= TO_DATE(?, 'YYYY-MM-DD')");
                params.add(filters.get("fecha_inicial").toString().trim());
            }

            if (filters.get("fecha_final") != null && !filters.get("fecha_final").toString().isBlank()) {
                where.append((count++ > 0 ? " AND " : " WHERE "));
                where.append("fecha_movimiento <= TO_DATE(?, 'YYYY-MM-DD')");
                params.add(filters.get("fecha_final").toString().trim());
            }

            // ==========================================================
            // 🔐 Filtro por agencias del usuario (solo DEPÓSITOS)
            // ==========================================================
            try {
                boolean esDepositos = schema.equalsIgnoreCase("depositos");

                if (esDepositos && (filters.get("id_agencia") == null || filters.get("id_agencia").toString().isBlank())) {
                    List<Integer> agenciasUsuario =
                            usuarioSesionService.agencias();

                    if (agenciasUsuario != null && !agenciasUsuario.isEmpty()) {
                        where.append((count++ > 0 ? " AND " : " WHERE "));
                        where.append(" id_agencia = ANY(?) ");
                        params.add(agenciasUsuario.toArray(new Integer[0]));
                    } else {
                        throw new SecurityException(
                                "El usuario no tiene agencias permitidas para consultar depósitos."
                        );
                    }
                }

            } catch (Exception e) {
                throw new SecurityException(
                        "No fue posible validar las agencias permitidas del usuario."
                );
            }

            sql.append(where);
        }

        // ======================================================
        // 🔹 Orden y límite de seguridad
        // ======================================================
        String orderClause;

        if (vista.toLowerCase().contains("extracto")) {
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
        List<Map<String, Object>> data =
                jdbc.queryForList(sql.toString(), params.toArray());
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
     * 🔹 Lista metadatos de vistas registradas en reporting.
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