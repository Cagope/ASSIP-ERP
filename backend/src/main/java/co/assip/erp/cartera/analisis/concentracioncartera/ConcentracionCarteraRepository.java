package co.assip.erp.cartera.analisis.concentracioncartera;

import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionCarteraControlDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionCarteraResumenDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionDetalleCreditoDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionDetalleDeudorDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionRankingDeudorDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionSegmentoDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ConcentracionCarteraRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ConcentracionCarteraRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String BASE_CTE = """
            WITH base_corte AS MATERIALIZED (
                SELECT DISTINCT ON (v.id_cartera_credito)
                       v.*
                FROM cartera.vw_cartera_resultados_mensuales_total v
                WHERE v.fecha_corte = :fechaCorte
                ORDER BY
                    v.id_cartera_credito,
                    v.id_cierre_cartera DESC
            ),
            base AS MATERIALIZED (
                SELECT
                    b.*
                FROM base_corte b
                WHERE COALESCE(b.saldo_credito_fecha_corte, 0) > 0
                  AND (:idAgencia IS NULL OR b.id_agencia = :idAgencia)
                  AND (:idLineaCredito IS NULL OR b.id_linea_credito = :idLineaCredito)
                  AND (:codigoGarantia IS NULL OR UPPER(TRIM(b.codigo_garantia_credito)) = :codigoGarantia)
                  AND (:codigoClasificacion IS NULL OR UPPER(TRIM(b.codigo_clasificacion_credito)) = :codigoClasificacion)
                  AND (:edadContable IS NULL OR UPPER(TRIM(b.edad_contable_resultado)) = :edadContable)
                  AND (:codigoDestino IS NULL OR UPPER(TRIM(b.codigo_destino_economico)) = :codigoDestino)
            )
            """;

    public ConcentracionCarteraControlDTO control() {
        String sql = """
                SELECT
                    MIN(v.fecha_corte) AS primer_corte_disponible,
                    MAX(v.fecha_corte) AS ultimo_corte_disponible,
                    MAX(v.fecha_corte) AS corte_sugerido
                FROM cartera.vw_cartera_resultados_mensuales_total v
                """;

        return jdbc.queryForObject(sql, new MapSqlParameterSource(), (rs, rowNum) ->
                new ConcentracionCarteraControlDTO(
                        rs.getObject("primer_corte_disponible", LocalDate.class),
                        rs.getObject("ultimo_corte_disponible", LocalDate.class),
                        rs.getObject("corte_sugerido", LocalDate.class)
                )
        );
    }

    public List<LocalDate> cortes() {
        String sql = """
                SELECT DISTINCT v.fecha_corte
                FROM cartera.vw_cartera_resultados_mensuales_total v
                WHERE v.fecha_corte IS NOT NULL
                ORDER BY v.fecha_corte DESC
                """;

        return jdbc.query(sql, new MapSqlParameterSource(),
                (rs, rowNum) -> rs.getObject("fecha_corte", LocalDate.class));
    }

    public LocalDate ultimoCorte() {
        String sql = """
                SELECT MAX(v.fecha_corte)
                FROM cartera.vw_cartera_resultados_mensuales_total v
                """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource(), LocalDate.class);
    }

    public boolean existeCorte(LocalDate fechaCorte) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM cartera.vw_cartera_resultados_mensuales_total v
                    WHERE v.fecha_corte = :fechaCorte
                )
                """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource().addValue("fechaCorte", fechaCorte, Types.DATE),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    public ConcentracionCarteraResumenDTO resumen(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String sql = BASE_CTE + """
                , deudores AS MATERIALIZED (
                    SELECT
                        b.id_datos_personal,
                        COUNT(*) AS cantidad_creditos,
                        SUM(COALESCE(b.saldo_credito_fecha_corte, 0)) AS saldo_deudor,
                        SUM(COALESCE(b.deterioro_total, 0)) AS deterioro_deudor
                    FROM base b
                    GROUP BY b.id_datos_personal
                ),
                totales AS MATERIALIZED (
                    SELECT
                        COUNT(*) AS cantidad_creditos,
                        COUNT(DISTINCT b.id_datos_personal) AS cantidad_deudores,
                        SUM(COALESCE(b.saldo_credito_fecha_corte, 0)) AS saldo_total,
                        SUM(COALESCE(b.deterioro_total, 0)) AS deterioro_total
                    FROM base b
                ),
                ranking_saldo AS MATERIALIZED (
                    SELECT
                        d.*,
                        ROW_NUMBER() OVER (
                            ORDER BY d.saldo_deudor DESC, d.id_datos_personal
                        ) AS posicion,
                        SUM(d.saldo_deudor) OVER () AS saldo_total,
                        SUM(d.saldo_deudor) OVER (
                            ORDER BY d.saldo_deudor DESC, d.id_datos_personal
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                        ) AS saldo_acumulado
                    FROM deudores d
                ),
                ranking_deterioro AS MATERIALIZED (
                    SELECT
                        d.*,
                        ROW_NUMBER() OVER (
                            ORDER BY d.deterioro_deudor DESC, d.id_datos_personal
                        ) AS posicion,
                        SUM(d.deterioro_deudor) OVER () AS deterioro_total,
                        SUM(d.deterioro_deudor) OVER (
                            ORDER BY d.deterioro_deudor DESC, d.id_datos_personal
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                        ) AS deterioro_acumulado
                    FROM deudores d
                ),
                metricas_saldo AS (
                    SELECT
                        COUNT(*) AS cantidad_deudores,
                        MAX(saldo_total) AS saldo_total,

                        MAX(saldo_deudor) FILTER (WHERE posicion = 1) AS saldo_mayor_deudor,
                        100.0 * MAX(saldo_deudor) FILTER (WHERE posicion = 1)
                            / NULLIF(MAX(saldo_total), 0) AS porcentaje_mayor_deudor,

                        SUM(saldo_deudor) FILTER (WHERE posicion <= 10) AS saldo_top_10,
                        100.0 * SUM(saldo_deudor) FILTER (WHERE posicion <= 10)
                            / NULLIF(MAX(saldo_total), 0) AS porcentaje_top_10,

                        SUM(saldo_deudor) FILTER (WHERE posicion <= 20) AS saldo_top_20,
                        100.0 * SUM(saldo_deudor) FILTER (WHERE posicion <= 20)
                            / NULLIF(MAX(saldo_total), 0) AS porcentaje_top_20,

                        SUM(saldo_deudor) FILTER (WHERE posicion <= 50) AS saldo_top_50,
                        100.0 * SUM(saldo_deudor) FILTER (WHERE posicion <= 50)
                            / NULLIF(MAX(saldo_total), 0) AS porcentaje_top_50,

                        SUM(
                            POWER(
                                saldo_deudor / NULLIF(saldo_total, 0),
                                2
                            )
                        ) * 10000.0 AS hhi_10000,

                        MIN(posicion) FILTER (
                            WHERE 100.0 * saldo_acumulado / NULLIF(saldo_total, 0) >= 50
                        ) AS deudores_para_50_pct,

                        MIN(posicion) FILTER (
                            WHERE 100.0 * saldo_acumulado / NULLIF(saldo_total, 0) >= 80
                        ) AS deudores_para_80_pct
                    FROM ranking_saldo
                ),
                metricas_deterioro AS (
                    SELECT
                        COUNT(*) FILTER (WHERE deterioro_deudor > 0) AS deudores_con_deterioro,
                        MAX(deterioro_total) AS deterioro_total,

                        MAX(deterioro_deudor) FILTER (WHERE posicion = 1) AS deterioro_mayor_deudor,
                        100.0 * MAX(deterioro_deudor) FILTER (WHERE posicion = 1)
                            / NULLIF(MAX(deterioro_total), 0) AS porcentaje_deterioro_mayor_deudor,

                        SUM(deterioro_deudor) FILTER (WHERE posicion <= 10) AS deterioro_top_10,
                        100.0 * SUM(deterioro_deudor) FILTER (WHERE posicion <= 10)
                            / NULLIF(MAX(deterioro_total), 0) AS porcentaje_deterioro_top_10,

                        SUM(deterioro_deudor) FILTER (WHERE posicion <= 20) AS deterioro_top_20,
                        100.0 * SUM(deterioro_deudor) FILTER (WHERE posicion <= 20)
                            / NULLIF(MAX(deterioro_total), 0) AS porcentaje_deterioro_top_20,

                        SUM(deterioro_deudor) FILTER (WHERE posicion <= 50) AS deterioro_top_50,
                        100.0 * SUM(deterioro_deudor) FILTER (WHERE posicion <= 50)
                            / NULLIF(MAX(deterioro_total), 0) AS porcentaje_deterioro_top_50
                    FROM ranking_deterioro
                )
                SELECT
                    :fechaCorte::date AS fecha_corte,

                    COALESCE(t.cantidad_creditos, 0) AS cantidad_creditos,
                    COALESCE(t.cantidad_deudores, 0) AS cantidad_deudores,
                    COALESCE(t.saldo_total, 0) AS saldo_total,
                    COALESCE(t.deterioro_total, 0) AS deterioro_total,

                    COALESCE(ms.saldo_mayor_deudor, 0) AS saldo_mayor_deudor,
                    COALESCE(ms.porcentaje_mayor_deudor, 0) AS porcentaje_mayor_deudor,

                    COALESCE(ms.saldo_top_10, 0) AS saldo_top_10,
                    COALESCE(ms.porcentaje_top_10, 0) AS porcentaje_top_10,

                    COALESCE(ms.saldo_top_20, 0) AS saldo_top_20,
                    COALESCE(ms.porcentaje_top_20, 0) AS porcentaje_top_20,

                    COALESCE(ms.saldo_top_50, 0) AS saldo_top_50,
                    COALESCE(ms.porcentaje_top_50, 0) AS porcentaje_top_50,

                    COALESCE(ms.hhi_10000, 0) AS hhi_10000,
                    COALESCE(ms.deudores_para_50_pct, 0) AS deudores_para_50_pct,
                    COALESCE(ms.deudores_para_80_pct, 0) AS deudores_para_80_pct,

                    CASE
                        WHEN COALESCE(ms.cantidad_deudores, 0) = 0 THEN 0
                        ELSE 100.0 * ms.deudores_para_50_pct / ms.cantidad_deudores
                    END AS porcentaje_deudores_para_50_pct,

                    CASE
                        WHEN COALESCE(ms.cantidad_deudores, 0) = 0 THEN 0
                        ELSE 100.0 * ms.deudores_para_80_pct / ms.cantidad_deudores
                    END AS porcentaje_deudores_para_80_pct,

                    COALESCE(md.deudores_con_deterioro, 0) AS deudores_con_deterioro,

                    COALESCE(md.deterioro_mayor_deudor, 0) AS deterioro_mayor_deudor,
                    COALESCE(md.porcentaje_deterioro_mayor_deudor, 0) AS porcentaje_deterioro_mayor_deudor,

                    COALESCE(md.deterioro_top_10, 0) AS deterioro_top_10,
                    COALESCE(md.porcentaje_deterioro_top_10, 0) AS porcentaje_deterioro_top_10,

                    COALESCE(md.deterioro_top_20, 0) AS deterioro_top_20,
                    COALESCE(md.porcentaje_deterioro_top_20, 0) AS porcentaje_deterioro_top_20,

                    COALESCE(md.deterioro_top_50, 0) AS deterioro_top_50,
                    COALESCE(md.porcentaje_deterioro_top_50, 0) AS porcentaje_deterioro_top_50

                FROM totales t
                CROSS JOIN metricas_saldo ms
                CROSS JOIN metricas_deterioro md
                """;

        return jdbc.queryForObject(
                sql,
                parametros(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                        codigoClasificacion, edadContable, codigoDestino),
                (rs, rowNum) -> new ConcentracionCarteraResumenDTO(
                        rs.getObject("fecha_corte", LocalDate.class),
                        rs.getLong("cantidad_creditos"),
                        rs.getLong("cantidad_deudores"),
                        rs.getBigDecimal("saldo_total"),
                        rs.getBigDecimal("deterioro_total"),

                        rs.getBigDecimal("saldo_mayor_deudor"),
                        rs.getBigDecimal("porcentaje_mayor_deudor"),

                        rs.getBigDecimal("saldo_top_10"),
                        rs.getBigDecimal("porcentaje_top_10"),
                        rs.getBigDecimal("saldo_top_20"),
                        rs.getBigDecimal("porcentaje_top_20"),
                        rs.getBigDecimal("saldo_top_50"),
                        rs.getBigDecimal("porcentaje_top_50"),

                        rs.getBigDecimal("hhi_10000"),
                        rs.getLong("deudores_para_50_pct"),
                        rs.getLong("deudores_para_80_pct"),
                        rs.getBigDecimal("porcentaje_deudores_para_50_pct"),
                        rs.getBigDecimal("porcentaje_deudores_para_80_pct"),

                        rs.getLong("deudores_con_deterioro"),
                        rs.getBigDecimal("deterioro_mayor_deudor"),
                        rs.getBigDecimal("porcentaje_deterioro_mayor_deudor"),
                        rs.getBigDecimal("deterioro_top_10"),
                        rs.getBigDecimal("porcentaje_deterioro_top_10"),
                        rs.getBigDecimal("deterioro_top_20"),
                        rs.getBigDecimal("porcentaje_deterioro_top_20"),
                        rs.getBigDecimal("deterioro_top_50"),
                        rs.getBigDecimal("porcentaje_deterioro_top_50")
                )
        );
    }

    public List<ConcentracionRankingDeudorDTO> deudoresExposicion(
            LocalDate fechaCorte,
            int limite,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String sql = BASE_CTE + rankingDeudoresCte("saldo_deudor") + """
                SELECT
                    r.posicion,
                    r.id_datos_personal,
                    r.documento,
                    r.nombre_completo,
                    r.cantidad_creditos,
                    r.saldo_deudor AS saldo,
                    100.0 * r.saldo_deudor / NULLIF(r.saldo_total, 0) AS porcentaje_cartera,
                    100.0 * r.saldo_acumulado / NULLIF(r.saldo_total, 0) AS porcentaje_cartera_acumulado,
                    r.deterioro_deudor AS deterioro,
                    100.0 * r.deterioro_deudor / NULLIF(r.deterioro_total, 0) AS porcentaje_deterioro,
                    100.0 * r.deterioro_acumulado / NULLIF(r.deterioro_total, 0) AS porcentaje_deterioro_acumulado
                FROM ranking r
                WHERE r.posicion <= :limite
                ORDER BY r.posicion
                """;

        MapSqlParameterSource p = parametros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino)
                .addValue("limite", limite, Types.INTEGER);

        return jdbc.query(sql, p, this::mapRankingDeudor);
    }

    public List<ConcentracionRankingDeudorDTO> deudoresDeterioro(
            LocalDate fechaCorte,
            int limite,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String sql = BASE_CTE + rankingDeudoresCte("deterioro_deudor") + """
                SELECT
                    r.posicion,
                    r.id_datos_personal,
                    r.documento,
                    r.nombre_completo,
                    r.cantidad_creditos,
                    r.saldo_deudor AS saldo,
                    100.0 * r.saldo_deudor / NULLIF(r.saldo_total, 0) AS porcentaje_cartera,
                    100.0 * r.saldo_acumulado / NULLIF(r.saldo_total, 0) AS porcentaje_cartera_acumulado,
                    r.deterioro_deudor AS deterioro,
                    100.0 * r.deterioro_deudor / NULLIF(r.deterioro_total, 0) AS porcentaje_deterioro,
                    100.0 * r.deterioro_acumulado / NULLIF(r.deterioro_total, 0) AS porcentaje_deterioro_acumulado
                FROM ranking r
                WHERE r.posicion <= :limite
                ORDER BY r.posicion
                """;

        MapSqlParameterSource p = parametros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino)
                .addValue("limite", limite, Types.INTEGER);

        return jdbc.query(sql, p, this::mapRankingDeudor);
    }

    private String rankingDeudoresCte(String criterio) {
        String ordenSecundario = "d.id_datos_personal";

        return """
                , deudores AS MATERIALIZED (
                    SELECT
                        b.id_datos_personal,
                        MAX(b.documento) AS documento,
                        MAX(b.nombre_completo) AS nombre_completo,
                        COUNT(*) AS cantidad_creditos,
                        SUM(COALESCE(b.saldo_credito_fecha_corte, 0)) AS saldo_deudor,
                        SUM(COALESCE(b.deterioro_total, 0)) AS deterioro_deudor
                    FROM base b
                    GROUP BY b.id_datos_personal
                ),
                ranking AS MATERIALIZED (
                    SELECT
                        d.*,
                        ROW_NUMBER() OVER (
                            ORDER BY d.%s DESC, %s
                        ) AS posicion,
                        SUM(d.saldo_deudor) OVER () AS saldo_total,
                        SUM(d.deterioro_deudor) OVER () AS deterioro_total,
                        SUM(d.saldo_deudor) OVER (
                            ORDER BY d.%s DESC, %s
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                        ) AS saldo_acumulado,
                        SUM(d.deterioro_deudor) OVER (
                            ORDER BY d.%s DESC, %s
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                        ) AS deterioro_acumulado
                    FROM deudores d
                )
                """.formatted(
                criterio, ordenSecundario,
                criterio, ordenSecundario,
                criterio, ordenSecundario
        );
    }

    public List<ConcentracionSegmentoDTO> lineas(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String select = """
                b.id_linea_credito::bigint AS id_segmento,
                COALESCE(MAX(b.codigo_linea_credito), 'SIN') AS codigo,
                COALESCE(MAX(b.nombre_linea_credito), 'SIN LÍNEA') AS descripcion
                """;

        String groupBy = "b.id_linea_credito";

        return consultarSegmento(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino, select, groupBy,
                "saldo DESC, codigo");
    }

    public List<ConcentracionSegmentoDTO> agencias(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String extraJoin = """
                LEFT JOIN general.datos_agencias da
                       ON da.id_agencia = b.id_agencia
                """;

        String select = """
                b.id_agencia::bigint AS id_segmento,
                COALESCE(MAX(da.codigo_agencia), 'SIN') AS codigo,
                COALESCE(MAX(da.nombre_agencia), 'SIN AGENCIA') AS descripcion
                """;

        String groupBy = "b.id_agencia";

        return consultarSegmento(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino, select, groupBy,
                "saldo DESC, codigo", extraJoin);
    }

    public List<ConcentracionSegmentoDTO> garantias(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String select = """
                NULL::bigint AS id_segmento,
                COALESCE(b.codigo_garantia_credito, 'SIN') AS codigo,
                COALESCE(MAX(b.descripcion_garantia_credito), 'SIN GARANTÍA CLASIFICADA') AS descripcion
                """;

        String groupBy = "COALESCE(b.codigo_garantia_credito, 'SIN')";

        return consultarSegmento(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino, select, groupBy,
                "saldo DESC, codigo");
    }

    public List<ConcentracionSegmentoDTO> clasificaciones(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String select = """
                NULL::bigint AS id_segmento,
                COALESCE(b.codigo_clasificacion_credito, 'SIN') AS codigo,
                COALESCE(MAX(b.descripcion_clasificacion_credito), 'SIN CLASIFICACIÓN') AS descripcion
                """;

        String groupBy = "COALESCE(b.codigo_clasificacion_credito, 'SIN')";

        return consultarSegmento(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino, select, groupBy,
                "saldo DESC, codigo");
    }

    public List<ConcentracionSegmentoDTO> edadesContables(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String select = """
                NULL::bigint AS id_segmento,
                COALESCE(b.edad_contable_resultado, 'SIN') AS codigo,
                CASE COALESCE(b.edad_contable_resultado, 'SIN')
                    WHEN 'A' THEN 'A'
                    WHEN 'B' THEN 'B'
                    WHEN 'C' THEN 'C'
                    WHEN 'D' THEN 'D'
                    WHEN 'E' THEN 'E'
                    WHEN 'F' THEN 'F'
                    ELSE 'SIN EDAD CONTABLE'
                END AS descripcion
                """;

        String groupBy = "COALESCE(b.edad_contable_resultado, 'SIN')";

        return consultarSegmento(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino, select, groupBy,
                """
                CASE codigo
                    WHEN 'A' THEN 1
                    WHEN 'B' THEN 2
                    WHEN 'C' THEN 3
                    WHEN 'D' THEN 4
                    WHEN 'E' THEN 5
                    WHEN 'F' THEN 6
                    ELSE 7
                END,
                codigo
                """);
    }

    public List<ConcentracionSegmentoDTO> destinos(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String select = """
                NULL::bigint AS id_segmento,
                COALESCE(b.codigo_destino_economico, 'SIN') AS codigo,
                COALESCE(MAX(b.descripcion_destino_economico), 'SIN DESTINO ECONÓMICO') AS descripcion
                """;

        String groupBy = "COALESCE(b.codigo_destino_economico, 'SIN')";

        return consultarSegmento(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino, select, groupBy,
                "saldo DESC, codigo");
    }

    private List<ConcentracionSegmentoDTO> consultarSegmento(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino,
            String selectSegmento,
            String groupBy,
            String orderBy
    ) {
        return consultarSegmento(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino,
                selectSegmento, groupBy, orderBy, "");
    }

    private List<ConcentracionSegmentoDTO> consultarSegmento(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino,
            String selectSegmento,
            String groupBy,
            String orderBy,
            String extraJoin
    ) {
        String sql = BASE_CTE + """
                , totales AS MATERIALIZED (
                    SELECT
                        SUM(COALESCE(b.saldo_credito_fecha_corte, 0)) AS saldo_total,
                        SUM(COALESCE(b.deterioro_total, 0)) AS deterioro_total
                    FROM base b
                ),
                segmentos AS MATERIALIZED (
                    SELECT
                        %s,
                        COUNT(*) AS cantidad_creditos,
                        COUNT(DISTINCT b.id_datos_personal) AS cantidad_deudores,
                        SUM(COALESCE(b.saldo_credito_fecha_corte, 0)) AS saldo,
                        SUM(COALESCE(b.deterioro_total, 0)) AS deterioro,
                        AVG(COALESCE(b.saldo_credito_fecha_corte, 0)) AS saldo_promedio,
                        MAX(COALESCE(b.saldo_credito_fecha_corte, 0)) AS mayor_credito
                    FROM base b
                    %s
                    GROUP BY %s
                )
                SELECT
                    s.id_segmento,
                    s.codigo,
                    s.descripcion,
                    s.cantidad_creditos,
                    s.cantidad_deudores,
                    s.saldo,
                    100.0 * s.saldo / NULLIF(t.saldo_total, 0) AS porcentaje_cartera,
                    s.deterioro,
                    100.0 * s.deterioro / NULLIF(t.deterioro_total, 0) AS porcentaje_deterioro,
                    (
                        100.0 * s.deterioro / NULLIF(t.deterioro_total, 0)
                        -
                        100.0 * s.saldo / NULLIF(t.saldo_total, 0)
                    ) AS brecha_deterioro,
                    s.saldo_promedio,
                    s.mayor_credito
                FROM segmentos s
                CROSS JOIN totales t
                ORDER BY %s
                """.formatted(selectSegmento, extraJoin, groupBy, orderBy);

        return jdbc.query(
                sql,
                parametros(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                        codigoClasificacion, edadContable, codigoDestino),
                this::mapSegmento
        );
    }

    public List<ConcentracionDetalleDeudorDTO> detalleDeudores(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String sql = BASE_CTE + """
                , totales AS MATERIALIZED (
                    SELECT
                        SUM(COALESCE(b.saldo_credito_fecha_corte, 0)) AS saldo_total,
                        SUM(COALESCE(b.deterioro_total, 0)) AS deterioro_total
                    FROM base b
                ),
                contactos AS MATERIALIZED (
                    SELECT
                        dp.id_datos_personal,
                        MAX(dp.telefono) AS telefono,
                        MAX(dp.celular_uno) AS celular,
                        MAX(dp.correo_personal) AS correo
                    FROM reporting.vw_datos_personales_operativa dp
                    GROUP BY dp.id_datos_personal
                ),
                deudores AS MATERIALIZED (
                    SELECT
                        b.id_datos_personal,
                        MAX(b.documento) AS documento,
                        MAX(b.nombre_completo) AS nombre_completo,
                        COUNT(*) AS cantidad_creditos,
                        SUM(COALESCE(b.saldo_credito_fecha_corte, 0)) AS saldo,
                        SUM(COALESCE(b.deterioro_total, 0)) AS deterioro,

                        STRING_AGG(
                            DISTINCT COALESCE(da.nombre_agencia, b.id_agencia::text),
                            ' | '
                            ORDER BY COALESCE(da.nombre_agencia, b.id_agencia::text)
                        ) AS agencias,

                        STRING_AGG(
                            DISTINCT COALESCE(b.nombre_linea_credito, b.codigo_linea_credito, 'SIN LÍNEA'),
                            ' | '
                            ORDER BY COALESCE(b.nombre_linea_credito, b.codigo_linea_credito, 'SIN LÍNEA')
                        ) AS lineas,

                        MAX(
                            CASE COALESCE(b.edad_contable_resultado, 'SIN')
                                WHEN 'A' THEN 1
                                WHEN 'B' THEN 2
                                WHEN 'C' THEN 3
                                WHEN 'D' THEN 4
                                WHEN 'E' THEN 5
                                WHEN 'F' THEN 6
                                ELSE 0
                            END
                        ) AS orden_edad_maxima,

                        MAX(COALESCE(b.dias_mora, 0)) AS dias_mora_maximos

                    FROM base b
                    LEFT JOIN general.datos_agencias da
                           ON da.id_agencia = b.id_agencia
                    GROUP BY b.id_datos_personal
                ),
                ranking AS MATERIALIZED (
                    SELECT
                        d.*,
                        ROW_NUMBER() OVER (
                            ORDER BY d.saldo DESC, d.id_datos_personal
                        ) AS posicion,
                        SUM(d.saldo) OVER () AS saldo_total,
                        SUM(d.saldo) OVER (
                            ORDER BY d.saldo DESC, d.id_datos_personal
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                        ) AS saldo_acumulado
                    FROM deudores d
                )
                SELECT
                    r.posicion,
                    r.id_datos_personal,
                    r.documento,
                    r.nombre_completo,
                    r.cantidad_creditos,
                    r.saldo,
                    100.0 * r.saldo / NULLIF(t.saldo_total, 0) AS porcentaje_cartera,
                    100.0 * r.saldo_acumulado / NULLIF(t.saldo_total, 0) AS porcentaje_acumulado,
                    r.deterioro,
                    100.0 * r.deterioro / NULLIF(t.deterioro_total, 0) AS porcentaje_deterioro,
                    r.agencias,
                    r.lineas,

                    CASE r.orden_edad_maxima
                        WHEN 1 THEN 'A'
                        WHEN 2 THEN 'B'
                        WHEN 3 THEN 'C'
                        WHEN 4 THEN 'D'
                        WHEN 5 THEN 'E'
                        WHEN 6 THEN 'F'
                        ELSE NULL
                    END AS mayor_edad_contable,

                    r.dias_mora_maximos,
                    c.telefono,
                    c.celular,
                    c.correo

                FROM ranking r
                CROSS JOIN totales t
                LEFT JOIN contactos c
                       ON c.id_datos_personal = r.id_datos_personal
                ORDER BY r.posicion
                """;

        return jdbc.query(
                sql,
                parametros(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                        codigoClasificacion, edadContable, codigoDestino),
                (rs, rowNum) -> new ConcentracionDetalleDeudorDTO(
                        rs.getLong("posicion"),
                        rs.getLong("id_datos_personal"),
                        rs.getString("documento"),
                        rs.getString("nombre_completo"),
                        rs.getLong("cantidad_creditos"),
                        rs.getBigDecimal("saldo"),
                        rs.getBigDecimal("porcentaje_cartera"),
                        rs.getBigDecimal("porcentaje_acumulado"),
                        rs.getBigDecimal("deterioro"),
                        rs.getBigDecimal("porcentaje_deterioro"),
                        rs.getString("agencias"),
                        rs.getString("lineas"),
                        rs.getString("mayor_edad_contable"),
                        rs.getInt("dias_mora_maximos"),
                        rs.getString("telefono"),
                        rs.getString("celular"),
                        rs.getString("correo")
                )
        );
    }

    public List<ConcentracionDetalleCreditoDTO> detalleCreditos(
            LocalDate fechaCorte,
            Long idDatosPersonal,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        String sql = BASE_CTE + """
                , totales AS MATERIALIZED (
                    SELECT
                        SUM(COALESCE(b.saldo_credito_fecha_corte, 0)) AS saldo_total
                    FROM base b
                )
                SELECT
                    b.id_cartera_credito,
                    b.id_datos_personal,
                    b.documento,
                    b.nombre_completo,
                    b.pagare_cartera,

                    b.id_agencia,
                    da.codigo_agencia,
                    da.nombre_agencia,

                    b.id_linea_credito,
                    b.codigo_linea_credito,
                    b.nombre_linea_credito,

                    b.codigo_clasificacion_credito,
                    b.descripcion_clasificacion_credito,

                    b.codigo_garantia_credito,
                    b.descripcion_garantia_credito,
                    b.tipo_garantia,
                    b.codigo_subgarantia,
                    b.descripcion_subgarantia,

                    b.codigo_destino_economico,
                    b.descripcion_destino_economico,

                    b.fecha_desembolso,
                    COALESCE(b.saldo_credito_fecha_corte, 0) AS saldo,

                    100.0 * COALESCE(b.saldo_credito_fecha_corte, 0)
                        / NULLIF(t.saldo_total, 0) AS porcentaje_cartera,

                    b.edad_contable_resultado AS edad_contable,
                    COALESCE(b.dias_mora, 0) AS dias_mora,

                    COALESCE(b.deterioro_capital, 0) AS deterioro_capital,
                    COALESCE(b.deterioro_intereses, 0) AS deterioro_intereses,
                    COALESCE(b.deterioro_otros, 0) AS deterioro_otros,
                    COALESCE(b.deterioro_total, 0) AS deterioro_total,
                    COALESCE(b.perdida_esperada, 0) AS perdida_esperada,

                    COALESCE(b.cantidad_bienes_garantia, 0) AS cantidad_bienes_garantia,
                    COALESCE(b.valor_garantias_total, 0) AS valor_garantias_total,
                    COALESCE(b.porcentaje_garantias_credito, 0) AS porcentaje_garantias_credito,
                    COALESCE(b.valor_garantias_credito, 0) AS valor_garantias_credito,
                    COALESCE(b.valor_fondos_garantias, 0) AS valor_fondos_garantias

                FROM base b
                CROSS JOIN totales t
                LEFT JOIN general.datos_agencias da
                       ON da.id_agencia = b.id_agencia

                WHERE (:idDatosPersonal IS NULL OR b.id_datos_personal = :idDatosPersonal)

                ORDER BY
                    b.nombre_completo,
                    COALESCE(b.saldo_credito_fecha_corte, 0) DESC,
                    b.id_cartera_credito
                """;

        MapSqlParameterSource p = parametros(fechaCorte, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino)
                .addValue("idDatosPersonal", idDatosPersonal, Types.BIGINT);

        return jdbc.query(
                sql,
                p,
                (rs, rowNum) -> new ConcentracionDetalleCreditoDTO(
                        rs.getLong("id_cartera_credito"),
                        rs.getLong("id_datos_personal"),
                        rs.getString("documento"),
                        rs.getString("nombre_completo"),
                        rs.getString("pagare_cartera"),

                        (Integer) rs.getObject("id_agencia"),
                        rs.getString("codigo_agencia"),
                        rs.getString("nombre_agencia"),

                        (Integer) rs.getObject("id_linea_credito"),
                        rs.getString("codigo_linea_credito"),
                        rs.getString("nombre_linea_credito"),

                        rs.getString("codigo_clasificacion_credito"),
                        rs.getString("descripcion_clasificacion_credito"),

                        rs.getString("codigo_garantia_credito"),
                        rs.getString("descripcion_garantia_credito"),
                        rs.getString("tipo_garantia"),
                        rs.getString("codigo_subgarantia"),
                        rs.getString("descripcion_subgarantia"),

                        rs.getString("codigo_destino_economico"),
                        rs.getString("descripcion_destino_economico"),

                        rs.getObject("fecha_desembolso", LocalDate.class),
                        rs.getBigDecimal("saldo"),
                        rs.getBigDecimal("porcentaje_cartera"),

                        rs.getString("edad_contable"),
                        rs.getInt("dias_mora"),

                        rs.getBigDecimal("deterioro_capital"),
                        rs.getBigDecimal("deterioro_intereses"),
                        rs.getBigDecimal("deterioro_otros"),
                        rs.getBigDecimal("deterioro_total"),
                        rs.getBigDecimal("perdida_esperada"),

                        rs.getInt("cantidad_bienes_garantia"),
                        rs.getBigDecimal("valor_garantias_total"),
                        rs.getBigDecimal("porcentaje_garantias_credito"),
                        rs.getBigDecimal("valor_garantias_credito"),
                        rs.getBigDecimal("valor_fondos_garantias")
                )
        );
    }

    private ConcentracionRankingDeudorDTO mapRankingDeudor(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new ConcentracionRankingDeudorDTO(
                rs.getLong("posicion"),
                rs.getLong("id_datos_personal"),
                rs.getString("documento"),
                rs.getString("nombre_completo"),
                rs.getLong("cantidad_creditos"),
                rs.getBigDecimal("saldo"),
                rs.getBigDecimal("porcentaje_cartera"),
                rs.getBigDecimal("porcentaje_cartera_acumulado"),
                rs.getBigDecimal("deterioro"),
                rs.getBigDecimal("porcentaje_deterioro"),
                rs.getBigDecimal("porcentaje_deterioro_acumulado")
        );
    }

    private ConcentracionSegmentoDTO mapSegmento(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        Long idSegmento = rs.getObject("id_segmento") == null
                ? null
                : rs.getLong("id_segmento");

        return new ConcentracionSegmentoDTO(
                idSegmento,
                rs.getString("codigo"),
                rs.getString("descripcion"),
                rs.getLong("cantidad_creditos"),
                rs.getLong("cantidad_deudores"),
                rs.getBigDecimal("saldo"),
                rs.getBigDecimal("porcentaje_cartera"),
                rs.getBigDecimal("deterioro"),
                rs.getBigDecimal("porcentaje_deterioro"),
                rs.getBigDecimal("brecha_deterioro"),
                rs.getBigDecimal("saldo_promedio"),
                rs.getBigDecimal("mayor_credito")
        );
    }

    private MapSqlParameterSource parametros(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoGarantia,
            String codigoClasificacion,
            String edadContable,
            String codigoDestino
    ) {
        return new MapSqlParameterSource()
                .addValue("fechaCorte", fechaCorte, Types.DATE)
                .addValue("idAgencia", idAgencia, Types.INTEGER)
                .addValue("idLineaCredito", idLineaCredito, Types.INTEGER)
                .addValue("codigoGarantia", codigoGarantia, Types.VARCHAR)
                .addValue("codigoClasificacion", codigoClasificacion, Types.VARCHAR)
                .addValue("edadContable", edadContable, Types.VARCHAR)
                .addValue("codigoDestino", codigoDestino, Types.VARCHAR);
    }
}
