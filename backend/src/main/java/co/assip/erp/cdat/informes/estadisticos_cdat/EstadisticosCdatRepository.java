package co.assip.erp.cdat.informes.estadisticos_cdat;

import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatGrupoDTO;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatResumenDTO;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatTasaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatDetalleDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EstadisticosCdatRepository {

    private final JdbcTemplate jdbc;

    public EstadisticosCdatResumenDTO obtenerResumen(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT
                    COUNT(*) AS total_cdats,

                    COALESCE(
                        SUM(c.saldo_actual_cdat),
                        0
                    ) AS valor_total,

                    COALESCE(
                        AVG(c.tasa_nominal_anual),
                        0
                    ) AS promedio_tasa,

                    COALESCE(
                        AVG(c.plazo_meses),
                        0
                    ) AS promedio_plazo,

                    COUNT(*) FILTER (
                        WHERE c.fecha_vencimiento_cdat
                              BETWEEN ?
                              AND (? + INTERVAL '30 DAY')
                    ) AS vencen_30
                FROM cdat.cuentas_cdats c
                WHERE c.fecha_apertura_cdat <= ?
                """;

        return jdbc.queryForObject(
                sql,
                (rs, rowNum) ->
                        EstadisticosCdatResumenDTO.builder()
                                .totalCdats(
                                        rs.getInt("total_cdats")
                                )
                                .valorTotalCaptado(
                                        rs.getBigDecimal("valor_total")
                                )
                                .promedioTasa(
                                        rs.getBigDecimal("promedio_tasa")
                                )
                                .promedioPlazo(
                                        rs.getBigDecimal("promedio_plazo")
                                )
                                .vencen30Dias(
                                        rs.getInt("vencen_30")
                                )
                                .build(),
                fechaCorte,
                fechaCorte,
                fechaCorte
        );
    }

    public List<EstadisticosCdatGrupoDTO> obtenerRangos(
            LocalDate fechaCorte
    ) {

        String sql = """
            WITH base AS (
                SELECT
                    CASE
                        WHEN c.saldo_actual_cdat < 5000000
                            THEN 1
                        WHEN c.saldo_actual_cdat < 10000000
                            THEN 2
                        WHEN c.saldo_actual_cdat < 30000000
                            THEN 3
                        WHEN c.saldo_actual_cdat < 50000000
                            THEN 4
                        ELSE 5
                    END AS orden,

                    CASE
                        WHEN c.saldo_actual_cdat < 5000000
                            THEN '0 - 5 MILLONES'
                        WHEN c.saldo_actual_cdat < 10000000
                            THEN '5 - 10 MILLONES'
                        WHEN c.saldo_actual_cdat < 30000000
                            THEN '10 - 30 MILLONES'
                        WHEN c.saldo_actual_cdat < 50000000
                            THEN '30 - 50 MILLONES'
                        ELSE 'MAS DE 50 MILLONES'
                    END AS concepto,

                    c.saldo_actual_cdat,
                    c.tasa_nominal_anual,
                    c.plazo_meses
                FROM cdat.cuentas_cdats c
                WHERE c.fecha_apertura_cdat <= ?
            ),
            agrupado AS (
                SELECT
                    orden,
                    concepto,
                    COUNT(*) AS cantidad,
                    COALESCE(SUM(saldo_actual_cdat), 0) AS valor_total,
                    COALESCE(AVG(tasa_nominal_anual), 0) AS promedio_tasa,
                    COALESCE(AVG(plazo_meses), 0) AS promedio_plazo
                FROM base
                GROUP BY orden, concepto
            ),
            total AS (
                SELECT
                    COALESCE(SUM(valor_total), 0) AS total_general
                FROM agrupado
            )
            SELECT
                a.concepto,
                a.cantidad,
                a.valor_total,
                a.promedio_tasa,
                a.promedio_plazo,
                CASE
                    WHEN t.total_general = 0 THEN 0
                    ELSE ROUND((a.valor_total * 100.0) / t.total_general, 2)
                END AS participacion
            FROM agrupado a
            CROSS JOIN total t
            ORDER BY a.orden
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EstadisticosCdatGrupoDTO.builder()
                                .concepto(rs.getString("concepto"))
                                .cantidad(rs.getInt("cantidad"))
                                .valorTotal(rs.getBigDecimal("valor_total"))
                                .promedioTasa(rs.getBigDecimal("promedio_tasa"))
                                .promedioPlazo(rs.getBigDecimal("promedio_plazo"))
                                .participacion(rs.getBigDecimal("participacion"))
                                .build(),
                fechaCorte
        );
    }

    public List<EstadisticosCdatGrupoDTO> obtenerAmortizacion(
            LocalDate fechaCorte
    ) {

        String sql = """
            WITH base AS (
                SELECT
                    CASE
                        WHEN c.amortizacion_deposito = 'M' THEN 'Mensual'
                        WHEN c.amortizacion_deposito = 'B' THEN 'Bimestral'
                        WHEN c.amortizacion_deposito = 'T' THEN 'Trimestral'
                        WHEN c.amortizacion_deposito = 'S' THEN 'Semestral'
                        WHEN c.amortizacion_deposito = 'A' THEN 'Anual'
                        ELSE 'Sin definir'
                    END AS concepto,

                    c.saldo_actual_cdat,
                    c.tasa_nominal_anual,
                    c.plazo_meses
                FROM cdat.cuentas_cdats c
                WHERE c.fecha_apertura_cdat <= ?
            ),

            agrupado AS (
                SELECT
                    concepto,
                    COUNT(*) AS cantidad,
                    COALESCE(SUM(saldo_actual_cdat), 0) AS valor_total,
                    COALESCE(AVG(tasa_nominal_anual), 0) AS promedio_tasa,
                    COALESCE(AVG(plazo_meses), 0) AS promedio_plazo
                FROM base
                GROUP BY concepto
            ),

            total AS (
                SELECT
                    COALESCE(SUM(valor_total), 0) AS total_general
                FROM agrupado
            )

            SELECT
                a.concepto,
                a.cantidad,
                a.valor_total,
                a.promedio_tasa,
                a.promedio_plazo,

                CASE
                    WHEN t.total_general = 0 THEN 0
                    ELSE ROUND((a.valor_total * 100.0) / t.total_general, 2)
                END AS participacion

            FROM agrupado a
            CROSS JOIN total t
            ORDER BY a.concepto
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EstadisticosCdatGrupoDTO.builder()
                                .concepto(rs.getString("concepto"))
                                .cantidad(rs.getInt("cantidad"))
                                .valorTotal(rs.getBigDecimal("valor_total"))
                                .promedioTasa(rs.getBigDecimal("promedio_tasa"))
                                .promedioPlazo(rs.getBigDecimal("promedio_plazo"))
                                .participacion(rs.getBigDecimal("participacion"))
                                .build(),
                fechaCorte
        );
    }

    public List<EstadisticosCdatGrupoDTO> obtenerPlazos(
            LocalDate fechaCorte
    ) {

        String sql = """
            WITH base AS (
                SELECT
                    CASE
                        WHEN c.plazo_meses < 6
                            THEN 1
                        WHEN c.plazo_meses BETWEEN 6 AND 12
                            THEN 2
                        WHEN c.plazo_meses > 12
                             AND c.plazo_meses <= 18
                            THEN 3
                        ELSE 4
                    END AS orden,

                    CASE
                        WHEN c.plazo_meses < 6
                            THEN 'MENOR A 6 MESES'
                        WHEN c.plazo_meses BETWEEN 6 AND 12
                            THEN '6 A 12 MESES'
                        WHEN c.plazo_meses > 12
                             AND c.plazo_meses <= 18
                            THEN '12 A 18 MESES'
                        ELSE 'MAS DE 18 MESES'
                    END AS concepto,

                    c.saldo_actual_cdat,
                    c.tasa_nominal_anual,
                    c.plazo_meses
                FROM cdat.cuentas_cdats c
                WHERE c.fecha_apertura_cdat <= ?
            ),
            agrupado AS (
                SELECT
                    orden,
                    concepto,
                    COUNT(*) AS cantidad,
                    COALESCE(SUM(saldo_actual_cdat), 0) AS valor_total,
                    COALESCE(AVG(tasa_nominal_anual), 0) AS promedio_tasa,
                    COALESCE(AVG(plazo_meses), 0) AS promedio_plazo
                FROM base
                GROUP BY orden, concepto
            ),
            total AS (
                SELECT
                    COALESCE(SUM(valor_total), 0) AS total_general
                FROM agrupado
            )
            SELECT
                a.concepto,
                a.cantidad,
                a.valor_total,
                a.promedio_tasa,
                a.promedio_plazo,
                CASE
                    WHEN t.total_general = 0 THEN 0
                    ELSE ROUND((a.valor_total * 100.0) / t.total_general, 2)
                END AS participacion
            FROM agrupado a
            CROSS JOIN total t
            ORDER BY a.orden
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EstadisticosCdatGrupoDTO.builder()
                                .concepto(rs.getString("concepto"))
                                .cantidad(rs.getInt("cantidad"))
                                .valorTotal(rs.getBigDecimal("valor_total"))
                                .promedioTasa(rs.getBigDecimal("promedio_tasa"))
                                .promedioPlazo(rs.getBigDecimal("promedio_plazo"))
                                .participacion(rs.getBigDecimal("participacion"))
                                .build(),
                fechaCorte
        );
    }

    public List<EstadisticosCdatTasaDTO> obtenerTasas(
            LocalDate fechaCorte
    ) {

        String sql = """
            WITH base AS (
                SELECT
                    CASE
                        WHEN c.tasa_nominal_anual <= 5
                            THEN '0% - 5%'

                        WHEN c.tasa_nominal_anual <= 8
                            THEN '5.01% - 8%'

                        WHEN c.tasa_nominal_anual <= 10
                            THEN '8.01% - 10%'

                        ELSE 'MAS DE 10%'
                    END AS tasa,

                    c.saldo_actual_cdat,
                    c.plazo_meses
                FROM cdat.cuentas_cdats c
                WHERE c.fecha_apertura_cdat <= ?
            ),

            agrupado AS (
                SELECT
                    tasa,

                    COUNT(*) AS cantidad,

                    COALESCE(
                        SUM(saldo_actual_cdat),
                        0
                    ) AS valor_total,

                    COALESCE(
                        AVG(plazo_meses),
                        0
                    ) AS promedio_plazo
                FROM base
                GROUP BY tasa
            ),

            total AS (
                SELECT
                    COALESCE(
                        SUM(valor_total),
                        0
                    ) AS total_general
                FROM agrupado
            )

            SELECT
                a.tasa,
                a.cantidad,
                a.valor_total,
                a.promedio_plazo,

                CASE
                    WHEN t.total_general = 0
                        THEN 0

                    ELSE ROUND(
                        (a.valor_total * 100.0)
                        / t.total_general,
                        2
                    )
                END AS participacion

            FROM agrupado a
            CROSS JOIN total t

            ORDER BY a.tasa
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EstadisticosCdatTasaDTO.builder()
                                .tasa(
                                        rs.getString("tasa")
                                )
                                .cantidad(
                                        rs.getInt("cantidad")
                                )
                                .valorTotal(
                                        rs.getBigDecimal("valor_total")
                                )
                                .promedioPlazo(
                                        rs.getBigDecimal("promedio_plazo")
                                )
                                .participacion(
                                        rs.getBigDecimal("participacion")
                                )
                                .build(),
                fechaCorte
        );
    }

    public List<EstadisticosCdatGrupoDTO> obtenerPlazosDetalle(
            LocalDate fechaCorte
    ) {

        String sql = """
            WITH agrupado AS (

                SELECT
                    c.plazo_meses::text AS concepto,

                    COUNT(*) AS cantidad,

                    COALESCE(
                        SUM(c.saldo_actual_cdat),
                        0
                    ) AS valor_total,

                    COALESCE(
                        AVG(c.tasa_nominal_anual),
                        0
                    ) AS promedio_tasa,

                    COALESCE(
                        AVG(c.plazo_meses),
                        0
                    ) AS promedio_plazo

                FROM cdat.cuentas_cdats c

                WHERE c.fecha_apertura_cdat <= ?

                GROUP BY c.plazo_meses
            ),

            total AS (

                SELECT
                    COALESCE(
                        SUM(valor_total),
                        0
                    ) AS total_general

                FROM agrupado
            )

            SELECT
                a.concepto,
                a.cantidad,
                a.valor_total,
                a.promedio_tasa,
                a.promedio_plazo,

                CASE
                    WHEN t.total_general = 0
                        THEN 0

                    ELSE ROUND(
                        (a.valor_total * 100.0)
                        / t.total_general,
                        2
                    )
                END AS participacion

            FROM agrupado a
            CROSS JOIN total t

            ORDER BY CAST(a.concepto AS INTEGER)
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EstadisticosCdatGrupoDTO.builder()
                                .concepto(rs.getString("concepto"))
                                .cantidad(rs.getInt("cantidad"))
                                .valorTotal(rs.getBigDecimal("valor_total"))
                                .promedioTasa(rs.getBigDecimal("promedio_tasa"))
                                .promedioPlazo(rs.getBigDecimal("promedio_plazo"))
                                .participacion(rs.getBigDecimal("participacion"))
                                .build(),
                fechaCorte
        );
    }

    public List<EstadisticosCdatTasaDTO> obtenerTasasDetalle(
            LocalDate fechaCorte
    ) {

        String sql = """
            WITH agrupado AS (

                SELECT
                    TO_CHAR(
                        c.tasa_nominal_anual,
                        'FM999999990.00'
                    ) || '%' AS tasa,

                    COUNT(*) AS cantidad,

                    COALESCE(
                        SUM(c.saldo_actual_cdat),
                        0
                    ) AS valor_total,

                    COALESCE(
                        AVG(c.plazo_meses),
                        0
                    ) AS promedio_plazo

                FROM cdat.cuentas_cdats c

                WHERE c.fecha_apertura_cdat <= ?

                GROUP BY c.tasa_nominal_anual
            ),

            total AS (

                SELECT
                    COALESCE(
                        SUM(valor_total),
                        0
                    ) AS total_general

                FROM agrupado
            )

            SELECT
                a.tasa,
                a.cantidad,
                a.valor_total,
                a.promedio_plazo,

                CASE
                    WHEN t.total_general = 0
                        THEN 0

                    ELSE ROUND(
                        (a.valor_total * 100.0)
                        / t.total_general,
                        2
                    )
                END AS participacion

            FROM agrupado a
            CROSS JOIN total t

            ORDER BY a.tasa
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EstadisticosCdatTasaDTO.builder()
                                .tasa(rs.getString("tasa"))
                                .cantidad(rs.getInt("cantidad"))
                                .valorTotal(rs.getBigDecimal("valor_total"))
                                .promedioPlazo(rs.getBigDecimal("promedio_plazo"))
                                .participacion(rs.getBigDecimal("participacion"))
                                .build(),
                fechaCorte
        );
    }

    public List<EstadisticosCdatDetalleDTO> obtenerDetalle(
            LocalDate fechaCorte,
            String tipoBloque,
            String concepto
    ) {

        String condicion = obtenerCondicionDetalleVista(tipoBloque);

        String sql = """
            SELECT
                v.id_cuenta_cdat,
                v.codigo_cdat,
                COALESCE(v.documento, '') AS documento,
                COALESCE(
                    v.nombre_completo_apellidos,
                    v.nombre_completo_nombres,
                    ''
                ) AS nombre_completo,
                COALESCE(v.nombre_agencia, '') AS agencia,
                v.fecha_apertura_cdat,
                v.fecha_vencimiento_cdat,
                v.plazo_meses,
                v.tasa_nominal_anual,
                v.saldo_actual_cdat,
                COALESCE(v.descripcion_estado_cdat, '') AS estado_cdat
            FROM cdat.vw_cdat_cuentas_total_extendida v
            WHERE v.fecha_apertura_cdat <= ?
            """ + condicion + """
            ORDER BY
                v.fecha_apertura_cdat,
                v.codigo_cdat
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        EstadisticosCdatDetalleDTO.builder()
                                .idCuentaCdat(rs.getLong("id_cuenta_cdat"))
                                .codigoCdat(rs.getString("codigo_cdat"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .agencia(rs.getString("agencia"))
                                .fechaAperturaCdat(
                                        rs.getDate("fecha_apertura_cdat") == null
                                                ? null
                                                : rs.getDate("fecha_apertura_cdat").toLocalDate()
                                )
                                .fechaVencimientoCdat(
                                        rs.getDate("fecha_vencimiento_cdat") == null
                                                ? null
                                                : rs.getDate("fecha_vencimiento_cdat").toLocalDate()
                                )
                                .plazoMeses(rs.getInt("plazo_meses"))
                                .tasaNominalAnual(rs.getBigDecimal("tasa_nominal_anual"))
                                .saldoActualCdat(rs.getBigDecimal("saldo_actual_cdat"))
                                .estadoCdat(rs.getString("estado_cdat"))
                                .build(),
                fechaCorte,
                concepto
        );
    }

    private String obtenerCondicionDetalle(String tipoBloque) {

        if (tipoBloque == null) {
            throw new RuntimeException("No se recibió el tipo de bloque.");
        }

        return switch (tipoBloque) {

            case "RANGOS" -> """
                AND (
                    CASE
                        WHEN c.saldo_actual_cdat < 5000000
                            THEN '0 - 5 MILLONES'
                        WHEN c.saldo_actual_cdat < 10000000
                            THEN '5 - 10 MILLONES'
                        WHEN c.saldo_actual_cdat < 30000000
                            THEN '10 - 30 MILLONES'
                        WHEN c.saldo_actual_cdat < 50000000
                            THEN '30 - 50 MILLONES'
                        ELSE 'MAS DE 50 MILLONES'
                    END
                ) = ?
                """;

            case "AMORTIZACION" -> """
                AND (
                    CASE
                        WHEN c.amortizacion_deposito = 'M' THEN 'Mensual'
                        WHEN c.amortizacion_deposito = 'B' THEN 'Bimestral'
                        WHEN c.amortizacion_deposito = 'T' THEN 'Trimestral'
                        WHEN c.amortizacion_deposito = 'S' THEN 'Semestral'
                        WHEN c.amortizacion_deposito = 'A' THEN 'Anual'
                        ELSE 'Sin definir'
                    END
                ) = ?
                """;

            case "PLAZOS" -> """
                AND (
                    CASE
                        WHEN c.plazo_meses < 6
                            THEN 'MENOR A 6 MESES'
                        WHEN c.plazo_meses BETWEEN 6 AND 12
                            THEN '6 A 12 MESES'
                        WHEN c.plazo_meses > 12
                             AND c.plazo_meses <= 18
                            THEN '12 A 18 MESES'
                        ELSE 'MAS DE 18 MESES'
                    END
                ) = ?
                """;

            case "PLAZOS_DETALLE" -> """
                AND c.plazo_meses::text = ?
                """;

            case "TASAS" -> """
                AND (
                    CASE
                        WHEN c.tasa_nominal_anual <= 5
                            THEN '0% - 5%'
                        WHEN c.tasa_nominal_anual <= 8
                            THEN '5.01% - 8%'
                        WHEN c.tasa_nominal_anual <= 10
                            THEN '8.01% - 10%'
                        ELSE 'MAS DE 10%'
                    END
                ) = ?
                """;

            case "TASAS_DETALLE" -> """
                AND (
                    TO_CHAR(
                        c.tasa_nominal_anual,
                        'FM999999990.00'
                    ) || '%'
                ) = ?
                """;

            default -> throw new RuntimeException(
                    "Tipo de bloque no válido: " + tipoBloque
            );
        };
    }

    private String obtenerCondicionDetalleVista(String tipoBloque) {

        if (tipoBloque == null) {
            throw new RuntimeException("No se recibió el tipo de bloque.");
        }

        return switch (tipoBloque) {

            case "RANGOS" -> """
                AND (
                    CASE
                        WHEN v.saldo_actual_cdat < 5000000
                            THEN '0 - 5 MILLONES'
                        WHEN v.saldo_actual_cdat < 10000000
                            THEN '5 - 10 MILLONES'
                        WHEN v.saldo_actual_cdat < 30000000
                            THEN '10 - 30 MILLONES'
                        WHEN v.saldo_actual_cdat < 50000000
                            THEN '30 - 50 MILLONES'
                        ELSE 'MAS DE 50 MILLONES'
                    END
                ) = ?
                """;

            case "AMORTIZACION" -> """
                AND (
                    CASE
                        WHEN v.amortizacion_deposito = 'M' THEN 'Mensual'
                        WHEN v.amortizacion_deposito = 'B' THEN 'Bimestral'
                        WHEN v.amortizacion_deposito = 'T' THEN 'Trimestral'
                        WHEN v.amortizacion_deposito = 'S' THEN 'Semestral'
                        WHEN v.amortizacion_deposito = 'A' THEN 'Anual'
                        ELSE 'Sin definir'
                    END
                ) = ?
                """;

            case "PLAZOS" -> """
                AND (
                    CASE
                        WHEN v.plazo_meses < 6
                            THEN 'MENOR A 6 MESES'
                        WHEN v.plazo_meses BETWEEN 6 AND 12
                            THEN '6 A 12 MESES'
                        WHEN v.plazo_meses > 12
                             AND v.plazo_meses <= 18
                            THEN '12 A 18 MESES'
                        ELSE 'MAS DE 18 MESES'
                    END
                ) = ?
                """;

            case "PLAZOS_DETALLE" -> """
                AND v.plazo_meses::text = ?
                """;

            case "TASAS" -> """
                AND (
                    CASE
                        WHEN v.tasa_nominal_anual <= 5
                            THEN '0% - 5%'
                        WHEN v.tasa_nominal_anual <= 8
                            THEN '5.01% - 8%'
                        WHEN v.tasa_nominal_anual <= 10
                            THEN '8.01% - 10%'
                        ELSE 'MAS DE 10%'
                    END
                ) = ?
                """;

            case "TASAS_DETALLE" -> """
                AND (
                    TO_CHAR(
                        v.tasa_nominal_anual,
                        'FM999999990.00'
                    ) || '%'
                ) = ?
                """;

            default -> throw new RuntimeException(
                    "Tipo de bloque no válido: " + tipoBloque
            );
        };
    }

}