package co.assip.erp.activosfijos.catalogos;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoAdquisicionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<TipoAdquisicionDTO> listar() {

        String sql = """
            SELECT
                id_tipo_adquisicion,
                nombre_tipo_adquisicion
            FROM activos_fijos.tipos_adquisicion
            ORDER BY nombre_tipo_adquisicion
        """;

        return jdbc.query(sql, (rs, i) ->
                new TipoAdquisicionDTO(
                        rs.getInt("id_tipo_adquisicion"),
                        rs.getString("nombre_tipo_adquisicion")
                )
        );
    }
}
