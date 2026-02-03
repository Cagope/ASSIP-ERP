package co.assip.erp.nomina.cesantias;

import co.assip.erp.nomina.cesantias.dto.CesantiasDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CesantiasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR (IGUAL A ARL → documento viene del tercero)
    // ============================================================
    public List<CesantiasDTO> listar() {

        String sql = """
            SELECT
              ec.id_cesantias      AS idCesantias,
              ec.nombre_cesantias  AS nombreCesantias,
              ec.id_datos_personal AS idDatosPersonal,
              dp.documento         AS documento,
              ec.activo            AS activo
            FROM nomina.entidades_cesantias ec
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = ec.id_datos_personal
            ORDER BY ec.nombre_cesantias
        """;

        return jdbc.query(sql, (rs, rowNum) -> CesantiasDTO.builder()
                .idCesantias(rs.getInt("idCesantias"))
                .nombreCesantias(rs.getString("nombreCesantias"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER (IGUAL A ARL)
    // ============================================================
    public Optional<CesantiasDTO> obtener(Integer idCesantias) {

        String sql = """
            SELECT
              ec.id_cesantias      AS idCesantias,
              ec.nombre_cesantias  AS nombreCesantias,
              ec.id_datos_personal AS idDatosPersonal,
              dp.documento         AS documento,
              ec.activo            AS activo
            FROM nomina.entidades_cesantias ec
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = ec.id_datos_personal
            WHERE ec.id_cesantias = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idCesantias);

        List<CesantiasDTO> rows = jdbc.query(sql, params, (rs, rowNum) -> CesantiasDTO.builder()
                .idCesantias(rs.getInt("idCesantias"))
                .nombreCesantias(rs.getString("nombreCesantias"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(CesantiasDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.entidades_cesantias (
              nombre_cesantias,
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
            RETURNING id_cesantias
        """;

        var params = new MapSqlParameterSource()
                .addValue("nombre", dto.getNombreCesantias())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idCesantias, CesantiasDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.entidades_cesantias
            SET
              nombre_cesantias = :nombre,
              id_datos_personal = :idDatosPersonal,
              activo = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_cesantias = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idCesantias)
                .addValue("nombre", dto.getNombreCesantias())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(Integer idCesantias) {

        String sql = """
            DELETE FROM nomina.entidades_cesantias
            WHERE id_cesantias = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idCesantias);

        jdbc.update(sql, params);
    }
}
