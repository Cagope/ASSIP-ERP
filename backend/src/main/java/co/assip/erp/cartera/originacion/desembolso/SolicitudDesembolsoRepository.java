package co.assip.erp.cartera.originacion.desembolso;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudDesembolsoRepository {

    private static final int CODIGO_PARAMETRO_IVA = 1;

    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudDesembolsoRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // BANDEJA DE CRÉDITOS PENDIENTES
    // =========================================================

    private static final String SQL_BANDEJA = """
            SELECT
                cc.id_cartera_credito,
                sc.id_solicitud_credito,

                cc.id_agencia,
                cc.id_linea_credito,
                cc.id_datos_personal,
                cc.id_cuenta_aportes,

                cc.pagare_cartera,

                persona.documento,
                persona.nombre_completo_apellidos AS nombre_completo,

                lc.codigo_linea_credito,
                lc.nombre_linea_credito,

                cc.valor_inicial_credito,
                cc.valor_desembolsado,
                cc.plazo,

                cc.fecha_inclusion_sistema,
                cc.fecha_primera_cuota,
                cc.fecha_final,

                cc.codigo_estado_cartera

            FROM cartera.carteras_creditos cc

            INNER JOIN cartera.solicitudes_creditos sc
                ON sc.id_cartera_credito = cc.id_cartera_credito
               AND sc.activo = true

            LEFT JOIN reporting.vw_datos_personales_operativa persona
                ON persona.id_datos_personal = cc.id_datos_personal

            LEFT JOIN cartera.lineas_creditos lc
                ON lc.id_linea_credito = cc.id_linea_credito

            WHERE cc.codigo_estado_cartera = 'P'

            ORDER BY
                cc.fecha_inclusion_sistema,
                cc.id_cartera_credito
            """;

    // =========================================================
    // DATOS DEL CRÉDITO PARA DESEMBOLSO
    // =========================================================

    private static final String SQL_CREDITO = """
            SELECT
                cc.id_cartera_credito,
                sc.id_solicitud_credito,

                cc.id_agencia,
                cc.id_linea_credito,
                cc.id_datos_personal,
                cc.id_cuenta_aportes,

                cc.pagare_cartera,

                persona.documento,
                persona.nombre_completo_apellidos AS nombre_completo,

                lc.codigo_linea_credito,
                lc.nombre_linea_credito,

                cc.codigo_clasificacion_credito,
                cc.codigo_garantia_credito,
                gc.tipo_garantia,
                cc.codigo_destino_economico,
                cc.periodo_codigo_interes,
                cc.tipo_modalidad_interes,
                cc.codigo_tipo_cuota,
                cc.codigo_forma_pago,

                cc.plazo,
                cc.amortizacion_capital,

                cc.valor_inicial_credito,
                cc.valor_desembolsado,
                cc.valor_base_calculo_cuota,
                cc.valor_primera_cuota,
                cc.valor_cuota,
                cc.saldo_actual,

                cc.tasa_nominal_anual,
                cc.tasa_efectiva_anual,

                cc.fecha_inclusion_sistema,
                cc.fecha_primera_cuota,
                cc.fecha_primera_cuota_capital,
                cc.fecha_primera_cuota_interes,
                cc.fecha_final,

                cc.codigo_estado_cartera,

                sc.id_fondo_garantia,
                sc.valor_fondo_garantia,
                sc.porcentaje_fondo_aplicado,

                fg.codigo_fondo,
                fg.nombre_fondo,
                fg.id_cuenta_pasivo_fondo,
                fg.id_cuenta_iva_fondo,

                lc.valor_consulta_central_riesgo,
                lc.valor_papeleria,
                lc.porcentaje_papeleria,
                lc.valor_aportes,
                lc.porcentaje_aportes

            FROM cartera.carteras_creditos cc

            INNER JOIN cartera.solicitudes_creditos sc
                ON sc.id_cartera_credito = cc.id_cartera_credito
               AND sc.activo = true

            LEFT JOIN reporting.vw_datos_personales_operativa persona
                ON persona.id_datos_personal = cc.id_datos_personal

            INNER JOIN cartera.lineas_creditos lc
                ON lc.id_linea_credito = cc.id_linea_credito
            
            LEFT JOIN cartera.garantias_creditos gc
                ON gc.codigo_garantia_credito = cc.codigo_garantia_credito
               AND gc.activo = true
           
            LEFT JOIN cartera.fondos_garantias fg
                ON fg.id_fondo_garantia = sc.id_fondo_garantia
               AND fg.activo = true
            
            WHERE cc.id_cartera_credito = :idCarteraCredito
            """;

    // =========================================================
    // BLOQUEO DEL CRÉDITO PARA OPERACIÓN DEFINITIVA
    // =========================================================

    private static final String SQL_BLOQUEAR_CREDITO = """
            SELECT
                cc.id_cartera_credito,
                sc.id_solicitud_credito,

                cc.id_agencia,
                cc.id_linea_credito,
                cc.id_datos_personal,
                cc.id_cuenta_aportes,

                cc.pagare_cartera,

                persona.documento,
                persona.nombre_completo_apellidos AS nombre_completo,

                lc.codigo_linea_credito,
                lc.nombre_linea_credito,

                cc.codigo_clasificacion_credito,
                cc.codigo_garantia_credito,
                gc.tipo_garantia,
                cc.codigo_destino_economico,
                cc.periodo_codigo_interes,
                cc.tipo_modalidad_interes,
                cc.codigo_tipo_cuota,
                cc.codigo_forma_pago,

                cc.plazo,
                cc.amortizacion_capital,

                cc.valor_inicial_credito,
                cc.valor_desembolsado,
                cc.valor_base_calculo_cuota,
                cc.valor_primera_cuota,
                cc.valor_cuota,
                cc.saldo_actual,

                cc.tasa_nominal_anual,
                cc.tasa_efectiva_anual,

                cc.fecha_inclusion_sistema,
                cc.fecha_primera_cuota,
                cc.fecha_primera_cuota_capital,
                cc.fecha_primera_cuota_interes,
                cc.fecha_final,

                cc.codigo_estado_cartera,

                sc.id_fondo_garantia,
                
                sc.valor_fondo_garantia,
                sc.porcentaje_fondo_aplicado,

                fg.codigo_fondo,
                fg.nombre_fondo,
                fg.id_cuenta_pasivo_fondo,
                fg.id_cuenta_iva_fondo,

                lc.valor_consulta_central_riesgo,
                lc.valor_papeleria,
                lc.porcentaje_papeleria,
                lc.valor_aportes,
                lc.porcentaje_aportes

            FROM cartera.carteras_creditos cc

            INNER JOIN cartera.solicitudes_creditos sc
                ON sc.id_cartera_credito = cc.id_cartera_credito
               AND sc.activo = true

            LEFT JOIN reporting.vw_datos_personales_operativa persona
                ON persona.id_datos_personal = cc.id_datos_personal

            INNER JOIN cartera.lineas_creditos lc
                ON lc.id_linea_credito = cc.id_linea_credito
            
            LEFT JOIN cartera.garantias_creditos gc
                ON gc.codigo_garantia_credito = cc.codigo_garantia_credito
               AND gc.activo = true
            
            LEFT JOIN cartera.fondos_garantias fg
                ON fg.id_fondo_garantia = sc.id_fondo_garantia
               AND fg.activo = true

            WHERE cc.id_cartera_credito = :idCarteraCredito
              AND cc.codigo_estado_cartera = 'P'

            FOR UPDATE OF cc
            """;

    // =========================================================
    // CENTRALES DILIGENCIADAS EN LA SOLICITUD
    // =========================================================

    private static final String SQL_CANTIDAD_CENTRALES = """
            SELECT COUNT(*)

            FROM cartera.solicitudes_deudores_centrales sdc

            INNER JOIN cartera.solicitudes_deudores sd
                ON sd.id_solicitud_deudor = sdc.id_solicitud_deudor

            WHERE sd.id_solicitud_credito = :idSolicitudCredito
              AND sd.activo = true
              AND sdc.activo = true
            """;

    // =========================================================
    // IVA GENERAL POR AGENCIA
    // =========================================================

    private static final String SQL_PORCENTAJE_IVA = """
            SELECT
                p.valor_parametro

            FROM general.parametros p

            WHERE p.id_agencia = :idAgencia
              AND p.codigo_parametro = :codigoParametro

            LIMIT 1
            """;

    // =========================================================
    // PARÁMETROS CONTABLES SUELTOS
    // =========================================================

    private static final String SQL_PARAMETRO_CONTABLE = """
            SELECT
                pc.id_catalogo_cuenta,
                pc.codigo_cuenta

            FROM cartera.parametros_contables pc

            WHERE pc.id_agencia = :idAgencia
              AND pc.concepto = :concepto
              AND pc.activo = true
              AND (
                    pc.id_linea_credito = :idLineaCredito
                    OR pc.id_linea_credito IS NULL
              )

            ORDER BY
                CASE
                    WHEN pc.id_linea_credito = :idLineaCredito
                        THEN 0
                    ELSE 1
                END,
                pc.id_catalogo_cuenta

            LIMIT 1
            """;

    // =========================================================
    // MATRIZ CONTABLE - CAPITAL
    // =========================================================

    private static final String SQL_CUENTA_CAPITAL = """
            SELECT
                ccc.id_catalogo_cuenta,
                ccc.codigo_cuenta

            FROM cartera.cuentas_contables_cartera ccc

            WHERE ccc.id_agencia = :idAgencia
              AND ccc.concepto = 'CAPITAL'

              AND ccc.codigo_clasificacion_credito =
                  :codigoClasificacionCredito

              AND (
                    ccc.codigo_forma_pago = :codigoFormaPago
                    OR ccc.codigo_forma_pago IS NULL
              )

              AND (
                    ccc.tipo_garantia = :tipoGarantia
                    OR ccc.tipo_garantia IS NULL
              )

              AND ccc.codigo_edad_riesgo = :codigoEdadRiesgo

              AND (
                    ccc.codigo_segmento_productivo =
                        :codigoSegmentoProductivo
                    OR ccc.codigo_segmento_productivo IS NULL
              )

              AND ccc.activo = true

            ORDER BY
                CASE
                    WHEN ccc.codigo_forma_pago = :codigoFormaPago
                        THEN 0
                    ELSE 1
                END,

                CASE
                    WHEN ccc.tipo_garantia = :tipoGarantia
                        THEN 0
                    ELSE 1
                END,

                CASE
                    WHEN ccc.codigo_segmento_productivo =
                         :codigoSegmentoProductivo
                        THEN 0
                    ELSE 1
                END,

                ccc.id_catalogo_cuenta

            LIMIT 1
            """;

    // =========================================================
    // CUENTA DE AHORRO PARA DISTRIBUCIÓN DEL DESEMBOLSO
    //
    // Se utiliza únicamente para validar la cuenta seleccionada
    // y obtener la cuenta contable asociada a su forma de ahorro.
    //
    // No registra movimientos.
    // =========================================================

    private static final String SQL_CUENTA_AHORRO_DESEMBOLSO = """
        SELECT
            ca.id_cuenta_ahorro,
            ca.id_datos_personal,
            ca.id_forma_ahorro,

            fa.codigo_forma AS codigo_forma_ahorro,
            fa.nombre_forma AS nombre_forma_ahorro,

            fa.cuenta_forma_corto AS id_catalogo_cuenta

        FROM depositos.cuentas_ahorro ca

        INNER JOIN depositos.formas_ahorro fa
            ON fa.id_forma_ahorro = ca.id_forma_ahorro

        WHERE ca.id_cuenta_ahorro = :idCuentaAhorro
        """;

    // =========================================================
    // EDADES DE RIESGO DE OTROS CRÉDITOS
    // =========================================================

    private static final String SQL_EDADES_CREDITOS_ACTIVOS = """
            SELECT
                cc.id_cartera_credito,
                cc.edad_de_riesgo

            FROM cartera.carteras_creditos cc

            WHERE cc.id_datos_personal = :idDatosPersonal
              AND cc.id_cartera_credito <> :idCarteraCredito
              AND cc.codigo_estado_cartera = 'A'
              AND cc.saldo_actual > 0
              AND cc.credito_evaluado = true
              AND cc.edad_de_riesgo IS NOT NULL

            ORDER BY cc.id_cartera_credito
            """;

    // =========================================================
    // CRÉDITOS CANCELADOS PARA NOVACIÓN
    // =========================================================

    private static final String SQL_CREDITOS_CANCELADOS = """
            SELECT
                cc.id_cartera_credito,
                cc.pagare_cartera,
                cc.numero_novaciones,
                cc.ultima_fecha_capital

            FROM cartera.carteras_creditos cc

            WHERE cc.id_datos_personal = :idDatosPersonal
              AND cc.id_cartera_credito <> :idCarteraCredito
              AND cc.codigo_estado_cartera = 'C'
              AND COALESCE(cc.saldo_actual, 0) = 0

            ORDER BY
                cc.ultima_fecha_capital DESC NULLS LAST,
                cc.id_cartera_credito DESC
            """;

    // =========================================================
    // ACTIVACIÓN DEFINITIVA DEL CRÉDITO
    // =========================================================

    private static final String SQL_ACTIVAR_CREDITO = """
            UPDATE cartera.carteras_creditos

            SET
                tipo_comprobante = :tipoComprobante,
                numero_comprobante = :numeroComprobante,

                codigo_estado_cartera = 'A',

                codigo_estado_juridico = '0',
                fecha_estado_juridico = :fechaDesembolso,

                valor_desembolsado = valor_inicial_credito,
                saldo_actual = valor_inicial_credito,

                abonos_pendientes = 0,
                altura_cuota = 0,

                fecha_contable = :fechaContable,
                fecha_desembolso = :fechaDesembolso,

                ultima_fecha_capital = :fechaDesembolso,
                ultima_fecha_interes = :fechaDesembolso,
                ultima_fecha_mora = :fechaDesembolso,
                ultima_fecha_seguro = :fechaDesembolso,
                ultima_fecha_fondo = :fechaDesembolso,

                proxima_fecha_capital = fecha_primera_cuota_capital,
                proxima_fecha_interes = fecha_primera_cuota_interes,
                proxima_fecha_seguro = :fechaDesembolso,
                proxima_fecha_fondo = :fechaDesembolso,

                intereses_pagados_hasta = :fechaDesembolso,
                intereses_mora_hasta = :fechaDesembolso,

                credito_evaluado = false,
                fecha_evaluacion = :fechaDesembolso,

                edad_de_riesgo = :edadRiesgo,
                edad_riesgo_inicial = :edadRiesgoInicial,

                edad_de_mora = 'A',
                edad_de_pe = 'A',
                edad_de_homologacion = 'A',
                edad_contable = 'A',

                codigo_modificacion_credito =
                    :codigoModificacionCredito,

                numero_novaciones = :numeroNovaciones,

                credito_reestructurado = false,
                fecha_reestructuracion = :fechaDesembolso,

                edad_reestructurado = 'A',
                edad_reestructuracion_inicial = 'A',

                ultima_fecha_cifin = :fechaDesembolso,
                ultima_fecha_datacredito = :fechaDesembolso,
                ultima_fecha_otra = :fechaDesembolso,

                establecimiento = 'OFICINA',

                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP

            WHERE id_cartera_credito = :idCarteraCredito
              AND codigo_estado_cartera = 'P'
            """;

    // =========================================================
    // BANDEJA
    // =========================================================

    public List<CreditoPendienteDesembolso> listarPendientes() {

        return jdbc.query(
                SQL_BANDEJA,
                new MapSqlParameterSource(),
                (rs, rowNum) ->
                        new CreditoPendienteDesembolso(
                                rs.getInt("id_cartera_credito"),
                                rs.getInt("id_solicitud_credito"),

                                rs.getInt("id_agencia"),
                                rs.getInt("id_linea_credito"),
                                rs.getInt("id_datos_personal"),
                                rs.getObject(
                                        "id_cuenta_aportes",
                                        Integer.class
                                ),

                                rs.getString("pagare_cartera"),

                                rs.getString("documento"),
                                rs.getString("nombre_completo"),

                                rs.getString("codigo_linea_credito"),
                                rs.getString("nombre_linea_credito"),

                                rs.getBigDecimal("valor_inicial_credito"),
                                rs.getBigDecimal("valor_desembolsado"),
                                rs.getObject("plazo", Integer.class),

                                rs.getObject(
                                        "fecha_inclusion_sistema",
                                        LocalDate.class
                                ),
                                rs.getObject(
                                        "fecha_primera_cuota",
                                        LocalDate.class
                                ),
                                rs.getObject(
                                        "fecha_final",
                                        LocalDate.class
                                ),

                                rs.getString("codigo_estado_cartera")
                        )
        );
    }

    // =========================================================
    // CONSULTAR CRÉDITO
    // =========================================================

    public Optional<CreditoDesembolsoDatos> buscarCredito(
            Integer idCarteraCredito
    ) {

        return consultarCredito(
                SQL_CREDITO,
                idCarteraCredito
        );
    }

    // =========================================================
    // BLOQUEAR CRÉDITO
    // =========================================================

    public Optional<CreditoDesembolsoDatos> bloquearCredito(
            Integer idCarteraCredito
    ) {

        return consultarCredito(
                SQL_BLOQUEAR_CREDITO,
                idCarteraCredito
        );
    }

    private Optional<CreditoDesembolsoDatos> consultarCredito(
            String sql,
            Integer idCarteraCredito
    ) {

        List<CreditoDesembolsoDatos> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idCarteraCredito",
                                        idCarteraCredito
                                ),
                        (rs, rowNum) ->
                                new CreditoDesembolsoDatos(
                                        rs.getInt("id_cartera_credito"),
                                        rs.getInt("id_solicitud_credito"),

                                        rs.getInt("id_agencia"),
                                        rs.getInt("id_linea_credito"),
                                        rs.getInt("id_datos_personal"),
                                        rs.getObject(
                                                "id_cuenta_aportes",
                                                Integer.class
                                        ),

                                        rs.getString("pagare_cartera"),

                                        rs.getString("documento"),
                                        rs.getString("nombre_completo"),

                                        rs.getString("codigo_linea_credito"),
                                        rs.getString("nombre_linea_credito"),

                                        rs.getString(
                                                "codigo_clasificacion_credito"
                                        ),
                                        rs.getString(
                                                "codigo_garantia_credito"
                                        ),
                                        rs.getString(
                                                "tipo_garantia"
                                        ),
                                        rs.getString(
                                                "codigo_destino_economico"
                                        ),
                                        rs.getString(
                                                "periodo_codigo_interes"
                                        ),
                                        rs.getString(
                                                "tipo_modalidad_interes"
                                        ),
                                        rs.getString(
                                                "codigo_tipo_cuota"
                                        ),
                                        rs.getString(
                                                "codigo_forma_pago"
                                        ),

                                        rs.getObject(
                                                "plazo",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "amortizacion_capital",
                                                Integer.class
                                        ),

                                        rs.getBigDecimal(
                                                "valor_inicial_credito"
                                        ),
                                        rs.getBigDecimal(
                                                "valor_desembolsado"
                                        ),
                                        rs.getBigDecimal(
                                                "valor_base_calculo_cuota"
                                        ),
                                        rs.getBigDecimal(
                                                "valor_primera_cuota"
                                        ),
                                        rs.getBigDecimal(
                                                "valor_cuota"
                                        ),
                                        rs.getBigDecimal(
                                                "saldo_actual"
                                        ),

                                        rs.getBigDecimal(
                                                "tasa_nominal_anual"
                                        ),
                                        rs.getBigDecimal(
                                                "tasa_efectiva_anual"
                                        ),

                                        rs.getObject(
                                                "fecha_inclusion_sistema",
                                                LocalDate.class
                                        ),
                                        rs.getObject(
                                                "fecha_primera_cuota",
                                                LocalDate.class
                                        ),
                                        rs.getObject(
                                                "fecha_primera_cuota_capital",
                                                LocalDate.class
                                        ),
                                        rs.getObject(
                                                "fecha_primera_cuota_interes",
                                                LocalDate.class
                                        ),
                                        rs.getObject(
                                                "fecha_final",
                                                LocalDate.class
                                        ),

                                        rs.getString(
                                                "codigo_estado_cartera"
                                        ),

                                        rs.getObject(
                                                "id_fondo_garantia",
                                                Integer.class
                                        ),

                                        rs.getString(
                                                "codigo_fondo"
                                        ),

                                        rs.getString(
                                                "nombre_fondo"
                                        ),

                                        rs.getObject(
                                                "id_cuenta_pasivo_fondo",
                                                Integer.class
                                        ),

                                        rs.getObject(
                                                "id_cuenta_iva_fondo",
                                                Integer.class
                                        ),

                                        rs.getBigDecimal(
                                                "valor_fondo_garantia"
                                        ),

                                        rs.getBigDecimal(
                                                "porcentaje_fondo_aplicado"
                                        ),

                                        rs.getBigDecimal(
                                                "valor_consulta_central_riesgo"
                                        ),

                                        rs.getBigDecimal(
                                                "valor_papeleria"
                                        ),
                                        rs.getBigDecimal(
                                                "porcentaje_papeleria"
                                        ),

                                        rs.getBigDecimal(
                                                "valor_aportes"
                                        ),
                                        rs.getBigDecimal(
                                                "porcentaje_aportes"
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }

    // =========================================================
    // CENTRAL DE RIESGO
    // =========================================================

    public int contarConsultasCentralRiesgo(
            Integer idSolicitudCredito
    ) {

        Integer cantidad =
                jdbc.queryForObject(
                        SQL_CANTIDAD_CENTRALES,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                ),
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }

    // =========================================================
    // IVA
    // =========================================================

    public BigDecimal obtenerPorcentajeIva(
            Integer idAgencia
    ) {

        List<BigDecimal> resultados =
                jdbc.query(
                        SQL_PORCENTAJE_IVA,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idAgencia",
                                        idAgencia
                                )
                                .addValue(
                                        "codigoParametro",
                                        CODIGO_PARAMETRO_IVA
                                ),
                        (rs, rowNum) ->
                                rs.getBigDecimal(
                                        "valor_parametro"
                                )
                );

        if (resultados.isEmpty()
                || resultados.get(0) == null) {

            throw new IllegalStateException(
                    "No está configurado el porcentaje de IVA "
                            + "para la agencia "
                            + idAgencia
                            + "."
            );
        }

        return resultados.get(0);
    }

    // =========================================================
    // PARÁMETRO CONTABLE
    // =========================================================

    public Optional<CuentaContableDatos> buscarParametroContable(
            Integer idAgencia,
            Integer idLineaCredito,
            String concepto
    ) {

        List<CuentaContableDatos> resultados =
                jdbc.query(
                        SQL_PARAMETRO_CONTABLE,
                        new MapSqlParameterSource()
                                .addValue("idAgencia", idAgencia)
                                .addValue(
                                        "idLineaCredito",
                                        idLineaCredito
                                )
                                .addValue("concepto", concepto),
                        (rs, rowNum) ->
                                new CuentaContableDatos(
                                        rs.getInt(
                                                "id_catalogo_cuenta"
                                        ),
                                        rs.getString(
                                                "codigo_cuenta"
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }

    // =========================================================
    // CUENTA DE CAPITAL
    // =========================================================

    public Optional<CuentaContableDatos> buscarCuentaCapital(
            Integer idAgencia,
            String codigoClasificacionCredito,
            String codigoFormaPago,
            String tipoGarantia,
            String codigoEdadRiesgo,
            String codigoSegmentoProductivo
    ) {

        List<CuentaContableDatos> resultados =
                jdbc.query(
                        SQL_CUENTA_CAPITAL,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idAgencia",
                                        idAgencia
                                )
                                .addValue(
                                        "codigoClasificacionCredito",
                                        codigoClasificacionCredito
                                )
                                .addValue(
                                        "codigoFormaPago",
                                        codigoFormaPago
                                )
                                .addValue(
                                        "tipoGarantia",
                                        tipoGarantia
                                )
                                .addValue(
                                        "codigoEdadRiesgo",
                                        codigoEdadRiesgo
                                )
                                .addValue(
                                        "codigoSegmentoProductivo",
                                        codigoSegmentoProductivo
                                ),
                        (rs, rowNum) ->
                                new CuentaContableDatos(
                                        rs.getInt(
                                                "id_catalogo_cuenta"
                                        ),
                                        rs.getString(
                                                "codigo_cuenta"
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }

    // =========================================================
// CUENTA DE AHORRO PARA DESEMBOLSO
// =========================================================

    public Optional<CuentaAhorroDesembolsoDatos>
    buscarCuentaAhorroDesembolso(
            Integer idCuentaAhorro
    ) {

        List<CuentaAhorroDesembolsoDatos> resultados =
                jdbc.query(
                        SQL_CUENTA_AHORRO_DESEMBOLSO,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idCuentaAhorro",
                                        idCuentaAhorro
                                ),
                        (rs, rowNum) ->
                                new CuentaAhorroDesembolsoDatos(
                                        rs.getInt(
                                                "id_cuenta_ahorro"
                                        ),
                                        rs.getInt(
                                                "id_datos_personal"
                                        ),
                                        rs.getInt(
                                                "id_forma_ahorro"
                                        ),
                                        rs.getString(
                                                "codigo_forma_ahorro"
                                        ),
                                        rs.getString(
                                                "nombre_forma_ahorro"
                                        ),
                                        rs.getObject(
                                                "id_catalogo_cuenta",
                                                Integer.class
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }

    // =========================================================
    // EDADES DE RIESGO
    // =========================================================

    public List<CreditoEdadDatos> consultarEdadesCreditosActivos(
            Integer idDatosPersonal,
            Integer idCarteraCredito
    ) {

        return jdbc.query(
                SQL_EDADES_CREDITOS_ACTIVOS,
                new MapSqlParameterSource()
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        )
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        ),
                (rs, rowNum) ->
                        new CreditoEdadDatos(
                                rs.getInt(
                                        "id_cartera_credito"
                                ),
                                rs.getString(
                                        "edad_de_riesgo"
                                )
                        )
        );
    }

    // =========================================================
    // CRÉDITOS CANCELADOS
    // =========================================================

    public List<CreditoCanceladoDatos> consultarCreditosCancelados(
            Integer idDatosPersonal,
            Integer idCarteraCredito
    ) {

        return jdbc.query(
                SQL_CREDITOS_CANCELADOS,
                new MapSqlParameterSource()
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        )
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        ),
                (rs, rowNum) ->
                        new CreditoCanceladoDatos(
                                rs.getInt(
                                        "id_cartera_credito"
                                ),
                                rs.getString(
                                        "pagare_cartera"
                                ),
                                rs.getObject(
                                        "numero_novaciones",
                                        Integer.class
                                ),
                                rs.getObject(
                                        "ultima_fecha_capital",
                                        LocalDate.class
                                )
                        )
        );
    }

    // =========================================================
    // ACTIVAR CRÉDITO
    // =========================================================

    public int activarCredito(
            Integer idCarteraCredito,
            String tipoComprobante,
            String numeroComprobante,
            LocalDate fechaContable,
            LocalDate fechaDesembolso,
            String edadRiesgoInicial,
            String edadRiesgo,
            String codigoModificacionCredito,
            Integer numeroNovaciones,
            Integer idUsuario
    ) {

        return jdbc.update(
                SQL_ACTIVAR_CREDITO,
                new MapSqlParameterSource()
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        )
                        .addValue(
                                "tipoComprobante",
                                tipoComprobante
                        )
                        .addValue(
                                "numeroComprobante",
                                numeroComprobante
                        )
                        .addValue(
                                "fechaContable",
                                fechaContable
                        )
                        .addValue(
                                "fechaDesembolso",
                                fechaDesembolso
                        )
                        .addValue(
                                "edadRiesgoInicial",
                                edadRiesgoInicial
                        )
                        .addValue(
                                "edadRiesgo",
                                edadRiesgo
                        )
                        .addValue(
                                "codigoModificacionCredito",
                                codigoModificacionCredito
                        )
                        .addValue(
                                "numeroNovaciones",
                                numeroNovaciones
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
        );
    }

    // =========================================================
    // RECORDS
    // =========================================================

    public record CreditoPendienteDesembolso(
            Integer idCarteraCredito,
            Integer idSolicitudCredito,

            Integer idAgencia,
            Integer idLineaCredito,
            Integer idDatosPersonal,
            Integer idCuentaAportes,

            String pagareCartera,

            String documento,
            String nombreCompleto,

            String codigoLineaCredito,
            String nombreLineaCredito,

            BigDecimal valorInicialCredito,
            BigDecimal valorDesembolsado,
            Integer plazo,

            LocalDate fechaInclusionSistema,
            LocalDate fechaPrimeraCuota,
            LocalDate fechaFinal,

            String codigoEstadoCartera
    ) {
    }

    public record CreditoDesembolsoDatos(
            Integer idCarteraCredito,
            Integer idSolicitudCredito,

            Integer idAgencia,
            Integer idLineaCredito,
            Integer idDatosPersonal,
            Integer idCuentaAportes,

            String pagareCartera,

            String documento,
            String nombreCompleto,

            String codigoLineaCredito,
            String nombreLineaCredito,

            String codigoClasificacionCredito,
            String codigoGarantiaCredito,
            String tipoGarantia,
            String codigoDestinoEconomico,
            String periodoCodigoInteres,
            String tipoModalidadInteres,
            String codigoTipoCuota,
            String codigoFormaPago,

            Integer plazo,
            Integer amortizacionCapital,

            BigDecimal valorInicialCredito,
            BigDecimal valorDesembolsado,
            BigDecimal valorBaseCalculoCuota,
            BigDecimal valorPrimeraCuota,
            BigDecimal valorCuota,
            BigDecimal saldoActual,

            BigDecimal tasaNominalAnual,
            BigDecimal tasaEfectivaAnual,

            LocalDate fechaInclusionSistema,
            LocalDate fechaPrimeraCuota,
            LocalDate fechaPrimeraCuotaCapital,
            LocalDate fechaPrimeraCuotaInteres,
            LocalDate fechaFinal,

            String codigoEstadoCartera,

            Integer idFondoGarantia,
            String codigoFondo,
            String nombreFondo,
            Integer idCuentaPasivoFondo,
            Integer idCuentaIvaFondo,

            BigDecimal valorFondoGarantia,
            BigDecimal porcentajeFondoAplicado,

            BigDecimal valorConsultaCentralRiesgo,

            BigDecimal valorPapeleria,
            BigDecimal porcentajePapeleria,

            BigDecimal valorAportes,
            BigDecimal porcentajeAportes
    ) {
    }

    public record CuentaContableDatos(
            Integer idCatalogoCuenta,
            String codigoCuenta
    ) {
    }

    public record CuentaAhorroDesembolsoDatos(
            Integer idCuentaAhorro,
            Integer idDatosPersonal,
            Integer idFormaAhorro,

            String codigoFormaAhorro,
            String nombreFormaAhorro,

            Integer idCatalogoCuenta
    ) {
    }

    public record CreditoEdadDatos(
            Integer idCarteraCredito,
            String edadRiesgo
    ) {
    }

    public record CreditoCanceladoDatos(
            Integer idCarteraCredito,
            String pagareCartera,
            Integer numeroNovaciones,
            LocalDate ultimaFechaCapital
    ) {
    }
}