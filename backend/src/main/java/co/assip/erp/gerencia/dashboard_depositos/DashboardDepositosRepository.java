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

        List<DashboardDepositosTipoCaptacionDTO> tiposCaptacion =
                obtenerTiposCaptacion(formas);

        return DashboardDepositosResponseDTO.builder()
                .resumen(resumen)
                .formas(formas)
                .tiposCaptacion(tiposCaptacion)
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
            GROUP BY
                e.id_cuenta_ahorro
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
            GROUP BY
                e.id_cuenta_ahorro
        ),

        movimientos_periodo_forma AS (
            SELECT
                TRIM(fa.codigo_forma) AS codigo_forma,
                TRIM(fa.nombre_forma) AS nombre_forma,

                COALESCE(
                    SUM(mp.creditos_periodo),
                    0
                ) AS ingresos_periodo,

                COALESCE(
                    SUM(mp.debitos_periodo),
                    0
                ) AS egresos_periodo

            FROM depositos.cuentas_ahorro ca

            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro

            INNER JOIN movimientos_periodo mp
                ON mp.id_cuenta_ahorro = ca.id_cuenta_ahorro

            GROUP BY
                TRIM(fa.codigo_forma),
                TRIM(fa.nombre_forma)
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

                TRIM(fa.codigo_forma) AS codigo_forma,
                TRIM(fa.nombre_forma) AS nombre_forma,

                ca.id_cuenta_ahorro,

                COALESCE(pe.tipo_persona, '') AS tipo_persona,
                COALESCE(pe.codigo_genero, '') AS codigo_genero,

                CASE
                    WHEN COALESCE(mt.cantidad_movimientos, 0) = 0
                        THEN COALESCE(ca.saldo_actual_cuenta, 0)

                    ELSE
                        COALESCE(mt.total_creditos, 0)
                        -
                        COALESCE(mt.total_debitos, 0)
                END AS saldo_actual

            FROM depositos.cuentas_ahorro ca

            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro

            LEFT JOIN movimientos_total mt
                ON mt.id_cuenta_ahorro = ca.id_cuenta_ahorro

            LEFT JOIN personas pe
                ON pe.id_datos_personal = ca.id_datos_personal

            WHERE (
                CASE
                    WHEN COALESCE(mt.cantidad_movimientos, 0) = 0
                        THEN COALESCE(ca.saldo_actual_cuenta, 0)

                    ELSE
                        COALESCE(mt.total_creditos, 0)
                        -
                        COALESCE(mt.total_debitos, 0)
                END
            ) <> 0
        ),

        actual AS (
            SELECT
                MIN(id_forma_ahorro) AS id_forma_ahorro,

                codigo_forma,
                nombre_forma,

                COUNT(*) AS cuentas_actuales,

                COALESCE(
                    SUM(saldo_actual),
                    0
                ) AS saldo_actual,

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
                codigo_forma,
                nombre_forma
        ),

        anterior AS (
            SELECT
                MIN(r.id_forma_ahorro) AS id_forma_ahorro,

                TRIM(r.codigo_forma) AS codigo_forma,
                TRIM(r.nombre_forma) AS nombre_forma,

                COALESCE(
                    SUM(r.cantidad_cuentas),
                    0
                ) AS cuentas_anteriores,

                COALESCE(
                    SUM(r.saldo_total),
                    0
                ) AS saldo_anterior

            FROM depositos.cierres_mensuales_resumen r

            INNER JOIN depositos.cierres_mensuales c
                ON c.id_cierre_mensual = r.id_cierre_mensual

            CROSS JOIN parametros p

            WHERE c.fecha_cierre = p.fecha_anterior

            GROUP BY
                TRIM(r.codigo_forma),
                TRIM(r.nombre_forma)
        )

        SELECT
            COALESCE(
                a.id_forma_ahorro,
                ant.id_forma_ahorro
            ) AS id_forma_ahorro,

            COALESCE(
                a.codigo_forma,
                ant.codigo_forma
            ) AS codigo_forma,

            COALESCE(
                a.nombre_forma,
                ant.nombre_forma
            ) AS nombre_forma,

            COALESCE(
                a.cuentas_actuales,
                0
            ) AS cuentas_actuales,

            COALESCE(
                a.saldo_actual,
                0
            ) AS saldo_actual,

            COALESCE(
                a.hombres,
                0
            ) AS hombres,

            COALESCE(
                a.mujeres,
                0
            ) AS mujeres,

            COALESCE(
                a.juridicas,
                0
            ) AS juridicas,

            COALESCE(
                ant.cuentas_anteriores,
                0
            ) AS cuentas_anteriores,

            COALESCE(
                ant.saldo_anterior,
                0
            ) AS saldo_anterior,

            COALESCE(
                mpf.ingresos_periodo,
                0
            ) AS ingresos_periodo,

            COALESCE(
                mpf.egresos_periodo,
                0
            ) AS egresos_periodo,

            COALESCE(a.saldo_actual, 0)
            -
            COALESCE(ant.saldo_anterior, 0)
                AS variacion_saldo,

            COALESCE(a.cuentas_actuales, 0)
            -
            COALESCE(ant.cuentas_anteriores, 0)
                AS variacion_cuentas,

            CASE
                WHEN COALESCE(ant.saldo_anterior, 0) = 0
                    THEN 0

                ELSE ROUND(
                    (
                        (
                            COALESCE(a.saldo_actual, 0)
                            -
                            COALESCE(ant.saldo_anterior, 0)
                        ) * 100.0
                    )
                    /
                    ant.saldo_anterior,
                    2
                )
            END AS porcentaje_crecimiento

        FROM actual a

        FULL JOIN anterior ant
            ON ant.codigo_forma = a.codigo_forma
           AND ant.nombre_forma = a.nombre_forma

        LEFT JOIN movimientos_periodo_forma mpf
            ON mpf.codigo_forma =
                COALESCE(
                    a.codigo_forma,
                    ant.codigo_forma
                )

           AND mpf.nombre_forma =
                COALESCE(
                    a.nombre_forma,
                    ant.nombre_forma
                )

        ORDER BY
            COALESCE(
                a.codigo_forma,
                ant.codigo_forma
            )
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardDepositosFormaDTO.builder()
                                .idFormaAhorro(
                                        rs.getInt("id_forma_ahorro")
                                )
                                .codigoForma(
                                        rs.getString("codigo_forma")
                                )
                                .nombreForma(
                                        rs.getString("nombre_forma")
                                )
                                .cuentasActuales(
                                        rs.getInt("cuentas_actuales")
                                )
                                .saldoActual(
                                        rs.getBigDecimal("saldo_actual")
                                )
                                .hombres(
                                        rs.getInt("hombres")
                                )
                                .mujeres(
                                        rs.getInt("mujeres")
                                )
                                .juridicas(
                                        rs.getInt("juridicas")
                                )
                                .cuentasAnteriores(
                                        rs.getInt("cuentas_anteriores")
                                )
                                .saldoAnterior(
                                        rs.getBigDecimal("saldo_anterior")
                                )
                                .ingresosPeriodo(
                                        rs.getBigDecimal("ingresos_periodo")
                                )
                                .egresosPeriodo(
                                        rs.getBigDecimal("egresos_periodo")
                                )
                                .variacionSaldo(
                                        rs.getBigDecimal("variacion_saldo")
                                )
                                .variacionCuentas(
                                        rs.getInt("variacion_cuentas")
                                )
                                .porcentajeCrecimiento(
                                        rs.getBigDecimal("porcentaje_crecimiento")
                                )
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
                    ELSE COALESCE(mt.total_creditos, 0) - COALESCE(mt.total_debitos, 0)
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
                        ELSE COALESCE(mt.total_creditos, 0) - COALESCE(mt.total_debitos, 0)
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

    private List<DashboardDepositosTipoCaptacionDTO> obtenerTiposCaptacion(
            List<DashboardDepositosFormaDTO> formas
    ) {

        String sql = """
        SELECT DISTINCT
            TRIM(f.codigo_forma) AS codigo_forma,
            TRIM(f.tipo_captacion_forma) AS codigo_captacion,
            TRIM(tc.descripcion_captacion) AS descripcion_captacion
        FROM depositos.formas_ahorro f
        INNER JOIN depositos.tipos_captaciones tc
            ON TRIM(tc.codigo_captacion) =
               TRIM(f.tipo_captacion_forma)
        ORDER BY
            codigo_forma
        """;

        record Clasificacion(
                String codigoForma,
                String codigoCaptacion,
                String descripcionCaptacion
        ) {}

        List<Clasificacion> clasificaciones =
                jdbc.query(
                        sql,
                        (rs, rowNum) ->
                                new Clasificacion(
                                        rs.getString("codigo_forma"),
                                        rs.getString("codigo_captacion"),
                                        rs.getString("descripcion_captacion")
                                )
                );

        BigDecimal saldoTotal =
                formas.stream()
                        .map(DashboardDepositosFormaDTO::getSaldoActual)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return clasificaciones.stream()
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                Clasificacion::codigoCaptacion,
                                java.util.LinkedHashMap::new,
                                java.util.stream.Collectors.toList()
                        )
                )
                .entrySet()
                .stream()
                .map(entry -> {

                    String codigoCaptacion = entry.getKey();

                    String descripcion =
                            entry.getValue()
                                    .get(0)
                                    .descripcionCaptacion();

                    java.util.Set<String> codigosFormas =
                            entry.getValue()
                                    .stream()
                                    .map(Clasificacion::codigoForma)
                                    .collect(java.util.stream.Collectors.toSet());

                    List<DashboardDepositosFormaDTO> formasTipo =
                            formas.stream()
                                    .filter(f ->
                                            codigosFormas.contains(
                                                    f.getCodigoForma().trim()
                                            )
                                    )
                                    .toList();

                    int cuentasAnteriores =
                            formasTipo.stream()
                                    .map(DashboardDepositosFormaDTO::getCuentasAnteriores)
                                    .filter(java.util.Objects::nonNull)
                                    .reduce(0, Integer::sum);

                    int cuentasActuales =
                            formasTipo.stream()
                                    .map(DashboardDepositosFormaDTO::getCuentasActuales)
                                    .filter(java.util.Objects::nonNull)
                                    .reduce(0, Integer::sum);

                    BigDecimal saldoAnterior =
                            sumar(
                                    formasTipo,
                                    DashboardDepositosFormaDTO::getSaldoAnterior
                            );

                    BigDecimal saldoActual =
                            sumar(
                                    formasTipo,
                                    DashboardDepositosFormaDTO::getSaldoActual
                            );

                    BigDecimal ingresos =
                            sumar(
                                    formasTipo,
                                    DashboardDepositosFormaDTO::getIngresosPeriodo
                            );

                    BigDecimal egresos =
                            sumar(
                                    formasTipo,
                                    DashboardDepositosFormaDTO::getEgresosPeriodo
                            );

                    BigDecimal variacionSaldo =
                            saldoActual.subtract(saldoAnterior);

                    int variacionCuentas =
                            cuentasActuales - cuentasAnteriores;

                    BigDecimal porcentajeCrecimiento =
                            saldoAnterior.compareTo(BigDecimal.ZERO) == 0
                                    ? BigDecimal.ZERO
                                    : variacionSaldo
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(
                                            saldoAnterior,
                                            2,
                                            java.math.RoundingMode.HALF_UP
                                    );

                    BigDecimal participacion =
                            saldoTotal.compareTo(BigDecimal.ZERO) == 0
                                    ? BigDecimal.ZERO
                                    : saldoActual
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(
                                            saldoTotal,
                                            2,
                                            java.math.RoundingMode.HALF_UP
                                    );

                    return DashboardDepositosTipoCaptacionDTO.builder()
                            .codigoCaptacion(codigoCaptacion)
                            .descripcionCaptacion(descripcion)
                            .cuentasAnteriores(cuentasAnteriores)
                            .saldoAnterior(saldoAnterior)
                            .cuentasActuales(cuentasActuales)
                            .saldoActual(saldoActual)
                            .ingresosPeriodo(ingresos)
                            .egresosPeriodo(egresos)
                            .variacionSaldo(variacionSaldo)
                            .variacionCuentas(variacionCuentas)
                            .porcentajeCrecimiento(porcentajeCrecimiento)
                            .participacion(participacion)
                            .build();

                })
                .toList();
    }

    private BigDecimal sumar(
            List<DashboardDepositosFormaDTO> formas,
            java.util.function.Function<
                    DashboardDepositosFormaDTO,
                    BigDecimal
                    > extractor
    ) {

        return formas.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}