package co.assip.erp.shared.referencias.general.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🌐 Controlador unificado para catálogos complementarios del esquema GENERAL.
 *
 * ⚙️ Este controlador expone únicamente endpoints de apoyo y relación
 * entre entidades del esquema "general", que se consumen desde el frontend
 * (por ejemplo, combos dependientes como Zona → SubZona).
 *
 * 🚫 IMPORTANTE:
 * No duplica rutas CRUD de controladores existentes.
 *
 * Ejemplo de ruta activa:
 *   - GET /api/v1/general/zonas/{idZona}/sub-zonas
 *
 * Autor: ERP ASSIP Solidaria y Financiera
 * Módulo: shared.referencias.general.web
 */
@RestController
@RequestMapping("/general")
public class GeneralController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ============================================================
    // 📍 SUBZONAS POR ZONA (RELACIÓN DEPENDIENTE)
    // ============================================================

    /**
     * 📋 Lista todas las subzonas pertenecientes a una zona específica.
     * Se usa en combos dependientes (zona → subzona) del módulo de Ubicaciones.
     *
     * Ejemplo:
     *   GET /api/v1/general/zonas/1/sub-zonas
     *
     * Retorna:
     * [
     *   { "idSubZona": 1, "nombreSubZona": "Centro", "idZona": 1 },
     *   { "idSubZona": 2, "nombreSubZona": "Sur", "idZona": 1 }
     * ]
     */
    @GetMapping("/zonas/{idZona}/sub-zonas")
    public List<Map<String, Object>> listarSubZonasPorZona(@PathVariable Integer idZona) {
        String sql = """
            SELECT 
                id_sub_zona AS "idSubZona",
                nombre_sub_zona AS "nombreSubZona",
                id_zona AS "idZona"
            FROM general.sub_zonas
            WHERE id_zona = ?
            ORDER BY nombre_sub_zona;
        """;
        return jdbcTemplate.queryForList(sql, idZona);
    }

    // ============================================================
    // 🧭 OPCIONAL: CONSULTA GLOBAL DE ZONAS Y SUBZONAS (LECTURA)
    // ============================================================

    /**
     * 📋 (Opcional) Lista todas las zonas con sus subzonas.
     * Puede servir para reportes o exportaciones generales.
     */
    @GetMapping("/zonas-con-subzonas")
    public List<Map<String, Object>> listarZonasConSubzonas() {
        String sql = """
            SELECT 
                z.id_zona AS "idZona",
                z.nombre_zona AS "nombreZona",
                s.id_sub_zona AS "idSubZona",
                s.nombre_sub_zona AS "nombreSubZona"
            FROM general.zonas z
            LEFT JOIN general.sub_zonas s ON s.id_zona = z.id_zona
            ORDER BY z.nombre_zona, s.nombre_sub_zona;
        """;
        return jdbcTemplate.queryForList(sql);
    }
}
