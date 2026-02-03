package co.assip.erp.nomina.caja_compensacion;

import co.assip.erp.nomina.caja_compensacion.dto.CajaCompensacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CajaCompensacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR (incluye documento)
    // ============================================================
    public List<CajaCompensacionDTO> listar() {

        String sql = """
            SELECT
              c.id_caja            AS idCaja,
              c.nombre_caja        AS nombreCaja,
              c.id_datos_personal  AS idDatosPersonal,
              dp.documento         AS documento,
              c.activo             AS activo
            FROM nomina.entidades_caja_compensacion c
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = c.id_datos_personal
            ORDER BY c.nombre_caja
        """;

        return jdbc.query(sql, (rs, rowNum) -> CajaCompensacionDTO.builder()
                .idCaja(rs.getInt("idCaja"))
                .nombreCaja(rs.getString("nombreCaja"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public Optional<CajaCompensacionDTO> obtener(Integer idCaja) {

        String sql = """
            SELECT
              c.id_caja            AS idCaja,
              c.nombre_caja        AS nombreCaja,
              c.id_datos_personal  AS idDatosPersonal,
              dp.documento         AS documento,
              c.activo             AS activo
            FROM nomina.entidades_caja_compensacion c
            LEFT JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = c.id_datos_personal
            WHERE c.id_caja = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idCaja);

        List<CajaCompensacionDTO> rows = jdbc.query(sql, params, (rs, rowNum) -> CajaCompensacionDTO.builder()
                .idCaja(rs.getInt("idCaja"))
                .nombreCaja(rs.getString("nombreCaja"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .documento(rs.getString("documento"))
                .activo((Boolean) rs.getObject("activo"))
                .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(CajaCompensacionDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.entidades_caja_compensacion (
              nombre_caja,
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
            RETURNING id_caja
        """;

        var params = new MapSqlParameterSource()
                .addValue("nombre", dto.getNombreCaja())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idCaja, CajaCompensacionDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.entidades_caja_compensacion
            SET
              nombre_caja = :nombre,
              id_datos_personal = :idDatosPersonal,
              activo = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_caja = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idCaja)
                .addValue("nombre", dto.getNombreCaja())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(Integer idCaja) {

        String sql = """
            DELETE FROM nomina.entidades_caja_compensacion
            WHERE id_caja = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idCaja);

        jdbc.update(sql, params);
    }
}
