package co.assip.erp.cdat.informes.simulador_cdat;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CdatSimuladorRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<Map<String, Object>> buscarCuentasAhorro(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido,
            Integer idAgencia
    ) {

        StringBuilder sql = new StringBuilder("""
            SELECT *
            FROM depositos.vw_depositos_cuentas_ahorro_total
            WHERE id_agencia = :idAgencia
              AND UPPER(TRIM(COALESCE(codigo_estado, ''))) = 'A'
            """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia);

        if (documento != null && !documento.isBlank()) {
            sql.append("""
                 AND documento ILIKE :documento
                """);

            params.addValue(
                    "documento",
                    "%" + documento.trim() + "%"
            );
        }

        if (nombres != null && !nombres.isBlank()) {
            sql.append("""
                 AND nombres ILIKE :nombres
                """);

            params.addValue(
                    "nombres",
                    "%" + nombres.trim() + "%"
            );
        }

        if (primerApellido != null && !primerApellido.isBlank()) {
            sql.append("""
                 AND primer_apellido ILIKE :primerApellido
                """);

            params.addValue(
                    "primerApellido",
                    "%" + primerApellido.trim() + "%"
            );
        }

        if (segundoApellido != null && !segundoApellido.isBlank()) {
            sql.append("""
                 AND segundo_apellido ILIKE :segundoApellido
                """);

            params.addValue(
                    "segundoApellido",
                    "%" + segundoApellido.trim() + "%"
            );
        }

        sql.append("""
            ORDER BY codigo_forma, codigo_cuenta
            LIMIT 200
            """);

        return jdbc.queryForList(
                sql.toString(),
                params
        );
    }
}