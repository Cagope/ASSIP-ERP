package co.assip.erp.ses.formato9026;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class Formato9026Repository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Consulta el Formato SES 9026 - Ahorros.
     *
     * Integra los detalles históricos de los cierres
     * mensuales de Depósitos y CDAT.
     *
     * Reglas:
     * - Excluye Aportes Sociales.
     * - Utiliza saldos históricos, no saldos actuales.
     * - Identifica el tipo de ahorro.
     * - Resuelve cuentas contables parametrizadas.
     * - Evita duplicaciones por la vista de Hoja de Vida.
     * - Genera consecutivos únicos para ambos módulos.
     * - No modifica información financiera.
     */
    public List<Map<String, Object>> consultar(
            String fechaCorte
    ) {

        LocalDate fecha = LocalDate.parse(fechaCorte);

        String sql = """
            WITH entidad AS (
                SELECT
                    TRIM(e.codigo_supersolidaria)
                        AS codigo_entidad
                FROM general.empresas e
                WHERE e.codigo_supersolidaria IS NOT NULL
                  AND TRIM(e.codigo_supersolidaria) <> ''
                ORDER BY e.documento_empresa
                LIMIT 1
            ),

            cierre_depositos AS (
                SELECT
                    c.id_cierre_mensual,
                    c.fecha_cierre
                FROM depositos.cierres_mensuales c
                WHERE c.fecha_cierre = :fechaCorte
                  AND c.estado_cierre = 'C'
            ),

            cierre_cdats AS (
                SELECT
                    c.id_cierre_mensual_cdat,
                    c.id_agencia,
                    c.fecha_corte
                FROM cdat.cierres_mensuales_cdats c
                WHERE c.fecha_corte = :fechaCorte
                  AND c.estado = 'GENERADO'
            ),

            depositos AS (
                SELECT
                    'DEPOSITOS'::text
                        AS modulo_origen,

                    d.id_agencia,
                    d.id_datos_personal,
                    d.documento,
                    d.nombre_completo,

                    CASE
                        WHEN TRIM(d.codigo_forma) = '07'
                            THEN 3
                        ELSE 1
                    END AS tipo_ahorro,

                    d.nombre_forma
                        AS nombre_deposito,

                    CASE
                        WHEN TRIM(d.codigo_forma) = '07'
                             AND COALESCE(d.plazo, 0) > 12
                            THEN cl.codigo_cuenta
                        ELSE cc.codigo_cuenta
                    END AS codigo_contable,

                    d.codigo_cuenta::text
                        AS numero_cuenta,

                    d.fecha_apertura_cuenta
                        AS fecha_apertura,

                    d.fecha_final
                        AS fecha_vencimiento,

                    CASE
                        WHEN TRIM(d.codigo_forma) = '07'
                            THEN COALESCE(d.plazo, 0) * 30
                        ELSE 0
                    END AS plazo,

                    d.saldo_cierre
                        AS saldo,

                    a.saldo_inicial_cuenta
                        AS deposito_inicial,

                    d.tasa::numeric
                        AS tasa_interes_nominal,

                    NULL::numeric
                        AS tasa_interes_efectiva,

                    NULL::numeric
                        AS intereses_causados,

                    d.gmf_cuenta::text
                        AS gmf_origen,

                    d.fecha_gmf
                        AS fecha_aceptacion_gmf,

                    d.estado_cuenta::text
                        AS estado_origen,

                    d.cuenta_conjunta::text
                        AS conjunta_origen,

                    d.accion_conjunta::text
                        AS accion_conjunta,

                    CASE
                        WHEN TRIM(d.codigo_forma) = '05' THEN '1'
                        ELSE '30'
                    END AS amortizacion_origen,

                    '2'::text
                        AS modalidad_origen

                FROM depositos.cierres_mensuales_detalle d

                INNER JOIN cierre_depositos c
                    ON c.id_cierre_mensual =
                       d.id_cierre_mensual

                LEFT JOIN depositos.cuentas_ahorro a
                    ON a.id_cuenta_ahorro =
                       d.id_cuenta_ahorro

                LEFT JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro =
                       d.id_forma_ahorro
                   AND f.id_agencia =
                       d.id_agencia

                LEFT JOIN contabilidad.catalogo_cuentas cc
                    ON cc.id_catalogo_cuenta =
                       f.cuenta_forma_corto
                   AND cc.id_agencia =
                       d.id_agencia

                LEFT JOIN contabilidad.catalogo_cuentas cl
                    ON cl.id_catalogo_cuenta =
                       f.cuenta_forma_largo
                   AND cl.id_agencia =
                       d.id_agencia

                WHERE TRIM(d.codigo_forma) <> '01'
                  AND d.id_cuenta_ahorro IS NOT NULL
                  AND d.saldo_cierre > 0
            ),

            cdats AS (
                SELECT
                    'CDAT'::text
                        AS modulo_origen,

                    d.id_agencia,
                    d.id_datos_personal,
                    d.documento,
                    d.nombre_completo,

                    2 AS tipo_ahorro,

                    'CDAT'::text
                        AS nombre_deposito,

                    cc.codigo_cuenta
                        AS codigo_contable,

                    d.codigo_cdat::text
                        AS numero_cuenta,

                    d.fecha_apertura_cdat
                        AS fecha_apertura,

                    d.fecha_vencimiento_cdat
                        AS fecha_vencimiento,

                    CASE
                        WHEN COALESCE(d.plazo_dias, 0) > 0
                            THEN d.plazo_dias
                        WHEN COALESCE(d.plazo_meses, 0) > 0
                            THEN d.plazo_meses * 30
                        ELSE 0
                    END AS plazo,

                    d.saldo_actual_cdat
                        AS saldo,

                    d.valor_apertura_cdat
                        AS deposito_inicial,

                    d.tasa_nominal_anual
                        AS tasa_interes_nominal,

                    d.tasa_efectiva_anual
                        AS tasa_interes_efectiva,

                    COALESCE(ci.valor_interes, 0)::numeric
                        AS intereses_causados,

                    NULL::text
                        AS gmf_origen,

                    NULL::date
                        AS fecha_aceptacion_gmf,

                    d.estado_cdat::text
                        AS estado_origen,

                    CASE
                        WHEN d.cuenta_conjunta
                            THEN 'S'
                        ELSE 'N'
                    END AS conjunta_origen,

                    d.accion_conjunta::text
                        AS accion_conjunta,

                    CASE UPPER(TRIM(d.amortizacion_deposito::text))
                        WHEN 'M' THEN '30'
                        WHEN 'B' THEN '60'
                        WHEN 'T' THEN '90'
                        WHEN 'S' THEN '180'
                        WHEN 'A' THEN '360'
                        ELSE '30'
                    END AS amortizacion_origen,

                    CASE UPPER(TRIM(d.modalidad_cdat::text))
                        WHEN 'A' THEN '1'
                        WHEN 'V' THEN '2'
                        ELSE NULL
                    END AS modalidad_origen

                FROM cdat.cierres_mensuales_cdats_detalle d

                INNER JOIN cierre_cdats c
                    ON c.id_cierre_mensual_cdat =
                       d.id_cierre_mensual_cdat
                   AND c.id_agencia =
                       d.id_agencia

                LEFT JOIN LATERAL (
                    SELECT
                        SUM(det.valor_interes) AS valor_interes
                    FROM cdat.causaciones_mensuales_intereses ca
                    INNER JOIN cdat.causaciones_mensuales_intereses_detalle det
                        ON det.id_causacion_mensual_interes =
                           ca.id_causacion_mensual_interes
                       AND det.id_cierre_mensual_cdat_detalle =
                           d.id_cierre_mensual_cdat_detalle
                    WHERE ca.id_cierre_mensual_cdat =
                          d.id_cierre_mensual_cdat
                      AND ca.fecha_corte = c.fecha_corte
                      AND ca.id_agencia = d.id_agencia
                      AND ca.estado = 'GENERADO'
                ) ci ON TRUE

                LEFT JOIN LATERAL (
                    SELECT
                        p.id_catalogo_cuenta_credito
                    FROM cdat.conceptos_contables_cdats p
                    WHERE p.codigo_proceso = 'APERTURA_CDAT'
                      AND p.id_agencia = d.id_agencia
                      AND p.activo = TRUE
                      AND d.plazo_meses BETWEEN
                          p.plazo_desde_meses
                          AND p.plazo_hasta_meses
                    ORDER BY
                        p.id_concepto_contable_cdat
                    LIMIT 1
                ) pc ON TRUE

                LEFT JOIN contabilidad.catalogo_cuentas cc
                    ON cc.id_catalogo_cuenta =
                       pc.id_catalogo_cuenta_credito
                   AND cc.id_agencia =
                       d.id_agencia

                WHERE d.estado_cdat = 'A'
                AND d.saldo_actual_cdat > 0
            ),

            consolidado AS (
                SELECT * FROM depositos
                UNION ALL
                SELECT * FROM cdats
            ),

            hoja_vida_unica AS (
                SELECT DISTINCT ON (hv.id_datos_personal)
                    hv.id_datos_personal,
                    hv.tipo_documento,
                    hv.documento,
                    hv.primer_apellido,
                    hv.segundo_apellido,
                    hv.nombres
                FROM reporting.vw_hoja_vida_general_total_extendida hv
                ORDER BY
                    hv.id_datos_personal,
                    hv.tipo_documento NULLS LAST,
                    hv.documento NULLS LAST
            )

            SELECT
                e.codigo_entidad
                    AS "codigoEntidad",

                TO_CHAR(
                    CAST(:fechaCorte AS DATE),
                    'DDMMYYYY'
                ) AS "fechaCorte",

                ROW_NUMBER() OVER (
                    ORDER BY
                        c.modulo_origen,
                        c.id_agencia,
                        c.numero_cuenta
                ) AS "consecutivo",

                hv.tipo_documento
                    AS "tipoIdentificacion",

                COALESCE(
                    NULLIF(TRIM(c.documento), ''),
                    hv.documento
                ) AS "nit",

                LEFT(TRIM(c.codigo_contable::text), 6)
                    AS "codigoContable",

                c.nombre_deposito
                    AS "nombreDeposito",

                c.tipo_ahorro
                    AS "tipoAhorro",

                c.amortizacion_origen
                    AS "amortizacion",

                TO_CHAR(
                    c.fecha_apertura,
                    'DDMMYYYY'
                ) AS "fechaApertura",

                c.plazo
                    AS "plazo",

                TO_CHAR(
                    c.fecha_vencimiento,
                    'DDMMYYYY'
                ) AS "fechaVencimiento",

                c.modalidad_origen
                    AS "modalidad",

                c.tasa_interes_nominal
                    AS "tasaInteresNominal",

                c.tasa_interes_efectiva
                    AS "tasaInteresEfectiva",

                c.intereses_causados
                    AS "interesesCausados",

                c.saldo
                    AS "saldo",

                c.deposito_inicial
                    AS "depositoInicial",

                TRIM(c.numero_cuenta)
                    AS "numeroCuenta",

                CASE
                    WHEN c.modulo_origen = 'CDAT' THEN '0'
                    WHEN UPPER(TRIM(c.gmf_origen)) IN ('N', 'U') THEN '1'
                    ELSE '0'
                END AS "exentaGmf",

                CASE
                    WHEN c.modulo_origen = 'DEPOSITOS'
                     AND UPPER(TRIM(c.gmf_origen)) IN ('N', 'U')
                    THEN TO_CHAR(
                        GREATEST(c.fecha_aceptacion_gmf, c.fecha_apertura),
                        'DDMMYYYY'
                    )
                    ELSE NULL
                END AS "fechaAceptacionGmf",

                CASE
                    WHEN c.estado_origen = 'A' THEN '1'
                    ELSE '0'
                END AS "estado",

                '0'::text
                    AS "ctaBajoMonto",

                CASE
                    WHEN UPPER(TRIM(c.conjunta_origen)) IN ('S', '1', 'T', 'TRUE')
                        THEN 1
                    ELSE 0
                END AS "cotitulares",

                NULL::text
                    AS "conjuntaColectivo",

                NULL::text
                    AS "paso",

                c.modulo_origen
                    AS "moduloOrigen",

                c.id_agencia
                    AS "idAgencia",

                COALESCE(
                    NULLIF(TRIM(c.nombre_completo), ''),
                    TRIM(
                        CONCAT_WS(
                            ' ',
                            hv.primer_apellido,
                            hv.segundo_apellido,
                            hv.nombres
                        )
                    )
                ) AS "nombreTitular"

            FROM consolidado c

            LEFT JOIN hoja_vida_unica hv
                ON hv.id_datos_personal =
                   c.id_datos_personal

            CROSS JOIN entidad e

            ORDER BY
                c.modulo_origen,
                c.id_agencia,
                c.numero_cuenta
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue("fechaCorte", fecha);

        return jdbc.queryForList(
                sql,
                parametros
        );
    }

    /**
     * Verifica que exista un cierre mensual definitivo
     * de Depósitos para la fecha solicitada.
     *
     * Estado válido: C.
     */
    public boolean existeCierreDepositos(
            String fechaCorte
    ) {

        LocalDate fecha = LocalDate.parse(fechaCorte);

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM depositos.cierres_mensuales
                WHERE fecha_cierre = :fechaCorte
                  AND estado_cierre = 'C'
            )
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue("fechaCorte", fecha);

        Boolean existe = jdbc.queryForObject(
                sql,
                parametros,
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    /**
     * Verifica que exista al menos un cierre mensual
     * de CDAT para la fecha solicitada.
     *
     * Estado actualmente utilizado: GENERADO.
     *
     * Esta comprobación no garantiza por sí sola
     * que todas las agencias tengan cierre.
     */
    public boolean existeCierreCdat(
            String fechaCorte
    ) {

        LocalDate fecha = LocalDate.parse(fechaCorte);

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cdat.cierres_mensuales_cdats
                WHERE fecha_corte = :fechaCorte
                  AND estado = 'GENERADO'
            )
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue("fechaCorte", fecha);

        Boolean existe = jdbc.queryForObject(
                sql,
                parametros,
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }
}
