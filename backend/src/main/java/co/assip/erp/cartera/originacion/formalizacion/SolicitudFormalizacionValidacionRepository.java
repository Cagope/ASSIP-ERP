package co.assip.erp.cartera.originacion.formalizacion;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudFormalizacionValidacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudFormalizacionValidacionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // SOLICITUD
    // =========================================================

    private static final String SQL_SOLICITUD = """
            SELECT
                sc.id_solicitud_credito,
                sc.numero_solicitud,
                sc.id_agencia,
                sc.id_datos_personal,
                sc.id_cuenta_aportes,
                sc.id_linea_credito,
                sc.id_cartera_credito,
                sc.id_solicitud_proceso,
                sc.id_solicitud_resultado,
                sc.valor_solicitado,
                sc.valor_aportes_requerido,
                sc.valor_aportes_validacion,
                sc.cumple_aportes_validacion

            FROM cartera.solicitudes_creditos sc

            WHERE sc.id_solicitud_credito =
                  :idSolicitudCredito

              AND sc.activo = true
            """;

    // =========================================================
    // SOLICITUD CON BLOQUEO TRANSACCIONAL
    //
    // Se utilizará al generar el pagaré, dentro de la transacción.
    // =========================================================

    private static final String SQL_BLOQUEAR_SOLICITUD = """
            SELECT
                sc.id_solicitud_credito,
                sc.numero_solicitud,
                sc.id_agencia,
                sc.id_datos_personal,
                sc.id_cuenta_aportes,
                sc.id_linea_credito,
                sc.id_cartera_credito,
                sc.id_solicitud_proceso,
                sc.id_solicitud_resultado,
                sc.valor_solicitado,
                sc.valor_aportes_requerido,
                sc.valor_aportes_validacion,
                sc.cumple_aportes_validacion

            FROM cartera.solicitudes_creditos sc

            WHERE sc.id_solicitud_credito =
                  :idSolicitudCredito

              AND sc.activo = true

            FOR UPDATE OF sc
            """;

    // =========================================================
    // PERSONAS VINCULADAS A LA SOLICITUD
    //
    // PRINCIPAL = deudor principal
    // CODEUDOR  = codeudor
    //
    // Se utiliza la misma clasificación de originación.
    // =========================================================

    private static final String SQL_PERSONAS_SOLICITUD = """
            SELECT
                sd.id_solicitud_deudor,
                sd.id_datos_personal,
                sd.tipo_deudor,

                persona.documento
                    AS cedula,

                persona.nombre_completo_apellidos
                    AS nombre_completo

            FROM cartera.solicitudes_deudores sd

            LEFT JOIN reporting.vw_datos_personales_operativa persona
                ON persona.id_datos_personal =
                   sd.id_datos_personal

            WHERE sd.id_solicitud_credito =
                  :idSolicitudCredito

              AND sd.activo = true

              AND UPPER(
                      TRIM(sd.tipo_deudor)
                  ) IN (
                      'PRINCIPAL',
                      'CODEUDOR'
                  )

            ORDER BY
                CASE
                    WHEN UPPER(
                        TRIM(sd.tipo_deudor)
                    ) = 'PRINCIPAL'
                        THEN 0

                    ELSE 1
                END,

                sd.orden_deudor,
                sd.id_solicitud_deudor
            """;

    // =========================================================
    // BLOQUEOS POR MORA
    //
    // Incluye:
    //
    // 1. Créditos propios del deudor principal.
    // 2. Créditos propios de los codeudores.
    // 3. Obligaciones donde cualquiera de ellos figura
    //    como fiador/codeudor de otro crédito.
    //
    // No limita por agencia.
    //
    // Los días de mora provienen del vector ACTUAL.
    // =========================================================

    private static final String SQL_BLOQUEOS_MORA = """
            WITH personas_solicitud AS (

                SELECT DISTINCT
                    sd.id_datos_personal,

                    CASE
                        WHEN UPPER(
                            TRIM(sd.tipo_deudor)
                        ) = 'PRINCIPAL'
                            THEN 'DEUDOR'

                        ELSE 'CODEUDOR'
                    END::varchar
                        AS calidad

                FROM cartera.solicitudes_deudores sd

                WHERE sd.id_solicitud_credito =
                      :idSolicitudCredito

                  AND sd.activo = true

                  AND UPPER(
                      TRIM(sd.tipo_deudor)
                  ) IN (
                      'PRINCIPAL',
                      'CODEUDOR'
                  )
            ),

            obligaciones_propias AS (

                SELECT
                    ps.id_datos_personal,
                    ps.calidad,

                    'PROPIA'::varchar
                        AS tipo_obligacion,

                    credito.id_cartera_credito

                FROM personas_solicitud ps

                INNER JOIN cartera.carteras_creditos credito
                    ON credito.id_datos_personal =
                       ps.id_datos_personal

                WHERE COALESCE(
                    credito.saldo_actual,
                    0
                ) > 0
            ),

            obligaciones_codeudadas AS (

                SELECT DISTINCT
                    ps.id_datos_personal,
                    ps.calidad,

                    'CODEUDA'::varchar
                        AS tipo_obligacion,

                    credito.id_cartera_credito

                FROM personas_solicitud ps

                INNER JOIN cartera.obligaciones_fiadores fiador
                    ON fiador.id_datos_personal =
                       ps.id_datos_personal

                INNER JOIN cartera.obligaciones_juridicas obligacion
                    ON obligacion.id_obligacion_juridica =
                       fiador.id_obligacion_juridica

                INNER JOIN cartera.carteras_creditos credito
                    ON credito.id_obligacion_juridica =
                       obligacion.id_obligacion_juridica

                WHERE credito.id_datos_personal <>
                      ps.id_datos_personal

                  AND COALESCE(
                      credito.saldo_actual,
                      0
                  ) > 0
            ),

            obligaciones AS (

                SELECT *
                FROM obligaciones_propias

                UNION ALL

                SELECT *
                FROM obligaciones_codeudadas
            )

            SELECT DISTINCT
                o.id_datos_personal,

                persona.documento
                    AS cedula,

                persona.nombre_completo_apellidos
                    AS nombre_completo,

                o.calidad,
                o.tipo_obligacion,

                credito.id_cartera_credito,
                credito.id_agencia,
                credito.id_linea_credito,

                linea.codigo_linea_credito,
                linea.nombre_linea_credito,

                credito.pagare_cartera,

                COALESCE(
                    credito.saldo_actual,
                    0
                )::numeric
                    AS saldo_actual,

                vector.dias_mora

            FROM obligaciones o

            INNER JOIN cartera.carteras_creditos credito
                ON credito.id_cartera_credito =
                   o.id_cartera_credito

            LEFT JOIN cartera.lineas_creditos linea
                ON linea.id_linea_credito =
                   credito.id_linea_credito

            LEFT JOIN reporting.vw_datos_personales_operativa persona
                ON persona.id_datos_personal =
                   o.id_datos_personal

            INNER JOIN cartera.vw_cartera_vector_comportamiento_detalle vector
                ON vector.id_cartera_credito =
                   credito.id_cartera_credito

               AND vector.tipo_posicion =
                   'ACTUAL'

            WHERE vector.dias_mora > 0

            ORDER BY
                o.calidad,
                persona.documento,
                credito.id_agencia,
                credito.pagare_cartera
            """;

    // =========================================================
