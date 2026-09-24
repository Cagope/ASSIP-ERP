package co.assip.erp.cartera.referencias;

import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaListadoDTO;
import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaParticipanteDTO;
import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaPersonalDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SolicitudReferenciaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // MAPEADORES
    // =========================================================

    private final BeanPropertyRowMapper<SolicitudReferenciaListadoDTO>
            listadoMapper =
            BeanPropertyRowMapper.newInstance(
                    SolicitudReferenciaListadoDTO.class
            );

    private final BeanPropertyRowMapper<SolicitudReferenciaParticipanteDTO>
            participanteMapper =
            BeanPropertyRowMapper.newInstance(
                    SolicitudReferenciaParticipanteDTO.class
            );

    private final BeanPropertyRowMapper<SolicitudReferenciaPersonalDTO>
            referenciaMapper =
            BeanPropertyRowMapper.newInstance(
                    SolicitudReferenciaPersonalDTO.class
            );

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SolicitudReferenciaRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // SQL BASE - LISTADO DE SOLICITUDES
    // =========================================================

    private static final String SQL_LISTADO = """
            SELECT
                id_solicitud_credito,
                numero_solicitud,
                fecha_inicio_solicitud,
                fecha_ultima_gestion,

                id_agencia,
                codigo_agencia,
                nombre_agencia,

                id_datos_personal,
                tipo_documento,
                documento,
                nombre_solicitante,

                id_asesor,
                usuario_asesor,
                nombre_asesor,

                id_solicitud_proceso,
                nombre_proceso,

                id_solicitud_resultado,
                nombre_resultado,

                valor_solicitado,

                id_linea_credito,
                codigo_linea_credito,
                nombre_linea_credito,

                id_solicitud_referencia_proceso,

                estado_referencias,
                tipo_cierre,
                observacion_cierre,

                fecha_inicio_referencias,
                fecha_cierre,
                fk_seguridad_cierre,

                cantidad_referencias

            FROM cartera.vw_solicitudes_referencias_lista

            WHERE 1 = 1
            """;

    // =========================================================
    // SQL BASE - PARTICIPANTES
    // =========================================================

    private static final String SQL_PARTICIPANTES = """
            SELECT
                id_solicitud_credito,
                id_solicitud_deudor,
                id_datos_personal,

                tipo_deudor,
                orden_deudor,

                tipo_documento,
                documento,
                nombre_completo,

                id_solicitud_referencia_proceso,
                estado_referencias,

                cantidad_referencias,
                cantidad_contactadas,
                cantidad_no_contactadas,
                cantidad_pendientes

            FROM cartera.vw_solicitudes_referencias_participantes

            WHERE 1 = 1
            """;

    // =========================================================
    // SQL BASE - REFERENCIAS PERSONALES
    // =========================================================

    private static final String SQL_REFERENCIAS = """
            SELECT
                id_solicitud_referencia_personal,
                id_solicitud_referencia_proceso,

                id_solicitud_credito,
                id_solicitud_deudor,

                nombre_completo,
                telefono_celular,
                telefono_fijo,

                medio_entrevista,
                fecha_hora_llamada,
                contacto_establecido,
                concepto_referencia,

                fk_seguridad_entrevistador,

                activo,

                fk_seguridad_creacion,
                fecha_creacion,

                fk_seguridad_edicion,
                fecha_edicion

            FROM cartera.solicitudes_referencias_personales

            WHERE 1 = 1
            """;

    // =========================================================
    // 1. LISTAR SOLICITUDES
    // =========================================================

    public List<SolicitudReferenciaListadoDTO> listar(
            Integer idAgencia,
            String numeroSolicitud,
            String documento,
            String nombreSolicitante,
            Integer idAsesor,
            Integer idSolicitudProceso,
            String estadoReferencias
    ) {

        StringBuilder sql = new StringBuilder(SQL_LISTADO);

        MapSqlParameterSource params =
                new MapSqlParameterSource();

        // -----------------------------------------------------
        // AGENCIA
        // -----------------------------------------------------

        if (idAgencia != null && idAgencia > 0) {

            sql.append("""
                    
                    AND id_agencia = :idAgencia
                    """);

            params.addValue(
                    "idAgencia",
                    idAgencia
            );
        }

        // -----------------------------------------------------
        // NÚMERO DE SOLICITUD
        // -----------------------------------------------------

        if (tieneTexto(numeroSolicitud)) {

            sql.append("""
                    
                    AND numero_solicitud ILIKE :numeroSolicitud
                    """);

            params.addValue(
                    "numeroSolicitud",
                    "%" + numeroSolicitud.trim() + "%"
            );
        }

        // -----------------------------------------------------
        // DOCUMENTO
        // -----------------------------------------------------

        if (tieneTexto(documento)) {

            sql.append("""
                    
                    AND documento ILIKE :documento
                    """);

            params.addValue(
                    "documento",
                    "%" + documento.trim() + "%"
            );
        }

        // -----------------------------------------------------
        // NOMBRE DEL SOLICITANTE
        // -----------------------------------------------------

        if (tieneTexto(nombreSolicitante)) {

            sql.append("""
                    
                    AND nombre_solicitante ILIKE :nombreSolicitante
                    """);

            params.addValue(
                    "nombreSolicitante",
                    "%" + nombreSolicitante.trim() + "%"
            );
        }

        // -----------------------------------------------------
        // ASESOR
        // -----------------------------------------------------

        if (idAsesor != null && idAsesor > 0) {

            sql.append("""
                    
                    AND id_asesor = :idAsesor
                    """);

            params.addValue(
                    "idAsesor",
                    idAsesor
            );
        }

        // -----------------------------------------------------
        // ETAPA DE LA SOLICITUD
        // -----------------------------------------------------

        if (
                idSolicitudProceso != null
                        && idSolicitudProceso > 0
        ) {

            sql.append("""
                    
                    AND id_solicitud_proceso = :idSolicitudProceso
                    """);

            params.addValue(
                    "idSolicitudProceso",
                    idSolicitudProceso
            );
        }

        // -----------------------------------------------------
        // ESTADO DE REFERENCIAS
        // -----------------------------------------------------

        if (tieneTexto(estadoReferencias)) {

            sql.append("""
                    
                    AND estado_referencias = :estadoReferencias
                    """);

            params.addValue(
                    "estadoReferencias",
                    estadoReferencias.trim().toUpperCase()
            );
        }

        // -----------------------------------------------------
        // ORDEN
        // -----------------------------------------------------

        sql.append("""
                
                ORDER BY
                    fecha_inicio_solicitud DESC NULLS LAST,
                    id_solicitud_credito DESC
                """);

        return jdbc.query(
                sql.toString(),
                params,
                listadoMapper
        );
    }

    // =========================================================
    // 2. BUSCAR SOLICITUD
    // =========================================================

    public SolicitudReferenciaListadoDTO buscarPorSolicitud(
            Integer idSolicitudCredito
    ) {

        String sql = SQL_LISTADO + """
                
                AND id_solicitud_credito = :idSolicitudCredito
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        List<SolicitudReferenciaListadoDTO> resultados =
                jdbc.query(
                        sql,
                        params,
                        listadoMapper
                );

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }

    // =========================================================
    // 3. LISTAR PARTICIPANTES DE UNA SOLICITUD
    // =========================================================

    public List<SolicitudReferenciaParticipanteDTO> listarPorSolicitud(
            Integer idSolicitudCredito
    ) {

        String sql = SQL_PARTICIPANTES + """
                
                AND id_solicitud_credito = :idSolicitudCredito

                ORDER BY
                    orden_deudor,
                    id_solicitud_deudor
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        return jdbc.query(
                sql,
                params,
                participanteMapper
        );
    }

    // =========================================================
    // 4. BUSCAR PARTICIPANTE
    // =========================================================

    public SolicitudReferenciaParticipanteDTO buscarPorParticipante(
            Integer idSolicitudCredito,
            Integer idSolicitudDeudor
    ) {

        String sql = SQL_PARTICIPANTES + """
                
                AND id_solicitud_credito = :idSolicitudCredito

                AND id_solicitud_deudor = :idSolicitudDeudor
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        );

        List<SolicitudReferenciaParticipanteDTO> resultados =
                jdbc.query(
                        sql,
                        params,
                        participanteMapper
                );

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }

    // =========================================================
    // 5. LISTAR REFERENCIAS DE UN PARTICIPANTE
    // =========================================================

    public List<SolicitudReferenciaPersonalDTO>
    listarReferenciasPorParticipante(
            Integer idSolicitudCredito,
            Integer idSolicitudDeudor
    ) {

        String sql = SQL_REFERENCIAS + """
                
                AND id_solicitud_credito = :idSolicitudCredito

                AND id_solicitud_deudor = :idSolicitudDeudor

                AND activo = TRUE

                ORDER BY
                    id_solicitud_referencia_personal
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        );

        return jdbc.query(
                sql,
                params,
                referenciaMapper
        );
    }

    // =========================================================
    // 6. LISTAR TODAS LAS REFERENCIAS DE UNA SOLICITUD
    // =========================================================

    public List<SolicitudReferenciaPersonalDTO>
    listarReferenciasPorSolicitud(
            Integer idSolicitudCredito
    ) {

        String sql = SQL_REFERENCIAS + """
                
                AND id_solicitud_credito = :idSolicitudCredito

                AND activo = TRUE

                ORDER BY
                    id_solicitud_deudor,
                    id_solicitud_referencia_personal
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        return jdbc.query(
                sql,
                params,
                referenciaMapper
        );
    }

    // =========================================================
    // 7. BUSCAR REFERENCIA PERSONAL
    // =========================================================

    public SolicitudReferenciaPersonalDTO buscarReferenciaPorId(
            Long idSolicitudReferenciaPersonal
    ) {

        String sql = SQL_REFERENCIAS + """
                
                AND id_solicitud_referencia_personal =
                    :idSolicitudReferenciaPersonal

                AND activo = TRUE
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudReferenciaPersonal",
                                idSolicitudReferenciaPersonal
                        );

        List<SolicitudReferenciaPersonalDTO> resultados =
                jdbc.query(
                        sql,
                        params,
                        referenciaMapper
                );

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }

    // =========================================================
    // 8. CREAR O RECUPERAR PROCESO DE REFERENCIAS
    // =========================================================

    public Long crearORecuperarProceso(
            Integer idSolicitudCredito,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.solicitudes_referencias_procesos (
                    id_solicitud_credito,
                    estado_proceso,
                    fecha_inicio,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )

                SELECT
                    sc.id_solicitud_credito,
                    'EN_PROCESO',
                    CURRENT_TIMESTAMP,
                    :idUsuario,
                    :idUsuario

                FROM cartera.solicitudes_creditos sc

                WHERE sc.id_solicitud_credito = :idSolicitudCredito
                  AND sc.activo = TRUE
                  AND sc.id_solicitud_resultado = 1

                ON CONFLICT (id_solicitud_credito)

                DO UPDATE SET

                    estado_proceso =
                        CASE
                            WHEN cartera.solicitudes_referencias_procesos.estado_proceso = 'PENDIENTE'
                            THEN 'EN_PROCESO'
                            ELSE cartera.solicitudes_referencias_procesos.estado_proceso
                        END,

                    fecha_inicio =
                        COALESCE(
                            cartera.solicitudes_referencias_procesos.fecha_inicio,
                            CURRENT_TIMESTAMP
                        ),

                    fk_seguridad_edicion = :idUsuario,

                    fecha_edicion = CURRENT_TIMESTAMP

                WHERE cartera.solicitudes_referencias_procesos.estado_proceso
                      IN ('PENDIENTE', 'EN_PROCESO')

                RETURNING id_solicitud_referencia_proceso
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        List<Long> resultados =
                jdbc.queryForList(
                        sql,
                        params,
                        Long.class
                );

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }

    // =========================================================
    // 9. REGISTRAR REFERENCIA PERSONAL
    // =========================================================

    public Long crearReferencia(
            SolicitudReferenciaPersonalDTO dto,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.solicitudes_referencias_personales (
                    id_solicitud_referencia_proceso,
                    id_solicitud_credito,
                    id_solicitud_deudor,

                    nombre_completo,
                    telefono_celular,
                    telefono_fijo,

                    activo,

                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )

                SELECT
                    rp.id_solicitud_referencia_proceso,
                    rp.id_solicitud_credito,
                    sd.id_solicitud_deudor,

                    :nombreCompleto,
                    :telefonoCelular,
                    :telefonoFijo,

                    TRUE,

                    :idUsuario,
                    :idUsuario

                FROM cartera.solicitudes_referencias_procesos rp

                JOIN cartera.solicitudes_creditos sc
                  ON sc.id_solicitud_credito =
                     rp.id_solicitud_credito

                JOIN cartera.solicitudes_deudores sd
                  ON sd.id_solicitud_credito =
                     rp.id_solicitud_credito

                WHERE rp.id_solicitud_referencia_proceso =
                      :idSolicitudReferenciaProceso

                  AND rp.id_solicitud_credito =
                      :idSolicitudCredito

                  AND sd.id_solicitud_deudor =
                      :idSolicitudDeudor

                  AND rp.estado_proceso = 'EN_PROCESO'

                  AND sc.activo = TRUE

                  AND sc.id_solicitud_resultado = 1

                  AND sd.activo = TRUE

                RETURNING id_solicitud_referencia_personal
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudReferenciaProceso",
                                dto.getIdSolicitudReferenciaProceso()
                        )
                        .addValue(
                                "idSolicitudCredito",
                                dto.getIdSolicitudCredito()
                        )
                        .addValue(
                                "idSolicitudDeudor",
                                dto.getIdSolicitudDeudor()
                        )
                        .addValue(
                                "nombreCompleto",
                                dto.getNombreCompleto()
                        )
                        .addValue(
                                "telefonoCelular",
                                dto.getTelefonoCelular()
                        )
                        .addValue(
                                "telefonoFijo",
                                dto.getTelefonoFijo()
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        List<Long> resultados =
                jdbc.queryForList(
                        sql,
                        params,
                        Long.class
                );

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }

    // =========================================================
    // 10. ACTUALIZAR DATOS DE CONTACTO
    // =========================================================

    public int actualizarReferencia(
            SolicitudReferenciaPersonalDTO dto,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.solicitudes_referencias_personales r

                SET
                    nombre_completo = :nombreCompleto,

                    telefono_celular = :telefonoCelular,

                    telefono_fijo = :telefonoFijo,

                    fk_seguridad_edicion = :idUsuario,

                    fecha_edicion = CURRENT_TIMESTAMP

                FROM cartera.solicitudes_referencias_procesos rp

                JOIN cartera.solicitudes_creditos sc
                  ON sc.id_solicitud_credito =
                     rp.id_solicitud_credito

                WHERE r.id_solicitud_referencia_proceso =
                      rp.id_solicitud_referencia_proceso

                  AND r.id_solicitud_referencia_personal =
                      :idSolicitudReferenciaPersonal

                  AND r.activo = TRUE

                  AND rp.estado_proceso = 'EN_PROCESO'

                  AND sc.activo = TRUE

                  AND sc.id_solicitud_resultado = 1
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudReferenciaPersonal",
                                dto.getIdSolicitudReferenciaPersonal()
                        )
                        .addValue(
                                "nombreCompleto",
                                dto.getNombreCompleto()
                        )
                        .addValue(
                                "telefonoCelular",
                                dto.getTelefonoCelular()
                        )
                        .addValue(
                                "telefonoFijo",
                                dto.getTelefonoFijo()
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                params
        );
    }

    // =========================================================
    // 11. REGISTRAR ENTREVISTA
    // =========================================================

    public int registrarEntrevista(
            Long idSolicitudReferenciaPersonal,
            String medioEntrevista,
            Boolean contactoEstablecido,
            String conceptoReferencia,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.solicitudes_referencias_personales r

                SET
                    medio_entrevista = :medioEntrevista,

                    fecha_hora_llamada = CURRENT_TIMESTAMP,

                    contacto_establecido = :contactoEstablecido,

                    concepto_referencia = :conceptoReferencia,

                    fk_seguridad_entrevistador = :idUsuario,

                    fk_seguridad_edicion = :idUsuario,

                    fecha_edicion = CURRENT_TIMESTAMP

                FROM cartera.solicitudes_referencias_procesos rp

                JOIN cartera.solicitudes_creditos sc
                  ON sc.id_solicitud_credito =
                     rp.id_solicitud_credito

                WHERE r.id_solicitud_referencia_proceso =
                      rp.id_solicitud_referencia_proceso

                  AND r.id_solicitud_referencia_personal =
                      :idSolicitudReferenciaPersonal

                  AND r.activo = TRUE

                  AND rp.estado_proceso = 'EN_PROCESO'

                  AND sc.activo = TRUE

                  AND sc.id_solicitud_resultado = 1
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudReferenciaPersonal",
                                idSolicitudReferenciaPersonal
                        )
                        .addValue(
                                "medioEntrevista",
                                medioEntrevista
                        )
                        .addValue(
                                "contactoEstablecido",
                                contactoEstablecido
                        )
                        .addValue(
                                "conceptoReferencia",
                                conceptoReferencia
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                params
        );
    }

    // =========================================================
    // 12. DESACTIVAR REFERENCIA PERSONAL
    // =========================================================

    public int desactivarReferencia(
            Long idSolicitudReferenciaPersonal,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.solicitudes_referencias_personales r

                SET
                    activo = FALSE,

                    fk_seguridad_edicion = :idUsuario,

                    fecha_edicion = CURRENT_TIMESTAMP

                FROM cartera.solicitudes_referencias_procesos rp

                JOIN cartera.solicitudes_creditos sc
                  ON sc.id_solicitud_credito =
                     rp.id_solicitud_credito

                WHERE r.id_solicitud_referencia_proceso =
                      rp.id_solicitud_referencia_proceso

                  AND r.id_solicitud_referencia_personal =
                      :idSolicitudReferenciaPersonal

                  AND r.activo = TRUE

                  AND rp.estado_proceso = 'EN_PROCESO'

                  AND sc.activo = TRUE

                  AND sc.id_solicitud_resultado = 1
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudReferenciaPersonal",
                                idSolicitudReferenciaPersonal
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                params
        );
    }

    // =========================================================
    // 13. CERRAR PROCESO DE REFERENCIAS
    // =========================================================

    public int cerrarProceso(
            Integer idSolicitudCredito,
            String tipoCierre,
            String observacionCierre,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.solicitudes_referencias_procesos rp

                SET
                    estado_proceso = 'CERRADO',

                    tipo_cierre = :tipoCierre,

                    observacion_cierre = :observacionCierre,

                    fecha_cierre = CURRENT_TIMESTAMP,

                    fk_seguridad_cierre = :idUsuario,

                    fk_seguridad_edicion = :idUsuario,

                    fecha_edicion = CURRENT_TIMESTAMP

                FROM cartera.solicitudes_creditos sc

                WHERE sc.id_solicitud_credito =
                      rp.id_solicitud_credito

                  AND rp.id_solicitud_credito =
                      :idSolicitudCredito

                  AND rp.estado_proceso = 'EN_PROCESO'

                  AND sc.activo = TRUE

                  AND sc.id_solicitud_resultado = 1

                  AND (
                      (
                          :tipoCierre = 'CON_REFERENCIAS'

                          AND EXISTS (
                              SELECT 1

                              FROM cartera.solicitudes_referencias_personales r

                              WHERE r.id_solicitud_referencia_proceso =
                                    rp.id_solicitud_referencia_proceso

                                AND r.activo = TRUE
                          )
                      )

                      OR

                      (
                          :tipoCierre = 'SIN_REFERENCIAS'

                          AND NULLIF(
                              BTRIM(:observacionCierre),
                              ''
                          ) IS NOT NULL

                          AND NOT EXISTS (
                              SELECT 1

                              FROM cartera.solicitudes_referencias_personales r

                              WHERE r.id_solicitud_referencia_proceso =
                                    rp.id_solicitud_referencia_proceso

                                AND r.activo = TRUE
                          )
                      )
                  )
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )
                        .addValue(
                                "tipoCierre",
                                tipoCierre
                        )
                        .addValue(
                                "observacionCierre",
                                observacionCierre
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                params
        );
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private boolean tieneTexto(String valor) {

        return valor != null
                && !valor.trim().isEmpty();
    }
}