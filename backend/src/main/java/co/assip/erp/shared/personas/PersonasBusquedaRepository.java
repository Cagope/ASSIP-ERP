package co.assip.erp.shared.personas;

import co.assip.erp.shared.personas.dto.PersonaBusquedaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PersonasBusquedaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // 1) AUTOCOMPLETE (YA EXISTENTE - NO SE TOCA)
    // =========================================================
    public List<PersonaBusquedaDTO> buscar(String q, int limit) {

        String sql = """
            SELECT
                dp.id_datos_personal,
                dp.documento,
                TRIM(
                    COALESCE(dp.primer_apellido,'') || ' ' ||
                    COALESCE(dp.segundo_apellido,'') || ' ' ||
                    COALESCE(dp.nombres,'')
                ) AS nombre_completo
            FROM hoja_vida.datos_personales dp
            WHERE
                dp.documento ILIKE :qLike
                OR dp.nombres ILIKE :qLike
                OR dp.primer_apellido ILIKE :qLike
                OR dp.segundo_apellido ILIKE :qLike
            ORDER BY dp.documento
            LIMIT :limit
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("qLike", "%" + q.trim() + "%")
                .addValue("limit", limit);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            PersonaBusquedaDTO dto = new PersonaBusquedaDTO();
            dto.setIdDatosPersonal(rs.getLong("id_datos_personal"));
            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre_completo"));
            return dto;
        });
    }

    // =========================================================
    // 2) OBTENER PERSONA POR ID (NUEVO – PARA CARGA DE FORM)
    // =========================================================
    public PersonaBusquedaDTO obtenerPorId(Long idDatosPersonal) {

        String sql = """
            SELECT
                dp.id_datos_personal,
                dp.documento,
                TRIM(
                    COALESCE(dp.primer_apellido,'') || ' ' ||
                    COALESCE(dp.segundo_apellido,'') || ' ' ||
                    COALESCE(dp.nombres,'')
                ) AS nombre_completo
            FROM hoja_vida.datos_personales dp
            WHERE dp.id_datos_personal = :id
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", idDatosPersonal);

        return jdbc.queryForObject(sql, params, (rs, rowNum) -> {
            PersonaBusquedaDTO dto = new PersonaBusquedaDTO();
            dto.setIdDatosPersonal(rs.getLong("id_datos_personal"));
            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre_completo"));
            return dto;
        });
    }

    // =========================================================
// ✅ MAPA: id_datos_personal -> (documento, nombre_completo)
// =========================================================
    public java.util.Map<Long, PersonaBasica> mapPersonasBasicasByIds(java.util.Set<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        String sql = """
        SELECT
            dp.id_datos_personal,
            dp.documento,
            TRIM(
                COALESCE(dp.primer_apellido,'') || ' ' ||
                COALESCE(dp.segundo_apellido,'') || ' ' ||
                COALESCE(dp.nombres,'')
            ) AS nombre_completo
        FROM hoja_vida.datos_personales dp
        WHERE dp.id_datos_personal IN (:ids)
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", ids);

        return jdbc.query(sql, params, rs -> {
            java.util.Map<Long, PersonaBasica> map = new java.util.HashMap<>();
            while (rs.next()) {
                Long id = rs.getLong("id_datos_personal");
                map.put(id, new PersonaBasica(
                        rs.getString("documento"),
                        rs.getString("nombre_completo")
                ));
            }
            return map;
        });
    }

    // DTO interno simple para Excel / enriquecimientos
    public record PersonaBasica(String documento, String nombreCompleto) {}

}
