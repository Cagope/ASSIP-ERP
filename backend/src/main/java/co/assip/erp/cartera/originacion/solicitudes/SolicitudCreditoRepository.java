package co.assip.erp.cartera.originacion.solicitudes;

import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoDetalleDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoResumenDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudEnteAprobadorPreviewDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public class SolicitudCreditoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudCreditoRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<SolicitudCreditoGuardarResponseDTO>
            GUARDAR_CREDITO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudCreditoGuardarResponseDTO.class
            );

    private static final BeanPropertyRowMapper<SolicitudCreditoResumenDTO>
            RESUMEN_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudCreditoResumenDTO.class
            );

    private static final BeanPropertyRowMapper<SolicitudCreditoDetalleDTO>
            DETALLE_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudCreditoDetalleDTO.class
            );


    // =========================================================
    // VALIDAR PERSONA
    // =========================================================

    public boolean existePersona(
            Integer idDatosPersonal
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1
                    FROM hoja_vida.datos_personales dp
                    WHERE dp.id_datos_personal = :idDatosPersonal
                )
                """;

        Boolean existe =
                jdbc.queryForObject(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idDatosPersonal",
                                        idDatosPersonal
                                ),
                        Boolean.class
                );

        return Boolean.TRUE.equals(existe);
    }

    // =========================================================
// VIGENCIA DE ACTUALIZACIÓN DE HOJA DE VIDA
// =========================================================

    public VigenciaHojaVida consultarVigenciaHojaVida(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        String sql = """
            SELECT
                dp.fecha_actualizacion,
                p.valor_parametro AS dias_maximos,
                (
                    dp.fecha_actualizacion IS NOT NULL
                    AND dp.fecha_actualizacion
                        + p.valor_parametro::integer >= CURRENT_DATE
                ) AS vigente
            FROM hoja_vida.datos_personales dp
            CROSS JOIN general.parametros p
            WHERE dp.id_datos_personal = :idDatosPersonal
              AND p.id_agencia = :idAgencia
              AND p.codigo_parametro = 121
            """;

        List<VigenciaHojaVida> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idDatosPersonal",
                                        idDatosPersonal
                                )
                                .addValue(
                                        "idAgencia",
                                        idAgencia
                                ),
                        (rs, rowNum) ->
                                new VigenciaHojaVida(
                                        rs.getObject(
                                                "fecha_actualizacion",
                                                LocalDate.class
                                        ),
                                        rs.getInt(
                                                "dias_maximos"
                                        ),
                                        rs.getBoolean(
                                                "vigente"
                                        )
                                )
                );

        if (resultados.size() != 1) {

            throw new IllegalStateException(
                    "No se pudo determinar la vigencia de la hoja de vida. "
                            + "Verifique el parámetro 121 para la agencia "
                            + idAgencia
                            + " y la existencia del asociado "
                            + idDatosPersonal
                            + "."
            );
        }

        VigenciaHojaVida resultado = resultados.get(0);

        if (resultado.diasMaximos() <= 0) {

            throw new IllegalStateException(
                    "El parámetro 121 debe tener un valor mayor que cero "
                            + "para la agencia "
                            + idAgencia
                            + "."
            );
        }

        return resultado;
    }

    // =========================================================
    // VALIDACIÓN DE CRÉDITOS SIMULTÁNEOS POR LÍNEA
    // =========================================================

    public ValidacionCreditosSimultaneos consultarCreditosSimultaneos(
            Integer idDatosPersonal,
            Integer idLineaCredito
    ) {

        String sql = """
        SELECT
            lc.id_linea_credito,
            lc.nombre_linea_credito,
            lc.permite_creditos_simultaneos,

            cc.pagare_cartera,
            cc.saldo_actual

        FROM cartera.lineas_creditos lc

        LEFT JOIN cartera.carteras_creditos cc
            ON cc.id_datos_personal = :idDatosPersonal
           AND cc.codigo_estado_cartera = 'A'
           AND cc.saldo_actual <> 0

        WHERE lc.id_linea_credito = :idLineaCredito
          AND lc.activo = true

        ORDER BY
            cc.pagare_cartera
        """;

        List<CreditoSimultaneoFila> filas = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        )
                        .addValue(
                                "idLineaCredito",
                                idLineaCredito
                        ),
                (rs, rowNum) -> new CreditoSimultaneoFila(
                        rs.getInt("id_linea_credito"),
                        rs.getString("nombre_linea_credito"),
                        rs.getBoolean("permite_creditos_simultaneos"),
                        rs.getString("pagare_cartera"),
                        rs.getBigDecimal("saldo_actual")
                )
        );

        if (filas.isEmpty()) {
            throw new IllegalStateException(
                    "La línea de crédito seleccionada no existe "
                            + "o no se encuentra activa."
            );
        }

        CreditoSimultaneoFila linea = filas.get(0);

        List<CreditoActivoConSaldo> creditos = filas.stream()
                .filter(fila -> fila.pagareCartera() != null)
                .map(fila -> new CreditoActivoConSaldo(
                        fila.pagareCartera(),
                        fila.saldoActual()
                ))
                .toList();

        boolean permitido =
                linea.permiteCreditosSimultaneos()
                        || creditos.isEmpty();

        return new ValidacionCreditosSimultaneos(
                linea.idLineaCredito(),
                linea.nombreLineaCredito(),
                linea.permiteCreditosSimultaneos(),
                permitido,
                creditos
        );
    }


    // =========================================================
    // PROCESO INICIADA
    // =========================================================

    public CatalogoProceso obtenerProcesoIniciada() {

        String sql = """
                SELECT
                    sp.id_solicitud_proceso,
                    sp.nombre_proceso
                FROM cartera.solicitudes_procesos sp
                WHERE UPPER(TRIM(sp.nombre_proceso)) = 'INICIADA'
                  AND sp.activo = true
                ORDER BY sp.id_solicitud_proceso
                """;

        List<CatalogoProceso> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        (rs, rowNum) ->
                                new CatalogoProceso(
                                        rs.getInt(
                                                "id_solicitud_proceso"
                                        ),
                                        rs.getString(
                                                "nombre_proceso"
                                        )
                                )
                );

        return obtenerUnico(
                resultados,
                "proceso activo INICIADA"
        );
    }

    // =========================================================
// PROCESO APROBACION
// =========================================================

    public CatalogoProceso obtenerProcesoAprobacion() {

        String sql = """
            SELECT
                sp.id_solicitud_proceso,
                sp.nombre_proceso
            FROM cartera.solicitudes_procesos sp
            WHERE UPPER(TRIM(sp.nombre_proceso)) = 'APROBACION'
              AND sp.activo = true
            ORDER BY sp.id_solicitud_proceso
            """;

        List<CatalogoProceso> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        (rs, rowNum) ->
                                new CatalogoProceso(
                                        rs.getInt(
                                                "id_solicitud_proceso"
                                        ),
                                        rs.getString(
                                                "nombre_proceso"
                                        )
                                )
                );

        return obtenerUnico(
                resultados,
                "proceso activo APROBACION"
        );
    }


    // =========================================================
    // RESULTADO EN CURSO
    // =========================================================

    public CatalogoResultado obtenerResultadoEnCurso() {

        String sql = """
                SELECT
                    sr.id_solicitud_resultado,
                    sr.nombre_resultado
                FROM cartera.solicitudes_resultados sr
                WHERE UPPER(TRIM(sr.nombre_resultado)) = 'EN CURSO'
                  AND sr.activo = true
                  AND sr.es_final = false
                ORDER BY sr.id_solicitud_resultado
                """;

        List<CatalogoResultado> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        (rs, rowNum) ->
                                new CatalogoResultado(
                                        rs.getInt(
                                                "id_solicitud_resultado"
                                        ),
                                        rs.getString(
                                                "nombre_resultado"
                                        )
                                )
                );

        return obtenerUnico(
                resultados,
                "resultado activo EN CURSO"
        );
    }


    // =========================================================
    // CUENTA OPERATIVA DE APORTES
    // =========================================================

    // =========================================================
    // CUENTA OPERATIVA DE APORTES
    // =========================================================

    public CuentaAportesResumen buscarCuentaAportes(
            Integer idDatosPersonal
    ) {

        String sql = """
        SELECT
            COUNT(*)::integer AS cantidad,

            CASE
                WHEN COUNT(*) = 1
                THEN MIN(ca.id_cuenta_ahorro)
                ELSE NULL
            END AS id_cuenta_aportes,

            CASE
                WHEN COUNT(*) = 1
                THEN MIN(ca.id_agencia)
                ELSE NULL
            END AS id_agencia

        FROM depositos.cuentas_ahorro ca

        INNER JOIN depositos.formas_ahorro fa
            ON fa.id_forma_ahorro =
               ca.id_forma_ahorro

        INNER JOIN depositos.estados_ahorros ea
            ON TRIM(ea.codigo_estado_ahorro) =
               TRIM(ca.estado_cuenta_cuenta)

        WHERE ca.id_datos_personal = :idDatosPersonal
          AND TRIM(fa.codigo_forma) = '01'
          AND TRIM(fa.tipo_captacion_forma) = '1'
          AND ea.operativo = true
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        ),
                (rs, rowNum) ->
                        new CuentaAportesResumen(
                                rs.getInt("cantidad"),

                                rs.getObject(
                                        "id_cuenta_aportes",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "id_agencia",
                                        Integer.class
                                )
                        )
        );
    }

    // =========================================================
    // SOLICITUD PARA RETOMAR
    // =========================================================

    public Optional<SolicitudRetomarDatos> buscarParaRetomar(
            Integer idSolicitudCredito
    ) {

        String sql = """
                SELECT
                    sc.id_solicitud_credito,
                    sc.numero_solicitud,

                    sc.id_agencia,
                    sc.id_datos_personal,
                    sc.id_cuenta_aportes,

                    sc.id_solicitud_proceso,
                    sp.nombre_proceso,

                    sc.id_solicitud_resultado,
                    sr.nombre_resultado,
                    sr.es_final

                FROM cartera.solicitudes_creditos sc

                INNER JOIN cartera.solicitudes_procesos sp
                    ON sp.id_solicitud_proceso =
                       sc.id_solicitud_proceso

                INNER JOIN cartera.solicitudes_resultados sr
                    ON sr.id_solicitud_resultado =
                       sc.id_solicitud_resultado

                WHERE sc.id_solicitud_credito =
                      :idSolicitudCredito

                  AND sc.activo = true

                FOR UPDATE OF sc
                """;

        List<SolicitudRetomarDatos> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                ),
                        (rs, rowNum) ->
                                new SolicitudRetomarDatos(
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
                                        rs.getInt(
                                                "id_solicitud_proceso"
                                        ),
                                        rs.getString(
                                                "nombre_proceso"
                                        ),
                                        rs.getInt(
                                                "id_solicitud_resultado"
                                        ),
                                        rs.getString(
                                                "nombre_resultado"
                                        ),
                                        rs.getBoolean(
                                                "es_final"
                                        )
                                )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Se encontró más de una solicitud con id "
                            + idSolicitudCredito
                            + "."
            );
        }

        return Optional.of(
                resultados.get(0)
        );
    }


    // =========================================================
    // DEUDOR PRINCIPAL
    // =========================================================

    public boolean existeDeudorPrincipalCorrecto(
            Integer idSolicitudCredito,
            Integer idDatosPersonal
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1
                    FROM cartera.solicitudes_deudores sd
                    WHERE sd.id_solicitud_credito =
                          :idSolicitudCredito

                      AND sd.id_datos_personal =
                          :idDatosPersonal

                      AND sd.tipo_deudor =
                          'PRINCIPAL'

                      AND sd.orden_deudor = 1

                      AND sd.activo = true
                )
                """;

        Boolean existe =
                jdbc.queryForObject(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                )
                                .addValue(
                                        "idDatosPersonal",
                                        idDatosPersonal
                                ),
                        Boolean.class
                );

        return Boolean.TRUE.equals(existe);
    }


    public boolean existeInconsistenciaDeudorPrincipal(
            Integer idSolicitudCredito,
            Integer idDatosPersonal
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1
                    FROM cartera.solicitudes_deudores sd
                    WHERE sd.id_solicitud_credito =
                          :idSolicitudCredito

                      AND
                      (
                             sd.id_datos_personal =
                             :idDatosPersonal

                          OR sd.orden_deudor = 1

                          OR sd.tipo_deudor =
                             'PRINCIPAL'
                      )
                )
                """;

        Boolean existe =
                jdbc.queryForObject(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                )
                                .addValue(
                                        "idDatosPersonal",
                                        idDatosPersonal
                                ),
                        Boolean.class
                );

        return Boolean.TRUE.equals(existe);
    }


    public void insertarDeudorPrincipal(
            Integer idSolicitudCredito,
            Integer idDatosPersonal,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.solicitudes_deudores
                (
                    id_solicitud_credito,
                    id_datos_personal,
                    tipo_deudor,
                    orden_deudor,

                    activo,

                    fk_seguridad_creacion,
                    fecha_creacion,

                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES
                (
                    :idSolicitudCredito,
                    :idDatosPersonal,
                    'PRINCIPAL',
                    1,

                    true,

                    :idUsuario,
                    CURRENT_TIMESTAMP,

                    :idUsuario,
                    CURRENT_TIMESTAMP
                )
                """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
        );
    }


    // =========================================================
    // RETOMAR
    // =========================================================

    public LocalDateTime actualizarRetoma(
            Integer idSolicitudCredito,
            Integer idCuentaAportes,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.solicitudes_creditos AS sc
                SET
                    id_cuenta_aportes =
                        COALESCE(
                            sc.id_cuenta_aportes,
                            :idCuentaAportes
                        ),

                    fecha_ultima_gestion =
                        CURRENT_TIMESTAMP,

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                WHERE sc.id_solicitud_credito =
                      :idSolicitudCredito

                RETURNING
                    sc.fecha_ultima_gestion
                """;

        List<LocalDateTime> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                )
                                .addValue(
                                        "idCuentaAportes",
                                        idCuentaAportes
                                )
                                .addValue(
                                        "idUsuario",
                                        idUsuario
                                ),
                        (rs, rowNum) ->
                                rs.getTimestamp(
                                        "fecha_ultima_gestion"
                                ).toLocalDateTime()
                );

        return obtenerUnico(
                resultados,
                "actualizar la solicitud retomada"
        );
    }


    // =========================================================
    // CONSECUTIVO
    // =========================================================

    public Optional<BigDecimal> siguienteConsecutivo(
            Integer idAgencia,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE general.parametros AS p
                SET
                    valor_parametro =
                        COALESCE(
                            p.valor_parametro,
                            0
                        ) + 1,

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                WHERE p.id_agencia =
                      :idAgencia

                  AND p.codigo_parametro = 600

                RETURNING
                    p.valor_parametro
                """;

        List<BigDecimal> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idAgencia",
                                        idAgencia
                                )
                                .addValue(
                                        "idUsuario",
                                        idUsuario
                                ),
                        (rs, rowNum) ->
                                rs.getBigDecimal(
                                        "valor_parametro"
                                )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                resultados.get(0)
        );
    }


    // =========================================================
    // CREAR CABECERA
    // =========================================================

    public SolicitudCreadaDatos crearCabecera(
            String numeroSolicitud,
            Integer idAgencia,
            Integer idDatosPersonal,
            Integer idCuentaAportes,
            Integer idSolicitudProceso,
            Integer idSolicitudResultado,
            Integer idFondoGarantia,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO cartera.solicitudes_creditos
        (
            numero_solicitud,
            id_agencia,
            id_datos_personal,

            id_cuenta_aportes,

            id_fondo_garantia,

            fecha_inicio_solicitud,
            fecha_ultima_gestion,

            id_solicitud_proceso,
            id_solicitud_resultado,

            id_asesor,

            activo,

            fk_seguridad_creacion,
            fecha_creacion,

            fk_seguridad_edicion,
            fecha_edicion
        )
        VALUES
        (
            :numeroSolicitud,
            :idAgencia,
            :idDatosPersonal,

            :idCuentaAportes,

            :idFondoGarantia,

            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP,

            :idSolicitudProceso,
            :idSolicitudResultado,

            :idUsuario,

            true,

            :idUsuario,
            CURRENT_TIMESTAMP,

            :idUsuario,
            CURRENT_TIMESTAMP
        )

        RETURNING
            id_solicitud_credito,
            fecha_ultima_gestion
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "numeroSolicitud",
                                numeroSolicitud
                        )
                        .addValue(
                                "idAgencia",
                                idAgencia
                        )
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        )
                        .addValue(
                                "idCuentaAportes",
                                idCuentaAportes
                        )
                        .addValue(
                                "idFondoGarantia",
                                idFondoGarantia
                        )
                        .addValue(
                                "idSolicitudProceso",
                                idSolicitudProceso
                        )
                        .addValue(
                                "idSolicitudResultado",
                                idSolicitudResultado
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        ),
                (rs, rowNum) ->
                        new SolicitudCreadaDatos(
                                rs.getInt(
                                        "id_solicitud_credito"
                                ),
                                rs.getTimestamp(
                                        "fecha_ultima_gestion"
                                ).toLocalDateTime()
                        )
        );
    }

    // =========================================================
    // BLOQUEAR SOLICITUD PARA GUARDAR
    // =========================================================

    public Optional<SolicitudGuardarContexto>
    buscarContextoGuardar(
            Integer idSolicitudCredito
    ) {

        String sql = """
                SELECT
                    sc.id_solicitud_credito,
                    sc.numero_solicitud,

                    sc.id_agencia,
                    sc.id_datos_personal,

                    sc.id_cuenta_aportes,
                    sc.valor_aportes_inicio,

                    sr.es_final

                FROM cartera.solicitudes_creditos sc

                INNER JOIN cartera.solicitudes_resultados sr
                    ON sr.id_solicitud_resultado =
                       sc.id_solicitud_resultado

                WHERE sc.id_solicitud_credito =
                      :idSolicitudCredito

                  AND sc.activo = true

                FOR UPDATE OF sc
                """;

        List<SolicitudGuardarContexto> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                ),
                        (rs, rowNum) ->
                                new SolicitudGuardarContexto(
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
                                        rs.getBigDecimal(
                                                "valor_aportes_inicio"
                                        ),
                                        rs.getBoolean(
                                                "es_final"
                                        )
                                )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                resultados.get(0)
        );
    }


    // =========================================================
    // TIPO DE GARANTÍA
    // =========================================================

    public Optional<String> buscarTipoGarantia(
            String codigoGarantiaCredito
    ) {

        String sql = """
                SELECT
                    gc.tipo_garantia
                FROM cartera.garantias_creditos gc
                WHERE gc.codigo_garantia_credito =
                      TRIM(:codigoGarantiaCredito)

                  AND gc.activo = true
                """;

        List<String> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "codigoGarantiaCredito",
                                        codigoGarantiaCredito
                                ),
                        (rs, rowNum) ->
                                rs.getString(
                                        "tipo_garantia"
                                )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Existe más de una garantía activa con código "
                            + codigoGarantiaCredito
                            + "."
            );
        }

        return Optional.ofNullable(
                resultados.get(0)
        );
    }

    // =========================================================
// FONDO DE GARANTÍAS
// =========================================================

    public Optional<FondoGarantiaAplicable> buscarFondoGarantia(
            Integer idFondoGarantia
    ) {

        String sql = """
            SELECT
                fg.id_fondo_garantia,
                fg.codigo_fondo,
                fg.nombre_fondo,
                fg.porcentaje_fondo
            FROM cartera.fondos_garantias fg
            WHERE fg.id_fondo_garantia = :idFondoGarantia
              AND fg.activo = true
            """;

        List<FondoGarantiaAplicable> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idFondoGarantia",
                                        idFondoGarantia
                                ),
                        (rs, rowNum) ->
                                new FondoGarantiaAplicable(
                                        rs.getInt(
                                                "id_fondo_garantia"
                                        ),
                                        rs.getString(
                                                "codigo_fondo"
                                        ),
                                        rs.getString(
                                                "nombre_fondo"
                                        ),
                                        rs.getBigDecimal(
                                                "porcentaje_fondo"
                                        )
                                )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Existe más de un fondo de garantías activo con id "
                            + idFondoGarantia
                            + "."
            );
        }

        return Optional.of(
                resultados.get(0)
        );
    }


    // =========================================================
    // SMMLV
    // =========================================================

    public Optional<BigDecimal> buscarSmmlv(
            Integer idAgencia
    ) {

        String sql = """
                SELECT
                    gp.valor_parametro
                FROM general.parametros gp
                WHERE gp.id_agencia = :idAgencia
                  AND gp.codigo_parametro = 50
                """;

        List<BigDecimal> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idAgencia",
                                        idAgencia
                                ),
                        (rs, rowNum) ->
                                rs.getBigDecimal(
                                        "valor_parametro"
                                )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Existe más de un parámetro 50 para la agencia "
                            + idAgencia
                            + "."
            );
        }

        return Optional.ofNullable(
                resultados.get(0)
        );
    }

    // =========================================================
    // PREVISUALIZAR ENTE APROBADOR
    // =========================================================

    public Optional<SolicitudEnteAprobadorPreviewDTO>
    previsualizarEnteAprobador(
            Integer idDatosPersonal,
            String codigoGarantiaCredito,
            Integer plazoSolicitado,
            BigDecimal valorSolicitado,
            BigDecimal valorSmmlvAplicado,
            BigDecimal cantidadSmmlvSolicitada
    ) {

        String sql = """
        WITH base AS
        (
            SELECT
                :idDatosPersonal::integer
                    AS id_datos_personal,

                TRIM(:codigoGarantiaCredito)
                    AS codigo_garantia_credito,

                gc.descripcion_garantia_credito,
                gc.tipo_garantia,

                :plazoSolicitado::integer
                    AS plazo_solicitado,

                :valorSolicitado::numeric
                    AS valor_solicitado,

                :valorSmmlvAplicado::numeric
                    AS valor_smmlv_aplicado,

                :cantidadSmmlvSolicitada::numeric
                    AS cantidad_smmlv_solicitada

            FROM cartera.garantias_creditos gc

            WHERE gc.codigo_garantia_credito =
                  TRIM(:codigoGarantiaCredito)

              AND gc.activo = true
        ),

        directivo_actual AS
        (
            SELECT DISTINCT ON (d.id_datos_personal)
                d.id_datos_personal,
                d.id_directivo,
                d.codigo_tipo_directivo,
                td.nombre_tipo_directivo,
                d.calidad_directivo,

                CASE
                    WHEN d.calidad_directivo = '1'
                        THEN 'Principal'
                    WHEN d.calidad_directivo = '2'
                        THEN 'Suplente'
                    ELSE d.calidad_directivo
                END AS nombre_calidad_directivo

            FROM general.directivos d

            LEFT JOIN catalogos.tipos_directivos td
                ON td.codigo_tipo_directivo =
                   d.codigo_tipo_directivo

            WHERE d.estado_directivo = '1'
              AND d.fecha_retiro IS NULL

            ORDER BY
                d.id_datos_personal,
                d.id_directivo
        ),

        privilegiado_actual AS
        (
            SELECT DISTINCT ON (p.id_datos_personal)
                p.id_datos_personal,
                p.id_privilegiado,
                p.codigo_parentesco,
                pa.nombre_parentesco,

                d.id_directivo,

                dp.documento
                    AS documento_directivo,

                COALESCE(
                    NULLIF(
                        TRIM(dp.nombre_completo_nombres),
                        ''
                    ),
                    NULLIF(
                        TRIM(
                            concat_ws(
                                ' ',
                                dp.nombres,
                                dp.primer_apellido,
                                dp.segundo_apellido
                            )
                        ),
                        ''
                    )
                ) AS nombre_directivo,

                d.codigo_tipo_directivo,
                td.nombre_tipo_directivo,
                d.calidad_directivo,

                CASE
                    WHEN d.calidad_directivo = '1'
                        THEN 'Principal'
                    WHEN d.calidad_directivo = '2'
                        THEN 'Suplente'
                    ELSE d.calidad_directivo
                END AS nombre_calidad_directivo

            FROM general.privilegiados p

            INNER JOIN general.directivos d
                ON d.id_directivo =
                   p.id_directivo

            LEFT JOIN catalogos.parentescos pa
                ON pa.codigo_parentesco =
                   p.codigo_parentesco

            LEFT JOIN catalogos.tipos_directivos td
                ON td.codigo_tipo_directivo =
                   d.codigo_tipo_directivo

            LEFT JOIN reporting.vw_datos_personales_operativa dp
                ON dp.id_datos_personal =
                   d.id_datos_personal

            WHERE d.estado_directivo = '1'
              AND d.fecha_retiro IS NULL

            ORDER BY
                p.id_datos_personal,
                p.id_privilegiado
        ),

        resultado AS
        (
            SELECT
                b.*,

                da.id_directivo IS NOT NULL
                    AS es_directivo,

                pa.id_privilegiado IS NOT NULL
                    AS es_privilegiado,

                da.nombre_tipo_directivo
                    AS nombre_tipo_directivo_asociado,

                da.nombre_calidad_directivo
                    AS nombre_calidad_directivo_asociado,

                pa.nombre_parentesco,
                pa.documento_directivo,
                pa.nombre_directivo,

                pa.nombre_tipo_directivo
                    AS nombre_tipo_directivo_relacionado,

                pa.nombre_calidad_directivo
                    AS nombre_calidad_directivo_relacionado,

                CASE
                    WHEN da.id_directivo IS NOT NULL
                        THEN 3

                    WHEN pa.id_privilegiado IS NOT NULL
                        THEN 3

                    ELSE regla.id_ente_aprobacion
                END AS id_ente_aprobacion,

                CASE
                    WHEN da.id_directivo IS NOT NULL
                        THEN 'DIRECTIVO'

                    WHEN pa.id_privilegiado IS NOT NULL
                        THEN 'PRIVILEGIADO'

                    ELSE 'CUANTIA'
                END AS motivo_aprobacion,

                CASE
                    WHEN da.id_directivo IS NOT NULL
                      OR pa.id_privilegiado IS NOT NULL
                        THEN NULL

                    ELSE regla.valor_minimo_smmlv
                END AS valor_minimo_smmlv,

                CASE
                    WHEN da.id_directivo IS NOT NULL
                      OR pa.id_privilegiado IS NOT NULL
                        THEN NULL

                    ELSE regla.valor_maximo_smmlv
                END AS valor_tope_smmlv

            FROM base b

            LEFT JOIN directivo_actual da
                ON da.id_datos_personal =
                   b.id_datos_personal

            LEFT JOIN privilegiado_actual pa
                ON pa.id_datos_personal =
                   b.id_datos_personal

            LEFT JOIN LATERAL
            (
                SELECT
                    r.id_ente_aprobacion,
                    r.valor_minimo_smmlv,
                    r.valor_maximo_smmlv

                FROM cartera.entes_aprobacion_reglas r

                WHERE r.activo = true
                  AND r.privilegiado = false

                  AND r.tipo_garantia =
                      b.tipo_garantia

                  AND b.cantidad_smmlv_solicitada
                      BETWEEN r.valor_minimo_smmlv
                          AND r.valor_maximo_smmlv

                  AND
                  (
                      r.plazo_minimo IS NULL
                      OR b.plazo_solicitado >=
                         r.plazo_minimo
                  )

                  AND
                  (
                      r.plazo_maximo IS NULL
                      OR b.plazo_solicitado <=
                         r.plazo_maximo
                  )

                ORDER BY
                    r.id_ente_aprobacion,
                    r.id_ente_aprobacion_regla

                LIMIT 1

            ) regla ON true
        )

        SELECT
            r.id_ente_aprobacion,
            ea.nombre_ente_aprobacion,

            r.motivo_aprobacion,

            r.valor_solicitado,
            r.valor_smmlv_aplicado,
            r.cantidad_smmlv_solicitada,

            r.codigo_garantia_credito,
            r.descripcion_garantia_credito,
            r.tipo_garantia,

            CASE
                WHEN r.tipo_garantia = 'P'
                    THEN 'Personal'
                WHEN r.tipo_garantia = 'R'
                    THEN 'Real'
                ELSE r.tipo_garantia
            END AS nombre_tipo_garantia,

            r.plazo_solicitado,

            r.es_directivo,
            r.es_privilegiado,

            CASE
                WHEN r.es_directivo
                    THEN r.nombre_tipo_directivo_asociado
                WHEN r.es_privilegiado
                    THEN r.nombre_tipo_directivo_relacionado
                ELSE NULL
            END AS nombre_tipo_directivo,

            CASE
                WHEN r.es_directivo
                    THEN r.nombre_calidad_directivo_asociado
                WHEN r.es_privilegiado
                    THEN r.nombre_calidad_directivo_relacionado
                ELSE NULL
            END AS nombre_calidad_directivo,

            r.nombre_parentesco,
            r.documento_directivo,
            r.nombre_directivo,

            r.valor_minimo_smmlv,
            r.valor_tope_smmlv,

            CASE
                WHEN r.valor_tope_smmlv IS NOT NULL
                 AND r.valor_smmlv_aplicado IS NOT NULL
                    THEN ROUND(
                        r.valor_tope_smmlv *
                        r.valor_smmlv_aplicado,
                        0
                    )
                ELSE NULL
            END AS valor_tope_pesos,

            CASE
                WHEN r.id_ente_aprobacion IS NULL
                    THEN
                        'No existe una regla de aprobación aplicable'

                WHEN r.es_directivo
                    THEN
                        'El asociado es directivo'

                WHEN r.es_privilegiado
                    THEN
                        'El asociado es '
                        || COALESCE(
                            LOWER(r.nombre_parentesco),
                            'persona relacionada'
                        )
                        || ' del directivo '
                        || COALESCE(
                            r.nombre_directivo,
                            'sin identificar'
                        )

                ELSE
                    'Ente determinado por cuantía y tipo de garantía'
            END AS mensaje_aprobacion,

            CASE
                WHEN r.es_directivo
                    THEN concat_ws(
                        ' - ',
                        r.nombre_tipo_directivo_asociado,
                        r.nombre_calidad_directivo_asociado
                    )

                WHEN r.es_privilegiado
                    THEN concat_ws(
                        ' - ',
                        r.nombre_tipo_directivo_relacionado,
                        r.nombre_calidad_directivo_relacionado
                    )

                WHEN r.id_ente_aprobacion IS NOT NULL
                    THEN
                        'Cantidad solicitada: '
                        || ROUND(
                            r.cantidad_smmlv_solicitada,
                            2
                        )
                        || ' SMMLV'
                        || ' | Tope reglamentario: '
                        || ROUND(
                            r.valor_tope_smmlv,
                            2
                        )
                        || ' SMMLV'
                        || ' | Tipo de garantía: '
                        || CASE
                               WHEN r.tipo_garantia = 'P'
                                   THEN 'Personal'
                               WHEN r.tipo_garantia = 'R'
                                   THEN 'Real'
                               ELSE COALESCE(
                                   r.tipo_garantia,
                                   'Sin definir'
                               )
                           END

                ELSE NULL
            END AS detalle_aprobacion,

            CASE
                WHEN r.es_directivo
                    THEN
                        'Ente aprobador: '
                        || COALESCE(
                            ea.nombre_ente_aprobacion,
                            'Consejo de administración'
                        )
                        || '. El asociado es directivo activo de la entidad'
                        || CASE
                               WHEN r.nombre_tipo_directivo_asociado IS NOT NULL
                                 OR r.nombre_calidad_directivo_asociado IS NOT NULL
                                   THEN
                                       ', con calidad de '
                                       || concat_ws(
                                           ' - ',
                                           r.nombre_tipo_directivo_asociado,
                                           r.nombre_calidad_directivo_asociado
                                       )
                               ELSE ''
                           END
                        || '. Por su condición de directivo, la solicitud debe ser aprobada por '
                        || COALESCE(
                            ea.nombre_ente_aprobacion,
                            'Consejo de administración'
                        )
                        || ', independientemente de la cuantía.'

                WHEN r.es_privilegiado
                    THEN
                        'Ente aprobador: '
                        || COALESCE(
                            ea.nombre_ente_aprobacion,
                            'Consejo de administración'
                        )
                        || '. El asociado es '
                        || COALESCE(
                            LOWER(r.nombre_parentesco),
                            'persona relacionada'
                        )
                        || ' del directivo '
                        || COALESCE(
                            r.nombre_directivo,
                            'sin identificar'
                        )
                        || CASE
                               WHEN r.nombre_tipo_directivo_relacionado IS NOT NULL
                                 OR r.nombre_calidad_directivo_relacionado IS NOT NULL
                                   THEN
                                       ', '
                                       || concat_ws(
                                           ' - ',
                                           r.nombre_tipo_directivo_relacionado,
                                           r.nombre_calidad_directivo_relacionado
                                       )
                               ELSE ''
                           END
                        || '. Por su relación con un directivo activo, la solicitud debe ser aprobada por '
                        || COALESCE(
                            ea.nombre_ente_aprobacion,
                            'Consejo de administración'
                        )
                        || ', independientemente de la cuantía.'

                WHEN r.id_ente_aprobacion IS NOT NULL
                    THEN
                        'Ente aprobador: '
                        || ea.nombre_ente_aprobacion
                        || '. Valor solicitado: $'
                        || to_char(
                            r.valor_solicitado,
                            'FM999G999G999G999G990'
                        )
                        || ', equivalente a '
                        || to_char(
                            r.cantidad_smmlv_solicitada,
                            'FM999G999G990D00'
                        )
                        || ' SMMLV'
                        || '. Garantía: '
                        || CASE
                               WHEN r.tipo_garantia = 'P'
                                   THEN 'Personal'
                               WHEN r.tipo_garantia = 'R'
                                   THEN 'Real'
                               ELSE COALESCE(
                                   r.tipo_garantia,
                                   'Sin definir'
                               )
                           END
                        || '. Tope reglamentario de '
                        || ea.nombre_ente_aprobacion
                        || ' para garantía '
                        || CASE
                               WHEN r.tipo_garantia = 'P'
                                   THEN 'Personal'
                               WHEN r.tipo_garantia = 'R'
                                   THEN 'Real'
                               ELSE COALESCE(
                                   r.tipo_garantia,
                                   'Sin definir'
                               )
                           END
                        || ': '
                        || to_char(
                            r.valor_tope_smmlv,
                            'FM999G999G990D00'
                        )
                        || ' SMMLV'
                        || ', equivalente a $'
                        || to_char(
                            ROUND(
                                r.valor_tope_smmlv *
                                r.valor_smmlv_aplicado,
                                0
                            ),
                            'FM999G999G999G999G990'
                        )
                        || '. La solicitud se encuentra dentro del límite de aprobación de '
                        || ea.nombre_ente_aprobacion
                        || '.'

                ELSE
                    'No fue posible determinar el ente aprobador para la solicitud.'
            END AS justificacion_ente_aprobacion

        FROM resultado r

        LEFT JOIN cartera.entes_aprobacion ea
            ON ea.id_ente_aprobacion =
               r.id_ente_aprobacion

           AND ea.activo = true
        """;

        List<SolicitudEnteAprobadorPreviewDTO> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idDatosPersonal",
                                        idDatosPersonal
                                )
                                .addValue(
                                        "codigoGarantiaCredito",
                                        codigoGarantiaCredito
                                )
                                .addValue(
                                        "plazoSolicitado",
                                        plazoSolicitado
                                )
                                .addValue(
                                        "valorSolicitado",
                                        valorSolicitado
                                )
                                .addValue(
                                        "valorSmmlvAplicado",
                                        valorSmmlvAplicado
                                )
                                .addValue(
                                        "cantidadSmmlvSolicitada",
                                        cantidadSmmlvSolicitada
                                ),
                        BeanPropertyRowMapper.newInstance(
                                SolicitudEnteAprobadorPreviewDTO.class
                        )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Se encontró más de un resultado al previsualizar "
                            + "el ente aprobador."
            );
        }

        return Optional.of(
                resultados.get(0)
        );
    }


    // =========================================================
    // SALDO ACTUAL APORTES
    // =========================================================

    public Optional<BigDecimal> buscarSaldoAportes(
            Integer idCuentaAportes
    ) {

        if (idCuentaAportes == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT
                    ca.saldo_actual_cuenta
                FROM depositos.cuentas_ahorro ca
                WHERE ca.id_cuenta_ahorro =
                      :idCuentaAportes
                """;

        List<BigDecimal> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idCuentaAportes",
                                        idCuentaAportes
                                ),
                        (rs, rowNum) ->
                                rs.getBigDecimal(
                                        "saldo_actual_cuenta"
                                )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                resultados.get(0)
        );
    }

    // =========================================================
