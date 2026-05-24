package co.assip.erp.gerencia.dashboard_depositos;

import co.assip.erp.gerencia.dashboard_depositos.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardDepositosRepository {

    private final JdbcTemplate jdbc;

    public DashboardDepositosResponseDTO consultar(
            DashboardDepositosRequestDTO request
    ) {

        LocalDate fechaCorte =
                request.getFechaCorte();

        LocalDate fechaCierreAnterior =
                obtenerFechaCierreAnterior(fechaCorte);

        List<DashboardDepositosFormaDTO> formas =
                obtenerFormas(fechaCorte, fechaCierreAnterior);

        DashboardDepositosResumenDTO resumen =
                construirResumen(formas);

        List<DashboardDepositosGrupoDTO> agencias =
                obtenerAgencias(fechaCorte);

        List<DashboardDepositosTendenciaDTO> tendencia =
                obtenerTendencia();

        return DashboardDepositosResponseDTO.builder()
                .resumen(resumen)
                .formas(formas)
                .agencias(agencias)
                .tendencia(tendencia)
                .fechaCorteAnterior(fechaCierreAnterior)
                .build();
    }

    private LocalDate obtenerFechaCierreAnterior(
            LocalDate fechaCorte
    ) {

        String sql = """
            SELECT MAX(fecha_cierre)
            FROM depositos.cierres_mensuales
            WHERE fecha_cierre < ?
            """;

        return jdbc.queryForObject(
                sql,
                LocalDate.class,
                fechaCorte
        );
    }

    private List<DashboardDepositosFormaDTO> obtenerFormas(
            LocalDate fechaCorte,
            LocalDate fechaCierreAnterior
    ) {

        String sql = """
            WITH parametros AS (
                SELECT
                    ?::date AS fecha_corte,
                    ?::date AS fecha_anterior
            ),

            movimientos_total AS (
                SELECT
                    e.id_cuenta_ahorro,
                    COALESCE(SUM(e.valor_debito), 0) AS total_debitos,
                    COALESCE(SUM(e.valor_credito), 0) AS total_creditos,
                    COUNT(*) AS cantidad_movimientos
                FROM depositos.extractos_cuentas_ahorros e
                CROSS JOIN parametros p
                WHERE e.fecha_movimiento <= p.fecha_corte
                GROUP BY e.id_cuenta_ahorro
            ),

            movimientos_periodo AS (
                SELECT
                    e.id_cuenta_ahorro,
                    COALESCE(SUM(e.valor_debito), 0) AS debitos_periodo,
                    COALESCE(SUM(e.valor_credito), 0) AS creditos_periodo
                FROM depositos.extractos_cuentas_ahorros e
                CROSS JOIN parametros p
                WHERE p.fecha_anterior IS NOT NULL
                  AND e.fecha_movimiento > p.fecha_anterior
                  AND e.fecha_movimiento <= p.fecha_corte
                GROUP BY e.id_cuenta_ahorro
            ),

            movimientos_periodo_forma AS (
                SELECT
                    ca.id_forma_ahorro,
                    COALESCE(SUM(mp.debitos_periodo), 0) AS ingresos_periodo,
                    COALESCE(SUM(mp.creditos_periodo), 0) AS egresos_periodo
                FROM depositos.cuentas_ahorro ca

                INNER JOIN movimientos_periodo mp
                    ON mp.id_cuenta_ahorro = ca.id_cuenta_ahorro

                GROUP BY
                    ca.id_forma_ahorro
            ),

            personas AS (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    tipo_persona,
                    codigo_genero
                FROM reporting.vw_hoja_vida_general_total
                ORDER BY
                    id_datos_personal,
                    fecha_creacion DESC NULLS LAST
            ),

            detalle_actual AS (
                SELECT
                    ca.id_forma_ahorro,
                    fa.codigo_forma,
                    fa.nombre_forma,
                    ca.id_cuenta_ahorro,
                    ca.fecha_apertura_cuenta,
                    ca.fecha_estado_cuenta,
                    ca.estado_cuenta_cuenta,
                    ca.cuenta_activa,
                    COALESCE(pe.tipo_persona, '') AS tipo_persona,
                    COALESCE(pe.codigo_genero, '') AS codigo_genero,

                    CASE
                        WHEN COALESCE(mt.cantidad_movimientos, 0) = 0
                            THEN COALESCE(ca.saldo_actual_cuenta, 0)
                        ELSE COALESCE(mt.total_debitos, 0) - COALESCE(mt.total_creditos, 0)
                    END AS saldo_actual

                FROM depositos.cuentas_ahorro ca

                INNER JOIN depositos.formas_ahorro fa
                    ON fa.id_forma_ahorro = ca.id_forma_ahorro

                LEFT JOIN movimientos_total mt
                    ON mt.id_cuenta_ahorro = ca.id_cuenta_ahorro

                LEFT JOIN personas pe
                    ON pe.id_datos_personal = ca.id_datos_personal

                CROSS JOIN parametros p

                WHERE (
                    CASE
                        WHEN COALESCE(mt.cantidad_movimientos, 0) = 0
                            THEN COALESCE(ca.saldo_actual_cuenta, 0)
                        ELSE COALESCE(mt.total_debitos, 0) - COALESCE(mt.total_creditos, 0)
                    END
                ) <> 0
            ),

            actual AS (
                SELECT
                    id_forma_ahorro,
                    codigo_forma,
                    nombre_forma,

                    COUNT(*) AS cuentas_actuales,
                    COALESCE(SUM(saldo_actual), 0) AS saldo_actual,

                    COUNT(*) FILTER (
                        WHERE tipo_persona = '1'
                          AND codigo_genero = '1'
                    ) AS hombres,

                    COUNT(*) FILTER (
                        WHERE tipo_persona = '1'
                          AND codigo_genero = '2'
                    ) AS mujeres,

                    COUNT(*) FILTER (
                        WHERE tipo_persona = '2'
                    ) AS juridicas

                FROM detalle_actual

                GROUP BY
                    id_forma_ahorro,
                    codigo_forma,
                    nombre_forma
            ),

            anterior AS (
                SELECT
                    r.id_forma_ahorro,
                    r.codigo_forma,
                    r.nombre_forma,
                    r.cantidad_cuentas AS cuentas_anteriores,
                    r.saldo_total AS saldo_anterior
                FROM depositos.cierres_mensuales_resumen r

                INNER JOIN depositos.cierres_mensuales c
                    ON c.id_cierre_mensual = r.id_cierre_mensual

                CROSS JOIN parametros p

                WHERE c.fecha_cierre = p.fecha_anterior
            )

            SELECT
                COALESCE(a.id_forma_ahorro, ant.id_forma_ahorro) AS id_forma_ahorro,
                COALESCE(a.codigo_forma, ant.codigo_forma) AS codigo_forma,
                COALESCE(a.nombre_forma, ant.nombre_forma) AS nombre_forma,

                COALESCE(a.cuentas_actuales, 0) AS cuentas_actuales,
                COALESCE(a.saldo_actual, 0) AS saldo_actual,

                COALESCE(a.hombres, 0) AS hombres,
                COALESCE(a.mujeres, 0) AS mujeres,
                COALESCE(a.juridicas, 0) AS juridicas,

                COALESCE(ant.cuentas_anteriores, 0) AS cuentas_anteriores,
                COALESCE(ant.saldo_anterior, 0) AS saldo_anterior,

                COALESCE(mpf.ingresos_periodo, 0) AS ingresos_periodo,
                COALESCE(mpf.egresos_periodo, 0) AS egresos_periodo,

                COALESCE(a.saldo_actual, 0) - COALESCE(ant.saldo_anterior, 0) AS variacion_saldo,
                COALESCE(a.cuentas_actuales, 0) - COALESCE(ant.cuentas_anteriores, 0) AS variacion_cuentas,

                CASE
                    WHEN COALESCE(ant.saldo_anterior, 0) = 0 THEN 0
                    ELSE ROUND(
                        CAST(
                            (
                                (
                                    COALESCE(a.saldo_actual, 0)
                                    - COALESCE(ant.saldo_anterior, 0)
                                ) * 100.0
                            ) / COALESCE(ant.saldo_anterior, 0)
                            AS numeric
                        ),
                        2
                    )
                END AS porcentaje_crecimiento

            FROM actual a

            FULL JOIN anterior ant
                ON ant.id_forma_ahorro = a.id_forma_ahorro

            LEFT JOIN movimientos_periodo_forma mpf
                ON mpf.id_forma_ahorro = COALESCE(a.id_forma_ahorro, ant.id_forma_ahorro)

            ORDER BY
                COALESCE(a.codigo_forma, ant.codigo_forma)
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardDepositosFormaDTO.builder()
                                .idFormaAhorro(rs.getInt("id_forma_ahorro"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .cuentasActuales(rs.getInt("cuentas_actuales"))
                                .saldoActual(rs.getBigDecimal("saldo_actual"))
                                .hombres(rs.getInt("hombres"))
                                .mujeres(rs.getInt("mujeres"))
                                .juridicas(rs.getInt("juridicas"))
                                .cuentasAnteriores(rs.getInt("cuentas_anteriores"))
                                .saldoAnterior(rs.getBigDecimal("saldo_anterior"))
                                .ingresosPeriodo(rs.getBigDecimal("ingresos_periodo"))
                                .egresosPeriodo(rs.getBigDecimal("egresos_periodo"))
                                .variacionSaldo(rs.getBigDecimal("variacion_saldo"))
                                .variacionCuentas(rs.getInt("variacion_cuentas"))
                                .porcentajeCrecimiento(rs.getBigDecimal("porcentaje_crecimiento"))
                                .build(),
                fechaCorte,
                fechaCierreAnterior
        );
    }

    private DashboardDepositosResumenDTO construirResumen(
            List<DashboardDepositosFormaDTO> formas
    ) {

        DashboardDepositosFormaDTO aportes =
                formas.stream()
                        .filter(f -> "01".equals(f.getCodigoForma()))
                        .findFirst()
                        .orElse(null);

        BigDecimal saldoAportes =
                aportes == null ? BigDecimal.ZERO : aportes.getSaldoActual();

        Integer totalAsociados =
                aportes == null ? 0 : aportes.getCuentasActuales();

        Integer hombres =
                aportes == null ? 0 : aportes.getHombres();

        Integer mujeres =
                aportes == null ? 0 : aportes.getMujeres();

        Integer juridicas =
                aportes == null ? 0 : aportes.getJuridicas();

        BigDecimal saldoTac =
                formas.stream()
                        .filter(f -> "07".equals(f.getCodigoForma()))
                        .map(DashboardDepositosFormaDTO::getSaldoActual)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDepositos =
                formas.stream()
                        .filter(f -> !"01".equals(f.getCodigoForma()))
                        .map(DashboardDepositosFormaDTO::getSaldoActual)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalCuentasDepositos =
                formas.stream()
                        .filter(f -> !"01".equals(f.getCodigoForma()))
                        .map(DashboardDepositosFormaDTO::getCuentasActuales)
                        .reduce(0, Integer::sum);

        return DashboardDepositosResumenDTO.builder()
                .totalAsociados(totalAsociados)
                .totalCuentas(totalCuentasDepositos)
                .saldoTotal(totalDepositos)
                .totalDebitos(BigDecimal.ZERO)
                .totalCreditos(BigDecimal.ZERO)
                .totalFormas(formas.size())
                .hombres(hombres)
                .mujeres(mujeres)
                .juridicas(juridicas)
                .saldoAportes(saldoAportes)
                .saldoTac(saldoTac)
                .build();
    }

    private List<DashboardDepositosGrupoDTO> obtenerAgencias(
            LocalDate fechaCorte
    ) {

        String sql = """
        WITH detalle AS (

            SELECT
                ca.id_agencia,
                da.nombre_agencia,
                fa.codigo_forma,

                CASE
                    WHEN COALESCE(mt.cantidad_movimientos, 0) = 0
                        THEN COALESCE(ca.saldo_actual_cuenta, 0)
                    ELSE COALESCE(mt.total_debitos, 0) - COALESCE(mt.total_creditos, 0)
                END AS saldo_actual

            FROM depositos.cuentas_ahorro ca

            INNER JOIN general.datos_agencias da
                ON da.id_agencia = ca.id_agencia

            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro

            LEFT JOIN (
                SELECT
                    e.id_cuenta_ahorro,
                    COALESCE(SUM(e.valor_debito), 0) AS total_debitos,
                    COALESCE(SUM(e.valor_credito), 0) AS total_creditos,
                    COUNT(*) AS cantidad_movimientos
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.fecha_movimiento <= ?
                GROUP BY e.id_cuenta_ahorro
            ) mt
                ON mt.id_cuenta_ahorro = ca.id_cuenta_ahorro

            WHERE (
                    CASE
                        WHEN COALESCE(mt.cantidad_movimientos, 0) = 0
                            THEN COALESCE(ca.saldo_actual_cuenta, 0)
                        ELSE COALESCE(mt.total_debitos, 0) - COALESCE(mt.total_creditos, 0)
                    END
                  ) <> 0
        ),

        agencias AS (

            SELECT
                id_agencia,
                MAX(nombre_agencia) AS concepto,

                COUNT(*) FILTER (
                    WHERE codigo_forma = '01'
                ) AS cuentas_aportes,

                COALESCE(SUM(
                    CASE
                        WHEN codigo_forma = '01'
                        THEN saldo_actual
                        ELSE 0
                    END
                ), 0) AS valor_aportes,

                COUNT(*) FILTER (
                    WHERE codigo_forma <> '01'
                ) AS cuentas_ahorros,

                COALESCE(SUM(
                    CASE
                        WHEN codigo_forma <> '01'
                        THEN saldo_actual
                        ELSE 0
                    END
                ), 0) AS valor_ahorros

            FROM detalle

            GROUP BY
                id_agencia
        )

        SELECT
            concepto,
            cuentas_aportes,
            valor_aportes,

            ROUND(
                (
                    valor_aportes::numeric * 100
                ) / NULLIF(SUM(valor_aportes) OVER (), 0)::numeric,
                2
            ) AS participacion_aportes,

            cuentas_ahorros,
            valor_ahorros,

            ROUND(
                (
                    valor_ahorros::numeric * 100
                ) / NULLIF(SUM(valor_ahorros) OVER (), 0)::numeric,
                2
            ) AS participacion_ahorros,

            ROW_NUMBER() OVER (
                ORDER BY valor_ahorros DESC
            ) AS ranking

        FROM agencias

        ORDER BY
            valor_ahorros DESC
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardDepositosGrupoDTO.builder()
                                .concepto(rs.getString("concepto"))
                                .cuentasAportes(rs.getInt("cuentas_aportes"))
                                .valorAportes(rs.getBigDecimal("valor_aportes"))
                                .participacionAportes(rs.getBigDecimal("participacion_aportes"))
                                .cuentasAhorros(rs.getInt("cuentas_ahorros"))
                                .valorAhorros(rs.getBigDecimal("valor_ahorros"))
                                .participacionAhorros(rs.getBigDecimal("participacion_ahorros"))
                                .ranking(rs.getInt("ranking"))
                                .build(),
                fechaCorte
        );
    }

    private List<DashboardDepositosTendenciaDTO> obtenerTendencia() {

        String sql = """
        SELECT
            CONCAT(c.anio, '-', LPAD(c.mes::text, 2, '0')) AS periodo,

            COALESCE(SUM(
                CASE
                    WHEN r.codigo_forma = '01'
                    THEN r.cantidad_cuentas
                    ELSE 0
                END
            ), 0) AS total_cuentas_aportes,

            COALESCE(SUM(
                CASE
                    WHEN r.codigo_forma <> '01'
                    THEN r.cantidad_cuentas
                    ELSE 0
                END
            ), 0) AS total_cuentas_depositos,

            COALESCE(SUM(
                CASE
                    WHEN r.codigo_forma = '01'
                    THEN r.saldo_total
                    ELSE 0
                END
            ), 0) AS saldo_aportes,

            COALESCE(SUM(
                CASE
                    WHEN r.codigo_forma <> '01'
                    THEN r.saldo_total
                    ELSE 0
                END
            ), 0) AS saldo_depositos

        FROM depositos.cierres_mensuales c

        INNER JOIN depositos.cierres_mensuales_resumen r
            ON r.id_cierre_mensual = c.id_cierre_mensual

        GROUP BY
            c.anio,
            c.mes

        ORDER BY
            c.anio DESC,
            c.mes DESC

        LIMIT 12
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardDepositosTendenciaDTO.builder()
                                .periodo(rs.getString("periodo"))
                                .totalCuentasAportes(rs.getInt("total_cuentas_aportes"))
                                .totalCuentasDepositos(rs.getInt("total_cuentas_depositos"))
                                .saldoAportes(rs.getBigDecimal("saldo_aportes"))
                                .saldoDepositos(rs.getBigDecimal("saldo_depositos"))
                                .build()
        );
    }

}