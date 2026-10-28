package co.assip.erp.cartera.pago_cuotas_v1;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Pago de Cuotas V1.
 *
 * Responsabilidades:
 * - Listar los créditos de un asociado.
 * - Consultar las condiciones financieras de un crédito.
 *
 * La búsqueda de asociados por documento, nombres y
 * apellidos se reutiliza del módulo buscadorasociados.
 *
 * Este repositorio es exclusivamente de consulta.
 */
@Repository
@RequiredArgsConstructor
public class PagoCuotasV1Repository {

    private final JdbcTemplate jdbc;

    // =========================================================
    // LISTAR CRÉDITOS DEL ASOCIADO
    // =========================================================

    /**
     * Lista los créditos registrados para una persona.
     *
     * Incluye los créditos con y sin saldo para permitir
     * su identificación durante las pruebas.
     */
    public List<CreditoResumen> listarCreditosPorAsociado(
            Integer idDatosPersonal
    ) {

        final String sql = """
                SELECT
                    id_cartera_credito,
                    id_agencia,
                    id_linea_credito,
                    pagare_cartera,
                    id_datos_personal,
                    codigo_estado_cartera,
                    codigo_estado_juridico,
                    codigo_tipo_cuota,
                    fecha_desembolso,
                    fecha_final,
                    proxima_fecha_capital,
                    valor_desembolsado,
                    saldo_actual,
                    valor_cuota,
                    altura_cuota

                FROM cartera.carteras_creditos

                WHERE id_datos_personal = ?

                ORDER BY
                    CASE
                        WHEN saldo_actual > 0 THEN 0
                        ELSE 1
                    END,
                    fecha_desembolso DESC NULLS LAST,
                    id_cartera_credito DESC
                """;

        return jdbc.query(
                sql,
                this::mapCreditoResumen,
                idDatosPersonal
        );
    }

    // =========================================================
    // CONSULTAR CRÉDITO PARA LIQUIDACIÓN
    // =========================================================

    /**
     * Recupera las condiciones contractuales y financieras para liquidar una cuota.
     * Incluye la descripción de la modalidad de intereses sin alterar los cálculos.
     */
    public Optional<CreditoLiquidacion> buscarCredito(
            Integer idCarteraCredito
    ) {

        final String sql = """
                SELECT
                    c.id_cartera_credito,
                    c.id_agencia,
                    c.id_linea_credito,
                    c.pagare_cartera,
                    c.id_datos_personal,
                    c.codigo_estado_cartera,
                    c.codigo_estado_juridico,
                    c.codigo_tipo_cuota,
                    c.codigo_forma_pago,
                    c.tipo_modalidad_interes,
                    c.periodo_codigo_interes,
                    mi.descripcion_modalidad_interes,
                    c.amortizacion_capital,
                    c.plazo,
                    c.altura_cuota,
                    c.valor_inicial_credito,
                    c.valor_base_calculo_cuota,
                    c.valor_primera_cuota,
                    c.valor_cuota,
                    c.saldo_actual,
                    c.abonos_pendientes,
                    c.tasa_nominal_anual,
                    c.tasa_efectiva_anual,
                    c.fecha_desembolso,
                    c.fecha_primera_cuota,
                    c.fecha_primera_cuota_capital,
                    c.fecha_primera_cuota_interes,
                    c.fecha_final,
                    c.ultima_fecha_capital,
                    c.ultima_fecha_interes,
                    c.ultima_fecha_mora,
                    c.ultima_fecha_seguro,
                    c.ultima_fecha_fondo,
                    c.proxima_fecha_capital,
                    c.proxima_fecha_interes,
                    c.proxima_fecha_seguro,
                    c.proxima_fecha_fondo,
                    c.intereses_pagados_hasta,
                    c.intereses_mora_hasta,
                    l.codigo_linea_credito,
                    l.nombre_linea_credito,
                    c.codigo_clasificacion_credito,
                    cl.descripcion_clasificacion_credito,
                    c.codigo_garantia_credito,
                    g.descripcion_garantia_credito,
                    c.credito_evaluado,
                    c.credito_reestructurado,
                    c.codigo_modificacion_credito,
                    EXISTS (
                        SELECT 1 FROM cartera.creditos_prorrogas p
                        WHERE p.id_cartera_credito = c.id_cartera_credito
                          AND p.estado_prorroga = 'A'
                    ) AS tiene_prorrogas,
                    EXISTS (
                        SELECT 1 FROM cartera.creditos_alivios a
                        WHERE a.id_cartera_credito = c.id_cartera_credito
                          AND a.activo = TRUE
                    ) AS tiene_alivios

                FROM cartera.carteras_creditos c
                LEFT JOIN cartera.lineas_creditos l
                    ON l.id_linea_credito = c.id_linea_credito
                LEFT JOIN cartera.modalidades_intereses mi
                    ON mi.periodo_codigo = c.periodo_codigo_interes
                   AND mi.tipo_modalidad = c.tipo_modalidad_interes
                LEFT JOIN cartera.clasificaciones_creditos cl
                    ON cl.codigo_clasificacion_credito = c.codigo_clasificacion_credito
                LEFT JOIN cartera.garantias_creditos g
                    ON g.codigo_garantia_credito = c.codigo_garantia_credito

                WHERE c.id_cartera_credito = ?
                """;

        return jdbc.query(
                sql,
                this::mapCredito,
                idCarteraCredito
        ).stream().findFirst();
    }

