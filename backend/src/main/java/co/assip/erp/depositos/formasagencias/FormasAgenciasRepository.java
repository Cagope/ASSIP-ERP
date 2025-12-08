package co.assip.erp.depositos.formasagencias;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FormasAgenciasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<FormaAgenciaDTO> listarPorAgencia(Integer idAgencia) {

        String sql = """
            SELECT
                id_forma_ahorro AS idFormaAhorro,
                codigo_forma    AS codigoForma,
                nombre_forma    AS nombreForma
            FROM depositos.formas_ahorro
            WHERE id_agencia = :idAgencia
            ORDER BY codigo_forma;
        """;

        return jdbc.query(
                sql,
                Map.of("idAgencia", idAgencia),
                (rs, rowNum) -> new FormaAgenciaDTO(
                        rs.getInt("idFormaAhorro"),
                        rs.getString("codigoForma"),
                        rs.getString("nombreForma")
                )
        );
    }
}
