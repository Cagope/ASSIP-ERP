package co.assip.erp.nomina.arl;

import co.assip.erp.nomina.arl.dto.ArlDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArlRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR (incluye documento del tercero)
    // ============================================================
    public List<ArlDTO> listar() {

        String sql = """
            SELECT
              a.id_arl            AS idArl,
              a.nombre_arl        AS nombreArl,
              a.id_datos_personal AS idDatosPersonal,
              dp.documento        AS documento,
              a.activo            AS activo
            FROM nomina.entidades_arl a
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = a.id_datos_personal
            ORDER BY a.nombre_arl
        """;

        return jdbc.query(sql, (rs, rowNum) -> ArlDTO.builder()
                .idArl(rs.getInt("idArl"))
                .nombreArl(rs.getString("nombreArl"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public Optional<ArlDTO> obtener(Integer idArl) {

        String sql = """
            SELECT
              a.id_arl            AS idArl,
              a.nombre_arl        AS nombreArl,
              a.id_datos_personal AS idDatosPersonal,
              dp.documento        AS documento,
              a.activo            AS activo
            FROM nomina.entidades_arl a
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = a.id_datos_personal
            WHERE a.id_arl = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idArl);

        List<ArlDTO> rows = jdbc.query(sql, params, (rs, rowNum) -> ArlDTO.builder()
                .idArl(rs.getInt("idArl"))
                .nombreArl(rs.getString("nombreArl"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(ArlDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.entidades_arl (
              nombre_arl,
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
            RETURNING id_arl
        """;

        var params = new MapSqlParameterSource()
                .addValue("nombre", dto.getNombreArl())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idArl, ArlDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.entidades_arl
            SET
              nombre_arl = :nombre,
              id_datos_personal = :idDatosPersonal,
              activo = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_arl = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idArl)
                .addValue("nombre", dto.getNombreArl())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(Integer idArl) {

        String sql = """
            DELETE FROM nomina.entidades_arl
            WHERE id_arl = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idArl);

        jdbc.update(sql, params);
    }
}