// APORTES ACTUALES Y RECIPROCIDAD PARA FORMALIZACIÓN
//
// Recalcula los aportes requeridos sobre:
// cartera propia vigente + valor formalizado.
//
// Conserva intacta la fotografía de originación.
// =========================================================

    private static final String SQL_APORTES = """
        SELECT
            sc.id_solicitud_credito,
            sc.id_datos_personal,
            sc.id_cuenta_aportes,

            cuenta.saldo_actual_cuenta::numeric
                AS valor_aportes_actual,

            CASE
                WHEN sc.factor_reciprocidad_aportes_aplicado > 0
                 AND CAST(:valorFormalizado AS numeric) > 0
                THEN ROUND(
                    (
                        COALESCE(cartera.saldo_actual_cartera, 0)
                        + CAST(:valorFormalizado AS numeric)
                    )
                    / sc.factor_reciprocidad_aportes_aplicado,
                    2
                )
                ELSE NULL
            END AS valor_aportes_requerido

        FROM cartera.solicitudes_creditos sc

        LEFT JOIN depositos.cuentas_ahorro cuenta
            ON cuenta.id_cuenta_ahorro =
               sc.id_cuenta_aportes

           AND cuenta.id_datos_personal =
               sc.id_datos_personal

           AND cuenta.id_forma_ahorro = 1

        LEFT JOIN LATERAL (
            SELECT
                COALESCE(
                    SUM(cc.saldo_actual),
                    0
                ) AS saldo_actual_cartera

            FROM cartera.carteras_creditos cc

            WHERE cc.id_datos_personal =
                  sc.id_datos_personal

              AND cc.saldo_actual > 0
        ) cartera ON true

        WHERE sc.id_solicitud_credito =
              :idSolicitudCredito

          AND sc.activo = true
        """;

    // =========================================================
    // PARÁMETRO DE SIMULTANEIDAD DE LA LÍNEA SOLICITADA
    // =========================================================

    private static final String SQL_LINEA_SIMULTANEIDAD = """
            SELECT
                lc.id_linea_credito,
                lc.codigo_linea_credito,
                lc.nombre_linea_credito,
                lc.permite_creditos_simultaneos
            FROM cartera.solicitudes_creditos sc
            INNER JOIN cartera.lineas_creditos lc
                ON lc.id_linea_credito = sc.id_linea_credito
            WHERE sc.id_solicitud_credito = :idSolicitudCredito
              AND sc.activo = true
            """;

    // =========================================================
    // CRÉDITOS PROPIOS VIGENTES DEL TITULAR
    //
    // Todas las líneas y agencias. No incluye obligaciones en
    // las que la persona figura únicamente como fiador.
    // Excluye el crédito ya vinculado a la solicitud, si existe.
    // =========================================================

    private static final String SQL_CREDITOS_VIGENTES_TITULAR = """
            SELECT
                cc.id_cartera_credito,
                cc.id_agencia,
                cc.id_linea_credito,
                lc.codigo_linea_credito,
                lc.nombre_linea_credito,
                cc.pagare_cartera,
                cc.saldo_actual
            FROM cartera.solicitudes_creditos sc
            INNER JOIN cartera.carteras_creditos cc
                ON cc.id_datos_personal = sc.id_datos_personal
            INNER JOIN cartera.lineas_creditos lc
                ON lc.id_linea_credito = cc.id_linea_credito
            WHERE sc.id_solicitud_credito = :idSolicitudCredito
              AND sc.activo = true
              AND cc.codigo_estado_cartera = 'A'
              AND cc.saldo_actual > 0
              AND (
                  sc.id_cartera_credito IS NULL
                  OR cc.id_cartera_credito <> sc.id_cartera_credito
              )
            ORDER BY cc.id_linea_credito, cc.pagare_cartera
            """;

    // =========================================================
    // ACTUALIZAR APORTES DE VALIDACIÓN
    //
    // No modifica:
    //
    // - valor_aportes_inicio
    // - cumple_aportes_inicio
    // - valor_aportes_requerido
    // - valor_solicitado
    // - factor_reciprocidad_aportes_aplicado
    // =========================================================

    private static final String SQL_ACTUALIZAR_APORTES = """
            UPDATE cartera.solicitudes_creditos

            SET
                valor_aportes_validacion =
                    :valorAportesActual,

                cumple_aportes_validacion =
                    :cumpleAportes,

                fk_seguridad_edicion =
                    :idUsuario,

                fecha_edicion =
                    CURRENT_TIMESTAMP

            WHERE id_solicitud_credito =
                  :idSolicitudCredito

              AND activo = true

              AND id_solicitud_proceso = 4

              AND id_solicitud_resultado = 1

              AND id_cartera_credito IS NULL
            """;

    // =========================================================
    // ACTUALIZAR MORA DE CADA DEUDOR / CODEUDOR
    //
    // Los datos de inicio permanecen intactos.
    // =========================================================

    private static final String SQL_ACTUALIZAR_MORA_DEUDOR = """
            UPDATE cartera.solicitudes_deudores

            SET
                saldo_cartera_validacion =
                    :saldoCarteraValidacion,

                dias_mora_validacion =
                    :diasMoraValidacion,

                cumple_mora_validacion =
                    :cumpleMoraValidacion,

                fk_seguridad_edicion =
                    :idUsuario,

                fecha_edicion =
                    CURRENT_TIMESTAMP

            WHERE id_solicitud_deudor =
                  :idSolicitudDeudor

              AND id_solicitud_credito =
                  :idSolicitudCredito

              AND activo = true

              AND EXISTS (
                  SELECT 1
                  FROM cartera.solicitudes_creditos sc

                  WHERE sc.id_solicitud_credito =
                        :idSolicitudCredito

                    AND sc.activo = true

                    AND sc.id_solicitud_proceso = 4

                    AND sc.id_solicitud_resultado = 1

                    AND sc.id_cartera_credito IS NULL
              )
            """;

    // =========================================================
    // CONSULTAR SOLICITUD
    // =========================================================

    public Optional<SolicitudValidacionDatos> buscarSolicitud(
            Integer idSolicitudCredito
    ) {
        return consultarSolicitud(
                SQL_SOLICITUD,
                idSolicitudCredito
        );
    }

    // =========================================================
    // BLOQUEAR SOLICITUD
    // =========================================================

    public Optional<SolicitudValidacionDatos> bloquearSolicitud(
            Integer idSolicitudCredito
    ) {
        return consultarSolicitud(
                SQL_BLOQUEAR_SOLICITUD,
                idSolicitudCredito
        );
    }

    private Optional<SolicitudValidacionDatos> consultarSolicitud(
            String sql,
            Integer idSolicitudCredito
    ) {

        List<SolicitudValidacionDatos> resultados =
                jdbc.query(
                        sql,
                        parametrosSolicitud(
                                idSolicitudCredito
                        ),
                        (rs, rowNum) ->
                                new SolicitudValidacionDatos(
                                        rs.getInt(
                                                "id_solicitud_credito"
                                        ),
                                        rs.getString(
                                                "numero_solicitud"
                                        ),
                                        rs.getInt(
                                                "id_agencia"
                                        ),
                                        rs.getInt(
                                                "id_datos_personal"
                                        ),
                                        rs.getObject(
                                                "id_cuenta_aportes",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "id_linea_credito",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "id_cartera_credito",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "id_solicitud_proceso",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "id_solicitud_resultado",
                                                Integer.class
                                        ),
                                        rs.getBigDecimal(
                                                "valor_solicitado"
                                        ),
                                        rs.getBigDecimal(
                                                "valor_aportes_requerido"
                                        ),
                                        rs.getBigDecimal(
                                                "valor_aportes_validacion"
                                        ),
                                        rs.getObject(
                                                "cumple_aportes_validacion",
                                                Boolean.class
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }

    // =========================================================
    // CONSULTAR PERSONAS VINCULADAS
    // =========================================================

    public List<PersonaSolicitudDatos> consultarPersonasSolicitud(
            Integer idSolicitudCredito
    ) {

        return jdbc.query(
                SQL_PERSONAS_SOLICITUD,
                parametrosSolicitud(
                        idSolicitudCredito
                ),
                (rs, rowNum) ->
                        new PersonaSolicitudDatos(
                                rs.getInt(
                                        "id_solicitud_deudor"
                                ),
                                rs.getInt(
                                        "id_datos_personal"
                                ),
                                rs.getString(
                                        "tipo_deudor"
                                ),
                                rs.getString(
                                        "cedula"
                                ),
                                rs.getString(
                                        "nombre_completo"
                                )
                        )
        );
    }

    // =========================================================
    // CONSULTAR BLOQUEOS POR MORA
    // =========================================================

    public List<BloqueoMoraDatos> consultarBloqueosMora(
            Integer idSolicitudCredito
    ) {

        return jdbc.query(
                SQL_BLOQUEOS_MORA,
                parametrosSolicitud(
                        idSolicitudCredito
                ),
                (rs, rowNum) ->
                        new BloqueoMoraDatos(
                                rs.getInt(
                                        "id_datos_personal"
                                ),
                                rs.getString(
                                        "cedula"
                                ),
                                rs.getString(
                                        "nombre_completo"
                                ),
                                rs.getString(
                                        "calidad"
                                ),
                                rs.getString(
                                        "tipo_obligacion"
                                ),
                                rs.getInt(
                                        "id_cartera_credito"
                                ),
                                rs.getInt(
                                        "id_agencia"
                                ),
                                rs.getObject(
                                        "id_linea_credito",
                                        Integer.class
                                ),
                                rs.getString(
                                        "codigo_linea_credito"
                                ),
                                rs.getString(
                                        "nombre_linea_credito"
                                ),
                                rs.getString(
                                        "pagare_cartera"
                                ),
                                rs.getBigDecimal(
                                        "saldo_actual"
                                ),
                                rs.getObject(
                                        "dias_mora",
                                        Integer.class
                                )
                        )
        );
    }

    // =========================================================
    // CONSULTAR APORTES
    // =========================================================

    public Optional<AportesValidacionDatos> consultarAportes(
            Integer idSolicitudCredito,
            BigDecimal valorFormalizado
    ) {

        List<AportesValidacionDatos> resultados =
                jdbc.query(
                        SQL_APORTES,
                        parametrosSolicitud(
                                idSolicitudCredito
                        ).addValue("valorFormalizado", valorFormalizado),
                        (rs, rowNum) ->
                                new AportesValidacionDatos(
                                        rs.getInt(
                                                "id_solicitud_credito"
                                        ),
                                        rs.getInt(
                                                "id_datos_personal"
                                        ),
                                        rs.getObject(
                                                "id_cuenta_aportes",
                                                Integer.class
                                        ),
                                        rs.getBigDecimal(
                                                "valor_aportes_actual"
                                        ),
                                        rs.getBigDecimal(
                                                "valor_aportes_requerido"
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }

    // =========================================================
    // ACTUALIZAR APORTES
    // =========================================================

    public int actualizarValidacionAportes(
            Integer idSolicitudCredito,
            BigDecimal valorAportesActual,
            boolean cumpleAportes,
            Integer idUsuario
    ) {

        return jdbc.update(
                SQL_ACTUALIZAR_APORTES,
                parametrosSolicitud(
                        idSolicitudCredito
                )
                        .addValue(
                                "valorAportesActual",
                                valorAportesActual
                        )
                        .addValue(
                                "cumpleAportes",
                                cumpleAportes
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
        );
    }

    // =========================================================
    // ACTUALIZAR VALIDACIÓN DE MORA
    // =========================================================

    public int actualizarValidacionMoraDeudor(
            Integer idSolicitudCredito,
            Integer idSolicitudDeudor,
            BigDecimal saldoCarteraValidacion,
            Integer diasMoraValidacion,
            boolean cumpleMoraValidacion,
            Integer idUsuario
    ) {

        return jdbc.update(
                SQL_ACTUALIZAR_MORA_DEUDOR,
                parametrosSolicitud(
                        idSolicitudCredito
                )
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        )
                        .addValue(
                                "saldoCarteraValidacion",
                                saldoCarteraValidacion
                        )
                        .addValue(
                                "diasMoraValidacion",
                                diasMoraValidacion
                        )
                        .addValue(
                                "cumpleMoraValidacion",
                                cumpleMoraValidacion
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
        );
    }

    // =========================================================
    // CONSULTAR PARÁMETRO DE SIMULTANEIDAD
    // =========================================================

    public Optional<LineaSimultaneidadDatos> consultarLineaSimultaneidad(
            Integer idSolicitudCredito
    ) {
        List<LineaSimultaneidadDatos> resultados = jdbc.query(
                SQL_LINEA_SIMULTANEIDAD,
                parametrosSolicitud(idSolicitudCredito),
                (rs, rowNum) -> new LineaSimultaneidadDatos(
                        rs.getInt("id_linea_credito"),
                        rs.getString("codigo_linea_credito"),
                        rs.getString("nombre_linea_credito"),
                        rs.getBoolean("permite_creditos_simultaneos")
                )
        );

        return resultados.stream().findFirst();
    }

    // =========================================================
    // CONSULTAR CRÉDITOS PROPIOS VIGENTES DEL TITULAR
    // =========================================================

    public List<CreditoVigenteTitularDatos> consultarCreditosVigentesTitular(
            Integer idSolicitudCredito
    ) {
        return jdbc.query(
                SQL_CREDITOS_VIGENTES_TITULAR,
                parametrosSolicitud(idSolicitudCredito),
                (rs, rowNum) -> new CreditoVigenteTitularDatos(
                        rs.getInt("id_cartera_credito"),
                        rs.getInt("id_agencia"),
                        rs.getInt("id_linea_credito"),
                        rs.getString("codigo_linea_credito"),
                        rs.getString("nombre_linea_credito"),
                        rs.getString("pagare_cartera"),
                        rs.getBigDecimal("saldo_actual")
                )
        );
    }

    // =========================================================
    // TASA EFECTIVA MÁXIMA LEGAL POR AGENCIA (631)
    // =========================================================

    public Optional<BigDecimal> consultarTasaMaximaLegal(Integer idAgencia) {
        final String sql = """
            SELECT p.valor_parametro::numeric
            FROM general.parametros p
            WHERE p.id_agencia = :idAgencia
              AND p.codigo_parametro = 631
            """;

        List<BigDecimal> valores = jdbc.query(
                sql,
                new MapSqlParameterSource().addValue("idAgencia", idAgencia),
                (rs, rowNum) -> rs.getBigDecimal(1)
        );

        if (valores.size() != 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(valores.get(0));
    }

    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource parametrosSolicitud(
            Integer idSolicitudCredito
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "idSolicitudCredito",
                        idSolicitudCredito
                );
    }

    // =========================================================
    // REGISTROS INTERNOS
    // =========================================================

    public record SolicitudValidacionDatos(

            Integer idSolicitudCredito,

            String numeroSolicitud,

            Integer idAgencia,

            Integer idDatosPersonal,

            Integer idCuentaAportes,

            Integer idLineaCredito,

            Integer idCarteraCredito,

            Integer idSolicitudProceso,

            Integer idSolicitudResultado,

            BigDecimal valorSolicitado,

            BigDecimal valorAportesRequerido,

            BigDecimal valorAportesValidacion,

            Boolean cumpleAportesValidacion

    ) {
    }

    public record PersonaSolicitudDatos(

            Integer idSolicitudDeudor,

            Integer idDatosPersonal,

            String tipoDeudor,

            String cedula,

            String nombreCompleto

    ) {
    }

    public record BloqueoMoraDatos(

            Integer idDatosPersonal,

            String cedula,

            String nombreCompleto,

            String calidad,

            String tipoObligacion,

            Integer idCarteraCredito,

            Integer idAgencia,

            Integer idLineaCredito,

            String codigoLineaCredito,

            String nombreLineaCredito,

            String pagareCartera,

            BigDecimal saldoActual,

            Integer diasMora

    ) {
    }

    public record AportesValidacionDatos(

            Integer idSolicitudCredito,

            Integer idDatosPersonal,

            Integer idCuentaAportes,

            BigDecimal valorAportesActual,

            BigDecimal valorAportesRequerido

    ) {
    }

    // =========================================================
    // REGISTROS PARA VALIDACIÓN DE SIMULTANEIDAD
    // =========================================================

    public record LineaSimultaneidadDatos(
            Integer idLineaCredito,
            String codigoLineaCredito,
            String nombreLineaCredito,
            boolean permiteCreditosSimultaneos
    ) {
    }

    public record CreditoVigenteTitularDatos(
            Integer idCarteraCredito,
            Integer idAgencia,
            Integer idLineaCredito,
            String codigoLineaCredito,
            String nombreLineaCredito,
            String pagareCartera,
            BigDecimal saldoActual
    ) {
    }

    // =========================================================
// SALDO TOTAL DE CARTERA PROPIA DE UNA PERSONA
//
// Incluye créditos al día y en mora.
// Incluye todas las agencias.
// No suma obligaciones donde figura únicamente como fiador.
// =========================================================

    public BigDecimal consultarSaldoCarteraPersona(
            Integer idDatosPersonal
    ) {

        final String sql = """
        SELECT
            COALESCE(
                SUM(cc.saldo_actual),
                0
            ) AS saldo_cartera
        FROM cartera.carteras_creditos cc
        WHERE cc.id_datos_personal = :idDatosPersonal
          AND cc.saldo_actual > 0
        """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        );

        BigDecimal saldo = jdbc.queryForObject(
                sql,
                parametros,
                BigDecimal.class
        );

        return saldo != null
                ? saldo
                : BigDecimal.ZERO;
    }
}
