    package co.assip.erp.cartera.originacion.aprobacion;

    import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionActuacionDTO;
    import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionBandejaDTO;
    import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionDecisionDTO;
    import org.springframework.jdbc.core.BeanPropertyRowMapper;
    import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
    import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

    import org.springframework.stereotype.Repository;

    import java.util.List;
    import java.util.Optional;

    @Repository
    public class SolicitudAprobacionRepository {

        private final NamedParameterJdbcTemplate jdbc;

        public SolicitudAprobacionRepository(
                NamedParameterJdbcTemplate jdbc
        ) {
            this.jdbc = jdbc;
        }

        // =========================================================
        // MAPPERS
        // =========================================================

        private static final BeanPropertyRowMapper<SolicitudAprobacionBandejaDTO>
                BANDEJA_MAPPER =
                BeanPropertyRowMapper.newInstance(
                        SolicitudAprobacionBandejaDTO.class
                );

        private static final BeanPropertyRowMapper<SolicitudAprobacionDecisionDTO>
                DECISION_MAPPER =
                BeanPropertyRowMapper.newInstance(
                        SolicitudAprobacionDecisionDTO.class
                );


        // =========================================================
        // BANDEJA DEL USUARIO
        // =========================================================

        public List<SolicitudAprobacionBandejaDTO> listarBandeja(
                Integer idUsuario
        ) {

            String sql = """
                    SELECT
                        b.id_solicitud_credito,
                        b.numero_solicitud,
    
                        b.fecha_inicio_solicitud,
                        b.fecha_ultima_gestion,
    
                        b.id_agencia,
                        b.codigo_agencia,
                        b.nombre_agencia,
    
                        b.id_datos_personal,
                        b.tipo_documento,
                        b.documento,
                        b.nombre_completo,
    
                        b.id_linea_credito,
                        b.codigo_linea_credito,
                        b.nombre_linea_credito,
    
                        b.valor_solicitado,
                        b.plazo_solicitado,
                        b.tasa_colocacion_aplicada,
                        b.valor_cuota_proyectada,
    
                        b.codigo_garantia_credito,
                        b.nombre_garantia_credito,
                        b.tipo_garantia,
    
                        b.id_ente_final,
                        b.nombre_ente_final,
    
                        b.id_ente_actual
    
                    FROM cartera.vw_solicitudes_aprobacion_bandeja b
    
                    INNER JOIN cartera.entes_aprobacion_usuarios eau
                            ON eau.id_ente_aprobacion =
                               b.id_ente_actual
    
                    WHERE eau.id_usuario = :idUsuario
                      AND eau.activo = true
                      AND eau.fecha_inicio <= CURRENT_DATE
                      AND (
                            eau.fecha_fin IS NULL
                            OR eau.fecha_fin >= CURRENT_DATE
                          )
                      AND b.id_ente_actual IS NOT NULL
    
                    ORDER BY
                        b.fecha_ultima_gestion,
                        b.id_solicitud_credito
                    """;

            return jdbc.query(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(
                                    "idUsuario",
                                    idUsuario
                            ),
                    BANDEJA_MAPPER
            );
        }


        // =========================================================
        // ENTE ACTUAL DE LA SOLICITUD
        // =========================================================

        public Optional<Integer> obtenerEnteActual(
                Integer idSolicitudCredito
        ) {

            String sql = """
                    SELECT
                        id_ente_actual
                    FROM cartera.vw_solicitudes_aprobacion_bandeja
                    WHERE id_solicitud_credito =
                          :idSolicitudCredito
                      AND id_ente_actual IS NOT NULL
                    """;

            List<Integer> resultados =
                    jdbc.query(
                            sql,
                            new MapSqlParameterSource()
                                    .addValue(
                                            "idSolicitudCredito",
                                            idSolicitudCredito
                                    ),
                            (rs, rowNum) ->
                                    rs.getInt(
                                            "id_ente_actual"
                                    )
                    );

            if (resultados.isEmpty()) {
                return Optional.empty();
            }

            if (resultados.size() > 1) {
                throw new IllegalStateException(
                        "Se encontró más de un ente actual para la solicitud "
                                + idSolicitudCredito
                                + "."
                );
            }

            return Optional.of(
                    resultados.get(0)
            );
        }

        // =========================================================
        // ENTE APROBADOR FINAL
        // =========================================================

        public Integer obtenerEnteFinal(
                Integer idSolicitudCredito
        ) {

            String sql = """
                SELECT id_ente_aprobacion
                FROM cartera.solicitudes_creditos
                WHERE id_solicitud_credito = :idSolicitudCredito
                  AND activo = true
                """;

            Integer idEnteFinal = jdbc.queryForObject(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(
                                    "idSolicitudCredito",
                                    idSolicitudCredito
                            ),
                    Integer.class
            );

            if (idEnteFinal == null) {
                throw new IllegalStateException(
                        "La solicitud no tiene un ente aprobador final."
                );
            }

            return idEnteFinal;
        }


        // =========================================================
        // AUTORIZACIÓN DEL USUARIO
        // =========================================================

        public boolean usuarioPuedeActuar(
                Integer idSolicitudCredito,
                Integer idUsuario
        ) {

            String sql = """
                    SELECT EXISTS
                    (
                        SELECT 1
    
                        FROM cartera.vw_solicitudes_aprobacion_bandeja b
    
                        INNER JOIN cartera.entes_aprobacion_usuarios eau
                                ON eau.id_ente_aprobacion =
                                   b.id_ente_actual
    
                        WHERE b.id_solicitud_credito =
                              :idSolicitudCredito
    
                          AND b.id_ente_actual IS NOT NULL
    
                          AND eau.id_usuario =
                              :idUsuario
    
                          AND eau.activo = true
    
                          AND eau.fecha_inicio <= CURRENT_DATE
    
                          AND (
                                eau.fecha_fin IS NULL
                                OR eau.fecha_fin >= CURRENT_DATE
                              )
                    )
                    """;

            Boolean autorizado =
                    jdbc.queryForObject(
                            sql,
                            new MapSqlParameterSource()
                                    .addValue(
                                            "idSolicitudCredito",
                                            idSolicitudCredito
                                    )
                                    .addValue(
                                            "idUsuario",
                                            idUsuario
                                    ),
                            Boolean.class
                    );

            return Boolean.TRUE.equals(
                    autorizado
            );
        }


        // =========================================================
// AUTORIZACIÓN DEL ASESOR RESPONSABLE
// =========================================================

        public boolean usuarioEsAsesor(
                Integer idSolicitudCredito,
                Integer idUsuario
        ) {

            String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM cartera.solicitudes_creditos sc
            WHERE sc.id_solicitud_credito = :idSolicitudCredito
              AND sc.id_asesor = :idUsuario
              AND sc.activo = true
        )
        """;

            Boolean autorizado = jdbc.queryForObject(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(
                                    "idSolicitudCredito",
                                    idSolicitudCredito
                            )
                            .addValue(
                                    "idUsuario",
                                    idUsuario
                            ),
                    Boolean.class
            );

            return Boolean.TRUE.equals(autorizado);
        }

        // =========================================================
        // AUTORIZACIÓN PARA CONSULTAR HISTORIAL Y FOTOGRAFÍAS
        // =========================================================

        public boolean usuarioPuedeConsultar(
                Integer idSolicitudCredito,
                Integer idUsuario
        ) {

            String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM cartera.solicitudes_creditos s
                    WHERE s.id_solicitud_credito = :idSolicitudCredito
                      AND s.activo = true
                      AND (
                          -- Puede consultar si está autorizado
                          -- para actuar en la etapa actual.
                          EXISTS (
                              SELECT 1
                              FROM cartera.vw_solicitudes_aprobacion_bandeja b
                              INNER JOIN cartera.entes_aprobacion_usuarios eau
                                  ON eau.id_ente_aprobacion = b.id_ente_actual
                              WHERE b.id_solicitud_credito = s.id_solicitud_credito
                                AND eau.id_usuario = :idUsuario
                                AND eau.activo = true
                                AND eau.fecha_inicio <= CURRENT_DATE
                                AND (
                                    eau.fecha_fin IS NULL
                                    OR eau.fecha_fin >= CURRENT_DATE
                                )
                          )

                          OR

                          -- Puede consultar si participó
                          -- anteriormente en esta solicitud.
                          EXISTS (
                              SELECT 1
                              FROM cartera.solicitudes_aprobaciones sa
                              WHERE sa.id_solicitud_credito = s.id_solicitud_credito
                                AND sa.id_usuario_decision = :idUsuario
                          )
                      )
                )
                """;

            Boolean autorizado = jdbc.queryForObject(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(
                                    "idSolicitudCredito",
                                    idSolicitudCredito
                            )
                            .addValue(
                                    "idUsuario",
                                    idUsuario
                            ),
                    Boolean.class
            );

            return Boolean.TRUE.equals(autorizado);
        }


        // =========================================================
        // CATÁLOGO DE DECISIONES
        // =========================================================

        public List<SolicitudAprobacionDecisionDTO> listarDecisiones() {

            String sql = """
                    SELECT
                        id_aprobacion_decision,
                        codigo_decision,
                        nombre_decision
    
                    FROM cartera.solicitudes_aprobaciones_decisiones
    
                    WHERE activo = true
    
                    ORDER BY
                        id_aprobacion_decision
                    """;

            return jdbc.query(
                    sql,
                    new MapSqlParameterSource(),
                    DECISION_MAPPER
            );
        }


        // =========================================================
        // BUSCAR DECISIÓN
        // =========================================================

        public Optional<SolicitudAprobacionDecisionDTO> buscarDecision(
                Integer idAprobacionDecision
        ) {

            String sql = """
                    SELECT
                        id_aprobacion_decision,
                        codigo_decision,
                        nombre_decision
    
                    FROM cartera.solicitudes_aprobaciones_decisiones
    
                    WHERE id_aprobacion_decision =
                          :idAprobacionDecision
    
                      AND activo = true
                    """;

            List<SolicitudAprobacionDecisionDTO> resultados =
                    jdbc.query(
                            sql,
                            new MapSqlParameterSource()
                                    .addValue(
                                            "idAprobacionDecision",
                                            idAprobacionDecision
                                    ),
                            DECISION_MAPPER
                    );

            if (resultados.isEmpty()) {
                return Optional.empty();
            }

            if (resultados.size() > 1) {
                throw new IllegalStateException(
                        "Se encontró más de una decisión de aprobación con id "
                                + idAprobacionDecision
                                + "."
                );
            }

            return Optional.of(
                    resultados.get(0)
            );
        }

        // =========================================================
        // CONCEPTO VIGENTE PARA GESTIÓN DEL ASESOR
        // =========================================================

        public Optional<String> obtenerConceptoVigente(
                Integer idSolicitudCredito
        ) {

            String sql = """
        SELECT
            b.ultima_decision

        FROM cartera.vw_solicitudes_aprobacion_bandeja b

        WHERE b.id_solicitud_credito = :idSolicitudCredito

          AND b.id_ente_actual IS NULL

          AND b.id_ultima_aprobacion IS NOT NULL

          AND (
                b.ultima_decision = 'SOLICITA_AJUSTES'

                OR b.id_ente_ultima_actuacion = b.id_ente_final
              )
        """;

            List<String> resultados = jdbc.query(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(
                                    "idSolicitudCredito",
                                    idSolicitudCredito
                            ),
                    (rs, rowNum) ->
                            rs.getString("ultima_decision")
            );

            if (resultados.isEmpty()) {
                return Optional.empty();
            }

            if (resultados.size() > 1) {
                throw new IllegalStateException(
                        "Se encontró más de un concepto vigente "
                                + "para la solicitud "
                                + idSolicitudCredito
                );
            }

            return Optional.ofNullable(resultados.get(0));
        }


        // =========================================================
        // HISTORIAL DE ACTUACIONES
        // =========================================================

        public List<SolicitudAprobacionActuacionDTO> listarActuaciones(
                Integer idSolicitudCredito
        ) {

            String sql = """
                    SELECT
                        sa.id_solicitud_aprobacion,
                        sa.id_solicitud_credito,
    
                        sa.id_ente_aprobacion,
                        ea.nombre_ente_aprobacion,
    
                        sa.id_aprobacion_decision,
                        d.codigo_decision,
                        d.nombre_decision,
    
                        sa.numero_acta,
                        sa.fecha_acta,
    
                        sa.concepto,
    
                        sa.id_usuario_decision,
    
                        COALESCE(
                            NULLIF(TRIM(u.nombre_completo), ''),
                            NULLIF(TRIM(u.username), ''),
                            'USUARIO ' || sa.id_usuario_decision
                        ) AS nombre_usuario_decision,
    
                        sa.fecha_decision
    
                    FROM cartera.solicitudes_aprobaciones sa
    
                    INNER JOIN cartera.entes_aprobacion ea
                            ON ea.id_ente_aprobacion =
                               sa.id_ente_aprobacion
    
                    INNER JOIN cartera.solicitudes_aprobaciones_decisiones d
                            ON d.id_aprobacion_decision =
                               sa.id_aprobacion_decision
    
                    INNER JOIN seguridad.usuarios u
                             ON u.id_usuario =
                                sa.id_usuario_decision
    
                    WHERE sa.id_solicitud_credito =
                          :idSolicitudCredito
    
                    ORDER BY
                        sa.fecha_decision,
                        sa.id_solicitud_aprobacion
                    """;

            return jdbc.query(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(
                                    "idSolicitudCredito",
                                    idSolicitudCredito
                            ),
                    (rs, rowNum) -> {

                        SolicitudAprobacionActuacionDTO dto =
                                new SolicitudAprobacionActuacionDTO();

                        dto.setIdSolicitudAprobacion(
                                rs.getInt(
                                        "id_solicitud_aprobacion"
                                )
                        );

                        dto.setIdSolicitudCredito(
                                rs.getInt(
                                        "id_solicitud_credito"
                                )
                        );

                        dto.setIdEnteAprobacion(
                                rs.getInt(
                                        "id_ente_aprobacion"
                                )
                        );

                        dto.setNombreEnteAprobacion(
                                rs.getString(
                                        "nombre_ente_aprobacion"
                                )
                        );

                        dto.setIdAprobacionDecision(
                                rs.getInt(
                                        "id_aprobacion_decision"
                                )
                        );

                        dto.setCodigoDecision(
                                rs.getString(
                                        "codigo_decision"
                                )
                        );

                        dto.setNombreDecision(
                                rs.getString(
                                        "nombre_decision"
                                )
                        );

                        dto.setNumeroActa(
                                rs.getString(
                                        "numero_acta"
                                )
                        );

                        if (rs.getDate("fecha_acta") != null) {
                            dto.setFechaActa(
                                    rs.getDate(
                                            "fecha_acta"
                                    ).toLocalDate()
                            );
                        }

                        dto.setConcepto(
                                rs.getString(
                                        "concepto"
                                )
                        );

                        dto.setIdUsuarioDecision(
                                rs.getInt(
                                        "id_usuario_decision"
                                )
                        );

                        dto.setNombreUsuarioDecision(
                                rs.getString(
                                        "nombre_usuario_decision"
                                )
                        );

                        dto.setFechaDecision(
                                rs.getTimestamp(
                                        "fecha_decision"
                                ).toLocalDateTime()
                        );

                        return dto;
                    }
            );
        }

        // =========================================================
        // FOTOGRAFÍAS HISTÓRICAS DE UNA ACTUACIÓN
        // =========================================================

        public Optional<String[]> obtenerFotosActuacion(
                Integer idSolicitudAprobacion,
                Integer idSolicitudCredito
        ) {

            String sql = """
                SELECT
                    sa.foto_solicitud::text AS foto_solicitud,
                    sa.foto_deudores::text AS foto_deudores,
                    sa.foto_financiero::text AS foto_financiero,
                    sa.foto_bienes::text AS foto_bienes,
                    sa.foto_central_riesgo::text AS foto_central_riesgo,
                    sa.foto_analisis::text AS foto_analisis

                FROM cartera.solicitudes_aprobaciones sa

                INNER JOIN cartera.solicitudes_creditos s
                        ON s.id_solicitud_credito =
                           sa.id_solicitud_credito

                WHERE sa.id_solicitud_aprobacion =
                      :idSolicitudAprobacion

                  AND sa.id_solicitud_credito =
                      :idSolicitudCredito

                  AND s.activo = true
                """;

            List<String[]> resultados = jdbc.query(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(
                                    "idSolicitudAprobacion",
                                    idSolicitudAprobacion
                            )
                            .addValue(
                                    "idSolicitudCredito",
                                    idSolicitudCredito
                            ),
                    (rs, rowNum) -> new String[]{
                            rs.getString("foto_solicitud"),
                            rs.getString("foto_deudores"),
                            rs.getString("foto_financiero"),
                            rs.getString("foto_bienes"),
                            rs.getString("foto_central_riesgo"),
                            rs.getString("foto_analisis")
                    }
            );

            if (resultados.isEmpty()) {
                return Optional.empty();
            }

            if (resultados.size() > 1) {
                throw new IllegalStateException(
                        "Se encontró más de una actuación de aprobación "
                                + "con id "
                                + idSolicitudAprobacion
                                + "."
                );
            }

            return Optional.of(resultados.get(0));
        }


        // =========================================================
        // BLOQUEAR SOLICITUD PARA REGISTRAR DECISIÓN
        // =========================================================

        public boolean bloquearSolicitud(
                Integer idSolicitudCredito
        ) {

            String sql = """
                    SELECT
                        id_solicitud_credito
    
                    FROM cartera.solicitudes_creditos
    
                    WHERE id_solicitud_credito =
                          :idSolicitudCredito
    
                      AND activo = true
    
                    FOR UPDATE
                    """;

            List<Integer> resultados =
                    jdbc.query(
                            sql,
                            new MapSqlParameterSource()
                                    .addValue(
                                            "idSolicitudCredito",
                                            idSolicitudCredito
                                    ),
                            (rs, rowNum) ->
                                    rs.getInt(
                                            "id_solicitud_credito"
                                    )
                    );

            return resultados.size() == 1;
        }

        // =========================================================
        // REGISTRAR ACTUACIÓN DE APROBACIÓN
        // =========================================================

        // =========================================================
        // REGISTRAR ACTUACIÓN DE APROBACIÓN
        // =========================================================

        public Integer registrarActuacion(
                Integer idSolicitudCredito,
                Integer idEnteAprobacion,
                Integer idAprobacionDecision,
                String numeroActa,
                java.time.LocalDate fechaActa,
                String concepto,
                Integer idUsuario,
                String fotoSolicitud,
                String fotoDeudores,
                String fotoFinanciero,
                String fotoBienes,
                String fotoCentralRiesgo,
                String fotoAnalisis
        ) {

            String sql = """
                INSERT INTO cartera.solicitudes_aprobaciones
                (
                    id_solicitud_credito,
                    id_ente_aprobacion,
                    id_aprobacion_decision,

                    numero_acta,
                    fecha_acta,
                    concepto,

                    id_usuario_decision,
                    fecha_decision,

                    foto_solicitud,
                    foto_deudores,
                    foto_financiero,
                    foto_bienes,
                    foto_central_riesgo,
                    foto_analisis,

                    fk_seguridad_creacion,
                    fecha_creacion,

                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES
                (
                    :idSolicitudCredito,
                    :idEnteAprobacion,
                    :idAprobacionDecision,

                    :numeroActa,
                    :fechaActa,
                    :concepto,

                    :idUsuario,
                    CURRENT_TIMESTAMP,

                    CAST(:fotoSolicitud AS jsonb),
                    CAST(:fotoDeudores AS jsonb),
                    CAST(:fotoFinanciero AS jsonb),
                    CAST(:fotoBienes AS jsonb),
                    CAST(:fotoCentralRiesgo AS jsonb),
                    CAST(:fotoAnalisis AS jsonb),

                    :idUsuario,
                    CURRENT_TIMESTAMP,

                    :idUsuario,
                    CURRENT_TIMESTAMP
                )
                RETURNING id_solicitud_aprobacion
                """;

            MapSqlParameterSource params =
                    new MapSqlParameterSource()
                            .addValue(
                                    "idSolicitudCredito",
                                    idSolicitudCredito
                            )
                            .addValue(
                                    "idEnteAprobacion",
                                    idEnteAprobacion
                            )
                            .addValue(
                                    "idAprobacionDecision",
                                    idAprobacionDecision
                            )
                            .addValue(
                                    "numeroActa",
                                    numeroActa
                            )
                            .addValue(
                                    "fechaActa",
                                    fechaActa
                            )
                            .addValue(
                                    "concepto",
                                    concepto
                            )
                            .addValue(
                                    "idUsuario",
                                    idUsuario
                            )
                            .addValue(
                                    "fotoSolicitud",
                                    fotoSolicitud
                            )
                            .addValue(
                                    "fotoDeudores",
                                    fotoDeudores
                            )
                            .addValue(
                                    "fotoFinanciero",
                                    fotoFinanciero
                            )
                            .addValue(
                                    "fotoBienes",
                                    fotoBienes
                            )
                            .addValue(
                                    "fotoCentralRiesgo",
                                    fotoCentralRiesgo
                            )
                            .addValue(
                                    "fotoAnalisis",
                                    fotoAnalisis
                            );

            Integer idSolicitudAprobacion =
                    jdbc.queryForObject(
                            sql,
                            params,
                            Integer.class
                    );

            if (idSolicitudAprobacion == null) {

                throw new IllegalStateException(
                        "No fue posible registrar la actuación "
                                + "de aprobación para la solicitud "
                                + idSolicitudCredito
                                + "."
                );
            }

            return idSolicitudAprobacion;
        }

        // =========================================================
        // RESULTADO DEL PROCESO DE APROBACIÓN
        // APROBACIÓN FINAL → FORMALIZACIÓN
        // FORMALIZAR
        // Únicamente con aprobación del ente final
        // =========================================================

        public int enviarAFormalizacion(
                Integer idSolicitudCredito,
                Integer idUsuario
        ) {

            String sql = """
        UPDATE cartera.solicitudes_creditos
        SET id_solicitud_proceso = 4,
            id_solicitud_resultado = 1,
            fecha_fin_aprobacion = CURRENT_TIMESTAMP,
            fecha_ultima_gestion = CURRENT_TIMESTAMP,
            fk_seguridad_edicion = :idUsuario,
            fecha_edicion = CURRENT_TIMESTAMP
        WHERE id_solicitud_credito = :idSolicitudCredito
          AND activo = true
          AND id_solicitud_proceso = 3
          AND id_solicitud_resultado = 1

          AND EXISTS (
              SELECT 1
              FROM cartera.vw_solicitudes_aprobacion_bandeja b
              WHERE b.id_solicitud_credito =
                    cartera.solicitudes_creditos.id_solicitud_credito

                AND b.id_ente_actual IS NULL
                AND b.id_ultima_aprobacion IS NOT NULL

                AND b.ultima_decision = 'APROBADA'

                AND b.id_ente_ultima_actuacion = b.id_ente_final
          )
        """;

            return jdbc.update(
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
        }


        // =========================================================
        // RETOMAR
        // Permite volver a documentación cuando el concepto
        // ya está disponible para gestión del asesor
        // =========================================================

        public int devolverADocumentacion(
                Integer idSolicitudCredito,
                Integer idUsuario
        ) {

            String sql = """
        UPDATE cartera.solicitudes_creditos
        SET id_solicitud_proceso = 2,
            id_solicitud_resultado = 1,
            fecha_ultima_gestion = CURRENT_TIMESTAMP,
            fk_seguridad_edicion = :idUsuario,
            fecha_edicion = CURRENT_TIMESTAMP
        WHERE id_solicitud_credito = :idSolicitudCredito
          AND activo = true
          AND id_solicitud_proceso = 3
          AND id_solicitud_resultado = 1

          AND EXISTS (
              SELECT 1
              FROM cartera.vw_solicitudes_aprobacion_bandeja b
              WHERE b.id_solicitud_credito =
                    cartera.solicitudes_creditos.id_solicitud_credito

                AND b.id_ente_actual IS NULL
                AND b.id_ultima_aprobacion IS NOT NULL

                AND (
                    b.ultima_decision = 'SOLICITA_AJUSTES'

                    OR b.id_ente_ultima_actuacion = b.id_ente_final
                )
          )
        """;

            return jdbc.update(
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
        }


        // =========================================================
        // CERRAR POR NO VIABILIDAD
        // Únicamente con concepto NO_VIABLE del ente final
        // =========================================================

        public int finalizarNoViable(
                Integer idSolicitudCredito,
                Integer idUsuario
        ) {

            String sql = """
        UPDATE cartera.solicitudes_creditos
        SET id_solicitud_resultado = 3,
            fecha_fin_aprobacion = CURRENT_TIMESTAMP,
            fecha_ultima_gestion = CURRENT_TIMESTAMP,
            fk_seguridad_edicion = :idUsuario,
            fecha_edicion = CURRENT_TIMESTAMP
        WHERE id_solicitud_credito = :idSolicitudCredito
          AND activo = true
          AND id_solicitud_proceso = 3
          AND id_solicitud_resultado = 1

          AND EXISTS (
              SELECT 1
              FROM cartera.vw_solicitudes_aprobacion_bandeja b
              WHERE b.id_solicitud_credito =
                    cartera.solicitudes_creditos.id_solicitud_credito

                AND b.id_ente_actual IS NULL
                AND b.id_ultima_aprobacion IS NOT NULL

                AND b.ultima_decision = 'NO_VIABLE'

                AND b.id_ente_ultima_actuacion = b.id_ente_final
          )
        """;

            return jdbc.update(
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
        }


        // =========================================================
        // FOTOGRAFÍAS PARA APROBACIÓN
        // =========================================================

        public String obtenerFotoSolicitud(
                Integer idSolicitudCredito
        ) {
            String sql = """
                    SELECT to_jsonb(sc) AS foto
                    FROM cartera.vw_solicitudes_creditos sc
                    WHERE sc.id_solicitud_credito = :idSolicitudCredito
                    """;

            return obtenerFoto(
                    sql,
                    idSolicitudCredito,
                    "solicitud"
            );
        }


        public String obtenerFotoDeudores(
                Integer idSolicitudCredito
        ) {
            String sql = """
                    SELECT COALESCE(
                        jsonb_agg(
                            jsonb_build_object(
                                'id_solicitud_deudor', sd.id_solicitud_deudor,
                                'id_solicitud_credito', sd.id_solicitud_credito,
                                'id_datos_personal', sd.id_datos_personal,
    
                                'tipo_deudor', sd.tipo_deudor,
                                'orden_deudor', sd.orden_deudor,
    
                                'tipo_documento', dp.tipo_documento,
                                'documento', dp.documento,
    
                                'nombre_completo',
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
                                    ),
    
                                'saldo_cartera_inicio', sd.saldo_cartera_inicio,
                                'dias_mora_inicio', sd.dias_mora_inicio,
                                'cumple_mora_inicio', sd.cumple_mora_inicio,
    
                                'saldo_cartera_validacion', sd.saldo_cartera_validacion,
                                'dias_mora_validacion', sd.dias_mora_validacion,
                                'cumple_mora_validacion', sd.cumple_mora_validacion
                            )
                            ORDER BY
                                sd.orden_deudor,
                                sd.id_solicitud_deudor
                        ),
                        '[]'::jsonb
                    ) AS foto
    
                    FROM cartera.solicitudes_deudores sd
    
                    INNER JOIN hoja_vida.datos_personales dp
                            ON dp.id_datos_personal = sd.id_datos_personal
    
                    WHERE sd.id_solicitud_credito = :idSolicitudCredito
                      AND sd.activo = true
                    """;

            return obtenerFoto(
                    sql,
                    idSolicitudCredito,
                    "deudores"
            );
        }


        public String obtenerFotoFinanciero(
                Integer idSolicitudCredito
        ) {
            String sql = """
                    SELECT COALESCE(
                        jsonb_agg(
                            jsonb_build_object(
                                'id_solicitud_deudor', sd.id_solicitud_deudor,
                                'id_datos_personal', sd.id_datos_personal,
                                'tipo_deudor', sd.tipo_deudor,
                                'orden_deudor', sd.orden_deudor,
                                'financiero', to_jsonb(sdf)
                            )
                            ORDER BY
                                sd.orden_deudor,
                                sd.id_solicitud_deudor
                        ),
                        '[]'::jsonb
                    ) AS foto
    
                    FROM cartera.solicitudes_deudores sd
    
                    INNER JOIN cartera.solicitudes_deudores_financieros sdf
                            ON sdf.id_solicitud_deudor = sd.id_solicitud_deudor
    
                    WHERE sd.id_solicitud_credito = :idSolicitudCredito
                      AND sd.activo = true
                    """;

            return obtenerFoto(
                    sql,
                    idSolicitudCredito,
                    "financiero"
            );
        }


        public String obtenerFotoBienes(
                Integer idSolicitudCredito
        ) {
            String sql = """
                    SELECT COALESCE(
                        jsonb_agg(
                            jsonb_build_object(
                                'id_solicitud_deudor_bien', sdb.id_solicitud_deudor_bien,
                                'id_solicitud_deudor', sd.id_solicitud_deudor,
                                'id_datos_personal', sd.id_datos_personal,
    
                                'tipo_deudor', sd.tipo_deudor,
                                'orden_deudor', sd.orden_deudor,
    
                                'id_bien_persona', sdb.id_bien_persona,
                                'id_bien', bp.id_bien,
                                'descripcion_bien', b.descripcion_general,
    
                                'fecha_fotografia', sdb.fecha_fotografia,
    
                                'porcentaje_propiedad', sdb.porcentaje_propiedad,
                                'valor_comercial', sdb.valor_comercial,
                                'valor_gravamen', sdb.valor_gravamen,
    
                                'es_garantia_real', sdb.es_garantia_real,
    
                                'porcentaje_admisible', sdb.porcentaje_admisible,
                                'valor_garantia_admisible', sdb.valor_garantia_admisible,
                                'valor_comprometido_creditos', sdb.valor_comprometido_creditos,
                                'valor_garantia_disponible', sdb.valor_garantia_disponible,
    
                                'valor_requerido_solicitud', sdb.valor_requerido_solicitud,
                                'valor_asignado_solicitud', sdb.valor_asignado_solicitud,
    
                                'observacion', sdb.observacion
                            )
                            ORDER BY
                                sd.orden_deudor,
                                sdb.id_solicitud_deudor_bien
                        ),
                        '[]'::jsonb
                    ) AS foto
    
                    FROM cartera.solicitudes_deudores sd
    
                    INNER JOIN cartera.solicitudes_deudores_bienes sdb
                            ON sdb.id_solicitud_deudor = sd.id_solicitud_deudor
    
                    INNER JOIN hoja_vida.bienes_personas bp
                            ON bp.id_bien_persona = sdb.id_bien_persona
    
                    INNER JOIN hoja_vida.bienes b
                            ON b.id_bien = bp.id_bien
    
                    WHERE sd.id_solicitud_credito = :idSolicitudCredito
                      AND sd.activo = true
                      AND sdb.activo = true
                    """;

            return obtenerFoto(
                    sql,
                    idSolicitudCredito,
                    "bienes"
            );
        }


        public String obtenerFotoCentralRiesgo(
                Integer idSolicitudCredito
        ) {
            String sql = """
                    SELECT COALESCE(
                        jsonb_agg(
                            jsonb_build_object(
                                'id_solicitud_deudor', sd.id_solicitud_deudor,
                                'id_datos_personal', sd.id_datos_personal,
                                'tipo_deudor', sd.tipo_deudor,
                                'orden_deudor', sd.orden_deudor,
                                'central_riesgo', to_jsonb(src)
                            )
                            ORDER BY
                                sd.orden_deudor,
                                sd.id_solicitud_deudor
                        ),
                        '[]'::jsonb
                    ) AS foto
    
                    FROM cartera.solicitudes_deudores sd
    
                    INNER JOIN cartera.solicitudes_deudores_centrales src
                            ON src.id_solicitud_deudor = sd.id_solicitud_deudor
    
                    WHERE sd.id_solicitud_credito = :idSolicitudCredito
                      AND sd.activo = true
                      AND src.activo = true
                    """;

            return obtenerFoto(
                    sql,
                    idSolicitudCredito,
                    "central de riesgo"
            );
        }


        public String obtenerFotoAnalisis(
                Integer idSolicitudCredito
        ) {
            String sql = """
                    SELECT COALESCE(
                        jsonb_agg(
                            to_jsonb(sa)
                            ORDER BY sa.id_solicitud_deudor
                        ),
                        '[]'::jsonb
                    ) AS foto
    
                    FROM cartera.solicitudes_analisis sa
    
                    WHERE sa.id_solicitud_credito = :idSolicitudCredito
                    """;

            return obtenerFoto(
                    sql,
                    idSolicitudCredito,
                    "análisis"
            );
        }


        // =========================================================
        // SOPORTE FOTOGRAFÍAS
        // =========================================================

        private String obtenerFoto(
                String sql,
                Integer idSolicitudCredito,
                String nombreFoto
        ) {

            String foto = jdbc.queryForObject(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(
                                    "idSolicitudCredito",
                                    idSolicitudCredito
                            ),
                    (rs, rowNum) ->
                            rs.getString("foto")
            );

            if (foto == null || foto.isBlank()) {
                throw new IllegalStateException(
                        "No fue posible generar la fotografía de "
                                + nombreFoto
                                + " para la solicitud "
                                + idSolicitudCredito
                                + "."
                );
            }

            return foto;
        }

    }