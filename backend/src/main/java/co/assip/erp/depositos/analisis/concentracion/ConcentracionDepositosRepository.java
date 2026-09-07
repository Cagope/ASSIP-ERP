package co.assip.erp.depositos.analisis.concentracion;

import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosAgenciaDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosAsociadoDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosFormaDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosRequestDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ConcentracionDepositosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ConcentracionDepositosRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    /*
     * =========================================================
     * BASE COMÚN
     * =========================================================
     *
     * saldo_corte =
     * créditos acumulados hasta la fecha
     * menos débitos acumulados hasta la fecha.
     *
     * Solo participan cuentas con saldo > 0.
     *
     * idFormaAhorro = 0:
     *   la población principal excluye aportes sociales
     *   (tipo_captacion_forma = '1').
     *
     * idFormaAhorro > 0:
     *   analiza exclusivamente la forma seleccionada,
     *   incluso si corresponde a aportes sociales.
     */
    private static final String BASE_SQL = """
        WITH mov AS (
            SELECT
                e.id_cuenta_ahorro,
                SUM(COALESCE(e.valor_credito, 0)) AS total_creditos,
                SUM(COALESCE(e.valor_debito, 0))  AS total_debitos

            FROM depositos.extractos_cuentas_ahorros e

            WHERE
                e.fecha_movimiento <= CAST(:fechaCorte AS DATE)

            GROUP BY
                e.id_cuenta_ahorro
        ),

        hv_unica AS (
            SELECT *
            FROM (
                SELECT
                    h.*,

                    ROW_NUMBER() OVER (
                        PARTITION BY h.id_datos_personal
                        ORDER BY h.id_datos_personal
                    ) AS rn

                FROM reporting.vw_hoja_vida_general_total_reciente h
            ) x

            WHERE x.rn = 1
        ),

        cuentas AS (
            SELECT
                c.id_cuenta_ahorro,
                c.id_datos_personal,
                c.id_agencia,
                c.id_forma_ahorro,

                a.codigo_agencia,
                a.nombre_agencia,

                LPAD(
                    f.codigo_forma::text,
                    2,
                    '0'
                ) AS codigo_forma,

                f.nombre_forma,

                TRIM(
                    COALESCE(
                        f.tipo_captacion_forma,
                        ''
                    )
                ) AS tipo_captacion_forma,

                hv.tipo_documento,
                hv.documento,

                CONCAT_WS(
                    ' ',
                    hv.primer_apellido,
                    hv.segundo_apellido,
                    hv.nombres
                ) AS nombre_completo,

                (
                    COALESCE(
                        m.total_creditos,
                        0
                    )
                    -
                    COALESCE(
                        m.total_debitos,
                        0
                    )
                ) AS saldo

            FROM depositos.cuentas_ahorro c

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            INNER JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            LEFT JOIN hv_unica hv
                ON hv.id_datos_personal = c.id_datos_personal

            LEFT JOIN mov m
                ON m.id_cuenta_ahorro = c.id_cuenta_ahorro

            WHERE
                (
                       :idAgencia = 0
                    OR c.id_agencia = :idAgencia
                )

                AND (
                       :idFormaAhorro = 0
                    OR c.id_forma_ahorro = :idFormaAhorro
                )
        ),

        poblacion AS (
            SELECT *
            FROM cuentas

            WHERE
                saldo > 0

                AND (
                       :idFormaAhorro <> 0
                    OR tipo_captacion_forma <> '1'
                )
        )
        """;


    /*
     * =========================================================
     * RESUMEN DE LA POBLACIÓN ANALIZADA
     * =========================================================
     */
    public ResumenBase obtenerResumen(
            ConcentracionDepositosRequestDTO request
    ) {

        String sql = BASE_SQL + """
            SELECT
                COUNT(*) AS cantidad_cuentas,

                COUNT(
                    DISTINCT id_datos_personal
                ) AS cantidad_asociados,

                COALESCE(
                    SUM(saldo),
                    0
                ) AS saldo,

                COALESCE(
                    AVG(saldo),
                    0
                ) AS saldo_promedio_cuenta

            FROM poblacion
            """;

        return jdbc.queryForObject(
                sql,
                parametros(request),
                (rs, rowNum) ->
                        new ResumenBase(
                                rs.getInt(
                                        "cantidad_cuentas"
                                ),
                                rs.getInt(
                                        "cantidad_asociados"
                                ),
                                rs.getBigDecimal(
                                        "saldo"
                                ),
                                rs.getBigDecimal(
                                        "saldo_promedio_cuenta"
                                )
                        )
        );
    }


    /*
     * =========================================================
     * MAYOR SALDO CONSOLIDADO POR ASOCIADO
     * =========================================================
     */
    public BigDecimal obtenerMayorSaldoAsociado(
            ConcentracionDepositosRequestDTO request
    ) {

        String sql = BASE_SQL + """
            ,
            asociados AS (
                SELECT
                    id_datos_personal,
                    SUM(saldo) AS saldo

                FROM poblacion

                GROUP BY
                    id_datos_personal
            )

            SELECT
                COALESCE(
                    MAX(saldo),
                    0
                )

            FROM asociados
            """;

        BigDecimal valor =
                jdbc.queryForObject(
                        sql,
                        parametros(request),
                        BigDecimal.class
                );

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }


    /*
     * =========================================================
     * RANKING POR ASOCIADO
     * =========================================================
     */
    public List<ConcentracionDepositosAsociadoDTO> listarAsociados(
            ConcentracionDepositosRequestDTO request
    ) {

        String sql = BASE_SQL + """
            ,
            asociados AS (
                SELECT
                    id_datos_personal,

                    MAX(
                        tipo_documento
                    ) AS tipo_documento,

                    MAX(
                        documento
                    ) AS documento,

                    MAX(
                        nombre_completo
                    ) AS nombre_completo,

                    COUNT(*) AS cantidad_cuentas,

                    SUM(saldo) AS saldo

                FROM poblacion

                GROUP BY
                    id_datos_personal
            ),

            total AS (
                SELECT
                    COALESCE(
                        SUM(saldo),
                        0
                    ) AS saldo_total

                FROM asociados
            ),

            ranking AS (
                SELECT
                    ROW_NUMBER() OVER (
                        ORDER BY
                            a.saldo DESC,
                            a.id_datos_personal
                    ) AS posicion,

                    a.id_datos_personal,
                    a.tipo_documento,
                    a.documento,
                    a.nombre_completo,
                    a.cantidad_cuentas,
                    a.saldo,

                    CASE
                        WHEN t.saldo_total = 0
                            THEN 0
                        ELSE
                            (
                                a.saldo
                                / t.saldo_total
                            ) * 100
                    END AS porcentaje_participacion,

                    CASE
                        WHEN t.saldo_total = 0
                            THEN 0
                        ELSE
                            (
                                SUM(
                                    a.saldo
                                ) OVER (
                                    ORDER BY
                                        a.saldo DESC,
                                        a.id_datos_personal

                                    ROWS BETWEEN
                                        UNBOUNDED PRECEDING
                                        AND CURRENT ROW
                                )
                                / t.saldo_total
                            ) * 100
                    END AS porcentaje_acumulado

                FROM asociados a

                CROSS JOIN total t
            )

            SELECT
                posicion,
                id_datos_personal,
                tipo_documento,
                documento,
                nombre_completo,
                cantidad_cuentas,
                saldo,
                porcentaje_participacion,
                porcentaje_acumulado

            FROM ranking

            ORDER BY
                posicion
            """;

        return jdbc.query(
                sql,
                parametros(request),
                (rs, rowNum) ->
                        ConcentracionDepositosAsociadoDTO
                                .builder()
                                .posicion(
                                        rs.getInt(
                                                "posicion"
                                        )
                                )
                                .idDatosPersonal(
                                        rs.getInt(
                                                "id_datos_personal"
                                        )
                                )
                                .tipoDocumento(
                                        rs.getString(
                                                "tipo_documento"
                                        )
                                )
                                .documento(
                                        rs.getString(
                                                "documento"
                                        )
                                )
                                .nombreCompleto(
                                        rs.getString(
                                                "nombre_completo"
                                        )
                                )
                                .cantidadCuentas(
                                        rs.getInt(
                                                "cantidad_cuentas"
                                        )
                                )
                                .saldo(
                                        rs.getBigDecimal(
                                                "saldo"
                                        )
                                )
                                .porcentajeParticipacion(
                                        rs.getBigDecimal(
                                                "porcentaje_participacion"
                                        )
                                )
                                .porcentajeAcumulado(
                                        rs.getBigDecimal(
                                                "porcentaje_acumulado"
                                        )
                                )
                                .build()
        );
    }


    /*
     * =========================================================
     * CONCENTRACIÓN POR AGENCIA
     * =========================================================
     */
    public List<ConcentracionDepositosAgenciaDTO> listarAgencias(
            ConcentracionDepositosRequestDTO request
    ) {

        String sql = BASE_SQL + """
            ,
            total AS (
                SELECT
                    COALESCE(
                        SUM(saldo),
                        0
                    ) AS saldo_total

                FROM poblacion
            )

            SELECT
                p.id_agencia,
                p.codigo_agencia,
                p.nombre_agencia,

                COUNT(*) AS cantidad_cuentas,

                COUNT(
                    DISTINCT p.id_datos_personal
                ) AS cantidad_asociados,

                SUM(
                    p.saldo
                ) AS saldo,

                CASE
                    WHEN t.saldo_total = 0
                        THEN 0
                    ELSE
                        (
                            SUM(
                                p.saldo
                            )
                            / t.saldo_total
                        ) * 100
                END AS porcentaje_participacion

            FROM poblacion p

            CROSS JOIN total t

            GROUP BY
                p.id_agencia,
                p.codigo_agencia,
                p.nombre_agencia,
                t.saldo_total

            ORDER BY
                saldo DESC,
                p.codigo_agencia
            """;

        return jdbc.query(
                sql,
                parametros(request),
                (rs, rowNum) ->
                        new ConcentracionDepositosAgenciaDTO(
                                rs.getInt(
                                        "id_agencia"
                                ),
                                rs.getString(
                                        "codigo_agencia"
                                ),
                                rs.getString(
                                        "nombre_agencia"
                                ),
                                rs.getInt(
                                        "cantidad_cuentas"
                                ),
                                rs.getInt(
                                        "cantidad_asociados"
                                ),
                                rs.getBigDecimal(
                                        "saldo"
                                ),
                                rs.getBigDecimal(
                                        "porcentaje_participacion"
                                )
                        )
        );
    }


    /*
     * =========================================================
     * CONCENTRACIÓN POR FORMA DE AHORRO
     * =========================================================
     */
    public List<ConcentracionDepositosFormaDTO> listarFormas(
            ConcentracionDepositosRequestDTO request
    ) {

        String sql = BASE_SQL + """
        ,
        total AS (
            SELECT
                COALESCE(
                    SUM(saldo),
                    0
                ) AS saldo_total

            FROM poblacion
        )

        SELECT
            MIN(
                p.id_forma_ahorro
            ) AS id_forma_ahorro,

            p.codigo_forma,
            p.nombre_forma,
            p.tipo_captacion_forma,

            COUNT(*) AS cantidad_cuentas,

            COUNT(
                DISTINCT p.id_datos_personal
            ) AS cantidad_asociados,

            SUM(
                p.saldo
            ) AS saldo,

            CASE
                WHEN t.saldo_total = 0
                    THEN 0
                ELSE
                    (
                        SUM(
                            p.saldo
                        )
                        / t.saldo_total
                    ) * 100
            END AS porcentaje_participacion

        FROM poblacion p

        CROSS JOIN total t

        GROUP BY
            p.codigo_forma,
            p.nombre_forma,
            p.tipo_captacion_forma,
            t.saldo_total

        ORDER BY
            saldo DESC,
            p.codigo_forma
        """;

        return jdbc.query(
                sql,
                parametros(request),
                (rs, rowNum) ->
                        ConcentracionDepositosFormaDTO
                                .builder()
                                .idFormaAhorro(
                                        rs.getInt(
                                                "id_forma_ahorro"
                                        )
                                )
                                .codigoForma(
                                        rs.getString(
                                                "codigo_forma"
                                        )
                                )
                                .nombreForma(
                                        rs.getString(
                                                "nombre_forma"
                                        )
                                )
                                .tipoCaptacionForma(
                                        rs.getString(
                                                "tipo_captacion_forma"
                                        )
                                )
                                .cantidadCuentas(
                                        rs.getInt(
                                                "cantidad_cuentas"
                                        )
                                )
                                .cantidadAsociados(
                                        rs.getInt(
                                                "cantidad_asociados"
                                        )
                                )
                                .saldo(
                                        rs.getBigDecimal(
                                                "saldo"
                                        )
                                )
                                .porcentajeParticipacion(
                                        rs.getBigDecimal(
                                                "porcentaje_participacion"
                                        )
                                )
                                .build()
        );
    }

    /*
     * =========================================================
     * SALDO DE APORTES SOCIALES
     * =========================================================
     *
     * Se calcula independientemente de la población principal
     * para poder informarlo separado cuando se consultan todas
     * las formas.
     */
    public SaldoAportesBase obtenerAportes(
            ConcentracionDepositosRequestDTO request
    ) {

        String sql = """
            WITH mov AS (
                SELECT
                    e.id_cuenta_ahorro,

                    SUM(
                        COALESCE(
                            e.valor_credito,
                            0
                        )
                    ) AS total_creditos,

                    SUM(
                        COALESCE(
                            e.valor_debito,
                            0
                        )
                    ) AS total_debitos

                FROM depositos.extractos_cuentas_ahorros e

                WHERE
                    e.fecha_movimiento <= CAST(
                        :fechaCorte AS DATE
                    )

                GROUP BY
                    e.id_cuenta_ahorro
            ),

            base AS (
                SELECT
                    c.id_cuenta_ahorro,
                    c.id_datos_personal,

                    (
                        COALESCE(
                            m.total_creditos,
                            0
                        )
                        -
                        COALESCE(
                            m.total_debitos,
                            0
                        )
                    ) AS saldo

                FROM depositos.cuentas_ahorro c

                INNER JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro = c.id_forma_ahorro

                LEFT JOIN mov m
                    ON m.id_cuenta_ahorro = c.id_cuenta_ahorro

                WHERE
                    (
                           :idAgencia = 0
                        OR c.id_agencia = :idAgencia
                    )

                    AND TRIM(
                        COALESCE(
                            f.tipo_captacion_forma,
                            ''
                        )
                    ) = '1'
            )

            SELECT
                COUNT(*) FILTER (
                    WHERE saldo > 0
                ) AS cantidad_cuentas,

                COUNT(
                    DISTINCT id_datos_personal
                ) FILTER (
                    WHERE saldo > 0
                ) AS cantidad_asociados,

                COALESCE(
                    SUM(
                        saldo
                    ) FILTER (
                        WHERE saldo > 0
                    ),
                    0
                ) AS saldo

            FROM base
            """;

        return jdbc.queryForObject(
                sql,
                parametros(request),
                (rs, rowNum) ->
                        new SaldoAportesBase(
                                rs.getInt(
                                        "cantidad_cuentas"
                                ),
                                rs.getInt(
                                        "cantidad_asociados"
                                ),
                                rs.getBigDecimal(
                                        "saldo"
                                )
                        )
        );
    }


    /*
     * =========================================================
     * PARÁMETROS
     * =========================================================
     */
    private Map<String, Object> parametros(
            ConcentracionDepositosRequestDTO request
    ) {

        Map<String, Object> params =
                new HashMap<>();

        params.put(
                "fechaCorte",
                request.getFechaCorte()
        );

        params.put(
                "idAgencia",
                request.getIdAgencia() == null
                        ? 0
                        : request.getIdAgencia()
        );

        params.put(
                "idFormaAhorro",
                request.getIdFormaAhorro() == null
                        ? 0
                        : request.getIdFormaAhorro()
        );

        return params;
    }


    /*
     * =========================================================
     * RESULTADOS INTERNOS DEL REPOSITORY
     * =========================================================
     */
    public record ResumenBase(
            Integer cantidadCuentas,
            Integer cantidadAsociados,
            BigDecimal saldo,
            BigDecimal saldoPromedioCuenta
    ) {
    }


    public record SaldoAportesBase(
            Integer cantidadCuentas,
            Integer cantidadAsociados,
            BigDecimal saldo
    ) {
    }
}