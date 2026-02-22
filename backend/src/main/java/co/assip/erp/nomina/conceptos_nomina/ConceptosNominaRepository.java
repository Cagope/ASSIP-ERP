package co.assip.erp.nomina.conceptos_nomina;

import co.assip.erp.nomina.conceptos_nomina.dto.ConceptoNominaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConceptosNominaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    public List<ConceptoNominaDTO> listar() {

        String sql = """
            SELECT
              c.codigo_concepto   AS codigoConcepto,
              c.nombre_concepto   AS nombreConcepto,
              c.tipo_concepto     AS tipoConcepto,
              c.es_fijo           AS esFijo,
              c.activo            AS activo,
              c.tipo_calculo      AS tipoCalculo,
              c.base_calculo      AS baseCalculo,
              c.multiplicador     AS multiplicador
            FROM nomina.conceptos_nomina c
            ORDER BY c.codigo_concepto
        """;

        return jdbc.query(sql, (rs, rowNum) -> ConceptoNominaDTO.builder()
                .codigoConcepto(rs.getString("codigoConcepto"))
                .nombreConcepto(rs.getString("nombreConcepto"))
                .tipoConcepto(rs.getString("tipoConcepto"))
                .esFijo((Boolean) rs.getObject("esFijo"))
                .activo((Boolean) rs.getObject("activo"))
                .tipoCalculo(rs.getString("tipoCalculo"))
                .baseCalculo(rs.getString("baseCalculo"))
                .multiplicador(rs.getBigDecimal("multiplicador"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public Optional<ConceptoNominaDTO> obtener(String codigoConcepto) {

        String sql = """
            SELECT
              c.codigo_concepto   AS codigoConcepto,
              c.nombre_concepto   AS nombreConcepto,
              c.tipo_concepto     AS tipoConcepto,
              c.es_fijo           AS esFijo,
              c.activo            AS activo,
              c.tipo_calculo      AS tipoCalculo,
              c.base_calculo      AS baseCalculo,
              c.multiplicador     AS multiplicador
            FROM nomina.conceptos_nomina c
            WHERE c.codigo_concepto = :codigoConcepto
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigoConcepto", codigoConcepto);

        List<ConceptoNominaDTO> rows = jdbc.query(sql, params,
                (rs, rowNum) -> ConceptoNominaDTO.builder()
                        .codigoConcepto(rs.getString("codigoConcepto"))
                        .nombreConcepto(rs.getString("nombreConcepto"))
                        .tipoConcepto(rs.getString("tipoConcepto"))
                        .esFijo((Boolean) rs.getObject("esFijo"))
                        .activo((Boolean) rs.getObject("activo"))
                        .tipoCalculo(rs.getString("tipoCalculo"))
                        .baseCalculo(rs.getString("baseCalculo"))
                        .multiplicador(rs.getBigDecimal("multiplicador"))
                        .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public void crear(ConceptoNominaDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.conceptos_nomina (
              codigo_concepto,
              nombre_concepto,
              tipo_concepto,
              es_fijo,
              activo,
              tipo_calculo,
              base_calculo,
              multiplicador,
              fk_seguridad_creacion,
              fk_seguridad_edicion
            )
            VALUES (
              :codigoConcepto,
              :nombreConcepto,
              :tipoConcepto,
              :esFijo,
              :activo,
              :tipoCalculo,
              :baseCalculo,
              :multiplicador,
              :usr,
              :usr
            )
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigoConcepto", dto.getCodigoConcepto())
                .addValue("nombreConcepto", dto.getNombreConcepto())
                .addValue("tipoConcepto", dto.getTipoConcepto())
                .addValue("esFijo", dto.getEsFijo() != null ? dto.getEsFijo() : Boolean.FALSE)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("tipoCalculo", dto.getTipoCalculo())
                .addValue("baseCalculo", dto.getBaseCalculo())
                .addValue("multiplicador", dto.getMultiplicador())
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(String codigoConcepto, ConceptoNominaDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.conceptos_nomina
            SET
              nombre_concepto = :nombreConcepto,
              tipo_concepto   = :tipoConcepto,
              es_fijo         = :esFijo,
              activo          = :activo,
              tipo_calculo    = :tipoCalculo,
              base_calculo    = :baseCalculo,
              multiplicador   = :multiplicador,
              fk_seguridad_edicion = :usr,
              fecha_edicion   = CURRENT_TIMESTAMP
            WHERE codigo_concepto = :codigoConcepto
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigoConcepto", codigoConcepto)
                .addValue("nombreConcepto", dto.getNombreConcepto())
                .addValue("tipoConcepto", dto.getTipoConcepto())
                .addValue("esFijo", dto.getEsFijo() != null ? dto.getEsFijo() : Boolean.FALSE)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("tipoCalculo", dto.getTipoCalculo())
                .addValue("baseCalculo", dto.getBaseCalculo())
                .addValue("multiplicador", dto.getMultiplicador())
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(String codigoConcepto) {

        String sql = """
            DELETE FROM nomina.conceptos_nomina
            WHERE codigo_concepto = :codigoConcepto
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigoConcepto", codigoConcepto);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ MAPA: codigo_concepto -> nombre_concepto (para Excel)
    // ============================================================
    public java.util.Map<String, String> mapCodigoNombre(Boolean soloActivos) {

        String sql = """
        SELECT
          c.codigo_concepto AS codigo,
          c.nombre_concepto AS nombre
        FROM nomina.conceptos_nomina c
        WHERE (:soloActivos = FALSE OR c.activo = TRUE)
    """;

        var params = new MapSqlParameterSource()
                .addValue("soloActivos", soloActivos != null ? soloActivos : Boolean.TRUE);

        return jdbc.query(sql, params, rs -> {
            java.util.Map<String, String> map = new java.util.HashMap<>();
            while (rs.next()) {
                String codigo = rs.getString("codigo");
                String nombre = rs.getString("nombre");
                if (codigo != null) map.put(codigo, nombre);
            }
            return map;
        });
    }

}