// SALDO ACTUAL DE CARTERA DEL ASOCIADO
// =========================================================

    public BigDecimal buscarSaldoActualCartera(
            Integer idDatosPersonal
    ) {

        String sql = """
            SELECT
                COALESCE(
                    SUM(cc.saldo_actual),
                    0
                ) AS saldo_actual_cartera

            FROM cartera.carteras_creditos cc

            WHERE cc.id_datos_personal =
                  :idDatosPersonal

              AND cc.saldo_actual > 0
            """;

        BigDecimal saldo =
                jdbc.queryForObject(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idDatosPersonal",
                                        idDatosPersonal
                                ),
                        BigDecimal.class
                );

        return saldo != null
                ? saldo
                : BigDecimal.ZERO;
    }


    // =========================================================
    // CONDICIÓN INICIAL
    // =========================================================

    public List<CondicionInicialAplicable>
    buscarCondicionesAplicables(
            Integer idLineaCredito,
            String tipoGarantia,
            String codigoFormaPago,
            Integer plazoSolicitado,
            BigDecimal cantidadSmmlv
    ) {

        String sql = """
                SELECT
                    ci.id_condicion_inicial,
                    cid.id_condicion_inicial_detalle,

                    cid.plazo_minimo,
                    cid.plazo_maximo,

                    cid.cantidad_smmlv_minimo,
                    cid.cantidad_smmlv_maximo,

                    cid.factor_reciprocidad_aportes

                FROM cartera.condiciones_iniciales ci

                INNER JOIN cartera.condiciones_iniciales_detalle cid
                    ON cid.id_condicion_inicial =
                       ci.id_condicion_inicial

                WHERE ci.activo = true
                  AND cid.activo = true

                  AND CURRENT_DATE >=
                      ci.fecha_inicial

                  AND
                  (
                      ci.fecha_final IS NULL
                      OR CURRENT_DATE <= ci.fecha_final
                  )

                  AND cid.id_linea_credito =
                      :idLineaCredito

                  AND cid.tipo_garantia =
                      :tipoGarantia

                  AND TRIM(cid.codigo_forma_pago) =
                      TRIM(:codigoFormaPago)

                  AND :plazoSolicitado BETWEEN
                      cid.plazo_minimo
                      AND cid.plazo_maximo

                  AND :cantidadSmmlv BETWEEN
                      cid.cantidad_smmlv_minimo
                      AND cid.cantidad_smmlv_maximo

                ORDER BY
                    ci.id_condicion_inicial,
                    cid.id_condicion_inicial_detalle
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idLineaCredito",
                                idLineaCredito
                        )
                        .addValue(
                                "tipoGarantia",
                                tipoGarantia
                        )
                        .addValue(
                                "codigoFormaPago",
                                codigoFormaPago
                        )
                        .addValue(
                                "plazoSolicitado",
                                plazoSolicitado
                        )
                        .addValue(
                                "cantidadSmmlv",
                                cantidadSmmlv
                        ),
                (rs, rowNum) ->
                        new CondicionInicialAplicable(
                                rs.getInt(
                                        "id_condicion_inicial"
                                ),
                                rs.getInt(
                                        "id_condicion_inicial_detalle"
                                ),
                                rs.getInt(
                                        "plazo_minimo"
                                ),
                                rs.getInt(
                                        "plazo_maximo"
                                ),
                                rs.getBigDecimal(
                                        "cantidad_smmlv_minimo"
                                ),
                                rs.getBigDecimal(
                                        "cantidad_smmlv_maximo"
                                ),
                                rs.getBigDecimal(
                                        "factor_reciprocidad_aportes"
                                )
                        )
        );
    }


    // =========================================================
    // TASA DE COLOCACIÓN
    // =========================================================

    public List<TasaAplicable> buscarTasasAplicables(
            Integer idLineaCredito,
            String tipoGarantia,
            Integer amortizacionCapital,
            Integer plazoSolicitado
    ) {

        String sql = """
                SELECT
                    tcd.id_tasa_colocacion_detalle,
                    tcd.tasa_colocacion

                FROM cartera.tasas_colocacion tc

                INNER JOIN cartera.tasas_colocacion_detalle tcd
                    ON tcd.id_tasa_colocacion =
                       tc.id_tasa_colocacion

                WHERE tc.activo = true
                  AND tcd.activo = true

                  AND CURRENT_DATE >= tc.fecha_inicial

                  AND
                  (
                      tc.fecha_final IS NULL
                      OR CURRENT_DATE <= tc.fecha_final
                  )

                  AND tcd.id_linea_credito =
                      :idLineaCredito

                  AND tcd.tipo_garantia =
                      :tipoGarantia

                  AND tcd.amortizacion_meses =
                      :amortizacionCapital

                  AND
                  (
                      tcd.evalua_plazo = false

                      OR
                      (
                          tcd.evalua_plazo = true

                          AND :plazoSolicitado BETWEEN
                              tcd.plazo_minimo
                              AND tcd.plazo_maximo
                      )
                  )

                ORDER BY
                    tcd.id_tasa_colocacion_detalle
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idLineaCredito",
                                idLineaCredito
                        )
                        .addValue(
                                "tipoGarantia",
                                tipoGarantia
                        )
                        .addValue(
                                "amortizacionCapital",
                                amortizacionCapital
                        )
                        .addValue(
                                "plazoSolicitado",
                                plazoSolicitado
                        ),
                (rs, rowNum) ->
                        new TasaAplicable(
                                rs.getInt(
                                        "id_tasa_colocacion_detalle"
                                ),
                                rs.getBigDecimal(
                                        "tasa_colocacion"
                                )
                        )
        );
    }


    // =========================================================
    // MODALIDAD DE INTERESES
    // =========================================================

    public Optional<Integer> buscarPeriodoMeses(
            String periodoCodigoInteres,
            String tipoModalidadInteres
    ) {

        String sql = """
                SELECT
                    mi.periodo_meses
                FROM cartera.modalidades_intereses mi
                WHERE TRIM(mi.periodo_codigo) =
                      TRIM(:periodoCodigoInteres)

                  AND TRIM(mi.tipo_modalidad) =
                      TRIM(:tipoModalidadInteres)

                  AND mi.activo = true
                """;

        List<Integer> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "periodoCodigoInteres",
                                        periodoCodigoInteres
                                )
                                .addValue(
                                        "tipoModalidadInteres",
                                        tipoModalidadInteres
                                ),
                        (rs, rowNum) ->
                                rs.getObject(
                                        "periodo_meses",
                                        Integer.class
                                )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Existe más de una modalidad de intereses activa para "
                            + periodoCodigoInteres
                            + "/"
                            + tipoModalidadInteres
                            + "."
            );
        }

        return Optional.ofNullable(
                resultados.get(0)
        );
    }


    // =========================================================
    // GUARDAR DATOS DEL CRÉDITO
    // =========================================================

    public SolicitudCreditoGuardarResponseDTO guardarCredito(
            SolicitudCreditoGuardarRequestDTO request,
            PersistenciaCredito calculo,
            Integer idUsuario
    ) {

        String sql = """
                WITH actualizado AS
                (
                    UPDATE cartera.solicitudes_creditos AS sc
                    SET
                        id_linea_credito =
                            :idLineaCredito,

                        codigo_clasificacion_credito =
                            TRIM(:codigoClasificacionCredito),

                        codigo_destino_economico =
                            TRIM(:codigoDestinoEconomico),

                        codigo_garantia_credito =
                            TRIM(:codigoGarantiaCredito),

                        codigo_subgarantia =
                            NULLIF(
                                TRIM(:codigoSubgarantia),
                                ''
                            ),

                        codigo_forma_pago =
                            TRIM(:codigoFormaPago),

                        periodo_codigo_interes =
                            TRIM(:periodoCodigoInteres),

                        tipo_modalidad_interes =
                            TRIM(:tipoModalidadInteres),

                        amortizacion_capital =
                            :amortizacionCapital,

                        codigo_tipo_cuota =
                            TRIM(:codigoTipoCuota),

                        plazo_solicitado =
                            :plazoSolicitado,

                        meses_gracia_capital =
                            COALESCE(
                                :mesesGraciaCapital,
                                0
                            ),

                        meses_gracia_interes =
                            COALESCE(
                                :mesesGraciaInteres,
                                0
                            ),

                        valor_solicitado =
                            :valorSolicitado,
                            
                        id_fondo_garantia =
                            :idFondoGarantia,
                
                        porcentaje_fondo_aplicado =
                            :porcentajeFondoAplicado,
                
                        forma_cobro_fondo =
                            :formaCobroFondo,
                
                        valor_fondo_garantia =
                            :valorFondoGarantia,

                        id_empresa_libranza =
                            :idEmpresaLibranza,


                        id_condicion_inicial =
                            :idCondicionInicial,

                        id_condicion_inicial_detalle =
                            :idCondicionInicialDetalle,

                        plazo_minimo_aplicado =
                            :plazoMinimoAplicado,

                        plazo_maximo_aplicado =
                            :plazoMaximoAplicado,

                        cantidad_smmlv_minimo_aplicada =
                            :cantidadSmmlvMinimoAplicada,

                        cantidad_smmlv_maximo_aplicada =
                            :cantidadSmmlvMaximoAplicada,

                        factor_reciprocidad_aportes_aplicado =
                            :factorReciprocidadAportesAplicado,

                        valor_smmlv_aplicado =
                            :valorSmmlvAplicado,

                        cantidad_smmlv_solicitada =
                            :cantidadSmmlvSolicitada,


                        valor_aportes_inicio =
                            :valorAportesInicio,

                        cumple_aportes_inicio =
                            :cumpleAportesInicio,

                        cupo_maximo_por_aportes =
                            :cupoMaximoPorAportes,

                        valor_aportes_requerido =
                            :valorAportesRequerido,


                        id_tasa_colocacion_detalle =
                            :idTasaColocacionDetalle,

                        tasa_colocacion_aplicada =
                            :tasaColocacionAplicada,

                        tasa_efectiva_anual =
                            :tasaEfectivaAnual,

                        valor_cuota_proyectada =
                            :valorCuotaProyectada,

                        valor_primera_cuota_analisis =
                            :valorPrimeraCuotaAnalisis,

                        observacion_asesor =
                            NULLIF(
                                TRIM(:observacionAsesor),
                                ''
                            ),

                        fecha_ultima_gestion =
                            CURRENT_TIMESTAMP,

                        fk_seguridad_edicion =
                            :idUsuario,

                        fecha_edicion =
                            CURRENT_TIMESTAMP

                    WHERE sc.id_solicitud_credito =
                          :idSolicitudCredito

                    RETURNING
                        sc.id_solicitud_credito,
                        sc.numero_solicitud,
                        sc.fecha_ultima_gestion
                )

                SELECT
                    a.id_solicitud_credito,
                    a.numero_solicitud,

                    CAST(
                        :idCondicionInicial
                        AS integer
                    ) AS id_condicion_inicial,

                    CAST(
                        :idCondicionInicialDetalle
                        AS integer
                    ) AS id_condicion_inicial_detalle,

                    (
                        CAST(
                            :idCondicionInicialDetalle
                            AS integer
                        ) IS NOT NULL
                    ) AS condicion_encontrada,

                    CAST(
                        :plazoMinimoAplicado
                        AS integer
                    ) AS plazo_minimo_aplicado,

                    CAST(
                        :plazoMaximoAplicado
                        AS integer
                    ) AS plazo_maximo_aplicado,

                    CAST(
                        :cantidadSmmlvMinimoAplicada
                        AS numeric
                    ) AS cantidad_smmlv_minimo_aplicada,

                    CAST(
                        :cantidadSmmlvMaximoAplicada
                        AS numeric
                    ) AS cantidad_smmlv_maximo_aplicada,

                    CAST(
                        :valorSmmlvAplicado
                        AS numeric
                    ) AS valor_smmlv_aplicado,

                    CAST(
                        :cantidadSmmlvSolicitada
                        AS numeric
                    ) AS cantidad_smmlv_solicitada,

                    CAST(
                        :factorReciprocidadAportesAplicado
                        AS numeric
                    ) AS factor_reciprocidad_aportes_aplicado,

                    CAST(
                        :valorAportesInicio
                        AS numeric
                    ) AS valor_aportes_inicio,

                    CAST(
                        :cupoMaximoPorAportes
                        AS numeric
                    ) AS cupo_maximo_por_aportes,

                    CAST(
                        :valorAportesRequerido
                        AS numeric
                    ) AS valor_aportes_requerido,

                    CAST(
                        :cumpleAportesInicio
                        AS boolean
                    ) AS cumple_aportes_inicio,

                    CAST(
                        :idTasaColocacionDetalle
                        AS integer
                    ) AS id_tasa_colocacion_detalle,

                    CAST(
                        :tasaColocacionAplicada
                        AS numeric
                    ) AS tasa_colocacion_aplicada,

                    (
                        CAST(
                            :idTasaColocacionDetalle
                            AS integer
                        ) IS NOT NULL
                    ) AS tasa_encontrada,
                    
                    CAST(
                        :tasaEfectivaAnual
                        AS numeric
                    ) AS tasa_efectiva_anual,
                
                    CAST(
                        :valorCuotaProyectada
                        AS numeric
                    ) AS valor_cuota_proyectada,
                
                    a.fecha_ultima_gestion

                FROM actualizado a
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()

                        .addValue(
                                "idSolicitudCredito",
                                request.getIdSolicitudCredito()
                        )
                        .addValue(
                                "idLineaCredito",
                                request.getIdLineaCredito()
                        )
                        .addValue(
                                "codigoClasificacionCredito",
                                request.getCodigoClasificacionCredito()
                        )
                        .addValue(
                                "codigoDestinoEconomico",
                                request.getCodigoDestinoEconomico()
                        )
                        .addValue(
                                "codigoGarantiaCredito",
                                request.getCodigoGarantiaCredito()
                        )
                        .addValue(
                                "codigoSubgarantia",
                                request.getCodigoSubgarantia()
                        )
                        .addValue(
                                "codigoFormaPago",
                                request.getCodigoFormaPago()
                        )
                        .addValue(
                                "periodoCodigoInteres",
                                request.getPeriodoCodigoInteres()
                        )
                        .addValue(
                                "tipoModalidadInteres",
                                request.getTipoModalidadInteres()
                        )
                        .addValue(
                                "amortizacionCapital",
                                request.getAmortizacionCapital()
                        )
                        .addValue(
                                "codigoTipoCuota",
                                request.getCodigoTipoCuota()
                        )
                        .addValue(
                                "plazoSolicitado",
                                request.getPlazoSolicitado()
                        )
                        .addValue(
                                "mesesGraciaCapital",
                                request.getMesesGraciaCapital()
                        )
                        .addValue(
                                "mesesGraciaInteres",
                                request.getMesesGraciaInteres()
                        )
                        .addValue(
                                "valorSolicitado",
                                request.getValorSolicitado()
                        )
                        .addValue(
                                "idFondoGarantia",
                                calculo.idFondoGarantia()
                        )
                        .addValue(
                                "porcentajeFondoAplicado",
                                calculo.porcentajeFondoAplicado()
                        )
                        .addValue(
                                "formaCobroFondo",
                                calculo.formaCobroFondo()
                        )
                        .addValue(
                                "valorFondoGarantia",
                                calculo.valorFondoGarantia()
                        )
                        .addValue(
                                "idEmpresaLibranza",
                                request.getIdEmpresaLibranza()
                        )
                        .addValue(
                                "observacionAsesor",
                                request.getObservacionAsesor()
                        )

                        .addValue(
                                "idCondicionInicial",
                                calculo.idCondicionInicial()
                        )
                        .addValue(
                                "idCondicionInicialDetalle",
                                calculo.idCondicionInicialDetalle()
                        )
                        .addValue(
                                "plazoMinimoAplicado",
                                calculo.plazoMinimoAplicado()
                        )
                        .addValue(
                                "plazoMaximoAplicado",
                                calculo.plazoMaximoAplicado()
                        )
                        .addValue(
                                "cantidadSmmlvMinimoAplicada",
                                calculo.cantidadSmmlvMinimoAplicada()
                        )
                        .addValue(
                                "cantidadSmmlvMaximoAplicada",
                                calculo.cantidadSmmlvMaximoAplicada()
                        )
                        .addValue(
                                "factorReciprocidadAportesAplicado",
                                calculo.factorReciprocidadAportesAplicado()
                        )
                        .addValue(
                                "valorSmmlvAplicado",
                                calculo.valorSmmlvAplicado()
                        )
                        .addValue(
                                "cantidadSmmlvSolicitada",
                                calculo.cantidadSmmlvSolicitada()
                        )
                        .addValue(
                                "valorAportesInicio",
                                calculo.valorAportesInicio()
                        )
                        .addValue(
                                "cumpleAportesInicio",
                                calculo.cumpleAportesInicio()
                        )
                        .addValue(
                                "cupoMaximoPorAportes",
                                calculo.cupoMaximoPorAportes()
                        )
                        .addValue(
                                "valorAportesRequerido",
                                calculo.valorAportesRequerido()
                        )
                        .addValue(
                                "idTasaColocacionDetalle",
                                calculo.idTasaColocacionDetalle()
                        )
                        .addValue(
                                "tasaColocacionAplicada",
                                calculo.tasaColocacionAplicada()
                        )
                        .addValue(
                                "tasaEfectivaAnual",
                                calculo.tasaEfectivaAnual()
                        )
                        .addValue(
                                "valorCuotaProyectada",
                                calculo.valorCuotaProyectada()
                        )
                        .addValue(
                                "valorPrimeraCuotaAnalisis",
                                calculo.valorPrimeraCuotaAnalisis()
                        )

                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        List<SolicitudCreditoGuardarResponseDTO> resultados =
                jdbc.query(
                        sql,
                        parametros,
                        GUARDAR_CREDITO_MAPPER
                );

        return obtenerUnico(
                resultados,
                "guardar los datos del crédito"
        );
    }

    // =========================================================
// FOTOGRAFIAR ENTE APROBADOR
// =========================================================

    public void fotografiarEnteAprobador(
            Integer idSolicitudCredito,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.solicitudes_creditos sc
            SET
                id_ente_aprobacion =
                    ea.id_ente_aprobacion,

                motivo_aprobacion =
                    ea.motivo_aprobacion,

                valor_tope_smmlv_aplicado =
                    ea.valor_tope_smmlv,

                valor_tope_pesos_aplicado =
                    ea.valor_tope_pesos,

                justificacion_ente_aprobacion =
                    ea.justificacion_ente_aprobacion,

                fk_seguridad_edicion =
                    :idUsuario,

                fecha_edicion =
                    CURRENT_TIMESTAMP

            FROM cartera.vw_solicitudes_ente_aprobacion ea

            WHERE ea.id_solicitud_credito =
                  :idSolicitudCredito

              AND sc.id_solicitud_credito =
                  ea.id_solicitud_credito

              AND sc.activo = true
            """;

        int actualizados =
                jdbc.update(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                )
                                .addValue(
                                        "idUsuario",
                                        idUsuario
                                )
                );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No fue posible fotografiar el ente aprobador "
                            + "de la solicitud "
                            + idSolicitudCredito
                            + ". Registros actualizados: "
                            + actualizados
                            + "."
            );
        }
    }


    // =========================================================
    // LISTAR
    // =========================================================

    public List<SolicitudCreditoResumenDTO> listar() {

        String sql = """
                SELECT
                    v.id_solicitud_credito,
                    v.numero_solicitud,

                    v.id_agencia,
                    v.nombre_agencia,

                    v.id_datos_personal,
                    v.tipo_documento,
                    v.documento,
                    v.nombre_completo,

                    v.id_linea_credito,
                    v.nombre_linea_credito,

                    v.valor_solicitado,
                    v.plazo_solicitado,

                    v.id_solicitud_proceso,
                    v.nombre_proceso,

                    v.id_solicitud_resultado,
                    v.nombre_resultado,
                    v.resultado_final,

                    v.fecha_inicio_solicitud,
                    v.fecha_ultima_gestion,

                    v.activo

                FROM cartera.vw_solicitudes_creditos v

                WHERE v.activo = true

                ORDER BY
                    v.fecha_ultima_gestion DESC,
                    v.id_solicitud_credito DESC
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                RESUMEN_MAPPER
        );
    }


    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<SolicitudCreditoDetalleDTO> buscarPorId(
            Integer idSolicitudCredito
    ) {

        String sql = """
            SELECT
                v.id_solicitud_credito,
                v.numero_solicitud,

                v.fecha_inicio_solicitud,
                v.fecha_ultima_gestion,

                v.id_agencia,
                v.codigo_agencia,
                v.nombre_agencia,

                v.id_datos_personal,
                v.tipo_documento,
                v.documento,
                v.nombre_completo,

                v.id_cuenta_aportes,

                v.id_linea_credito,
                v.nombre_linea_credito,

                v.codigo_clasificacion_credito,
                v.nombre_clasificacion_credito,

                v.codigo_destino_economico,
                v.nombre_destino_economico,

                v.codigo_garantia_credito,
                v.nombre_garantia_credito,

                v.codigo_subgarantia,
                v.nombre_subgarantia,
                
                -- =====================================================
                -- FONDO DE GARANTÍAS
                -- =====================================================

                v.id_fondo_garantia,
                v.codigo_fondo_garantia,
                v.nombre_fondo_garantia,
                v.porcentaje_fondo_aplicado,
                v.forma_cobro_fondo,
                v.valor_fondo_garantia,

                v.codigo_forma_pago,
                v.nombre_forma_pago,

                v.periodo_codigo_interes,
                v.tipo_modalidad_interes,
                v.nombre_modalidad_interes,

                v.amortizacion_capital,

                v.codigo_tipo_cuota,
                v.nombre_tipo_cuota,

                v.plazo_solicitado,
                v.meses_gracia_capital,
                v.meses_gracia_interes,

                v.valor_solicitado,

                v.id_empresa_libranza,
                v.nombre_empresa_libranza,

                v.id_condicion_inicial,
                v.id_condicion_inicial_detalle,

                v.plazo_minimo_aplicado,
                v.plazo_maximo_aplicado,

                v.cantidad_smmlv_minimo_aplicada,
                v.cantidad_smmlv_maximo_aplicada,

                v.factor_reciprocidad_aportes_aplicado,

                v.valor_smmlv_aplicado,
                v.cantidad_smmlv_solicitada,

                v.valor_aportes_inicio,
                v.cumple_aportes_inicio,

                v.valor_aportes_validacion,
                v.cumple_aportes_validacion,

                v.cupo_maximo_por_aportes,
                v.valor_aportes_requerido,

                v.id_tasa_colocacion_detalle,
                v.tasa_colocacion_aplicada,
                v.tasa_efectiva_anual,
                v.valor_cuota_proyectada,
                sc.valor_primera_cuota_analisis,

                -- =====================================================
                -- ENTE APROBADOR
                -- =====================================================

                ea.id_ente_aprobacion,
                ea.nombre_ente_aprobacion,
                ea.motivo_aprobacion,

                ea.es_directivo,
                ea.es_privilegiado,


                -- =====================================================
                -- DIRECTIVO
                -- =====================================================

                ea.nombre_tipo_directivo_asociado,
                ea.nombre_calidad_directivo_asociado,


                -- =====================================================
                -- RELACIÓN PRIVILEGIADA
                -- =====================================================

                ea.nombre_parentesco,

                ea.id_datos_personal_directivo,
                ea.documento_directivo,
                ea.nombre_directivo,

                ea.nombre_tipo_directivo_relacionado,
                ea.nombre_calidad_directivo_relacionado,


                -- =====================================================
                -- TOPE DE APROBACIÓN
                -- =====================================================

                ea.valor_tope_smmlv,
                ea.valor_tope_pesos,


                -- =====================================================
                -- MENSAJES DE APROBACIÓN
                -- =====================================================

                ea.mensaje_aprobacion,
                ea.detalle_aprobacion,


                -- =====================================================
                -- ESTADO DE LA SOLICITUD
                -- =====================================================

                v.id_solicitud_proceso,
                v.nombre_proceso,

                v.id_solicitud_resultado,
                v.nombre_resultado,
                v.resultado_final,

                v.id_cartera_credito,

                v.observacion_asesor,

                v.fecha_fin_iniciada,
                v.fecha_fin_documentacion,
                v.fecha_fin_aprobacion,
                v.fecha_fin_desembolso,

                v.activo

                FROM cartera.vw_solicitudes_creditos v
                
                INNER JOIN cartera.solicitudes_creditos sc
                    ON sc.id_solicitud_credito =
                       v.id_solicitud_credito

                LEFT JOIN cartera.vw_solicitudes_ente_aprobacion ea
                 ON ea.id_solicitud_credito =
                   v.id_solicitud_credito

            WHERE v.id_solicitud_credito =
                  :idSolicitudCredito

              AND v.activo = true
            """;

        List<SolicitudCreditoDetalleDTO> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                ),
                        DETALLE_MAPPER
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Se encontró más de una solicitud con id "
                            + idSolicitudCredito
                            + "."
            );
        }

        return Optional.of(
                resultados.get(0)
        );
    }


    // =========================================================
    // SOPORTE
    // =========================================================

    private <T> T obtenerUnico(
            List<T> resultados,
            String descripcion
    ) {

        if (resultados == null || resultados.isEmpty()) {
            throw new IllegalStateException(
                    "No se encontró "
                            + descripcion
                            + "."
            );
        }

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Se encontró más de un registro para "
                            + descripcion
                            + "."
            );
        }

        return resultados.get(0);
    }

    // =========================================================
// CONTEXTO PARA ENVIAR A APROBACION
// =========================================================

    public Optional<SolicitudEnviarAprobacionContexto> buscarParaEnviarAprobacion(
            Integer idSolicitudCredito
    ) {

        String sql = """
        SELECT
            sc.id_solicitud_credito,
            sc.numero_solicitud,
            sc.id_solicitud_proceso,
            sc.id_solicitud_resultado,
            sr.nombre_resultado,
            sr.es_final,
            sc.id_ente_aprobacion,
            ea.nombre_ente_aprobacion,
            sc.id_linea_credito,
            sc.codigo_clasificacion_credito,
            sc.codigo_garantia_credito,
            sc.id_fondo_garantia,
            sc.valor_solicitado,
            sc.plazo_solicitado,
            sc.id_condicion_inicial,
            sc.id_tasa_colocacion_detalle

        FROM cartera.solicitudes_creditos sc

        INNER JOIN cartera.solicitudes_resultados sr
            ON sr.id_solicitud_resultado =
               sc.id_solicitud_resultado
        
        LEFT JOIN cartera.entes_aprobacion ea
            ON ea.id_ente_aprobacion =
               sc.id_ente_aprobacion

        WHERE sc.id_solicitud_credito =
              :idSolicitudCredito

          AND sc.activo = true

        FOR UPDATE OF sc
        """;

        List<SolicitudEnviarAprobacionContexto> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                ),
                        (rs, rowNum) ->
                                new SolicitudEnviarAprobacionContexto(
                                        rs.getInt("id_solicitud_credito"),
                                        rs.getString("numero_solicitud"),
                                        rs.getInt("id_solicitud_proceso"),
                                        rs.getInt("id_solicitud_resultado"),
                                        rs.getString("nombre_resultado"),
                                        rs.getBoolean("es_final"),
                                        rs.getObject(
                                                "id_ente_aprobacion",
                                                Integer.class
                                        ),
                                        rs.getString(
                                                "nombre_ente_aprobacion"
                                        ),
                                        rs.getObject(
                                                "id_linea_credito",
                                                Integer.class
                                        ),
                                        rs.getString(
                                                "codigo_clasificacion_credito"
                                        ),
                                        rs.getString(
                                                "codigo_garantia_credito"
                                        ),
                                        rs.getObject(
                                                "id_fondo_garantia",
                                                Integer.class
                                        ),
                                        rs.getBigDecimal(
                                                "valor_solicitado"
                                        ),
                                        rs.getObject(
                                                "plazo_solicitado",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "id_condicion_inicial",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "id_tasa_colocacion_detalle",
                                                Integer.class
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }

    // =========================================================
    // VALIDAR ETAPAS PARA ENVIAR A APROBACIÓN
    // =========================================================

    public ValidacionEtapasAprobacion validarEtapasParaAprobacion(
            Integer idSolicitudCredito
    ) {

        String sql = """
            SELECT
                (
                    SELECT COUNT(*)::integer
                    FROM cartera.solicitudes_deudores sd
                    WHERE sd.id_solicitud_credito =
                          :idSolicitudCredito
                      AND sd.activo = true
                ) AS cantidad_deudores,

                (
                    SELECT COUNT(*)::integer
                    FROM cartera.solicitudes_deudores sd
                    WHERE sd.id_solicitud_credito =
                          :idSolicitudCredito
                      AND sd.activo = true
                      AND EXISTS (
                          SELECT 1
                          FROM cartera.solicitudes_deudores_financieros sf
                          WHERE sf.id_solicitud_deudor =
                                sd.id_solicitud_deudor
                      )
                ) AS cantidad_financieros,

                (
                    SELECT COUNT(*)::integer
                    FROM cartera.solicitudes_deudores sd
                    WHERE sd.id_solicitud_credito =
                          :idSolicitudCredito
                      AND sd.activo = true
                      AND EXISTS (
                          SELECT 1
                          FROM cartera.solicitudes_deudores_centrales sc
                          WHERE sc.id_solicitud_deudor =
                                sd.id_solicitud_deudor
                            AND sc.activo = true
                      )
                ) AS cantidad_centrales,

                (
                    SELECT COUNT(*)::integer
                    FROM cartera.solicitudes_deudores sd
                    WHERE sd.id_solicitud_credito =
                          :idSolicitudCredito
                      AND sd.activo = true
                      AND EXISTS (
                          SELECT 1
                          FROM cartera.solicitudes_analisis sa
                          WHERE sa.id_solicitud_deudor =
                                sd.id_solicitud_deudor
                            AND sa.id_solicitud_credito =
                                sd.id_solicitud_credito
                      )
                ) AS cantidad_analisis
            """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        ),
                (rs, rowNum) ->
                        new ValidacionEtapasAprobacion(
                                rs.getInt("cantidad_deudores"),
                                rs.getInt("cantidad_financieros"),
                                rs.getInt("cantidad_centrales"),
                                rs.getInt("cantidad_analisis")
                        )
        );
    }

    // =========================================================
    // VALIDAR BIENES PARA ENVIAR A APROBACIÓN
    // =========================================================

    public boolean validarBienesParaAprobacion(
            Integer idSolicitudCredito
    ) {

        String sql = """
            SELECT
                CASE
                    WHEN TRIM(UPPER(gc.tipo_garantia)) = 'P'
                        THEN true

                    WHEN TRIM(UPPER(gc.tipo_garantia)) = 'R'
                        THEN EXISTS (
                            SELECT 1
                            FROM cartera.solicitudes_deudores sd

                            INNER JOIN cartera.solicitudes_deudores_bienes sdb
                                ON sdb.id_solicitud_deudor =
                                   sd.id_solicitud_deudor

                            WHERE sd.id_solicitud_credito =
                                  sc.id_solicitud_credito

                              AND sd.activo = true
                              AND sdb.activo = true
                        )

                    ELSE false
                END AS bienes_completos

            FROM cartera.solicitudes_creditos sc

            INNER JOIN cartera.garantias_creditos gc
                ON gc.codigo_garantia_credito =
                   sc.codigo_garantia_credito

            WHERE sc.id_solicitud_credito =
                  :idSolicitudCredito

              AND sc.activo = true
            """;

        List<Boolean> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                ),
                        (rs, rowNum) ->
                                rs.getBoolean(
                                        "bienes_completos"
                                )
                );

        if (resultados.isEmpty()) {
            return false;
        }

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Se encontró más de una garantía para validar los bienes de la solicitud "
                            + idSolicitudCredito
                            + "."
            );
        }

        return Boolean.TRUE.equals(
                resultados.get(0)
        );
    }

    // =========================================================
    // ENVIAR SOLICITUD A APROBACIÓN
    // =========================================================

    public LocalDateTime enviarAprobacion(
            Integer idSolicitudCredito,
            Integer idSolicitudProcesoAprobacion,
            String conceptoAsesorAprobacion,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.solicitudes_creditos
            SET
                id_solicitud_proceso =
                    :idSolicitudProcesoAprobacion,

                concepto_asesor_aprobacion =
                    TRIM(:conceptoAsesorAprobacion),

                fecha_fin_documentacion =
                    CURRENT_TIMESTAMP,

                fecha_ultima_gestion =
                    CURRENT_TIMESTAMP,

                fk_seguridad_edicion =
                    :idUsuario,

                fecha_edicion =
                    CURRENT_TIMESTAMP

            WHERE id_solicitud_credito =
                  :idSolicitudCredito

              AND activo = true

            RETURNING
                fecha_ultima_gestion
            """;

        List<LocalDateTime> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                )
                                .addValue(
                                        "idSolicitudProcesoAprobacion",
                                        idSolicitudProcesoAprobacion
                                )
                                .addValue(
                                        "conceptoAsesorAprobacion",
                                        conceptoAsesorAprobacion
                                )
                                .addValue(
                                        "idUsuario",
                                        idUsuario
                                ),
                        (rs, rowNum) ->
                                rs.getTimestamp(
                                        "fecha_ultima_gestion"
                                ).toLocalDateTime()
                );

        return obtenerUnico(
                resultados,
                "enviar la solicitud a aprobación"
        );
    }

    // =========================================================
    // FINALIZAR SOLICITUD
    // =========================================================

    public Optional<SolicitudFinalizarContexto> buscarParaFinalizar(
            Integer idSolicitudCredito
    ) {

        String sql = """
            SELECT
                sc.id_solicitud_credito,
                sc.numero_solicitud,
                sc.id_solicitud_proceso,
                sc.id_solicitud_resultado,
                sr.nombre_resultado,
                sr.es_final

            FROM cartera.solicitudes_creditos sc

            INNER JOIN cartera.solicitudes_resultados sr
                ON sr.id_solicitud_resultado =
                   sc.id_solicitud_resultado

            WHERE sc.id_solicitud_credito =
                  :idSolicitudCredito

              AND sc.activo = true

            FOR UPDATE OF sc
            """;

        List<SolicitudFinalizarContexto> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                ),
                        (rs, rowNum) ->
                                new SolicitudFinalizarContexto(
                                        rs.getInt(
                                                "id_solicitud_credito"
                                        ),
                                        rs.getString(
                                                "numero_solicitud"
                                        ),
                                        rs.getInt(
                                                "id_solicitud_proceso"
                                        ),
                                        rs.getInt(
                                                "id_solicitud_resultado"
                                        ),
                                        rs.getString(
                                                "nombre_resultado"
                                        ),
                                        rs.getBoolean(
                                                "es_final"
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }


    public Optional<CatalogoResultado> buscarResultadoFinalActivo(
            Integer idSolicitudResultado
    ) {

        String sql = """
            SELECT
                sr.id_solicitud_resultado,
                sr.nombre_resultado

            FROM cartera.solicitudes_resultados sr

            WHERE sr.id_solicitud_resultado =
                  :idSolicitudResultado

              AND sr.es_final = true
              AND sr.activo = true
            """;

        List<CatalogoResultado> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudResultado",
                                        idSolicitudResultado
                                ),
                        (rs, rowNum) ->
                                new CatalogoResultado(
                                        rs.getInt(
                                                "id_solicitud_resultado"
                                        ),
                                        rs.getString(
                                                "nombre_resultado"
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }


    public int finalizarSolicitud(
            Integer idSolicitudCredito,
            Integer idSolicitudResultado,
            String observacionFinal,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.solicitudes_creditos
            SET
                id_solicitud_resultado =
                    :idSolicitudResultado,

                observacion_final =
                    :observacionFinal,

                fecha_ultima_gestion =
                    CURRENT_TIMESTAMP,

                fk_seguridad_edicion =
                    :idUsuario,

                fecha_edicion =
                    CURRENT_TIMESTAMP

            WHERE id_solicitud_credito =
                  :idSolicitudCredito

              AND activo = true
            """;

        return jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )
                        .addValue(
                                "idSolicitudResultado",
                                idSolicitudResultado
                        )
                        .addValue(
                                "observacionFinal",
                                observacionFinal
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
        );
    }


    // =========================================================
    // RECORDS INTERNOS
    // =========================================================

    public record ValidacionEtapasAprobacion(
            Integer cantidadDeudores,
            Integer cantidadFinancieros,
            Integer cantidadCentrales,
            Integer cantidadAnalisis
    ) {

        public boolean deudoresCompletos() {
            return cantidadDeudores != null
                    && cantidadDeudores > 0;
        }

        public boolean financieroCompleto() {
            return deudoresCompletos()
                    && cantidadDeudores.equals(
                    cantidadFinancieros
            );
        }

        public boolean centralRiesgoCompleta() {
            return deudoresCompletos()
                    && cantidadDeudores.equals(
                    cantidadCentrales
            );
        }

        public boolean analisisCompleto() {
            return deudoresCompletos()
                    && cantidadDeudores.equals(
                    cantidadAnalisis
            );
        }
    }

    public record CatalogoProceso(
            Integer idSolicitudProceso,
            String nombreProceso
    ) {
    }

    public record CatalogoResultado(
            Integer idSolicitudResultado,
            String nombreResultado
    ) {
    }

    public record SolicitudEnviarAprobacionContexto(
            Integer idSolicitudCredito,
            String numeroSolicitud,
            Integer idSolicitudProceso,
            Integer idSolicitudResultado,
            String nombreResultado,
            boolean resultadoFinal,
            Integer idEnteAprobacion,
            String nombreEnteAprobacion,
            Integer idLineaCredito,
            String codigoClasificacionCredito,
            String codigoGarantiaCredito,
            Integer idFondoGarantia,
            java.math.BigDecimal valorSolicitado,
            Integer plazoSolicitado,
            Integer idCondicionInicial,
            Integer idTasaColocacionDetalle
    ) {
    }

    public record CuentaAportesResumen(
            Integer cantidad,
            Integer idCuentaAportes,
            Integer idAgencia
    ) {
    }

    public record SolicitudRetomarDatos(
            Integer idSolicitudCredito,
            String numeroSolicitud,
            Integer idAgencia,
            Integer idDatosPersonal,
            Integer idCuentaAportes,
            Integer idSolicitudProceso,
            String nombreProceso,
            Integer idSolicitudResultado,
            String nombreResultado,
            Boolean resultadoFinal
    ) {
    }

    public record SolicitudCreadaDatos(
            Integer idSolicitudCredito,
            LocalDateTime fechaUltimaGestion
    ) {
    }

    public record SolicitudGuardarContexto(
            Integer idSolicitudCredito,
            String numeroSolicitud,
            Integer idAgencia,
            Integer idDatosPersonal,
            Integer idCuentaAportes,
            BigDecimal valorAportesInicio,
            Boolean resultadoFinal
    ) {
    }

    public record CondicionInicialAplicable(
            Integer idCondicionInicial,
            Integer idCondicionInicialDetalle,
            Integer plazoMinimo,
            Integer plazoMaximo,
            BigDecimal cantidadSmmlvMinimo,
            BigDecimal cantidadSmmlvMaximo,
            BigDecimal factorReciprocidadAportes
    ) {
    }

    public record TasaAplicable(
            Integer idTasaColocacionDetalle,
            BigDecimal tasaColocacion
    ) {
    }

    public record PersistenciaCredito(
            Integer idCondicionInicial,
            Integer idCondicionInicialDetalle,
            Integer plazoMinimoAplicado,
            Integer plazoMaximoAplicado,
            BigDecimal cantidadSmmlvMinimoAplicada,
            BigDecimal cantidadSmmlvMaximoAplicada,
            BigDecimal factorReciprocidadAportesAplicado,
            BigDecimal valorSmmlvAplicado,
            BigDecimal cantidadSmmlvSolicitada,
            BigDecimal valorAportesInicio,
            Boolean cumpleAportesInicio,
            BigDecimal cupoMaximoPorAportes,
            BigDecimal valorAportesRequerido,
            Integer idTasaColocacionDetalle,
            BigDecimal tasaColocacionAplicada,
            BigDecimal tasaEfectivaAnual,
            BigDecimal valorCuotaProyectada,
            BigDecimal valorPrimeraCuotaAnalisis,

            Integer idFondoGarantia,
            BigDecimal porcentajeFondoAplicado,
            String formaCobroFondo,
            BigDecimal valorFondoGarantia
    ) {
    }

    public record FondoGarantiaAplicable(
            Integer idFondoGarantia,
            String codigoFondo,
            String nombreFondo,
            BigDecimal porcentajeFondo
    ) {
    }

    public record SolicitudFinalizarContexto(
            Integer idSolicitudCredito,
            String numeroSolicitud,
            Integer idSolicitudProceso,
            Integer idSolicitudResultado,
            String nombreResultado,
            Boolean resultadoFinal
    ) {
    }

    // =========================================================
    // VALIDACIONES DE CATÁLOGOS ACTIVOS
    // =========================================================

    public boolean existeLineaCreditoActiva(
            Integer idLineaCredito
    ) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.lineas_creditos
                WHERE id_linea_credito = :idLineaCredito
                  AND activo = true
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idLineaCredito",
                                idLineaCredito
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }


    public boolean existeClasificacionCreditoActiva(
            String codigoClasificacionCredito
    ) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.clasificaciones_creditos
                WHERE TRIM(codigo_clasificacion_credito) =
                      TRIM(:codigoClasificacionCredito)
                  AND activo = true
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "codigoClasificacionCredito",
                                codigoClasificacionCredito
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }


    public boolean existeDestinoEconomicoActivo(
            String codigoDestinoEconomico
    ) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.destinos_economicos
                WHERE TRIM(codigo_destino_economico) =
                      TRIM(:codigoDestinoEconomico)
                  AND activo = true
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "codigoDestinoEconomico",
                                codigoDestinoEconomico
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }


    public boolean existeGarantiaCreditoActiva(
            String codigoGarantiaCredito
    ) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.garantias_creditos
                WHERE TRIM(codigo_garantia_credito) =
                      TRIM(:codigoGarantiaCredito)
                  AND activo = true
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "codigoGarantiaCredito",
                                codigoGarantiaCredito
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }


    public boolean existeSubgarantiaCreditoActiva(
            String codigoSubgarantia
    ) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.subgarantias_creditos
                WHERE TRIM(codigo_subgarantia) =
                      TRIM(:codigoSubgarantia)
                  AND activo = true
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "codigoSubgarantia",
                                codigoSubgarantia
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }


    public boolean existeFormaPagoActiva(
            String codigoFormaPago
    ) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.formas_pago
                WHERE TRIM(codigo_forma_pago) =
                      TRIM(:codigoFormaPago)
                  AND activo = true
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "codigoFormaPago",
                                codigoFormaPago
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }


    public boolean existeModalidadInteresActiva(
            String periodoCodigoInteres,
            String tipoModalidadInteres
    ) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.modalidades_intereses
                WHERE TRIM(periodo_codigo) =
                      TRIM(:periodoCodigoInteres)
                  AND TRIM(tipo_modalidad) =
                      TRIM(:tipoModalidadInteres)
                  AND activo = true
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "periodoCodigoInteres",
                                periodoCodigoInteres
                        )
                        .addValue(
                                "tipoModalidadInteres",
                                tipoModalidadInteres
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }


    public boolean existeTipoCuotaActivo(
            String codigoTipoCuota
    ) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.tipos_cuotas
                WHERE TRIM(codigo_tipo_cuota) =
                      TRIM(:codigoTipoCuota)
                  AND activo = true
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "codigoTipoCuota",
                                codigoTipoCuota
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    public record VigenciaHojaVida(
            LocalDate fechaActualizacion,
            int diasMaximos,
            boolean vigente
    ) {
    }

    // =========================================================
// CRÉDITOS SIMULTÁNEOS
// =========================================================

    private record CreditoSimultaneoFila(
            Integer idLineaCredito,
            String nombreLineaCredito,
            boolean permiteCreditosSimultaneos,
            String pagareCartera,
            BigDecimal saldoActual
    ) {
    }

    public record CreditoActivoConSaldo(
            String pagareCartera,
            BigDecimal saldoActual
    ) {
    }

    public record ValidacionCreditosSimultaneos(
            Integer idLineaCredito,
            String nombreLineaCredito,
            boolean permiteCreditosSimultaneos,
            boolean permitido,
            List<CreditoActivoConSaldo> creditosActivos
    ) {
    }

}
