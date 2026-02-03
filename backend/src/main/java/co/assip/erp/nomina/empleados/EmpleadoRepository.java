package co.assip.erp.nomina.empleados;

import co.assip.erp.nomina.empleados.dto.EmpleadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmpleadoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    public List<EmpleadoDTO> listar() {

        String sql = """
            SELECT
              e.id_empleado       AS idEmpleado,
              e.id_agencia        AS idAgencia,
              e.id_datos_personal AS idDatosPersonal,
              e.activo            AS activo
            FROM nomina.empleados e
            ORDER BY e.id_empleado DESC
        """;

        return jdbc.query(sql, (rs, rowNum) -> EmpleadoDTO.builder()
                .idEmpleado(rs.getInt("idEmpleado"))
                .idAgencia((Integer) rs.getObject("idAgencia"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .activo((Boolean) rs.getObject("activo"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public Optional<EmpleadoDTO> obtener(Integer idEmpleado) {

        String sql = """
            SELECT
              e.id_empleado       AS idEmpleado,
              e.id_agencia        AS idAgencia,
              e.id_datos_personal AS idDatosPersonal,
              e.activo            AS activo
            FROM nomina.empleados e
            WHERE e.id_empleado = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idEmpleado);

        List<EmpleadoDTO> rows = jdbc.query(sql, params, (rs, rowNum) -> EmpleadoDTO.builder()
                .idEmpleado(rs.getInt("idEmpleado"))
                .idAgencia((Integer) rs.getObject("idAgencia"))
                .idDatosPersonal((Integer) rs.getObject("idDatosPersonal"))
                .activo((Boolean) rs.getObject("activo"))
                .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(EmpleadoDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.empleados (
              id_agencia,
              id_datos_personal,
              activo,
              fk_seguridad_creacion,
              fk_seguridad_edicion
            )
            VALUES (
              :idAgencia,
              :idDatosPersonal,
              :activo,
              :usr,
              :usr
            )
            RETURNING id_empleado
        """;

        var params = new MapSqlParameterSource()
                .addValue("idAgencia", dto.getIdAgencia())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idEmpleado, EmpleadoDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.empleados
            SET
              id_agencia = :idAgencia,
              id_datos_personal = :idDatosPersonal,
              activo = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_empleado = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idEmpleado)
                .addValue("idAgencia", dto.getIdAgencia())
                .addValue("idDatosPersonal", dto.getIdDatosPersonal())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(Integer idEmpleado) {

        String sql = """
            DELETE FROM nomina.empleados
            WHERE id_empleado = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idEmpleado);

        jdbc.update(sql, params);
    }
}
