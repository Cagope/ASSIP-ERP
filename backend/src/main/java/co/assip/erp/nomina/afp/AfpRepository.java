package co.assip.erp.nomina.afp;

import co.assip.erp.nomina.afp.dto.AfpDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AfpRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR (incluye documento del tercero)
    // ============================================================
    public List<AfpDTO> listar() {

        String sql = """
            SELECT
              a.id_afp            AS idAfp,
              a.nombre_afp        AS nombreAfp,
              a.id_datos_personal AS idDatosPersonal,
              dp.documento        AS documento,
              a.activo            AS activo
            FROM nomina.entidades_afp a
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = a.id_datos_personal
            ORDER BY a.nombre_afp
        """;

        return jdbc.query(sql, (rs, rowNum) -> AfpDTO.builder()
                .idAfp(rs.getInt("idAfp"))
                .nombreAfp(rs.getString("nombreAfp"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public Optional<AfpDTO> obtener(Integer idAfp) {

        String sql = """
            SELECT
              a.id_afp            AS idAfp,
              a.nombre_afp        AS nombreAfp,
              a.id_datos_personal AS idDatosPersonal,
              dp.documento        AS documento,
              a.activo            AS activo
            FROM nomina.entidades_afp a
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = a.id_datos_personal
            WHERE a.id_afp = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idAfp);

        List<AfpDTO> rows = jdbc.query(sql, params, (rs, rowNum) -> AfpDTO.builder()
                .idAfp(rs.getInt("idAfp"))
                .nombreAfp(rs.getString("nombreAfp"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(AfpDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.entidades_afp (
              nombre_afp,
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
            RETURNING id_afp
        """;

        var params = new MapSqlParameterSource()
                .addValue("nombre", dto.getNombreAfp())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idAfp, AfpDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.entidades_afp
            SET
              nombre_afp = :nombre,
              id_datos_personal = :idDatosPersonal,
              activo = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_afp = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idAfp)
                .addValue("nombre", dto.getNombreAfp())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(Integer idAfp) {

        String sql = """
            DELETE FROM nomina.entidades_afp
            WHERE id_afp = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idAfp);

        jdbc.update(sql, params);
    }
}
