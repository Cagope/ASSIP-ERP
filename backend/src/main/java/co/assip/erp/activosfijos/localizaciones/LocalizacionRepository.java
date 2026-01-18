package co.assip.erp.activosfijos.localizaciones;

import co.assip.erp.activosfijos.localizaciones.dto.LocalizacionFormDTO;
import co.assip.erp.activosfijos.localizaciones.dto.LocalizacionListDTO;
import co.assip.erp.activosfijos.localizaciones.dto.LocalizacionSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LocalizacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // 1. LISTADO
    // =========================================================
    public List<LocalizacionListDTO> listar() {

        String sql = """
            SELECT
                id_localizacion,
                nombre,
                telefono,
                id_agencia
            FROM activos_fijos.localizaciones
            ORDER BY nombre
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            LocalizacionListDTO dto = new LocalizacionListDTO();
            dto.setIdLocalizacion(rs.getLong("id_localizacion"));
            dto.setNombre(rs.getString("nombre"));
            dto.setTelefono(rs.getString("telefono"));
            dto.setIdAgencia(rs.getInt("id_agencia"));
            return dto;
        });
    }

    // =========================================================
    // 2. FORMULARIO
    // =========================================================
    public LocalizacionFormDTO obtener(Long id) {

        String sql = """
            SELECT
                id_localizacion,
                nombre,
                telefono,
                id_agencia
            FROM activos_fijos.localizaciones
            WHERE id_localizacion = :id
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("id", id),
                (rs, rowNum) -> {
                    LocalizacionFormDTO dto = new LocalizacionFormDTO();
                    dto.setIdLocalizacion(rs.getLong("id_localizacion"));
                    dto.setNombre(rs.getString("nombre"));
                    dto.setTelefono(rs.getString("telefono"));
                    dto.setIdAgencia(rs.getInt("id_agencia"));
                    return dto;
                }
        );
    }

    // =========================================================
    // 3. INSERT
    // =========================================================
    public void insertar(LocalizacionSaveDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO activos_fijos.localizaciones (
                nombre,
                telefono,
                id_agencia,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            ) VALUES (
                :nombre,
                :telefono,
                :idAgencia,
                :usuario,
                :usuario
            )
        """;

        jdbc.update(sql, params(dto, idUsuario));
    }

    // =========================================================
    // 4. UPDATE
    // =========================================================
    public void actualizar(Long id, LocalizacionSaveDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE activos_fijos.localizaciones SET
                nombre = :nombre,
                telefono = :telefono,
                id_agencia = :idAgencia,
                fk_seguridad_edicion = :usuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_localizacion = :id
        """;

        MapSqlParameterSource params = params(dto, idUsuario);
        params.addValue("id", id);

        jdbc.update(sql, params);
    }

    // =========================================================
    // 5. DELETE
    // =========================================================
    public void eliminar(Long id) {

        String sql = """
            DELETE FROM activos_fijos.localizaciones
            WHERE id_localizacion = :id
        """;

        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    // =========================================================
    // UTIL
    // =========================================================
    private MapSqlParameterSource params(LocalizacionSaveDTO dto, Integer usuario) {

        return new MapSqlParameterSource()
                .addValue("nombre", dto.getNombre())
                .addValue("telefono", dto.getTelefono())
                .addValue("idAgencia", dto.getIdAgencia())
                .addValue("usuario", usuario);
    }
}
