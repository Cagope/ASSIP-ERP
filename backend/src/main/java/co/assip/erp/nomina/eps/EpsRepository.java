package co.assip.erp.nomina.eps;

import co.assip.erp.nomina.eps.dto.EpsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EpsRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR (incluye documento del tercero)
    // ============================================================
    public List<EpsDTO> listar() {

        String sql = """
            SELECT
              e.id_eps            AS idEps,
              e.nombre_eps        AS nombreEps,
              e.id_datos_personal AS idDatosPersonal,
              dp.documento        AS documento,
              e.activo            AS activo
            FROM nomina.entidades_eps e
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = e.id_datos_personal
            ORDER BY e.nombre_eps
        """;

        return jdbc.query(sql, (rs, rowNum) -> EpsDTO.builder()
                .idEps(rs.getInt("idEps"))
                .nombreEps(rs.getString("nombreEps"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER (incluye documento del tercero)
    // ============================================================
    public Optional<EpsDTO> obtener(Integer idEps) {

        String sql = """
            SELECT
              e.id_eps            AS idEps,
              e.nombre_eps        AS nombreEps,
              e.id_datos_personal AS idDatosPersonal,
              dp.documento        AS documento,
              e.activo            AS activo
            FROM nomina.entidades_eps e
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = e.id_datos_personal
            WHERE e.id_eps = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idEps);

        List<EpsDTO> rows = jdbc.query(sql, params, (rs, rowNum) -> EpsDTO.builder()
                .idEps(rs.getInt("idEps"))
                .nombreEps(rs.getString("nombreEps"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(EpsDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.entidades_eps (
              nombre_eps,
              id_datos_personal,
              activo,
              fk_seguridad_creacion,
              fk_seguridad_edicion
            )
            VALUES (
              :nombre,
              :idDatosPersonal,
              :activo,
              :usr,
              :usr
            )
            RETURNING id_eps
        """;

        var params = new MapSqlParameterSource()
                .addValue("nombre", dto.getNombreEps())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idEps, EpsDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.entidades_eps
            SET
              nombre_eps = :nombre,
              id_datos_personal = :idDatosPersonal,
              activo = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_eps = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idEps)
                .addValue("nombre", dto.getNombreEps())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(Integer idEps) {

        String sql = """
            DELETE FROM nomina.entidades_eps
            WHERE id_eps = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idEps);

        jdbc.update(sql, params);
    }
}
