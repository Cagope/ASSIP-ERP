
package co.assip.erp.ses.formatos;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FormatosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Consulta el código de un formato SES activo.
     *
     * La identificación se realiza mediante el módulo
     * de origen y el nombre funcional del formato.
     *
     * No modifica información de la base de datos.
     */
    public Optional<String> consultarCodigoFormato(
            String moduloOrigen,
            String nombreFormato
    ) {

        String sql = """
            SELECT
                TRIM(f.codigo_formato) AS codigo_formato
            FROM supersolidaria.formatos f
            WHERE UPPER(TRIM(f.modulo_origen)) = UPPER(TRIM(:moduloOrigen))
              AND UPPER(TRIM(f.nombre_formato)) = UPPER(TRIM(:nombreFormato))
              AND f.activo = TRUE
            ORDER BY f.id_formato
            LIMIT 1
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue("moduloOrigen", moduloOrigen)
                        .addValue("nombreFormato", nombreFormato);

        List<String> resultados = jdbc.query(
                sql,
                parametros,
                (rs, rowNum) -> rs.getString("codigo_formato")
        );

        return resultados.stream().findFirst();
    }
}