    // =========================================================
// CONSULTAR TASA DE MORA VIGENTE
// =========================================================

    public Optional<BigDecimal> buscarTasaMoraVigente(
            LocalDate fechaPago
    ) {

        final String sql = """
            SELECT tasa_nominal_anual
            FROM cartera.tasas_mora
            WHERE ? BETWEEN fecha_inicio AND fecha_fin
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        rs.getBigDecimal("tasa_nominal_anual"),
                fechaPago
        ).stream().findFirst();
    }

    // =========================================================
    // CONSULTAR SALDO PENDIENTE DE SEGUROS
    // =========================================================

    public BigDecimal consultarSaldoSeguros(Integer idCarteraCredito) {

        final String sql = """
            SELECT
                COALESCE(
                    SUM(
                        COALESCE(d.valor_debito, 0)
                        - COALESCE(d.valor_credito, 0)
                    ),
                    0
                ) AS saldo_seguro

            FROM cartera.creditos_seguros s

            INNER JOIN cartera.creditos_seguros_detalle d
                ON d.id_credito_seguro = s.id_credito_seguro

            WHERE s.id_cartera_credito = ?
              AND s.activo = TRUE
              AND d.estado = 'A'
            """;

        BigDecimal saldo = jdbc.queryForObject(
                sql,
                BigDecimal.class,
                idCarteraCredito
        );

        return saldo != null ? saldo : BigDecimal.ZERO;
    }

    // =========================================================
    // CONSULTAR SALDO PENDIENTE DE COSTAS JUDICIALES
    // =========================================================

    /**
     * Consulta el saldo pendiente de costas judiciales del crédito.
     *
     * Las costas se causan en un proceso independiente.
     * Este método únicamente consulta los valores pendientes
     * de recaudo, sin generar causaciones ni movimientos.
     */
    public BigDecimal consultarSaldoCostasJudiciales(
            Integer idCarteraCredito) {

        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException(
                    "Debe indicar un crédito válido.");
        }

        final String sql = """
            SELECT
                COALESCE(
                    SUM(
                        COALESCE(d.valor_debito, 0)
                        - COALESCE(d.valor_credito, 0)
                    ),
                    0
                ) AS saldo_costas_judiciales

            FROM cartera.creditos_costas_judiciales c

            INNER JOIN cartera.creditos_costas_judiciales_detalle d
                ON d.id_credito_costa_judicial =
                   c.id_credito_costa_judicial

            WHERE c.id_cartera_credito = ?
              AND c.activo = TRUE
              AND d.estado = 'A'
            """;

        BigDecimal saldo = jdbc.queryForObject(
                sql,
                BigDecimal.class,
                idCarteraCredito
        );

        if (saldo == null) {
            return BigDecimal.ZERO;
        }

        if (saldo.signum() < 0) {
            throw new IllegalStateException(
                    "El crédito presenta un saldo negativo "
                            + "de costas judiciales.");
        }

        return saldo;
    }

    // =========================================================
    // CONSULTAR SALDO PENDIENTE DE ALIVIOS
    // =========================================================

    /**
     * Consulta el saldo de intereses de alivios pendiente de recaudo.
     * No genera causaciones ni registra movimientos.
     */
    public BigDecimal consultarSaldoAlivios(Integer idCarteraCredito) {

        final String sql = """
            SELECT
                COALESCE(
                    SUM(
                        COALESCE(d.valor_debito, 0)
                        - COALESCE(d.valor_credito, 0)
                    ),
                    0
                ) AS saldo_alivios
            FROM cartera.creditos_alivios a
            INNER JOIN cartera.creditos_alivios_detalle d
                ON d.id_credito_alivio = a.id_credito_alivio
            WHERE a.id_cartera_credito = ?
              AND d.estado = 'A'
            """;

        BigDecimal saldo = jdbc.queryForObject(
                sql,
                BigDecimal.class,
                idCarteraCredito
        );

        return saldo != null ? saldo : BigDecimal.ZERO;
    }

    // =========================================================
    // HISTORIAL DE PAGOS CONSOLIDADO POR COMPROBANTE
    // =========================================================

    /**
     * Consolida los registros activos del extracto por comprobante.
     * El total pagado procede de los medios de pago, no de la suma
     * de conceptos, para evitar duplicaciones.
     */
    public List<PagoRealizado> listarPagosRealizados(Integer idCarteraCredito) {

        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del crédito debe ser mayor que cero."
            );
        }

        final String sql = """
            SELECT
                e.fecha_pago,
                e.tipo_comprobante,
                e.numero_comprobante,

                COUNT(*) AS cantidad_registros,

                SUM(COALESCE(e.valor_capital, 0)) AS capital,
                SUM(COALESCE(e.valor_interes_causado, 0)) AS interes_causado,
                SUM(COALESCE(e.valor_interes_ingreso, 0)) AS interes_ingreso,
                SUM(COALESCE(e.valor_interes_anticipado, 0)) AS interes_anticipado,
                SUM(COALESCE(e.valor_interes_mora, 0)) AS mora,

                SUM(COALESCE(e.valor_seguro, 0)) AS seguros,

                SUM(COALESCE(e.valor_aportes, 0)) AS aportes,
                SUM(COALESCE(e.valor_papeleria, 0)) AS papeleria,
                SUM(COALESCE(e.valor_fondo_garantia, 0)) AS fondo_garantia,
                SUM(COALESCE(e.otros_valores, 0)) AS otros_valores,

                SUM(COALESCE(e.total_componentes_registrados, 0)) AS total_componentes,
                SUM(COALESCE(e.total_medios_pago, 0)) AS total_pagado

            FROM cartera.vw_cartera_extractos_total e

            WHERE e.id_cartera_credito = ?
              AND e.movimiento_activo = TRUE
              AND e.fecha_pago IS NOT NULL
              AND e.tipo_comprobante IS NOT NULL
              AND e.numero_comprobante IS NOT NULL

            GROUP BY
                e.fecha_pago,
                e.tipo_comprobante,
                e.numero_comprobante

            HAVING SUM(COALESCE(e.total_medios_pago, 0)) > 0

            ORDER BY
                e.fecha_pago DESC,
                e.tipo_comprobante,
                e.numero_comprobante DESC
            """;

        return jdbc.query(sql, (r, rowNum) -> new PagoRealizado(
                date(r, "fecha_pago"),
                r.getString("tipo_comprobante"),
                r.getString("numero_comprobante"),
                r.getInt("cantidad_registros"),

                r.getBigDecimal("capital"),
                r.getBigDecimal("interes_causado"),
                r.getBigDecimal("interes_ingreso"),
                r.getBigDecimal("interes_anticipado"),
                r.getBigDecimal("mora"),

                r.getBigDecimal("seguros"),

                r.getBigDecimal("aportes"),
                r.getBigDecimal("papeleria"),
                r.getBigDecimal("fondo_garantia"),
                r.getBigDecimal("otros_valores"),

                r.getBigDecimal("total_componentes"),
                r.getBigDecimal("total_pagado")
        ), idCarteraCredito);
    }

    // =========================================================
    // MAPEADORES
    // =========================================================

    private CreditoResumen mapCreditoResumen(
            ResultSet r,
            int rowNum
    ) throws SQLException {

        return new CreditoResumen(
                r.getInt("id_cartera_credito"),
                r.getInt("id_agencia"),
                r.getInt("id_linea_credito"),
                r.getString("pagare_cartera"),
                r.getInt("id_datos_personal"),
                r.getString("codigo_estado_cartera"),
                r.getString("codigo_estado_juridico"),
                r.getString("codigo_tipo_cuota"),
                date(r, "fecha_desembolso"),
                date(r, "fecha_final"),
                date(r, "proxima_fecha_capital"),
                r.getBigDecimal("valor_desembolsado"),
                r.getBigDecimal("saldo_actual"),
                r.getBigDecimal("valor_cuota"),
                nullableInt(r, "altura_cuota")
        );
    }

    private CreditoLiquidacion mapCredito(
            ResultSet r,
            int rowNum
    ) throws SQLException {

        return new CreditoLiquidacion(
                r.getInt("id_cartera_credito"),
                r.getInt("id_agencia"),
                r.getInt("id_linea_credito"),
                r.getString("pagare_cartera"),
                r.getInt("id_datos_personal"),

                r.getString("codigo_estado_cartera"),
                r.getString("codigo_estado_juridico"),
                r.getString("codigo_tipo_cuota"),
                r.getString("codigo_forma_pago"),
                r.getString("tipo_modalidad_interes"),
                r.getString("periodo_codigo_interes"),
                r.getString("descripcion_modalidad_interes"),

                nullableInt(r, "amortizacion_capital"),
                nullableInt(r, "plazo"),
                nullableInt(r, "altura_cuota"),

                r.getBigDecimal("valor_inicial_credito"),
                r.getBigDecimal("valor_base_calculo_cuota"),
                r.getBigDecimal("valor_primera_cuota"),
                r.getBigDecimal("valor_cuota"),

                r.getBigDecimal("saldo_actual"),
                r.getBigDecimal("abonos_pendientes"),

                r.getBigDecimal("tasa_nominal_anual"),
                r.getBigDecimal("tasa_efectiva_anual"),

                date(r, "fecha_desembolso"),
                date(r, "fecha_primera_cuota"),
                date(r, "fecha_primera_cuota_capital"),
                date(r, "fecha_primera_cuota_interes"),
                date(r, "fecha_final"),

                date(r, "ultima_fecha_capital"),
                date(r, "ultima_fecha_interes"),
                date(r, "ultima_fecha_mora"),
                date(r, "ultima_fecha_seguro"),
                date(r, "ultima_fecha_fondo"),

                date(r, "proxima_fecha_capital"),
                date(r, "proxima_fecha_interes"),
                date(r, "proxima_fecha_seguro"),
                date(r, "proxima_fecha_fondo"),

                date(r, "intereses_pagados_hasta"),
                date(r, "intereses_mora_hasta"),

                r.getString("codigo_linea_credito"),
                r.getString("nombre_linea_credito"),
                r.getString("codigo_clasificacion_credito"),
                r.getString("descripcion_clasificacion_credito"),
                r.getString("codigo_garantia_credito"),
                r.getString("descripcion_garantia_credito"),
                nullableBoolean(r, "credito_evaluado"),
                nullableBoolean(r, "credito_reestructurado"),
                r.getString("codigo_modificacion_credito"),
                r.getBoolean("tiene_prorrogas"),
                r.getBoolean("tiene_alivios")
        );
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private static LocalDate date(
            ResultSet r,
            String column
    ) throws SQLException {

        return r.getObject(column, LocalDate.class);
    }

    private static Integer nullableInt(
            ResultSet r,
            String column
    ) throws SQLException {

        int value = r.getInt(column);

        return r.wasNull() ? null : value;
    }

    private static Boolean nullableBoolean(ResultSet r, String column) throws SQLException {
        boolean value = r.getBoolean(column);
        return r.wasNull() ? null : value;
    }

    // =========================================================
    // DTO: RESUMEN DE CRÉDITO
    // =========================================================

    public record CreditoResumen(
            Integer idCarteraCredito,
            Integer idAgencia,
            Integer idLineaCredito,
            String pagareCartera,
            Integer idDatosPersonal,
            String codigoEstadoCartera,
            String codigoEstadoJuridico,
            String codigoTipoCuota,
            LocalDate fechaDesembolso,
            LocalDate fechaFinal,
            LocalDate proximaFechaCapital,
            BigDecimal valorDesembolsado,
            BigDecimal saldoActual,
            BigDecimal valorCuota,
            Integer alturaCuota
    ) {}

    // =========================================================
    // DTO: DATOS PARA LIQUIDACIÓN
    // =========================================================

    public record CreditoLiquidacion(
            Integer idCarteraCredito,
            Integer idAgencia,
            Integer idLineaCredito,
            String pagareCartera,
            Integer idDatosPersonal,

            String codigoEstadoCartera,
            String codigoEstadoJuridico,
            String codigoTipoCuota,
            String codigoFormaPago,
            String tipoModalidadInteres,
            String periodoCodigoInteres,
            String descripcionModalidadInteres,

            Integer amortizacionCapital,
            Integer plazo,
            Integer alturaCuota,

            BigDecimal valorInicialCredito,
            BigDecimal valorBaseCalculoCuota,
            BigDecimal valorPrimeraCuota,
            BigDecimal valorCuota,

            BigDecimal saldoActual,
            BigDecimal abonosPendientes,

            BigDecimal tasaNominalAnual,
            BigDecimal tasaEfectivaAnual,

            LocalDate fechaDesembolso,
            LocalDate fechaPrimeraCuota,
            LocalDate fechaPrimeraCuotaCapital,
            LocalDate fechaPrimeraCuotaInteres,
            LocalDate fechaFinal,

            LocalDate ultimaFechaCapital,
            LocalDate ultimaFechaInteres,
            LocalDate ultimaFechaMora,
            LocalDate ultimaFechaSeguro,
            LocalDate ultimaFechaFondo,

            LocalDate proximaFechaCapital,
            LocalDate proximaFechaInteres,
            LocalDate proximaFechaSeguro,
            LocalDate proximaFechaFondo,

            LocalDate interesesPagadosHasta,
            LocalDate interesesMoraHasta,

            String codigoLineaCredito,
            String nombreLineaCredito,
            String codigoClasificacionCredito,
            String descripcionClasificacionCredito,
            String codigoGarantiaCredito,
            String descripcionGarantiaCredito,
            Boolean creditoEvaluado,
            Boolean creditoReestructurado,
            String codigoModificacionCredito,
            boolean tieneProrrogas,
            boolean tieneAlivios
    ) {}

    // =========================================================
    // DTO: PAGO HISTÓRICO CONSOLIDADO
    // =========================================================

    // =========================================================
// DTO: PAGO HISTÓRICO CONSOLIDADO
// =========================================================

    public record PagoRealizado(
            LocalDate fechaPago,
            String tipoComprobante,
            String numeroComprobante,
            Integer cantidadRegistros,

            BigDecimal capital,
            BigDecimal interesCausado,
            BigDecimal interesIngreso,
            BigDecimal interesAnticipado,
            BigDecimal mora,

            BigDecimal seguros,

            BigDecimal aportes,
            BigDecimal papeleria,
            BigDecimal fondoGarantia,
            BigDecimal otrosValores,

            BigDecimal totalComponentes,
            BigDecimal totalPagado
    ) {

        public String comprobanteCompleto() {
            return tipoComprobante + "-" + numeroComprobante;
        }

        public BigDecimal diferencia() {
            return totalPagado.subtract(totalComponentes);
        }
    }
}
