package co.assip.erp.contabilidad.cierres.mensualcontable;

import co.assip.erp.contabilidad.cierres.mensualcontable.dto.CierreMensualContableDetalleCuentaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consulta y consolida los saldos del cierre mensual.
 * Utiliza el cierre anterior y los movimientos contables del mes.
 * Las cuentas sin registro anterior se incorporan con valor cero;
 * la existencia del cierre precedente se valida en el servicio.
 * La autorización, las validaciones y la transacción corresponden al servicio.
 */
@Repository
@RequiredArgsConstructor
public class CierreMensualContableRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Calcula los acumulados por cuenta sin modificar registros.
     * Enero toma el cierre anual anterior; los demás meses,
     * el cierre mensual inmediatamente anterior.
     */
    public List<CierreMensualContableDetalleCuentaDTO> consultarConsolidacion(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate finExclusivo = inicio.plusMonths(1);

        String sql = """
            WITH catalogo AS (
                SELECT
                    id_catalogo_cuenta,
                    TRIM(codigo_cuenta) AS codigo_cuenta,
                    TRIM(nombre_cuenta) AS nombre_cuenta,
                    TRIM(naturaleza_cuenta) AS naturaleza_cuenta,
                    nivel_cuenta,
                    COALESCE(cuenta_operable, FALSE) AS cuenta_operable
                FROM contabilidad.catalogo_cuentas
                WHERE id_agencia = :idAgencia
            ),
            movimientos_base AS (
                SELECT
                    a.id_catalogo_cuenta,
                    COALESCE(SUM(a.valor_debito), 0) AS debito,
                    COALESCE(SUM(a.valor_credito), 0) AS credito
                FROM contabilidad.auxiliares_contables a
                WHERE a.id_agencia = :idAgencia
                  AND a.fecha_auxiliar >= :inicio
                  AND a.fecha_auxiliar < :finExclusivo
                  AND a.estado_movimiento IN ('A', 'C')
                GROUP BY a.id_catalogo_cuenta
            ),
            movimientos_mes AS (
                SELECT
                    superior.id_catalogo_cuenta,
                    COALESCE(SUM(m.debito), 0) AS debito_mes,
                    COALESCE(SUM(m.credito), 0) AS credito_mes
                FROM catalogo superior
                JOIN catalogo origen
                  ON LEFT(
                         origen.codigo_cuenta,
                         LENGTH(superior.codigo_cuenta)
                     ) = superior.codigo_cuenta
                JOIN movimientos_base m
                  ON m.id_catalogo_cuenta = origen.id_catalogo_cuenta
                GROUP BY superior.id_catalogo_cuenta
            ),
            cierre_anterior AS (
                SELECT
                    id_catalogo_cuenta,
                    COALESCE(valor_debito, 0) AS debito_anterior,
                    COALESCE(valor_credito, 0) AS credito_anterior
                FROM contabilidad.cierres_anuales
                WHERE id_agencia = :idAgencia
                  AND ano = :anoAnterior
                  AND :mes = 1

                UNION ALL

                SELECT
                    id_catalogo_cuenta,
                    COALESCE(valor_debito, 0) AS debito_anterior,
                    COALESCE(valor_credito, 0) AS credito_anterior
                FROM contabilidad.periodos_contables_saldos
                WHERE id_agencia = :idAgencia
                  AND ano = :ano
                  AND mes = :mesAnterior
                  AND :mes > 1
            ),
            valores AS (
                SELECT
                    c.id_catalogo_cuenta,
                    c.codigo_cuenta,
                    c.nombre_cuenta,
                    c.naturaleza_cuenta,
                    c.nivel_cuenta,
                    c.cuenta_operable,
                    COALESCE(a.debito_anterior, 0) AS debito_anterior,
                    COALESCE(a.credito_anterior, 0) AS credito_anterior,
                    COALESCE(m.debito_mes, 0) AS debito_mes,
                    COALESCE(m.credito_mes, 0) AS credito_mes
                FROM catalogo c
                LEFT JOIN cierre_anterior a
                  ON a.id_catalogo_cuenta = c.id_catalogo_cuenta
                LEFT JOIN movimientos_mes m
                  ON m.id_catalogo_cuenta = c.id_catalogo_cuenta
            )
            SELECT
                *,
                debito_anterior + debito_mes AS debito_acumulado,
                credito_anterior + credito_mes AS credito_acumulado
            FROM valores
            ORDER BY codigo_cuenta
            """;

        MapSqlParameterSource parametros = parametros(idAgencia, ano, mes)
                .addValue("inicio", inicio)
                .addValue("finExclusivo", finExclusivo)
                .addValue("anoAnterior", ano - 1)
                .addValue("mesAnterior", mes - 1);

        return jdbc.query(sql, parametros, (rs, rowNum) -> {
            String naturaleza = rs.getString("naturaleza_cuenta");

            BigDecimal debitoAnterior = rs.getBigDecimal("debito_anterior");
            BigDecimal creditoAnterior = rs.getBigDecimal("credito_anterior");
            BigDecimal debitoMes = rs.getBigDecimal("debito_mes");
            BigDecimal creditoMes = rs.getBigDecimal("credito_mes");
            BigDecimal debitoAcumulado = rs.getBigDecimal("debito_acumulado");
            BigDecimal creditoAcumulado = rs.getBigDecimal("credito_acumulado");

            return new CierreMensualContableDetalleCuentaDTO(
                    rs.getInt("id_catalogo_cuenta"),
                    rs.getString("codigo_cuenta"),
                    rs.getString("nombre_cuenta"),
                    naturaleza,
                    rs.getInt("nivel_cuenta"),
                    rs.getBoolean("cuenta_operable"),
                    debitoAnterior,
                    creditoAnterior,
                    calcularSaldo(naturaleza, debitoAnterior, creditoAnterior),
                    debitoMes,
                    creditoMes,
                    debitoAcumulado,
                    creditoAcumulado,
                    calcularSaldo(naturaleza, debitoAcumulado, creditoAcumulado)
            );
        });
    }

    /**
     * Consulta los saldos históricos registrados en un cierre mensual.
     * No recalcula movimientos ni modifica los valores guardados.
     * Obtiene los movimientos del mes por diferencia de acumulados.
     */
    public List<CierreMensualContableDetalleCuentaDTO> consultarCierreHistorico(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        String sql = """
        WITH cierre_anterior AS (
            SELECT
                id_catalogo_cuenta,
                COALESCE(valor_debito, 0) AS debito_anterior,
                COALESCE(valor_credito, 0) AS credito_anterior
            FROM contabilidad.cierres_anuales
            WHERE id_agencia = :idAgencia
              AND ano = :anoAnterior
              AND :mes = 1

            UNION ALL

            SELECT
                id_catalogo_cuenta,
                COALESCE(valor_debito, 0) AS debito_anterior,
                COALESCE(valor_credito, 0) AS credito_anterior
            FROM contabilidad.periodos_contables_saldos
            WHERE id_agencia = :idAgencia
              AND ano = :ano
              AND mes = :mesAnterior
              AND :mes > 1
        )
        SELECT
            c.id_catalogo_cuenta,
            TRIM(c.codigo_cuenta) AS codigo_cuenta,
            TRIM(c.nombre_cuenta) AS nombre_cuenta,
            TRIM(c.naturaleza_cuenta) AS naturaleza_cuenta,
            c.nivel_cuenta,
            COALESCE(c.cuenta_operable, FALSE) AS cuenta_operable,
            COALESCE(a.debito_anterior, 0) AS debito_anterior,
            COALESCE(a.credito_anterior, 0) AS credito_anterior,
            COALESCE(cm.valor_debito, 0) AS debito_acumulado,
            COALESCE(cm.valor_credito, 0) AS credito_acumulado
        FROM contabilidad.periodos_contables_saldos cm
        JOIN contabilidad.catalogo_cuentas c
          ON c.id_catalogo_cuenta = cm.id_catalogo_cuenta
         AND c.id_agencia = cm.id_agencia
        LEFT JOIN cierre_anterior a
          ON a.id_catalogo_cuenta = cm.id_catalogo_cuenta
        WHERE cm.id_agencia = :idAgencia
          AND cm.ano = :ano
          AND cm.mes = :mes
        ORDER BY c.codigo_cuenta
        """;

        MapSqlParameterSource parametros = parametros(idAgencia, ano, mes)
                .addValue("anoAnterior", ano - 1)
                .addValue("mesAnterior", mes - 1);

        return jdbc.query(sql, parametros, (rs, rowNum) -> {
            String naturaleza = rs.getString("naturaleza_cuenta");

            BigDecimal debitoAnterior = rs.getBigDecimal("debito_anterior");
            BigDecimal creditoAnterior = rs.getBigDecimal("credito_anterior");
            BigDecimal debitoAcumulado = rs.getBigDecimal("debito_acumulado");
            BigDecimal creditoAcumulado = rs.getBigDecimal("credito_acumulado");

            BigDecimal debitoMes = debitoAcumulado.subtract(debitoAnterior);
            BigDecimal creditoMes = creditoAcumulado.subtract(creditoAnterior);

            return new CierreMensualContableDetalleCuentaDTO(
                    rs.getInt("id_catalogo_cuenta"),
                    rs.getString("codigo_cuenta"),
                    rs.getString("nombre_cuenta"),
                    naturaleza,
                    rs.getInt("nivel_cuenta"),
                    rs.getBoolean("cuenta_operable"),
                    debitoAnterior,
                    creditoAnterior,
                    calcularSaldo(
                            naturaleza,
                            debitoAnterior,
                            creditoAnterior
                    ),
                    debitoMes,
                    creditoMes,
                    debitoAcumulado,
                    creditoAcumulado,
                    calcularSaldo(
                            naturaleza,
                            debitoAcumulado,
                            creditoAcumulado
                    )
            );
        });
    }

    /**
     * Cuenta los movimientos contables válidos del mes.
     * Incluye estados A y C, igual que el cierre diario.
     */
    public long contarMovimientos(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);

        String sql = """
            SELECT COUNT(*)
            FROM contabilidad.auxiliares_contables
            WHERE id_agencia = :idAgencia
              AND fecha_auxiliar >= :inicio
              AND fecha_auxiliar < :finExclusivo
              AND estado_movimiento IN ('A', 'C')
            """;

        Long cantidad = jdbc.queryForObject(
                sql,
                parametros(idAgencia, ano, mes)
                        .addValue("inicio", inicio)
                        .addValue("finExclusivo", inicio.plusMonths(1)),
                Long.class
        );

        return cantidad == null ? 0L : cantidad;
    }

    /** Cuenta los saldos del cierre anual anterior para la agencia. */
    public long contarRegistrosCierreAnual(Integer idAgencia, int ano) {
        String sql = """
                SELECT COUNT(*)
                FROM contabilidad.cierres_anuales
                WHERE id_agencia = :idAgencia AND ano = :ano
                """;
        Long cantidad = jdbc.queryForObject(sql,
                new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia)
                        .addValue("ano", ano), Long.class);
        return cantidad == null ? 0L : cantidad;
    }

    /**
     * Cuenta los registros existentes del cierre mensual.
     * Permite impedir una segunda consolidación del mismo período.
     */
    public long contarRegistrosCierre(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        String sql = """
            SELECT COUNT(*)
            FROM contabilidad.periodos_contables_saldos
            WHERE id_agencia = :idAgencia
              AND ano = :ano
              AND mes = :mes
            """;

        Long cantidad = jdbc.queryForObject(
                sql,
                parametros(idAgencia, ano, mes),
                Long.class
        );

        return cantidad == null ? 0L : cantidad;
    }

    /**
     * Identifica movimientos del mes registrados en cuentas
     * inexistentes, de otra agencia o no operables.
     * No modifica registros y permite bloquear cierres inconsistentes.
     */
    public List<String> consultarMovimientosCuentasInvalidas(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);

        String sql = """
        SELECT
            a.id_catalogo_cuenta,
            COALESCE(TRIM(c.codigo_cuenta), 'SIN CUENTA') AS codigo_cuenta,
            COUNT(*) AS cantidad
        FROM contabilidad.auxiliares_contables a
        LEFT JOIN contabilidad.catalogo_cuentas c
          ON c.id_catalogo_cuenta = a.id_catalogo_cuenta
         AND c.id_agencia = a.id_agencia
        WHERE a.id_agencia = :idAgencia
          AND a.fecha_auxiliar >= :inicio
          AND a.fecha_auxiliar < :finExclusivo
          AND a.estado_movimiento IN ('A', 'C')
          AND (
              c.id_catalogo_cuenta IS NULL
              OR COALESCE(c.cuenta_operable, FALSE) = FALSE
          )
        GROUP BY a.id_catalogo_cuenta, c.codigo_cuenta
        ORDER BY codigo_cuenta
        """;

        MapSqlParameterSource parametros = parametros(idAgencia, ano, mes)
                .addValue("inicio", inicio)
                .addValue("finExclusivo", inicio.plusMonths(1));

        return jdbc.query(
                sql,
                parametros,
                (rs, rowNum) ->
                        rs.getString("codigo_cuenta")
                                + " (ID " + rs.getInt("id_catalogo_cuenta")
                                + ", movimientos: " + rs.getLong("cantidad") + ")"
        );
    }

    /**
     * Guarda los acumulados del período usando la secuencia existente.
     * Debe ejecutarse exclusivamente dentro de la transacción de cierre.
     * No reemplaza registros históricos existentes.
     */
    /** Retira los saldos vigentes de un mes reabierto; exige transacción y bloqueo del período. */
    public int retirarConsolidacion(Integer idAgencia, int ano, int mes) {
        String sql = """
            DELETE FROM contabilidad.periodos_contables_saldos
             WHERE id_agencia = :idAgencia AND ano = :ano AND mes = :mes
            """;
        return jdbc.update(sql, parametros(idAgencia, ano, mes));
    }

    public void guardarConsolidacion(
            Integer idAgencia,
            int ano,
            int mes,
            Integer idUsuario,
            List<CierreMensualContableDetalleCuentaDTO> cuentas
    ) {
        if (cuentas == null || cuentas.isEmpty()) {
            throw new IllegalStateException(
                    "No existen cuentas contables para consolidar."
            );
        }

        String sql = """
            INSERT INTO contabilidad.periodos_contables_saldos (
                id_agencia,
                id_catalogo_cuenta,
                ano,
                mes,
                valor_debito,
                valor_credito,
                fk_seguridad_creacion,
                fecha_creacion,
                fk_seguridad_edicion,
                fecha_edicion
            )
            VALUES (
                :idAgencia,
                :idCatalogoCuenta,
                :ano,
                :mes,
                :valorDebito,
                :valorCredito,
                :idUsuario,
                CURRENT_TIMESTAMP,
                :idUsuario,
                CURRENT_TIMESTAMP
            )
            """;

        MapSqlParameterSource[] parametros = cuentas.stream()
                .map(cuenta -> parametros(idAgencia, ano, mes)
                        .addValue("idCatalogoCuenta", cuenta.idCatalogoCuenta())
                        .addValue("valorDebito", cuenta.debitoAcumulado())
                        .addValue("valorCredito", cuenta.creditoAcumulado())
                        .addValue("idUsuario", idUsuario))
                .toArray(MapSqlParameterSource[]::new);

        int[] resultados = jdbc.batchUpdate(sql, parametros);

        if (resultados.length != cuentas.size()) {
            throw new IllegalStateException(
                    "No se guardaron todas las cuentas del cierre mensual."
            );
        }

        for (int resultado : resultados) {
            if (resultado == 0) {
                throw new IllegalStateException(
                        "No fue posible guardar una cuenta del cierre mensual."
                );
            }
        }
    }

    /**
     * Calcula el saldo según la naturaleza de la cuenta.
     * Naturaleza D: débitos menos créditos.
     * Naturaleza C: créditos menos débitos.
     */
    private BigDecimal calcularSaldo(
            String naturaleza,
            BigDecimal debito,
            BigDecimal credito
    ) {
        if ("D".equals(naturaleza)) {
            return debito.subtract(credito);
        }

        if ("C".equals(naturaleza)) {
            return credito.subtract(debito);
        }

        throw new IllegalStateException(
                "La cuenta contable no tiene una naturaleza válida."
        );
    }

    /** Construye los parámetros comunes de agencia y período. */
    private MapSqlParameterSource parametros(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        return new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("ano", ano)
                .addValue("mes", mes);
    }
}
