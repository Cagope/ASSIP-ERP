package co.assip.erp.cartera.originacion.centralriesgo;

import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoDTO;
import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoDetalleDTO;
import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoGuardarRequestDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudCentralRiesgoRepository {

    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    private static final String SQL_LISTAR_POR_SOLICITUD = """
        SELECT
            sdc.id_solicitud_deudor_central,

            sd.id_solicitud_deudor,
            sd.id_solicitud_credito,
            sd.id_datos_personal,

            dp.tipo_documento,
            dp.documento,

            NULLIF(
                TRIM(
                    CONCAT_WS(
                        ' ',
                        NULLIF(TRIM(dp.nombres), ''),
                        NULLIF(TRIM(dp.primer_apellido), ''),
                        NULLIF(TRIM(dp.segundo_apellido), '')
                    )
                ),
                ''
            ) AS nombre_completo,

            sd.tipo_deudor,
            sd.orden_deudor,

            sdc.id_central_riesgo,
            cr.codigo_central,
            cr.nombre_central,

            sdc.fecha_consulta,
            sdc.fecha_fotografia,

            sdc.saldo_actual_obligaciones,
            sdc.valor_cuotas_mensuales,

            sdc.puntaje_central,
            sdc.calificacion_central,
            sdc.calificacion_cualitativa,

            sdc.activo

        FROM cartera.solicitudes_deudores sd

        INNER JOIN hoja_vida.datos_personales dp
            ON dp.id_datos_personal =
               sd.id_datos_personal

        LEFT JOIN cartera.solicitudes_deudores_centrales sdc
            ON sdc.id_solicitud_deudor =
               sd.id_solicitud_deudor

        LEFT JOIN cartera.centrales_riesgo cr
            ON cr.id_central_riesgo =
               sdc.id_central_riesgo

        WHERE sd.id_solicitud_credito =
              :idSolicitudCredito

          AND sd.activo = true

        ORDER BY
            sd.orden_deudor
        """;


    // =========================================================
    // LISTAR POR DEUDOR
    // =========================================================
    //
    // Por regla funcional debe existir máximo un registro
    // activo de central de riesgo por id_solicitud_deudor.
    //
    // Se conserva List para mantener compatibilidad con
    // Service / Controller actuales.
    //
    // =========================================================

    private static final String SQL_LISTAR_POR_DEUDOR = """
            SELECT
                sdc.id_solicitud_deudor_central,
                sdc.id_solicitud_deudor,
                sd.id_solicitud_credito,

                sd.id_datos_personal,

                dp.tipo_documento,
                dp.documento,

                NULLIF(
                    TRIM(
                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(dp.nombres), ''),
                            NULLIF(TRIM(dp.primer_apellido), ''),
                            NULLIF(TRIM(dp.segundo_apellido), '')
                        )
                    ),
                    ''
                ) AS nombre_completo,

                sd.tipo_deudor,
                sd.orden_deudor,

                sdc.id_central_riesgo,
                cr.codigo_central,
                cr.nombre_central,

                sdc.fecha_consulta,
                sdc.fecha_fotografia,

                sdc.saldo_actual_obligaciones,
                sdc.valor_cuotas_mensuales,

                sdc.puntaje_central,
                sdc.calificacion_central,
                sdc.calificacion_cualitativa,

                sdc.activo

            FROM cartera.solicitudes_deudores_centrales sdc

            INNER JOIN cartera.solicitudes_deudores sd
                ON sd.id_solicitud_deudor =
                   sdc.id_solicitud_deudor

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal =
                   sd.id_datos_personal

            INNER JOIN cartera.centrales_riesgo cr
                ON cr.id_central_riesgo =
                   sdc.id_central_riesgo

            WHERE sdc.id_solicitud_deudor =
                  :idSolicitudDeudor

              AND sd.activo = true
              AND sdc.activo = true

            ORDER BY
                sdc.id_solicitud_deudor_central
            """;


    // =========================================================
    // DETALLE POR ID
    // =========================================================

    private static final String SQL_BUSCAR_POR_ID = """
            SELECT
                sdc.id_solicitud_deudor_central,
                sdc.id_solicitud_deudor,
                sd.id_solicitud_credito,

                sd.id_datos_personal,

                dp.tipo_documento,
                dp.documento,

                NULLIF(
                    TRIM(
                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(dp.nombres), ''),
                            NULLIF(TRIM(dp.primer_apellido), ''),
                            NULLIF(TRIM(dp.segundo_apellido), '')
                        )
                    ),
                    ''
                ) AS nombre_completo,

                sd.tipo_deudor,
                sd.orden_deudor,

                sdc.id_central_riesgo,
                cr.codigo_central,
                cr.documento_central,
                cr.nombre_central,
                cr.descripcion AS descripcion_central,

                sdc.fecha_consulta,
                sdc.fecha_fotografia,

                sdc.valor_inicial_obligaciones,
                sdc.saldo_actual_obligaciones,
                sdc.valor_cuotas_mensuales,

                sdc.cantidad_calificacion_a,
                sdc.cantidad_calificacion_b,
                sdc.cantidad_calificacion_c,
                sdc.cantidad_calificacion_d,
                sdc.cantidad_calificacion_e,
                sdc.cantidad_calificacion_k,

                sdc.cantidad_reestructuraciones,
                sdc.cantidad_refinanciaciones,
                sdc.cantidad_cuentas_embargadas,

                sdc.puntaje_central,
                sdc.calificacion_central,
                sdc.calificacion_cualitativa,
                sdc.observacion,

                sdc.activo,
                sdc.fecha_creacion,
                sdc.fecha_edicion

            FROM cartera.solicitudes_deudores_centrales sdc

            INNER JOIN cartera.solicitudes_deudores sd
                ON sd.id_solicitud_deudor =
                   sdc.id_solicitud_deudor

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal =
                   sd.id_datos_personal

            INNER JOIN cartera.centrales_riesgo cr
                ON cr.id_central_riesgo =
                   sdc.id_central_riesgo

            WHERE sdc.id_solicitud_deudor_central =
                  :idSolicitudDeudorCentral

              AND sd.activo = true
              AND sdc.activo = true
            """;


    // =========================================================
    // AGENCIA POR SOLICITUD
    // =========================================================

    private static final String SQL_AGENCIA_SOLICITUD = """
            SELECT
                sc.id_agencia

            FROM cartera.solicitudes_creditos sc

            WHERE sc.id_solicitud_credito =
                  :idSolicitudCredito

              AND sc.activo = true
            """;


    // =========================================================
    // AGENCIA POR DEUDOR
    // =========================================================

    private static final String SQL_AGENCIA_DEUDOR = """
            SELECT
                sc.id_agencia

            FROM cartera.solicitudes_deudores sd

            INNER JOIN cartera.solicitudes_creditos sc
                ON sc.id_solicitud_credito =
                   sd.id_solicitud_credito

            WHERE sd.id_solicitud_deudor =
                  :idSolicitudDeudor

              AND sd.activo = true
              AND sc.activo = true
            """;


    // =========================================================
    // BLOQUEAR SOLICITUD EDITABLE
    // =========================================================

    private static final String SQL_BLOQUEAR_SOLICITUD_EDITABLE = """
            SELECT
                sc.id_agencia

            FROM cartera.solicitudes_deudores sd

            INNER JOIN cartera.solicitudes_creditos sc
                ON sc.id_solicitud_credito =
                   sd.id_solicitud_credito

            INNER JOIN cartera.solicitudes_resultados sr
                ON sr.id_solicitud_resultado =
                   sc.id_solicitud_resultado

            WHERE sd.id_solicitud_deudor =
                  :idSolicitudDeudor

              AND sd.activo = true
              AND sc.activo = true
              AND sr.es_final = false

            FOR UPDATE OF sc
            """;


    // =========================================================
    // VALIDAR CENTRAL
    // =========================================================

    private static final String SQL_EXISTE_CENTRAL = """
            SELECT EXISTS (
                SELECT 1

                FROM cartera.centrales_riesgo cr

                WHERE cr.id_central_riesgo =
                      :idCentralRiesgo

                  AND cr.activo = true
            )
            """;


    // =========================================================
    // GUARDAR / ACTUALIZAR CENTRAL DE RIESGO
    // =========================================================
    //
    // Regla:
    //
    // 1 solicitud_deudor = 1 central de riesgo.
    //
    // La central seleccionada puede cambiar, pero se conserva
    // la misma fila física asociada al deudor.
    //
    // Al actualizar:
    //
    // - se conserva id_solicitud_deudor_central;
    // - se conserva auditoría de creación;
    // - puede cambiar id_central_riesgo;
    // - se reemplazan los resultados vigentes;
    // - se actualiza auditoría de edición.
    //
    // =========================================================

    private static final String SQL_GUARDAR = """
            INSERT INTO cartera.solicitudes_deudores_centrales (
                id_solicitud_deudor,
                id_central_riesgo,

                fecha_consulta,
                fecha_fotografia,

                valor_inicial_obligaciones,
                saldo_actual_obligaciones,
                valor_cuotas_mensuales,

                cantidad_calificacion_a,
                cantidad_calificacion_b,
                cantidad_calificacion_c,
                cantidad_calificacion_d,
                cantidad_calificacion_e,
                cantidad_calificacion_k,

                cantidad_reestructuraciones,
                cantidad_refinanciaciones,
                cantidad_cuentas_embargadas,

                puntaje_central,
                calificacion_central,
                calificacion_cualitativa,
                observacion,

                activo,

                fk_seguridad_creacion,
                fecha_creacion,
                fk_seguridad_edicion,
                fecha_edicion
            )
            VALUES (
                :idSolicitudDeudor,
                :idCentralRiesgo,

                :fechaConsulta,
                CURRENT_TIMESTAMP,

                COALESCE(:valorInicialObligaciones, 0),
                COALESCE(:saldoActualObligaciones, 0),
                COALESCE(:valorCuotasMensuales, 0),

                COALESCE(:cantidadCalificacionA, 0),
                COALESCE(:cantidadCalificacionB, 0),
                COALESCE(:cantidadCalificacionC, 0),
                COALESCE(:cantidadCalificacionD, 0),
                COALESCE(:cantidadCalificacionE, 0),
                COALESCE(:cantidadCalificacionK, 0),

                COALESCE(:cantidadReestructuraciones, 0),
                COALESCE(:cantidadRefinanciaciones, 0),
                COALESCE(:cantidadCuentasEmbargadas, 0),

                :puntajeCentral,
                :calificacionCentral,
                :calificacionCualitativa,
                :observacion,

                true,

                :idUsuario,
                CURRENT_TIMESTAMP,
                :idUsuario,
                CURRENT_TIMESTAMP
            )

            ON CONFLICT (id_solicitud_deudor)
            DO UPDATE
            SET
                id_central_riesgo =
                    EXCLUDED.id_central_riesgo,

                fecha_consulta =
                    EXCLUDED.fecha_consulta,

                fecha_fotografia =
                    CURRENT_TIMESTAMP,

                valor_inicial_obligaciones =
                    EXCLUDED.valor_inicial_obligaciones,

                saldo_actual_obligaciones =
                    EXCLUDED.saldo_actual_obligaciones,

                valor_cuotas_mensuales =
                    EXCLUDED.valor_cuotas_mensuales,

                cantidad_calificacion_a =
                    EXCLUDED.cantidad_calificacion_a,

                cantidad_calificacion_b =
                    EXCLUDED.cantidad_calificacion_b,

                cantidad_calificacion_c =
                    EXCLUDED.cantidad_calificacion_c,

                cantidad_calificacion_d =
                    EXCLUDED.cantidad_calificacion_d,

                cantidad_calificacion_e =
                    EXCLUDED.cantidad_calificacion_e,

                cantidad_calificacion_k =
                    EXCLUDED.cantidad_calificacion_k,

                cantidad_reestructuraciones =
                    EXCLUDED.cantidad_reestructuraciones,

                cantidad_refinanciaciones =
                    EXCLUDED.cantidad_refinanciaciones,

                cantidad_cuentas_embargadas =
                    EXCLUDED.cantidad_cuentas_embargadas,

                puntaje_central =
                    EXCLUDED.puntaje_central,

                calificacion_central =
                    EXCLUDED.calificacion_central,

                calificacion_cualitativa =
                    EXCLUDED.calificacion_cualitativa,

                observacion =
                    EXCLUDED.observacion,

                activo =
                    true,

                fk_seguridad_edicion =
                    :idUsuario,

                fecha_edicion =
                    CURRENT_TIMESTAMP

            RETURNING id_solicitud_deudor_central
            """;


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<SolicitudCentralRiesgoDTO>
            RESUMEN_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudCentralRiesgoDTO.class
            );

    private static final BeanPropertyRowMapper<SolicitudCentralRiesgoDetalleDTO>
            DETALLE_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudCentralRiesgoDetalleDTO.class
            );


    // =========================================================
    // DEPENDENCIA
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudCentralRiesgoRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    public List<SolicitudCentralRiesgoDTO> listarPorSolicitud(
            Integer idSolicitudCredito
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        return jdbc.query(
                SQL_LISTAR_POR_SOLICITUD,
                parametros,
                RESUMEN_MAPPER
        );
    }


    // =========================================================
    // LISTAR POR DEUDOR
    // =========================================================

    public List<SolicitudCentralRiesgoDTO> listarPorDeudor(
            Integer idSolicitudDeudor
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        );

        return jdbc.query(
                SQL_LISTAR_POR_DEUDOR,
                parametros,
                RESUMEN_MAPPER
        );
    }


    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<SolicitudCentralRiesgoDetalleDTO> buscarPorId(
            Integer idSolicitudDeudorCentral
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudorCentral",
                                idSolicitudDeudorCentral
                        );

        List<SolicitudCentralRiesgoDetalleDTO> resultados =
                jdbc.query(
                        SQL_BUSCAR_POR_ID,
                        parametros,
                        DETALLE_MAPPER
                );

        return resultados
                .stream()
                .findFirst();
    }


    // =========================================================
    // AGENCIA SOLICITUD
    // =========================================================

    public Optional<Integer> obtenerAgenciaSolicitud(
            Integer idSolicitudCredito
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        List<Integer> resultados =
                jdbc.query(
                        SQL_AGENCIA_SOLICITUD,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt("id_agencia")
                );

        return resultados
                .stream()
                .findFirst();
    }


    // =========================================================
    // AGENCIA DEUDOR
    // =========================================================

    public Optional<Integer> obtenerAgenciaDeudor(
            Integer idSolicitudDeudor
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        );

        List<Integer> resultados =
                jdbc.query(
                        SQL_AGENCIA_DEUDOR,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt("id_agencia")
                );

        return resultados
                .stream()
                .findFirst();
    }


    // =========================================================
    // BLOQUEAR SOLICITUD
    // =========================================================

    public Optional<Integer> bloquearSolicitudEditable(
            Integer idSolicitudDeudor
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        );

        List<Integer> resultados =
                jdbc.query(
                        SQL_BLOQUEAR_SOLICITUD_EDITABLE,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt("id_agencia")
                );

        return resultados
                .stream()
                .findFirst();
    }


    // =========================================================
    // CENTRAL ACTIVA
    // =========================================================

    public boolean existeCentralActiva(
            Integer idCentralRiesgo
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCentralRiesgo",
                                idCentralRiesgo
                        );

        Boolean existe =
                jdbc.queryForObject(
                        SQL_EXISTE_CENTRAL,
                        parametros,
                        Boolean.class
                );

        return Boolean.TRUE.equals(
                existe
        );
    }


    // =========================================================
    // GUARDAR / ACTUALIZAR
    // =========================================================

    public Integer guardar(
            SolicitudCentralRiesgoGuardarRequestDTO request,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()

                        .addValue(
                                "idSolicitudDeudor",
                                request.getIdSolicitudDeudor()
                        )

                        .addValue(
                                "idCentralRiesgo",
                                request.getIdCentralRiesgo()
                        )

                        .addValue(
                                "fechaConsulta",
                                request.getFechaConsulta()
                        )


                        // -----------------------------------------
                        // OBLIGACIONES
                        // -----------------------------------------

                        .addValue(
                                "valorInicialObligaciones",
                                request.getValorInicialObligaciones()
                        )

                        .addValue(
                                "saldoActualObligaciones",
                                request.getSaldoActualObligaciones()
                        )

                        .addValue(
                                "valorCuotasMensuales",
                                request.getValorCuotasMensuales()
                        )


                        // -----------------------------------------
                        // CALIFICACIONES
                        // -----------------------------------------

                        .addValue(
                                "cantidadCalificacionA",
                                request.getCantidadCalificacionA()
                        )

                        .addValue(
                                "cantidadCalificacionB",
                                request.getCantidadCalificacionB()
                        )

                        .addValue(
                                "cantidadCalificacionC",
                                request.getCantidadCalificacionC()
                        )

                        .addValue(
                                "cantidadCalificacionD",
                                request.getCantidadCalificacionD()
                        )

                        .addValue(
                                "cantidadCalificacionE",
                                request.getCantidadCalificacionE()
                        )

                        .addValue(
                                "cantidadCalificacionK",
                                request.getCantidadCalificacionK()
                        )


                        // -----------------------------------------
                        // NOVEDADES
                        // -----------------------------------------

                        .addValue(
                                "cantidadReestructuraciones",
                                request.getCantidadReestructuraciones()
                        )

                        .addValue(
                                "cantidadRefinanciaciones",
                                request.getCantidadRefinanciaciones()
                        )

                        .addValue(
                                "cantidadCuentasEmbargadas",
                                request.getCantidadCuentasEmbargadas()
                        )


                        // -----------------------------------------
                        // RESULTADO CENTRAL
                        // -----------------------------------------

                        .addValue(
                                "puntajeCentral",
                                request.getPuntajeCentral()
                        )

                        .addValue(
                                "calificacionCentral",
                                normalizarTexto(
                                        request.getCalificacionCentral()
                                )
                        )

                        .addValue(
                                "calificacionCualitativa",
                                normalizarTexto(
                                        request.getCalificacionCualitativa()
                                )
                        )

                        .addValue(
                                "observacion",
                                normalizarTexto(
                                        request.getObservacion()
                                )
                        )


                        // -----------------------------------------
                        // AUDITORÍA
                        // -----------------------------------------

                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.queryForObject(
                SQL_GUARDAR,
                parametros,
                Integer.class
        );
    }


    // =========================================================
    // SOPORTE
    // =========================================================

    private String normalizarTexto(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String texto =
                valor.trim();

        return texto.isEmpty()
                ? null
                : texto;
    }
}