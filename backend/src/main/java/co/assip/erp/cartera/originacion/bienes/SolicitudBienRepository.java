package co.assip.erp.cartera.originacion.bienes;

import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienCreditoRespaldadoDTO;
import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudBienRepository {

    // =========================================================
    // LISTAR BIENES DISPONIBLES POR SOLICITUD
    // =========================================================
    //
    // La fuente económica siempre es la información ACTUAL
    // de Hoja de Vida.
    //
    // solicitudes_deudores_bienes únicamente indica si el bien
    // ya fue seleccionado y conserva la fotografía de la
    // solicitud.
    //
    // =========================================================

    private static final String SQL_LISTAR_POR_SOLICITUD = """
            SELECT
                sd.id_solicitud_credito,
                sd.id_solicitud_deudor,

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

                vb.id_bien_persona,
                vb.id_bien,
                vb.id_tipo_bien,

                vb.codigo_tipo_bien,
                vb.nombre_tipo_bien,

                vb.descripcion_general,
                vb.fecha_adquisicion,
                vb.estado_bien,

                vb.porcentaje_propiedad,

                vb.valor_comercial,
                vb.valor_gravamen,
                vb.valor_propiedad,
                vb.valor_gravamen_propiedad,
                vb.valor_neto_propiedad,

                vb.cantidad_creditos_respaldados,
                vb.valor_creditos_respaldados,

                sdb.id_solicitud_deudor_bien,

                CASE
                    WHEN sdb.id_solicitud_deudor_bien IS NOT NULL
                         AND sdb.activo = true
                    THEN true
                    ELSE false
                END AS seleccionado,

                sdb.fecha_fotografia,

                sdb.porcentaje_admisible,
                sdb.valor_garantia_admisible,
                sdb.valor_comprometido_creditos,
                sdb.valor_garantia_disponible,
                sdb.valor_requerido_solicitud,
                sdb.valor_asignado_solicitud,
                sdb.observacion

            FROM cartera.solicitudes_deudores sd

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal =
                   sd.id_datos_personal

            INNER JOIN cartera.vw_solicitudes_bienes_disponibilidad vb
                ON vb.id_datos_personal =
                   sd.id_datos_personal

            LEFT JOIN cartera.solicitudes_deudores_bienes sdb
                ON sdb.id_solicitud_deudor =
                   sd.id_solicitud_deudor
               AND sdb.id_bien_persona =
                   vb.id_bien_persona

            WHERE sd.id_solicitud_credito =
                  :idSolicitudCredito

              AND sd.activo = true

              AND vb.estado_bien = 'A'

            ORDER BY
                sd.orden_deudor,
                vb.nombre_tipo_bien,
                vb.descripcion_general,
                vb.id_bien
            """;


    // =========================================================
    // LISTAR BIENES POR DEUDOR
    // =========================================================

    private static final String SQL_LISTAR_POR_DEUDOR = """
            SELECT
                sd.id_solicitud_credito,
                sd.id_solicitud_deudor,

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

                vb.id_bien_persona,
                vb.id_bien,
                vb.id_tipo_bien,

                vb.codigo_tipo_bien,
                vb.nombre_tipo_bien,

                vb.descripcion_general,
                vb.fecha_adquisicion,
                vb.estado_bien,

                vb.porcentaje_propiedad,

                vb.valor_comercial,
                vb.valor_gravamen,
                vb.valor_propiedad,
                vb.valor_gravamen_propiedad,
                vb.valor_neto_propiedad,

                vb.cantidad_creditos_respaldados,
                vb.valor_creditos_respaldados,

                sdb.id_solicitud_deudor_bien,

                CASE
                    WHEN sdb.id_solicitud_deudor_bien IS NOT NULL
                         AND sdb.activo = true
                    THEN true
                    ELSE false
                END AS seleccionado,

                sdb.fecha_fotografia,

                sdb.porcentaje_admisible,
                sdb.valor_garantia_admisible,
                sdb.valor_comprometido_creditos,
                sdb.valor_garantia_disponible,
                sdb.valor_requerido_solicitud,
                sdb.valor_asignado_solicitud,
                sdb.observacion

            FROM cartera.solicitudes_deudores sd

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal =
                   sd.id_datos_personal

            INNER JOIN cartera.vw_solicitudes_bienes_disponibilidad vb
                ON vb.id_datos_personal =
                   sd.id_datos_personal

            LEFT JOIN cartera.solicitudes_deudores_bienes sdb
                ON sdb.id_solicitud_deudor =
                   sd.id_solicitud_deudor
               AND sdb.id_bien_persona =
                   vb.id_bien_persona

            WHERE sd.id_solicitud_deudor =
                  :idSolicitudDeudor

              AND sd.activo = true

              AND vb.estado_bien = 'A'

            ORDER BY
                vb.nombre_tipo_bien,
                vb.descripcion_general,
                vb.id_bien
            """;


    // =========================================================
    // BUSCAR BIEN ACTUAL DEL DEUDOR
    // =========================================================
    //
    // Esta consulta es la fuente segura para construir la
    // fotografía. Los valores NO vienen del frontend.
    //
    // =========================================================

    private static final String SQL_BUSCAR_BIEN_ACTUAL = """
            SELECT
                sd.id_solicitud_credito,
                sd.id_solicitud_deudor,

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

                vb.id_bien_persona,
                vb.id_bien,
                vb.id_tipo_bien,

                vb.codigo_tipo_bien,
                vb.nombre_tipo_bien,

                vb.descripcion_general,
                vb.fecha_adquisicion,
                vb.estado_bien,

                vb.porcentaje_propiedad,

                vb.valor_comercial,
                vb.valor_gravamen,
                vb.valor_propiedad,
                vb.valor_gravamen_propiedad,
                vb.valor_neto_propiedad,

                vb.cantidad_creditos_respaldados,
                vb.valor_creditos_respaldados,

                sdb.id_solicitud_deudor_bien,

                CASE
                    WHEN sdb.id_solicitud_deudor_bien IS NOT NULL
                         AND sdb.activo = true
                    THEN true
                    ELSE false
                END AS seleccionado,

                sdb.fecha_fotografia,

                sdb.porcentaje_admisible,
                sdb.valor_garantia_admisible,
                sdb.valor_comprometido_creditos,
                sdb.valor_garantia_disponible,
                sdb.valor_requerido_solicitud,
                sdb.valor_asignado_solicitud,
                sdb.observacion

            FROM cartera.solicitudes_deudores sd

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal =
                   sd.id_datos_personal

            INNER JOIN cartera.vw_solicitudes_bienes_disponibilidad vb
                ON vb.id_datos_personal =
                   sd.id_datos_personal

            LEFT JOIN cartera.solicitudes_deudores_bienes sdb
                ON sdb.id_solicitud_deudor =
                   sd.id_solicitud_deudor
               AND sdb.id_bien_persona =
                   vb.id_bien_persona

            WHERE sd.id_solicitud_deudor =
                  :idSolicitudDeudor

              AND vb.id_bien_persona =
                  :idBienPersona

              AND sd.activo = true

              AND vb.estado_bien = 'A'
            """;


    // =========================================================
    // CRÉDITOS ACTUALES RESPALDADOS POR EL BIEN
    // =========================================================

    private static final String SQL_CREDITOS_RESPALDADOS = """
            SELECT
                id_bien,

                id_cartera_credito,
                id_obligacion_juridica,
                id_agencia,
                id_linea_credito,
                pagare_cartera,

                id_datos_personal,
                documento,
                nombre_completo,

                fecha_desembolso,
                valor_inicial_credito,
                valor_desembolsado,
                valor_cuota,
                saldo_actual,

                codigo_garantia_credito,
                descripcion_garantia_credito,
                tipo_garantia,

                codigo_estado_cartera,
                codigo_estado_juridico

            FROM cartera.vw_solicitudes_bienes_creditos_respaldados

            WHERE id_bien =
                  :idBien

            ORDER BY
                saldo_actual DESC,
                id_cartera_credito
            """;


    // =========================================================
    // BLOQUEAR SOLICITUD EDITABLE
    // =========================================================
    //
    // Igual filosofía utilizada en Financiero y Central:
    // solamente se puede modificar una solicitud cuyo resultado
    // todavía no sea final.
    //
    // =========================================================

    private static final String SQL_BLOQUEAR_SOLICITUD_EDITABLE = """
            SELECT
                sc.id_solicitud_credito

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
    // VALIDAR QUE EL BIEN PERTENECE AL DEUDOR
    // =========================================================

    private static final String SQL_EXISTE_BIEN_DEUDOR = """
            SELECT EXISTS (
                SELECT 1

                FROM cartera.solicitudes_deudores sd

                INNER JOIN hoja_vida.bienes_personas bp
                    ON bp.id_datos_personal =
                       sd.id_datos_personal

                INNER JOIN hoja_vida.bienes b
                    ON b.id_bien =
                       bp.id_bien

                WHERE sd.id_solicitud_deudor =
                      :idSolicitudDeudor

                  AND bp.id_bien_persona =
                      :idBienPersona

                  AND sd.activo = true
                  AND b.estado_bien = 'A'
            )
            """;


    // =========================================================
    // GUARDAR / ACTUALIZAR FOTOGRAFÍA
    // =========================================================
    //
    // Regla:
    //
    // - el frontend solamente envía el bien seleccionado;
    // - los valores económicos se toman de la vista actual;
    // - al volver a seleccionar se actualiza la misma fotografía;
    // - se conserva la auditoría de creación;
    // - valor_creditos_respaldados se congela actualmente en
    //   valor_comprometido_creditos;
    // - los valores de admisibilidad no se inventan aquí.
    //
    // =========================================================

    private static final String SQL_GUARDAR_FOTOGRAFIA = """
        INSERT INTO cartera.solicitudes_deudores_bienes (
            id_solicitud_deudor,
            id_bien_persona,

            fecha_fotografia,

            porcentaje_propiedad,
            valor_comercial,
            valor_gravamen,

            es_garantia_real,

            porcentaje_admisible,
            valor_comprometido_creditos,

            valor_requerido_solicitud,
            valor_asignado_solicitud,

            observacion,
            activo,

            fk_seguridad_creacion,
            fecha_creacion,
            fk_seguridad_edicion,
            fecha_edicion
        )

        SELECT
            :idSolicitudDeudor,
            vb.id_bien_persona,

            CURRENT_TIMESTAMP,

            vb.porcentaje_propiedad,
            vb.valor_comercial,
            vb.valor_gravamen,

            false,

            0,
            vb.valor_creditos_respaldados,

            0,
            0,

            :observacion,
            true,

            :idUsuario,
            CURRENT_TIMESTAMP,
            :idUsuario,
            CURRENT_TIMESTAMP

        FROM cartera.solicitudes_deudores sd

        INNER JOIN cartera.vw_solicitudes_bienes_disponibilidad vb
            ON vb.id_datos_personal =
               sd.id_datos_personal

        WHERE sd.id_solicitud_deudor =
              :idSolicitudDeudor

          AND vb.id_bien_persona =
              :idBienPersona

          AND sd.activo = true

          AND vb.estado_bien = 'A'

        ON CONFLICT (id_solicitud_deudor, id_bien_persona)
        DO UPDATE
        SET
            fecha_fotografia =
                CURRENT_TIMESTAMP,

            porcentaje_propiedad =
                EXCLUDED.porcentaje_propiedad,

            valor_comercial =
                EXCLUDED.valor_comercial,

            valor_gravamen =
                EXCLUDED.valor_gravamen,

            valor_comprometido_creditos =
                EXCLUDED.valor_comprometido_creditos,

            observacion =
                EXCLUDED.observacion,

            activo =
                true,

            fk_seguridad_edicion =
                :idUsuario,

            fecha_edicion =
                CURRENT_TIMESTAMP

        RETURNING id_solicitud_deudor_bien
        """;


    // =========================================================
    // DESACTIVAR FOTOGRAFÍA
    // =========================================================

    private static final String SQL_DESACTIVAR_FOTOGRAFIA = """
            UPDATE cartera.solicitudes_deudores_bienes

            SET
                activo = false,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP

            WHERE id_solicitud_deudor =
                  :idSolicitudDeudor

              AND id_bien_persona =
                  :idBienPersona

              AND activo = true
            """;


    // =========================================================
    // BUSCAR ID DE FOTOGRAFÍA ACTIVA
    // =========================================================

    private static final String SQL_ID_FOTOGRAFIA_ACTIVA = """
            SELECT
                id_solicitud_deudor_bien

            FROM cartera.solicitudes_deudores_bienes

            WHERE id_solicitud_deudor =
                  :idSolicitudDeudor

              AND id_bien_persona =
                  :idBienPersona

              AND activo = true
            """;


    // =========================================================
    // CONTAR BIENES SELECCIONADOS DE LA SOLICITUD
    // =========================================================

    private static final String SQL_CONTAR_SELECCIONADOS = """
            SELECT
                COUNT(*)

            FROM cartera.solicitudes_deudores sd

            INNER JOIN cartera.solicitudes_deudores_bienes sdb
                ON sdb.id_solicitud_deudor =
                   sd.id_solicitud_deudor

            WHERE sd.id_solicitud_credito =
                  :idSolicitudCredito

              AND sd.activo = true
              AND sdb.activo = true
            """;


    // =========================================================
    // TIPO DE GARANTÍA DE LA SOLICITUD
    // =========================================================

    private static final String SQL_TIPO_GARANTIA_SOLICITUD = """
            SELECT
                gc.tipo_garantia

            FROM cartera.solicitudes_creditos sc

            INNER JOIN cartera.garantias_creditos gc
                ON gc.codigo_garantia_credito =
                   sc.codigo_garantia_credito

            WHERE sc.id_solicitud_credito =
                  :idSolicitudCredito

              AND sc.activo = true
            """;


    // =========================================================
    // ACTUALIZAR ÚLTIMA GESTIÓN
    // =========================================================

    private static final String SQL_ACTUALIZAR_ULTIMA_GESTION = """
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

    private static final BeanPropertyRowMapper<SolicitudBienDTO>
            BIEN_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudBienDTO.class
            );

    private static final BeanPropertyRowMapper<SolicitudBienCreditoRespaldadoDTO>
            CREDITO_RESPALDADO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudBienCreditoRespaldadoDTO.class
            );


    // =========================================================
    // DEPENDENCIA
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudBienRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    public List<SolicitudBienDTO> listarPorSolicitud(
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
                BIEN_MAPPER
        );
    }


    // =========================================================
    // LISTAR POR DEUDOR
    // =========================================================

    public List<SolicitudBienDTO> listarPorDeudor(
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
                BIEN_MAPPER
        );
    }


    // =========================================================
    // BUSCAR BIEN ACTUAL
    // =========================================================

    public Optional<SolicitudBienDTO> buscarBienActual(
            Integer idSolicitudDeudor,
            Long idBienPersona
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        )
                        .addValue(
                                "idBienPersona",
                                idBienPersona
                        );

        return jdbc.query(
                        SQL_BUSCAR_BIEN_ACTUAL,
                        parametros,
                        BIEN_MAPPER
                )
                .stream()
                .findFirst();
    }


    // =========================================================
    // CRÉDITOS RESPALDADOS
    // =========================================================

    public List<SolicitudBienCreditoRespaldadoDTO>
    listarCreditosRespaldados(
            Long idBien
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idBien",
                                idBien
                        );

        return jdbc.query(
                SQL_CREDITOS_RESPALDADOS,
                parametros,
                CREDITO_RESPALDADO_MAPPER
        );
    }


    // =========================================================
    // BLOQUEAR SOLICITUD EDITABLE
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

        return jdbc.query(
                        SQL_BLOQUEAR_SOLICITUD_EDITABLE,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt(
                                        "id_solicitud_credito"
                                )
                )
                .stream()
                .findFirst();
    }


    // =========================================================
    // VALIDAR BIEN DEL DEUDOR
    // =========================================================

    public boolean existeBienDeudor(
            Integer idSolicitudDeudor,
            Long idBienPersona
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        )
                        .addValue(
                                "idBienPersona",
                                idBienPersona
                        );

        Boolean existe = jdbc.queryForObject(
                SQL_EXISTE_BIEN_DEUDOR,
                parametros,
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }


    // =========================================================
    // GUARDAR FOTOGRAFÍA
    // =========================================================

    public Integer guardarFotografia(
            Integer idSolicitudDeudor,
            Long idBienPersona,
            String observacion,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        )
                        .addValue(
                                "idBienPersona",
                                idBienPersona
                        )
                        .addValue(
                                "observacion",
                                observacion
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.queryForObject(
                SQL_GUARDAR_FOTOGRAFIA,
                parametros,
                Integer.class
        );
    }


    // =========================================================
    // DESACTIVAR FOTOGRAFÍA
    // =========================================================

    public int desactivarFotografia(
            Integer idSolicitudDeudor,
            Long idBienPersona,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        )
                        .addValue(
                                "idBienPersona",
                                idBienPersona
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                SQL_DESACTIVAR_FOTOGRAFIA,
                parametros
        );
    }


    // =========================================================
    // ID FOTOGRAFÍA ACTIVA
    // =========================================================

    public Optional<Integer> buscarIdFotografiaActiva(
            Integer idSolicitudDeudor,
            Long idBienPersona
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        )
                        .addValue(
                                "idBienPersona",
                                idBienPersona
                        );

        return jdbc.query(
                        SQL_ID_FOTOGRAFIA_ACTIVA,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt(
                                        "id_solicitud_deudor_bien"
                                )
                )
                .stream()
                .findFirst();
    }


    // =========================================================
    // CONTAR SELECCIONADOS
    // =========================================================

    public int contarSeleccionados(
            Integer idSolicitudCredito
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        Integer cantidad = jdbc.queryForObject(
                SQL_CONTAR_SELECCIONADOS,
                parametros,
                Integer.class
        );

        return cantidad != null
                ? cantidad
                : 0;
    }


    // =========================================================
    // TIPO GARANTÍA SOLICITUD
    // =========================================================

    public Optional<String> buscarTipoGarantiaSolicitud(
            Integer idSolicitudCredito
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        return jdbc.query(
                        SQL_TIPO_GARANTIA_SOLICITUD,
                        parametros,
                        (rs, rowNum) ->
                                rs.getString(
                                        "tipo_garantia"
                                )
                )
                .stream()
                .findFirst();
    }


    // =========================================================
    // ACTUALIZAR ÚLTIMA GESTIÓN
    // =========================================================

    public void actualizarUltimaGestion(
            Integer idSolicitudCredito,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        jdbc.update(
                SQL_ACTUALIZAR_ULTIMA_GESTION,
                parametros
        );
    }
}