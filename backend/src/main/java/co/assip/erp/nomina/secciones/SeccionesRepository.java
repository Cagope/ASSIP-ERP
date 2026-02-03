package co.assip.erp.nomina.secciones;

import co.assip.erp.nomina.secciones.dto.SeccionNominaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SeccionesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LISTAR
    // =========================================================
    public List<SeccionNominaDTO> listar() {

        String sql = """
            SELECT
              id_seccion AS "idSeccion",
              codigo AS "codigo",
              nombre_seccion AS "nombreSeccion",
              activo AS "activo",
              fk_seguridad_creacion AS "fkSeguridadCreacion",
              fecha_creacion AS "fechaCreacion",
              fk_seguridad_edicion AS "fkSeguridadEdicion",
              fecha_edicion AS "fechaEdicion"
            FROM nomina.secciones_nomina
            ORDER BY id_seccion
        """;

        return jdbc.query(sql, new MapSqlParameterSource(),
                (rs, rowNum) -> {
                    SeccionNominaDTO dto = new SeccionNominaDTO();
                    dto.setIdSeccion(rs.getInt("idSeccion"));
                    dto.setCodigo(rs.getString("codigo"));
                    dto.setNombreSeccion(rs.getString("nombreSeccion"));
                    dto.setActivo(rs.getBoolean("activo"));

                    dto.setFkSeguridadCreacion((Integer) rs.getObject("fkSeguridadCreacion"));
                    dto.setFechaCreacion(rs.getTimestamp("fechaCreacion") != null
                            ? rs.getTimestamp("fechaCreacion").toLocalDateTime() : null);

                    dto.setFkSeguridadEdicion((Integer) rs.getObject("fkSeguridadEdicion"));
                    dto.setFechaEdicion(rs.getTimestamp("fechaEdicion") != null
                            ? rs.getTimestamp("fechaEdicion").toLocalDateTime() : null);

                    return dto;
                }
        );
    }

    // =========================================================
    // OBTENER (para editar)
    // =========================================================
    public SeccionNominaDTO obtener(Integer id) {

        String sql = """
            SELECT
              id_seccion AS "idSeccion",
              codigo AS "codigo",
              nombre_seccion AS "nombreSeccion",
              activo AS "activo",
              fk_seguridad_creacion AS "fkSeguridadCreacion",
              fecha_creacion AS "fechaCreacion",
              fk_seguridad_edicion AS "fkSeguridadEdicion",
              fecha_edicion AS "fechaEdicion"
            FROM nomina.secciones_nomina
            WHERE id_seccion = :id
        """;

        var params = new MapSqlParameterSource().addValue("id", id);

        List<SeccionNominaDTO> list = jdbc.query(sql, params,
                (rs, rowNum) -> {
                    SeccionNominaDTO dto = new SeccionNominaDTO();
                    dto.setIdSeccion(rs.getInt("idSeccion"));
                    dto.setCodigo(rs.getString("codigo"));
                    dto.setNombreSeccion(rs.getString("nombreSeccion"));
                    dto.setActivo(rs.getBoolean("activo"));
                    return dto;
                }
        );

        return list.isEmpty() ? null : list.get(0);
    }

    // =========================================================
    // CREAR
    // =========================================================
    public void crear(SeccionNominaDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.secciones_nomina (
              codigo,
              nombre_seccion,
              activo,
              fk_seguridad_creacion,
              fecha_creacion,
              fk_seguridad_edicion,
              fecha_edicion
            )
            VALUES (
              :codigo,
              :nombre,
              :activo,
              :user,
              CURRENT_TIMESTAMP,
              :user,
              CURRENT_TIMESTAMP
            )
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigo", dto.getCodigo())
                .addValue("nombre", dto.getNombreSeccion())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : true)
                .addValue("user", idUsuario);

        jdbc.update(sql, params);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================
    public void actualizar(Integer id, SeccionNominaDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.secciones_nomina
            SET
              codigo = :codigo,
              nombre_seccion = :nombre,
              activo = :activo,
              fk_seguridad_edicion = :user,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_seccion = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("codigo", dto.getCodigo())
                .addValue("nombre", dto.getNombreSeccion())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : true)
                .addValue("user", idUsuario);

        jdbc.update(sql, params);
    }

    // =========================================================
    // ELIMINAR
    // =========================================================
    public void eliminar(Integer id) {

        String sql = """
            DELETE FROM nomina.secciones_nomina
            WHERE id_seccion = :id
        """;

        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }
}
