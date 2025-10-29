package co.assip.erp.shared.referencias.catalogos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 📚 Controlador unificado para catálogos de solo lectura del sistema ASSIP-ERP.
 * Origen: esquema catalogos.*
 * Uso: /api/v1/catalogos/{recurso}
 */
@RestController
@RequestMapping("/catalogos")
public class CatalogosController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ============================================================
    // 🔹 CATÁLOGOS BÁSICOS
    // ============================================================

    @GetMapping("/tipos-documentos")
    public List<Map<String, Object>> listarTiposDocumentos() {
        String sql = """
            SELECT tipo_documento AS codigo,
                   nombre_tipo_documento AS nombre,
                   tipo_dv AS requiereDv,
                   tipo_documento_dian AS codigoDian
            FROM catalogos.tipos_documentos
            ORDER BY nombre_tipo_documento
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/paises")
    public List<Map<String, Object>> listarPaises() {
        String sql = """
            SELECT id_pais AS idPais,
                   nombre_pais AS nombrePais,
                   codigo_pais AS codigoPais,
                   codigo_dos AS codigoDos
            FROM catalogos.paises
            ORDER BY nombre_pais
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/departamentos")
    public List<Map<String, Object>> listarDepartamentos() {
        String sql = """
            SELECT id_departamento AS idDepartamento,
                   nombre_departamento AS nombreDepartamento,
                   codigo_departamento AS codigoDepartamento
            FROM catalogos.departamentos
            ORDER BY nombre_departamento
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/departamentos/{idDepartamento}/ciudades")
    public List<Map<String, Object>> listarCiudadesPorDepartamento(@PathVariable Integer idDepartamento) {
        String sql = """
            SELECT id_ciudad AS idCiudad,
                   nombre_ciudad AS nombreCiudad,
                   codigo_ciudad AS codigoCiudad,
                   id_departamento AS idDepartamento
            FROM catalogos.ciudades
            WHERE id_departamento = ?
            ORDER BY nombre_ciudad
        """;
        return jdbcTemplate.queryForList(sql, idDepartamento);
    }

    @GetMapping("/ciudades")
    public List<Map<String, Object>> listarTodasLasCiudades() {
        String sql = """
            SELECT id_ciudad AS idCiudad,
                   nombre_ciudad AS nombreCiudad,
                   codigo_ciudad AS codigoCiudad,
                   id_departamento AS idDepartamento
            FROM catalogos.ciudades
            ORDER BY nombre_ciudad
        """;
        return jdbcTemplate.queryForList(sql);
    }

    // ============================================================
    // 🔹 CATÁLOGOS PERSONALES
    // ============================================================

    @GetMapping("/generos")
    public List<Map<String, Object>> listarGeneros() {
        String sql = """
            SELECT codigo_genero AS codigo,
                   nombre_genero AS nombre
            FROM catalogos.generos
            ORDER BY nombre_genero
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/estados-civiles")
    public List<Map<String, Object>> listarEstadosCiviles() {
        String sql = """
            SELECT codigo_estado_civil AS codigo,
                   nombre_estado_civil AS nombre
            FROM catalogos.estados_civiles
            ORDER BY nombre_estado_civil
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/niveles-escolares")
    public List<Map<String, Object>> listarNivelesEscolares() {
        String sql = """
            SELECT codigo_escolaridad AS codigo,
                   nombre_escolaridad AS nombre
            FROM catalogos.niveles_escolares
            ORDER BY nombre_escolaridad
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/ocupaciones")
    public List<Map<String, Object>> listarOcupaciones() {
        String sql = """
            SELECT codigo_ocupacion AS codigo,
                   nombre_ocupacion AS nombre
            FROM catalogos.ocupaciones
            ORDER BY nombre_ocupacion
        """;
        return jdbcTemplate.queryForList(sql);
    }

    // ============================================================
    // 🔹 CATÁLOGOS EMPRESARIALES Y ECONÓMICOS
    // ============================================================

    @GetMapping("/tipos-contratos")
    public List<Map<String, Object>> listarTiposContratos() {
        String sql = """
            SELECT codigo_tipo_contrato AS codigo,
                   nombre_tipo_contrato AS nombre
            FROM catalogos.tipos_contratos
            ORDER BY nombre_tipo_contrato
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/tipos-empresas")
    public List<Map<String, Object>> listarTiposEmpresas() {
        String sql = """
            SELECT codigo_tipo_empresa AS codigo,
                   nombre_tipo_empresa AS nombre
            FROM catalogos.tipos_empresas
            ORDER BY nombre_tipo_empresa
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/sectores-economicos")
    public List<Map<String, Object>> listarSectoresEconomicos() {
        String sql = """
            SELECT codigo_sector_economico AS codigo,
                   nombre_sector_economico AS nombre
            FROM catalogos.sectores_economicos
            ORDER BY nombre_sector_economico
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/actividades-economicas/ses")
    public List<Map<String, Object>> listarActividadesSES() {
        String sql = """
            SELECT codigo_actividad_ses AS codigo,
                   nombre_actividad_ses AS nombre
            FROM catalogos.actividades_economicas_ses
            ORDER BY nombre_actividad_ses
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/actividades-economicas/dian")
    public List<Map<String, Object>> listarActividadesDIAN() {
        String sql = """
            SELECT codigo_actividad_dian AS codigo,
                   nombre_actividad_dian AS nombre
            FROM catalogos.actividades_economicas_dian
            ORDER BY nombre_actividad_dian
        """;
        return jdbcTemplate.queryForList(sql);
    }

    // ============================================================
    // 🔹 OTROS CATÁLOGOS Y REFERENCIAS GENERALES
    // ============================================================

    @GetMapping("/tipos-peps")
    public List<Map<String, Object>> listarTiposPeps() {
        String sql = """
            SELECT tipo_peps AS codigo,
                   nombre_tipo_peps AS nombre
            FROM catalogos.tipos_peps
            ORDER BY nombre_tipo_peps
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/parentescos")
    public List<Map<String, Object>> listarParentescos() {
        String sql = """
            SELECT codigo_parentesco AS codigo,
                   nombre_parentesco AS nombre
            FROM catalogos.parentescos
            ORDER BY nombre_parentesco
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/tipos-bienes")
    public List<Map<String, Object>> listarTiposBienes() {
        String sql = """
            SELECT codigo_tipo_bien AS codigo,
                   descripcion_tipo_bien AS nombre
            FROM catalogos.tipos_bienes
            ORDER BY descripcion_tipo_bien
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/niveles-ingresos")
    public List<Map<String, Object>> listarNivelesIngresos() {
        String sql = """
            SELECT codigo_nivel_ingreso AS codigo,
                   valor1_natural,
                   valor2_natural,
                   valor1_juridico,
                   valor2_juridico
            FROM catalogos.niveles_ingresos
            ORDER BY codigo_nivel_ingreso
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/tipos-regimen")
    public List<Map<String, Object>> listarTiposRegimen() {
        String sql = """
            SELECT codigo_regimen AS codigo,
                   nombre_regimen AS nombre,
                   porcentaje_retencion AS porcentajeRetencion,
                   base_retencion AS baseRetencion
            FROM catalogos.tipos_regimen
            ORDER BY nombre_regimen
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/tipos-viviendas")
    public List<Map<String, Object>> listarTiposViviendas() {
        String sql = """
            SELECT codigo_tipo_vivienda AS codigo,
                   nombre_tipo_vivienda AS nombre
            FROM catalogos.tipos_viviendas
            ORDER BY nombre_tipo_vivienda
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/jornadas-laborales")
    public List<Map<String, Object>> listarJornadasLaborales() {
        String sql = """
            SELECT codigo_jornada AS codigo,
                   nombre_jornada AS nombre
            FROM catalogos.jornadas_laborales
            ORDER BY nombre_jornada
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/tipos-directivos")
    public List<Map<String, Object>> listarTiposDirectivos() {
        String sql = """
            SELECT codigo_tipo_directivo AS codigo,
                   nombre_tipo_directivo AS nombre
            FROM catalogos.tipos_directivos
            ORDER BY nombre_tipo_directivo
        """;
        return jdbcTemplate.queryForList(sql);
    }
}
