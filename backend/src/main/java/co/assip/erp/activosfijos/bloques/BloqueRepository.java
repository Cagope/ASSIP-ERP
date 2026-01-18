package co.assip.erp.activosfijos.bloques;

import co.assip.erp.activosfijos.bloques.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BloqueRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LISTAR
    // =========================================================
    public List<BloqueListDTO> listar() {

        String sql = """
            SELECT
                id_bloque,
                codigo_bloque,
                nombre_bloque,
                meses_depreciacion_defecto
            FROM activos_fijos.bloques
            ORDER BY codigo_bloque
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            BloqueListDTO dto = new BloqueListDTO();
            dto.setIdBloque(rs.getInt("id_bloque"));
            dto.setCodigoBloque(rs.getString("codigo_bloque"));
            dto.setNombreBloque(rs.getString("nombre_bloque"));
            dto.setMesesDepreciacionDefecto(rs.getInt("meses_depreciacion_defecto"));
            return dto;
        });
    }

    // =========================================================
    // OBTENER
    // =========================================================
    public BloqueFormDTO obtener(Integer id) {

        String sql = """
            SELECT
                id_bloque,
                codigo_bloque,
                nombre_bloque,
                meses_depreciacion_defecto
            FROM activos_fijos.bloques
            WHERE id_bloque = :id
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("id", id),
                (rs, rowNum) -> {
                    BloqueFormDTO dto = new BloqueFormDTO();
                    dto.setIdBloque(rs.getInt("id_bloque"));
                    dto.setCodigoBloque(rs.getString("codigo_bloque"));
                    dto.setNombreBloque(rs.getString("nombre_bloque"));
                    dto.setMesesDepreciacionDefecto(rs.getInt("meses_depreciacion_defecto"));
                    return dto;
                }
        );
    }

    // =========================================================
    // INSERT
    // =========================================================
    public void insertar(BloqueSaveDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO activos_fijos.bloques (
                codigo_bloque,
                nombre_bloque,
                meses_depreciacion_defecto,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            ) VALUES (
                :codigo,
                :nombre,
                :meses,
                :usuario,
                :usuario
            )
        """;

        jdbc.update(sql, params(dto, idUsuario));
    }

    // =========================================================
    // UPDATE
    // =========================================================
    public void actualizar(Integer id, BloqueSaveDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE activos_fijos.bloques SET
                codigo_bloque = :codigo,
                nombre_bloque = :nombre,
                meses_depreciacion_defecto = :meses,
                fk_seguridad_edicion = :usuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_bloque = :id
        """;

        MapSqlParameterSource params = params(dto, idUsuario);
        params.addValue("id", id);

        jdbc.update(sql, params);
    }

    // =========================================================
    // DELETE
    // =========================================================
    public void eliminar(Integer id) {

        String sql = """
            DELETE FROM activos_fijos.bloques
            WHERE id_bloque = :id
        """;

        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    // =========================================================
    // PARAMS
    // =========================================================
    private MapSqlParameterSource params(BloqueSaveDTO dto, Integer usuario) {

        return new MapSqlParameterSource()
                .addValue("codigo", dto.getCodigoBloque())
                .addValue("nombre", dto.getNombreBloque())
                .addValue("meses", dto.getMesesDepreciacionDefecto())
                .addValue("usuario", usuario);
    }
}
