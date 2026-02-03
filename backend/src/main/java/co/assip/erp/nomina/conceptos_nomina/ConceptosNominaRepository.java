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
              c.codigo       AS codigo,
              c.nombre       AS nombre,
              c.tipo         AS tipo,
              c.es_fijo      AS esFijo,
              c.activo       AS activo
            FROM nomina.conceptos_nomina c
            ORDER BY c.codigo
        """;

        return jdbc.query(sql, (rs, rowNum) -> ConceptoNominaDTO.builder()
                .codigo(rs.getString("codigo"))
                .nombre(rs.getString("nombre"))
                .tipo(rs.getString("tipo"))
                .esFijo((Boolean) rs.getObject("esFijo"))
                .activo((Boolean) rs.getObject("activo"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public Optional<ConceptoNominaDTO> obtener(String codigo) {

        String sql = """
            SELECT
              c.codigo       AS codigo,
              c.nombre       AS nombre,
              c.tipo         AS tipo,
              c.es_fijo      AS esFijo,
              c.activo       AS activo
            FROM nomina.conceptos_nomina c
            WHERE c.codigo = :codigo
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigo", codigo);

        List<ConceptoNominaDTO> rows = jdbc.query(sql, params, (rs, rowNum) -> ConceptoNominaDTO.builder()
                .codigo(rs.getString("codigo"))
                .nombre(rs.getString("nombre"))
                .tipo(rs.getString("tipo"))
                .esFijo((Boolean) rs.getObject("esFijo"))
                .activo((Boolean) rs.getObject("activo"))
                .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public void crear(ConceptoNominaDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.conceptos_nomina (
              codigo,
              nombre,
              tipo,
              es_fijo,
              activo,
              fk_seguridad_creacion,
              fk_seguridad_edicion
            )
            VALUES (
              :codigo,
              :nombre,
              :tipo,
              :esFijo,
              :activo,
              :usr,
              :usr
            )
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigo", dto.getCodigo())
                .addValue("nombre", dto.getNombre())
                .addValue("tipo", dto.getTipo())
                .addValue("esFijo", dto.getEsFijo() != null ? dto.getEsFijo() : Boolean.FALSE)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(String codigo, ConceptoNominaDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.conceptos_nomina
            SET
              nombre = :nombre,
              tipo = :tipo,
              es_fijo = :esFijo,
              activo = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE codigo = :codigo
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigo", codigo)
                .addValue("nombre", dto.getNombre())
                .addValue("tipo", dto.getTipo())
                .addValue("esFijo", dto.getEsFijo() != null ? dto.getEsFijo() : Boolean.FALSE)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(String codigo) {

        String sql = """
            DELETE FROM nomina.conceptos_nomina
            WHERE codigo = :codigo
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigo", codigo);

        jdbc.update(sql, params);
    }
}
