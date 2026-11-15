
package co.assip.erp.ses.formato3;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Formato3Repository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Consulta saldos consolidados del cierre mensual.
     *
     * No consulta auxiliares contables ni recalcula movimientos.
     * Cada cuenta utiliza su propio saldo de cierre.
     * Excluye cuentas con saldo consolidado igual a cero.
     */
    public List<SaldoContable> consultarSaldos(int ano, int mes) {

        String sql = """
            WITH saldos_agencia AS (
                SELECT
                    TRIM(c.codigo_cuenta) AS codigo_cuenta,
                    TRIM(c.nombre_cuenta) AS nombre_cuenta,
                    CASE
                        WHEN TRIM(c.naturaleza_cuenta) = 'D'
                            THEN COALESCE(s.valor_debito, 0)
                               - COALESCE(s.valor_credito, 0)
                        WHEN TRIM(c.naturaleza_cuenta) = 'C'
                            THEN COALESCE(s.valor_credito, 0)
                               - COALESCE(s.valor_debito, 0)
                        ELSE 0
                    END AS saldo
                FROM contabilidad.periodos_contables_saldos s
                INNER JOIN contabilidad.catalogo_cuentas c
                    ON c.id_catalogo_cuenta = s.id_catalogo_cuenta
                   AND c.id_agencia = s.id_agencia
                WHERE s.ano = :ano
                  AND s.mes = :mes
                  AND LENGTH(TRIM(c.codigo_cuenta)) BETWEEN 1 AND 6
                  AND TRIM(c.codigo_cuenta) ~ '^[0-9]+$'
            ),
            consolidados AS (
                SELECT
                    codigo_cuenta,
                    MIN(nombre_cuenta) AS nombre_cuenta,
                    SUM(saldo) AS saldo
                FROM saldos_agencia
                GROUP BY codigo_cuenta
            )
            SELECT
                RPAD(codigo_cuenta, 6, '0') AS codigo_renglon,
                nombre_cuenta AS descripcion,
                saldo
            FROM consolidados
            WHERE saldo <> 0
            ORDER BY codigo_renglon
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue("ano", ano)
                        .addValue("mes", mes);

        return jdbc.query(sql, parametros, (rs, rowNum) ->
                new SaldoContable(
                        rs.getString("codigo_renglon"),
                        rs.getString("descripcion"),
                        rs.getBigDecimal("saldo")
                )
        );
    }

    /**
     * Representa una fila consolidada del Formato 3.
     */
    public record SaldoContable(
            String codigoRenglon,
            String descripcion,
            BigDecimal saldo
    ) {
    }
}
