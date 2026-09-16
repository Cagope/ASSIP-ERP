package co.assip.erp.cartera.originacion.analisis;

import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisComponenteDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisDeudorDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisPersistenciaDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisResultadoDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudAnalisisRepository {

    // =========================================================
    // RESULTADO ACTUAL DE LA SOLICITUD
    // =========================================================

    private static final String SQL_RESULTADO_SOLICITUD = """
            SELECT
                id_solicitud_credito,
                numero_solicitud,
                id_agencia,
                id_datos_personal,
                fecha_inicio_solicitud,
                fecha_ultima_gestion,
                id_solicitud_proceso,
                id_solicitud_resultado,
                id_linea_credito,
                codigo_garantia_credito,
                valor_solicitado,
                valor_cuota_proyectada,
                id_solicitud_modelo,
                version_modelo,
                cantidad_modelos_detectados,
                cantidad_personas,
                cantidad_personas_con_resultado,
                cantidad_personas_evaluadas,
                cantidad_personas_pendientes,
                analisis_solicitud_completo,
                detalle_personas_pendientes,
                puntaje_referencia_solicitud,
                perfil_riesgo_solicitud,
                recomendacion_solicitud,
                cumple_otorgamiento_solicitud,
                estado_analisis_solicitud,
                motivo_resultado_solicitud
            FROM cartera.vw_solicitudes_analisis_resultado_solicitud
            WHERE id_solicitud_credito = :idSolicitudCredito
            """;


    // =========================================================
    // RESULTADOS POR DEUDOR
    // =========================================================

    private static final String SQL_RESULTADOS_DEUDORES = """
            SELECT
                id_solicitud_credito,
                numero_solicitud,
                id_solicitud_deudor,
                id_datos_personal,
                tipo_deudor,
                orden_deudor,
                id_solicitud_modelo,
                version_modelo,
                nombre_modelo,
                cantidad_componentes_obligatorios,
                cantidad_componentes_evaluados,
                cantidad_componentes_pendientes,
                analisis_completo,
                componentes_pendientes,
                indicador_capacidad_pago,
                indicador_endeudamiento,
                indicador_razon_corriente,
                puntaje_central_fuente,
                mora_maxima_24_meses,
                puntaje_capacidad_pago,
                puntaje_endeudamiento,
                puntaje_razon_corriente,
                puntaje_central_cuantitativo,
                puntaje_central_cualitativo,
                puntaje_garantia,
                puntaje_habito_pago_interno,
                puntaje_total,
                sin_capacidad_pago,
                perfil_riesgo,
                recomendacion,
                cumple_otorgamiento,
                motivo_resultado
            FROM cartera.vw_solicitudes_analisis_resultado_deudor
            WHERE id_solicitud_credito = :idSolicitudCredito
            ORDER BY
                orden_deudor,
                id_solicitud_deudor
            """;


    // =========================================================
    // COMPONENTES
    // =========================================================

    private static final String SQL_COMPONENTES = """
            SELECT
                id_solicitud_credito,
                numero_solicitud,
                id_solicitud_deudor,
                id_datos_personal,
                tipo_deudor,
                orden_deudor,
                id_solicitud_modelo,
                version_modelo,
                nombre_modelo,
                id_solicitud_modelo_componente,
                codigo_componente,
                nombre_componente,
                orden_componente,
                ponderacion,
                tipo_valor,
                obligatorio,
                valor_numerico_componente,
                valor_texto_componente,
                id_solicitud_modelo_regla,
                orden_regla,
                puntaje_obtenido,
                puntaje_ponderado,
                cumple,
                descripcion_resultado,
                regla_encontrada,
                estado_componente
            FROM cartera.vw_solicitudes_analisis_componentes
            WHERE id_solicitud_credito = :idSolicitudCredito
            ORDER BY
                orden_deudor,
                orden_componente,
                id_solicitud_modelo_componente
            """;


    // =========================================================
    // AGENCIA
    // =========================================================

    private static final String SQL_AGENCIA_SOLICITUD = """
            SELECT id_agencia
            FROM cartera.solicitudes_creditos
            WHERE id_solicitud_credito = :idSolicitudCredito
              AND activo = true
            """;


    // =========================================================
    // BLOQUEAR SOLICITUD
    // =========================================================

    private static final String SQL_BLOQUEAR_SOLICITUD = """
            SELECT id_solicitud_credito
            FROM cartera.solicitudes_creditos
            WHERE id_solicitud_credito = :idSolicitudCredito
              AND activo = true
            FOR UPDATE
            """;


    // =========================================================
    // CONTAR DEUDORES ACTIVOS
    // =========================================================

    private static final String SQL_CONTAR_DEUDORES = """
            SELECT COUNT(*)::integer
            FROM cartera.solicitudes_deudores
            WHERE id_solicitud_credito = :idSolicitudCredito
              AND activo = true
            """;


    // =========================================================
    // CONTAR RESULTADOS CALCULADOS
    // =========================================================

    private static final String SQL_CONTAR_RESULTADOS = """
            SELECT COUNT(*)::integer
            FROM cartera.vw_solicitudes_analisis_resultado_deudor
            WHERE id_solicitud_credito = :idSolicitudCredito
            """;


    // =========================================================
    // UPSERT ANÁLISIS
    // =========================================================

    private static final String SQL_GUARDAR_ANALISIS = """
            INSERT INTO cartera.solicitudes_analisis
            (
                id_solicitud_credito,
                id_solicitud_proceso,
                fecha_analisis,

                total_ingresos,
                total_egresos,
                valor_cuotas_entidades,
                valor_cuota_proyectada,
                ingreso_disponible,

                indicador_capacidad_pago,
                puntaje_capacidad_pago,
                cumple_capacidad_pago,

                activos_totales,
                pasivos_totales,
                patrimonio_total,

                puntaje_solvencia,

                saldo_centrales,
                cuota_centrales,
                puntaje_central,
                calificacion_central,
                puntaje_componente_centrales,

                valor_garantias,
                valor_garantias_disponible,
                valor_garantias_asignado,
                porcentaje_cobertura,
                puntaje_garantias,
                cumple_garantias,

                mora_maxima_historica,
                mora_maxima_24_meses,
                cantidad_eventos_mora,
                edad_riesgo_maxima,
                puntaje_comportamiento_interno,

                puntaje_total,
                perfil_riesgo,
                recomendacion,
                cumple_otorgamiento,
                motivo_resultado,

                fk_seguridad_creacion,
                fecha_creacion,

                id_solicitud_modelo,
                id_solicitud_deudor,

                indicador_endeudamiento,
                puntaje_endeudamiento,

                indicador_razon_corriente,
                puntaje_razon_corriente,

                puntaje_central_cuantitativo,
                puntaje_central_cualitativo,

                fk_seguridad_edicion,
                fecha_edicion
            )
            SELECT
                rd.id_solicitud_credito,
                b.id_solicitud_proceso,
                CURRENT_TIMESTAMP,

                b.total_ingresos,
                b.total_egresos,
                b.valor_cuotas_entidades,
                b.valor_cuota_proyectada,
                b.ingreso_disponible,

                rd.indicador_capacidad_pago,
                rd.puntaje_capacidad_pago,
                cap.cumple,

                b.total_activos,
                b.total_pasivos,
                b.patrimonio_total,

                CASE
                    WHEN rd.puntaje_endeudamiento IS NULL
                      OR rd.puntaje_razon_corriente IS NULL
                        THEN NULL
                    ELSE
                        rd.puntaje_endeudamiento
                        +
                        rd.puntaje_razon_corriente
                END,

                b.saldo_centrales,
                b.cuota_centrales,
                b.score_central,
                b.estado_consulta_cualitativo,

                CASE
                    WHEN rd.puntaje_central_cuantitativo IS NULL
                      OR rd.puntaje_central_cualitativo IS NULL
                        THEN NULL
                    ELSE
                        rd.puntaje_central_cuantitativo
                        +
                        rd.puntaje_central_cualitativo
                END,

                b.valor_garantia_admisible,
                b.valor_garantia_disponible,
                b.valor_asignado_solicitud,
                b.porcentaje_cobertura_solicitud,
                rd.puntaje_garantia,
                NULL,

                NULL,
                rd.mora_maxima_24_meses,
                b.cantidad_eventos_mora,
                b.edad_riesgo_maxima,
                rd.puntaje_habito_pago_interno,

                rd.puntaje_total,
                rd.perfil_riesgo,
                rd.recomendacion,
                rd.cumple_otorgamiento,
                rd.motivo_resultado,

                :idUsuario,
                CURRENT_TIMESTAMP,

                rd.id_solicitud_modelo,
                rd.id_solicitud_deudor,

                rd.indicador_endeudamiento,
                rd.puntaje_endeudamiento,

                rd.indicador_razon_corriente,
                rd.puntaje_razon_corriente,

                rd.puntaje_central_cuantitativo,
                rd.puntaje_central_cualitativo,

                :idUsuario,
                CURRENT_TIMESTAMP

            FROM cartera.vw_solicitudes_analisis_resultado_deudor rd

            INNER JOIN cartera.vw_solicitudes_analisis_base b
                ON b.id_solicitud_credito =
                   rd.id_solicitud_credito
               AND b.id_solicitud_deudor =
                   rd.id_solicitud_deudor

            LEFT JOIN cartera.vw_solicitudes_analisis_componentes cap
                ON cap.id_solicitud_credito =
                   rd.id_solicitud_credito
               AND cap.id_solicitud_deudor =
                   rd.id_solicitud_deudor
               AND cap.codigo_componente =
                   'CAPACIDAD_PAGO'

            WHERE rd.id_solicitud_credito =
                  :idSolicitudCredito

            ON CONFLICT (id_solicitud_deudor)
            DO UPDATE
            SET
                id_solicitud_credito =
                    EXCLUDED.id_solicitud_credito,

                id_solicitud_proceso =
                    EXCLUDED.id_solicitud_proceso,

                fecha_analisis =
                    CURRENT_TIMESTAMP,

                total_ingresos =
                    EXCLUDED.total_ingresos,

                total_egresos =
                    EXCLUDED.total_egresos,

                valor_cuotas_entidades =
                    EXCLUDED.valor_cuotas_entidades,

                valor_cuota_proyectada =
                    EXCLUDED.valor_cuota_proyectada,

                ingreso_disponible =
                    EXCLUDED.ingreso_disponible,

                indicador_capacidad_pago =
                    EXCLUDED.indicador_capacidad_pago,

                puntaje_capacidad_pago =
                    EXCLUDED.puntaje_capacidad_pago,

                cumple_capacidad_pago =
                    EXCLUDED.cumple_capacidad_pago,

                activos_totales =
                    EXCLUDED.activos_totales,

                pasivos_totales =
                    EXCLUDED.pasivos_totales,

                patrimonio_total =
                    EXCLUDED.patrimonio_total,

                puntaje_solvencia =
                    EXCLUDED.puntaje_solvencia,

                saldo_centrales =
                    EXCLUDED.saldo_centrales,

                cuota_centrales =
                    EXCLUDED.cuota_centrales,

                puntaje_central =
                    EXCLUDED.puntaje_central,

                calificacion_central =
                    EXCLUDED.calificacion_central,

                puntaje_componente_centrales =
                    EXCLUDED.puntaje_componente_centrales,

                valor_garantias =
                    EXCLUDED.valor_garantias,

                valor_garantias_disponible =
                    EXCLUDED.valor_garantias_disponible,

                valor_garantias_asignado =
                    EXCLUDED.valor_garantias_asignado,

                porcentaje_cobertura =
                    EXCLUDED.porcentaje_cobertura,

                puntaje_garantias =
                    EXCLUDED.puntaje_garantias,

                cumple_garantias =
                    EXCLUDED.cumple_garantias,

                mora_maxima_historica =
                    EXCLUDED.mora_maxima_historica,

                mora_maxima_24_meses =
                    EXCLUDED.mora_maxima_24_meses,

                cantidad_eventos_mora =
                    EXCLUDED.cantidad_eventos_mora,

                edad_riesgo_maxima =
                    EXCLUDED.edad_riesgo_maxima,

                puntaje_comportamiento_interno =
                    EXCLUDED.puntaje_comportamiento_interno,

                puntaje_total =
                    EXCLUDED.puntaje_total,

                perfil_riesgo =
                    EXCLUDED.perfil_riesgo,

                recomendacion =
                    EXCLUDED.recomendacion,

                cumple_otorgamiento =
                    EXCLUDED.cumple_otorgamiento,

                motivo_resultado =
                    EXCLUDED.motivo_resultado,

                id_solicitud_modelo =
                    EXCLUDED.id_solicitud_modelo,

                indicador_endeudamiento =
                    EXCLUDED.indicador_endeudamiento,

                puntaje_endeudamiento =
                    EXCLUDED.puntaje_endeudamiento,

                indicador_razon_corriente =
                    EXCLUDED.indicador_razon_corriente,

                puntaje_razon_corriente =
                    EXCLUDED.puntaje_razon_corriente,

                puntaje_central_cuantitativo =
                    EXCLUDED.puntaje_central_cuantitativo,

                puntaje_central_cualitativo =
                    EXCLUDED.puntaje_central_cualitativo,

                fk_seguridad_edicion =
                    :idUsuario,

                fecha_edicion =
                    CURRENT_TIMESTAMP
            """;


    // =========================================================
    // ELIMINAR DETALLE ACTUAL
    // =========================================================

    private static final String SQL_ELIMINAR_DETALLES = """
            DELETE FROM cartera.solicitudes_analisis_detalle sad
            USING cartera.solicitudes_analisis sa
            WHERE sad.id_solicitud_analisis =
                  sa.id_solicitud_analisis
              AND sa.id_solicitud_credito =
                  :idSolicitudCredito
            """;


    // =========================================================
    // INSERTAR DETALLES ACTUALES
    // =========================================================

    private static final String SQL_GUARDAR_DETALLES = """
            INSERT INTO cartera.solicitudes_analisis_detalle
            (
                id_solicitud_analisis,
                codigo_componente,
                nombre_componente,
                orden_componente,
                valor_numerico,
                valor_texto,
                puntaje_obtenido,
                ponderacion,
                puntaje_ponderado,
                cumple,
                observacion,
                fk_seguridad_creacion,
                fecha_creacion
            )
            SELECT
                sa.id_solicitud_analisis,

                c.codigo_componente,
                c.nombre_componente,
                c.orden_componente,

                c.valor_numerico_componente,
                c.valor_texto_componente,

                c.puntaje_obtenido,
                c.ponderacion,
                c.puntaje_ponderado,

                c.cumple,

                CASE
                    WHEN c.estado_componente = 'EVALUADO'
                        THEN c.descripcion_resultado

                    WHEN c.descripcion_resultado IS NOT NULL
                        THEN
                            c.estado_componente
                            || ' - '
                            || c.descripcion_resultado

                    ELSE
                        c.estado_componente
                END,

                :idUsuario,
                CURRENT_TIMESTAMP

            FROM cartera.vw_solicitudes_analisis_componentes c

            INNER JOIN cartera.solicitudes_analisis sa
                ON sa.id_solicitud_deudor =
                   c.id_solicitud_deudor

            WHERE c.id_solicitud_credito =
                  :idSolicitudCredito

              AND sa.id_solicitud_credito =
                  :idSolicitudCredito

            ORDER BY
                c.orden_deudor,
                c.orden_componente
            """;


    // =========================================================
    // ACTUALIZAR SOLICITUD
    // =========================================================

    private static final String SQL_ACTUALIZAR_SOLICITUD = """
            UPDATE cartera.solicitudes_creditos
            SET
                fecha_ultima_gestion =
                    CURRENT_TIMESTAMP,
                fk_seguridad_edicion =
                    :idUsuario,
                fecha_edicion =
                    CURRENT_TIMESTAMP
            WHERE id_solicitud_credito =
                  :idSolicitudCredito
            """;


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<SolicitudAnalisisResultadoDTO>
            RESULTADO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudAnalisisResultadoDTO.class
            );

    private static final BeanPropertyRowMapper<SolicitudAnalisisDeudorDTO>
            DEUDOR_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudAnalisisDeudorDTO.class
            );

    private static final BeanPropertyRowMapper<SolicitudAnalisisComponenteDTO>
            COMPONENTE_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudAnalisisComponenteDTO.class
            );


    // =========================================================
    // DEPENDENCIA
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudAnalisisRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // RESULTADO SOLICITUD
    // =========================================================

    public Optional<SolicitudAnalisisResultadoDTO> obtenerResultadoSolicitud(
            Integer idSolicitudCredito
    ) {

        List<SolicitudAnalisisResultadoDTO> resultados =
                jdbc.query(
                        SQL_RESULTADO_SOLICITUD,
                        parametroSolicitud(
                                idSolicitudCredito
                        ),
                        RESULTADO_MAPPER
                );

        return resultados.stream().findFirst();
    }


    // =========================================================
    // RESULTADOS DEUDORES
    // =========================================================

    public List<SolicitudAnalisisDeudorDTO> listarDeudores(
            Integer idSolicitudCredito
    ) {

        return jdbc.query(
                SQL_RESULTADOS_DEUDORES,
                parametroSolicitud(
                        idSolicitudCredito
                ),
                DEUDOR_MAPPER
        );
    }


    // =========================================================
    // COMPONENTES
    // =========================================================

    public List<SolicitudAnalisisComponenteDTO> listarComponentes(
            Integer idSolicitudCredito
    ) {

        return jdbc.query(
                SQL_COMPONENTES,
                parametroSolicitud(
                        idSolicitudCredito
                ),
                COMPONENTE_MAPPER
        );
    }


    // =========================================================
    // AGENCIA
    // =========================================================

    public Optional<Integer> obtenerAgenciaSolicitud(
            Integer idSolicitudCredito
    ) {

        List<Integer> resultados =
                jdbc.query(
                        SQL_AGENCIA_SOLICITUD,
                        parametroSolicitud(
                                idSolicitudCredito
                        ),
                        (rs, rowNum) ->
                                rs.getInt("id_agencia")
                );

        return resultados.stream().findFirst();
    }


    // =========================================================
    // PERSISTIR
    // =========================================================

    public SolicitudAnalisisPersistenciaDTO persistirAnalisis(
            Integer idSolicitudCredito,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
                parametroSolicitud(
                        idSolicitudCredito
                )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        // -----------------------------------------------------
        // Bloqueo de la solicitud durante la persistencia
        // -----------------------------------------------------

        List<Integer> solicitudes =
                jdbc.query(
                        SQL_BLOQUEAR_SOLICITUD,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt(
                                        "id_solicitud_credito"
                                )
                );

        if (solicitudes.isEmpty()) {

            throw new IllegalArgumentException(
                    "La solicitud de crédito no existe o se encuentra inactiva."
            );
        }


        // -----------------------------------------------------
        // Validar población
        // -----------------------------------------------------

        Integer cantidadDeudores =
                jdbc.queryForObject(
                        SQL_CONTAR_DEUDORES,
                        parametros,
                        Integer.class
                );

        if (cantidadDeudores == null
                || cantidadDeudores <= 0) {

            throw new IllegalStateException(
                    "La solicitud no tiene deudores activos."
            );
        }


        Integer cantidadResultados =
                jdbc.queryForObject(
                        SQL_CONTAR_RESULTADOS,
                        parametros,
                        Integer.class
                );

        if (cantidadResultados == null
                || !cantidadResultados.equals(
                cantidadDeudores
        )) {

            throw new IllegalStateException(
                    "No fue posible estructurar el análisis de todos los deudores de la solicitud."
            );
        }


        // -----------------------------------------------------
        // Resultado global
        // -----------------------------------------------------

        SolicitudAnalisisResultadoDTO resultado =
                obtenerResultadoSolicitud(
                        idSolicitudCredito
                )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No fue posible obtener el resultado global de la solicitud."
                                        )
                        );


        if (resultado.getIdSolicitudModelo() == null) {

            throw new IllegalStateException(
                    "No existe un modelo de otorgamiento aplicable a la solicitud."
            );
        }


        if (resultado.getCantidadModelosDetectados() == null
                || resultado.getCantidadModelosDetectados() != 1) {

            throw new IllegalStateException(
                    "La solicitud presenta inconsistencia en el modelo de otorgamiento."
            );
        }


        // -----------------------------------------------------
        // Un único análisis por deudor
        // -----------------------------------------------------

        int analisisGuardados =
                jdbc.update(
                        SQL_GUARDAR_ANALISIS,
                        parametros
                );


        if (analisisGuardados != cantidadDeudores) {

            throw new IllegalStateException(
                    "La cantidad de análisis guardados no coincide con la cantidad de deudores activos."
            );
        }


        // -----------------------------------------------------
        // Los componentes se reconstruyen con el cálculo actual.
        // No representan ejecuciones históricas.
        // -----------------------------------------------------

        jdbc.update(
                SQL_ELIMINAR_DETALLES,
                parametros
        );

        int detallesGuardados =
                jdbc.update(
                        SQL_GUARDAR_DETALLES,
                        parametros
                );


        // -----------------------------------------------------
        // Última gestión de la solicitud
        // -----------------------------------------------------

        jdbc.update(
                SQL_ACTUALIZAR_SOLICITUD,
                parametros
        );


        // -----------------------------------------------------
        // Respuesta temporal.
        //
        // idSolicitudAnalisisEjecucion queda null porque
        // la ejecución histórica fue eliminada.
        // El DTO se ajusta en el siguiente archivo.
        // -----------------------------------------------------

        return SolicitudAnalisisPersistenciaDTO.builder()
                .analisisGuardados(
                        analisisGuardados
                )
                .detallesGuardados(
                        detallesGuardados
                )
                .estadoAnalisis(
                        resultado.getEstadoAnalisisSolicitud()
                )
                .recomendacion(
                        resultado.getRecomendacionSolicitud()
                )
                .build();
    }


    // =========================================================
    // SOPORTE
    // =========================================================

    private MapSqlParameterSource parametroSolicitud(
            Integer idSolicitudCredito
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "idSolicitudCredito",
                        idSolicitudCredito
                );
    }
}