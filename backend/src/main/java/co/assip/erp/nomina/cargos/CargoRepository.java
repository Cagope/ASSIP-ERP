package co.assip.erp.nomina.cargos;

import co.assip.erp.nomina.cargos.dto.CargoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CargoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    public List<CargoDTO> listar() {

        String sql = """
            SELECT
                c.id_cargo      AS idCargo,
                c.nombre_cargo  AS nombreCargo,
                c.activo        AS activo
            FROM nomina.cargos c
            ORDER BY c.nombre_cargo
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            CargoDTO dto = new CargoDTO();
            dto.setIdCargo(rs.getInt("idCargo"));
            dto.setNombreCargo(rs.getString("nombreCargo"));
            dto.setActivo(rs.getBoolean("activo"));
            return dto;
        });
    }

    // ============================================================
    // ✅ OBTENER POR ID
    // ============================================================
    public CargoDTO obtener(Integer idCargo) {

        String sql = """
            SELECT
                c.id_cargo      AS idCargo,
                c.nombre_cargo  AS nombreCargo,
                c.activo        AS activo
            FROM nomina.cargos c
            WHERE c.id_cargo = :idCargo
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCargo", idCargo);

        List<CargoDTO> list = jdbc.query(sql, params, (rs, rowNum) -> {
            CargoDTO dto = new CargoDTO();
            dto.setIdCargo(rs.getInt("idCargo"));
            dto.setNombreCargo(rs.getString("nombreCargo"));
            dto.setActivo(rs.getBoolean("activo"));
            return dto;
        });

        return list.isEmpty() ? null : list.get(0);
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(CargoDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.cargos (
                nombre_cargo,
                activo,
                fk_seguridad_creacion,
                fecha_creacion,
                fk_seguridad_edicion,
                fecha_edicion
            ) VALUES (
                :nombreCargo,
                :activo,
                :idUsuario,
                CURRENT_TIMESTAMP,
                :idUsuario,
                CURRENT_TIMESTAMP
            )
            RETURNING id_cargo
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nombreCargo", dto.getNombreCargo())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : true)
                .addValue("idUsuario", idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idCargo, CargoDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.cargos
            SET
                nombre_cargo = :nombreCargo,
                activo = :activo,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_cargo = :idCargo
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCargo", idCargo)
                .addValue("nombreCargo", dto.getNombreCargo())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : true)
                .addValue("idUsuario", idUsuario);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ DESACTIVAR (NO ELIMINA FÍSICO)
    // ============================================================
    public void desactivar(Integer idCargo, Integer idUsuario) {

        String sql = """
            UPDATE nomina.cargos
            SET
                activo = false,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_cargo = :idCargo
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCargo", idCargo)
                .addValue("idUsuario", idUsuario);

        jdbc.update(sql, params);
    }
}
