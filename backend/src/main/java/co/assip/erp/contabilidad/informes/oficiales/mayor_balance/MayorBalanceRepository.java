package co.assip.erp.contabilidad.informes.oficiales.mayor_balance;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

/** Lee las fotografías de cierre, sin duplicar cuentas agrupadoras ni alterar saldos. */
@Repository
@RequiredArgsConstructor
public class MayorBalanceRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public record Agencia(String codigo, String nombre) {}
    public record Cuenta(String codigo, String nombre, String naturaleza,
                         BigDecimal debitoInicial, BigDecimal creditoInicial,
                         BigDecimal debitoFinal, BigDecimal creditoFinal) {}

    public Agencia agencia(int id) {
        var lista = jdbc.query("""
            SELECT TRIM(codigo_agencia) codigo, TRIM(nombre_agencia) nombre
            FROM general.datos_agencias WHERE id_agencia = :id
            """, new MapSqlParameterSource("id", id),
            (rs, i) -> new Agencia(rs.getString("codigo"), rs.getString("nombre")));
        if (lista.isEmpty()) throw new IllegalArgumentException("La agencia seleccionada no existe.");
        return lista.get(0);
    }

    public boolean existeCierreMensual(int agencia, int ano, int mes) {
        Integer n = jdbc.queryForObject("""
            SELECT COUNT(*) FROM contabilidad.periodos_contables_saldos
            WHERE id_agencia = :agencia AND ano = :ano AND mes = :mes
            """, new MapSqlParameterSource().addValue("agencia", agencia)
                .addValue("ano", ano).addValue("mes", mes), Integer.class);
        return n != null && n > 0;
    }
    public boolean existeCierreAnual(int agencia, int ano) {
        Integer n = jdbc.queryForObject("""
            SELECT COUNT(*) FROM contabilidad.cierres_anuales
            WHERE id_agencia = :agencia AND ano = :ano
            """, new MapSqlParameterSource().addValue("agencia", agencia)
                .addValue("ano", ano), Integer.class);
        return n != null && n > 0;
    }

    /**
     * Los saldos son acumulados de cierre: movimiento = cierre final - cierre inicial.
     * Se utiliza nivel 3, conforme al Mayor y Balance de VB6.
     */
    public List<Cuenta> consultar(int agencia, int ano, int mesFinal, boolean anual) {
        String sql = """
            WITH inicio AS (
                SELECT id_agencia, id_catalogo_cuenta, valor_debito, valor_credito
                FROM contabilidad.cierres_anuales
                WHERE id_agencia = :agencia AND ano = :anoAnterior AND (:anual OR :mesFinal = 1)
                UNION ALL
                SELECT id_agencia, id_catalogo_cuenta, valor_debito, valor_credito
                FROM contabilidad.periodos_contables_saldos
                WHERE id_agencia = :agencia AND ano = :ano AND mes = :mesAnterior
                  AND NOT :anual AND :mesFinal > 1
            ), fin AS (
                SELECT id_agencia, id_catalogo_cuenta, valor_debito, valor_credito
                FROM contabilidad.periodos_contables_saldos
                WHERE id_agencia = :agencia AND ano = :ano AND mes = :mesFinal
            )
            SELECT TRIM(c.codigo_cuenta) codigo, TRIM(c.nombre_cuenta) nombre,
                   TRIM(c.naturaleza_cuenta) naturaleza,
                   COALESCE(i.valor_debito, 0) debito_inicial,
                   COALESCE(i.valor_credito, 0) credito_inicial,
                   COALESCE(f.valor_debito, 0) debito_final,
                   COALESCE(f.valor_credito, 0) credito_final
            FROM contabilidad.catalogo_cuentas c
            LEFT JOIN inicio i ON i.id_catalogo_cuenta = c.id_catalogo_cuenta
                              AND i.id_agencia = c.id_agencia
            LEFT JOIN fin f ON f.id_catalogo_cuenta = c.id_catalogo_cuenta
                           AND f.id_agencia = c.id_agencia
            WHERE c.id_agencia = :agencia AND c.nivel_cuenta = 3
            ORDER BY TRIM(c.codigo_cuenta)
            """;
        var p = new MapSqlParameterSource().addValue("agencia", agencia)
            .addValue("ano", ano).addValue("anoAnterior", ano - 1)
            .addValue("mesFinal", mesFinal).addValue("mesAnterior", mesFinal - 1)
            .addValue("anual", anual);
        return jdbc.query(sql, p, (rs, i) -> new Cuenta(rs.getString("codigo"),
            rs.getString("nombre"), rs.getString("naturaleza"),
            rs.getBigDecimal("debito_inicial"), rs.getBigDecimal("credito_inicial"),
            rs.getBigDecimal("debito_final"), rs.getBigDecimal("credito_final")));
    }
}
