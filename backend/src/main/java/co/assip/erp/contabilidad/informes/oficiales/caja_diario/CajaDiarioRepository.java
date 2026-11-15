package co.assip.erp.contabilidad.informes.oficiales.caja_diario;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Agrupa auxiliares contables activos por día y nivel, sin tablas temporales. */
@Repository
@RequiredArgsConstructor
public class CajaDiarioRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public record Agencia(String codigo, String nombre) {}
    public record Movimiento(LocalDate fecha, String codigo, String nombre, BigDecimal debito, BigDecimal credito) {}
    /** Recupera el nombre de la cuenta agrupadora del catálogo de la misma agencia. */
    public List<Movimiento> consultar(int agencia, LocalDate inicio, LocalDate finExclusivo, int longitud) {
        String sql = """
            WITH agrupados AS (
              SELECT ac.fecha_auxiliar::date AS fecha,
                     LEFT(TRIM(cc.codigo_cuenta), :longitud) AS codigo,
                     SUM(COALESCE(ac.valor_debito, 0)) AS debito,
                     SUM(COALESCE(ac.valor_credito, 0)) AS credito
              FROM contabilidad.auxiliares_contables ac
              JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta AND cc.id_agencia = ac.id_agencia
              WHERE ac.id_agencia = :agencia
                AND ac.fecha_auxiliar >= :inicio AND ac.fecha_auxiliar < :fin
                AND TRIM(ac.estado_movimiento) IN ('A', 'C')
                AND cc.cuenta_operable = TRUE
              GROUP BY 1, 2
            )
            SELECT a.fecha, a.codigo, COALESCE(TRIM(c.nombre_cuenta), a.codigo) AS nombre,
                   a.debito, a.credito
            FROM agrupados a
            LEFT JOIN contabilidad.catalogo_cuentas c
              ON c.id_agencia = :agencia AND TRIM(c.codigo_cuenta) = a.codigo
            ORDER BY a.fecha, a.codigo
            """;
        var p = new MapSqlParameterSource().addValue("agencia", agencia)
            .addValue("inicio", inicio).addValue("fin", finExclusivo).addValue("longitud", longitud);
        return jdbc.query(sql, p, (rs, i) -> new Movimiento(
            rs.getObject("fecha", LocalDate.class), rs.getString("codigo"), rs.getString("nombre"),
            rs.getBigDecimal("debito"), rs.getBigDecimal("credito")));
    }
    /** Identifica la agencia sin aceptar nombres suministrados por el navegador. */
    public Agencia agencia(int id) {
        var lista = jdbc.query("""
            SELECT TRIM(codigo_agencia) codigo, TRIM(nombre_agencia) nombre
            FROM general.datos_agencias WHERE id_agencia = :id
            """, new MapSqlParameterSource("id", id),
            (rs, i) -> new Agencia(rs.getString("codigo"), rs.getString("nombre")));
        if (lista.isEmpty()) throw new IllegalArgumentException("La agencia seleccionada no existe.");
        return lista.get(0);
    }
}
