
package co.assip.erp.contabilidad.informes.pyg.sin_formato;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consulta movimientos mensuales de cuentas de resultados.
 * Permite seleccionar el nivel de detalle sin modificar contabilidad.
 */
@Repository
@RequiredArgsConstructor
public class PyGSinFormatoRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public record CuentaResultado(
            String codigoCuenta,
            String nombreCuenta,
            int nivelCuenta,
            String naturalezaCuenta,
            BigDecimal debitos,
            BigDecimal creditos
    ) {}

    /**
     * Mantiene el contrato original de los informes existentes.
     * Consulta únicamente cuentas de niveles 1 a 3.
     */
    public List<CuentaResultado> consultarCuentas(
            List<Integer> agencias,
            int ano,
            int mes
    ) {
        return consultarCuentas(agencias, ano, mes, 3);
    }

    /**
     * Consulta cuentas de resultados hasta el nivel indicado.
     * Agrupa movimientos operables por prefijo contable y agencia.
     * Cada cuenta se calcula independientemente para evitar duplicaciones.
     */
    public List<CuentaResultado> consultarCuentas(
            List<Integer> agencias,
            int ano,
            int mes,
            int nivelMaximo
    ) {
        if (agencias == null || agencias.isEmpty()) {
            return List.of();
        }
        if (agencias.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException(
                    "Las agencias consultadas no son válidas."
            );
        }
        if (ano < 1900 || ano > 9999 || mes < 1 || mes > 12) {
            throw new IllegalArgumentException(
                    "Año o mes inválido para el PyG."
            );
        }
        if (nivelMaximo < 1 || nivelMaximo > 6) {
            throw new IllegalArgumentException(
                    "El nivel de detalle debe estar entre 1 y 6."
            );
        }

        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate finExclusivo = inicio.plusMonths(1);

        String sql = """
                WITH movimientos AS (
                    SELECT ac.id_agencia,
                           TRIM(operable.codigo_cuenta) AS codigo_cuenta,
                           SUM(COALESCE(ac.valor_debito, 0)) AS debitos,
                           SUM(COALESCE(ac.valor_credito, 0)) AS creditos
                    FROM contabilidad.auxiliares_contables ac
                    JOIN contabilidad.catalogo_cuentas operable
                      ON operable.id_catalogo_cuenta = ac.id_catalogo_cuenta
                     AND operable.id_agencia = ac.id_agencia
                    WHERE ac.id_agencia IN (:agencias)
                      AND ac.fecha_auxiliar >= :inicio
                      AND ac.fecha_auxiliar < :finExclusivo
                      AND TRIM(ac.estado_movimiento) IN ('A', 'C')
                      AND operable.cuenta_operable = TRUE
                      AND LEFT(TRIM(operable.codigo_cuenta), 1)
                          IN ('4', '5', '6', '7')
                    GROUP BY ac.id_agencia,
                             TRIM(operable.codigo_cuenta)
                )
                SELECT TRIM(c.codigo_cuenta) AS codigo_cuenta,
                       MIN(TRIM(c.nombre_cuenta)) AS nombre_cuenta,
                       c.nivel_cuenta,
                       MIN(TRIM(c.naturaleza_cuenta)) AS naturaleza_cuenta,
                       COALESCE(SUM(m.debitos), 0) AS debitos,
                       COALESCE(SUM(m.creditos), 0) AS creditos
                FROM contabilidad.catalogo_cuentas c
                LEFT JOIN movimientos m
                  ON m.id_agencia = c.id_agencia
                 AND LEFT(
                       m.codigo_cuenta,
                       LENGTH(TRIM(c.codigo_cuenta))
                     ) = TRIM(c.codigo_cuenta)
                WHERE c.id_agencia IN (:agencias)
                  AND c.nivel_cuenta <= :nivelMaximo
                  AND LEFT(TRIM(c.codigo_cuenta), 1)
                      IN ('4', '5', '6', '7')
                GROUP BY TRIM(c.codigo_cuenta),
                         c.nivel_cuenta
                ORDER BY TRIM(c.codigo_cuenta),
                         c.nivel_cuenta
                """;

        var parametros = new MapSqlParameterSource()
                .addValue("agencias", agencias)
                .addValue("inicio", inicio)
                .addValue("finExclusivo", finExclusivo)
                .addValue("nivelMaximo", nivelMaximo);

        return jdbc.query(
                sql,
                parametros,
                (rs, i) -> new CuentaResultado(
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta"),
                        rs.getInt("nivel_cuenta"),
                        rs.getString("naturaleza_cuenta"),
                        rs.getBigDecimal("debitos"),
                        rs.getBigDecimal("creditos")
                )
        );
    }
}
