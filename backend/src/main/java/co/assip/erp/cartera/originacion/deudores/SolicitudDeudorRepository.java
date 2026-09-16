package co.assip.erp.cartera.originacion.deudores;

import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorDTO;
import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorDetalleDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudDeudorRepository {

    private static final String TIPO_CODEUDOR = "CODEUDOR";


    // =========================================================
    // SQL - LISTAR POR SOLICITUD
    // =========================================================

    private static final String SQL_LISTAR_POR_SOLICITUD = """
            SELECT
                sd.id_solicitud_deudor,
                sd.id_solicitud_credito,
                sd.id_datos_personal,
                sd.tipo_deudor,
                sd.orden_deudor,

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

                sd.saldo_cartera_inicio,
                sd.dias_mora_inicio,
                sd.cumple_mora_inicio,

                sd.saldo_cartera_validacion,
                sd.dias_mora_validacion,
                sd.cumple_mora_validacion,

                sd.activo

            FROM cartera.solicitudes_deudores sd

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal = sd.id_datos_personal

            WHERE sd.id_solicitud_credito = :idSolicitudCredito
              AND sd.activo = true

            ORDER BY
                sd.orden_deudor,
                sd.id_solicitud_deudor
            """;


    // =========================================================
    // SQL - BUSCAR POR ID
    // =========================================================

    private static final String SQL_BUSCAR_POR_ID = """
            SELECT
                sd.id_solicitud_deudor,
                sd.id_solicitud_credito,
                sd.id_datos_personal,
                sd.tipo_deudor,
                sd.orden_deudor,

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

                sd.saldo_cartera_inicio,
                sd.dias_mora_inicio,
                sd.cumple_mora_inicio,

                sd.saldo_cartera_validacion,
                sd.dias_mora_validacion,
                sd.cumple_mora_validacion,

                sd.activo,
                sd.fecha_creacion,
                sd.fecha_edicion

            FROM cartera.solicitudes_deudores sd

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal = sd.id_datos_personal

            WHERE sd.id_solicitud_deudor = :idSolicitudDeudor
              AND sd.activo = true
            """;


    // =========================================================
    // SQL - AGENCIA DE LA SOLICITUD
    // =========================================================

    private static final String SQL_AGENCIA_SOLICITUD = """
            SELECT sc.id_agencia
            FROM cartera.solicitudes_creditos sc
            WHERE sc.id_solicitud_credito = :idSolicitudCredito
              AND sc.activo = true
            """;


    // =========================================================
    // SQL - BLOQUEAR SOLICITUD EDITABLE
    // =========================================================

    private static final String SQL_BLOQUEAR_SOLICITUD_EDITABLE = """
            SELECT sc.id_agencia

            FROM cartera.solicitudes_creditos sc

            INNER JOIN cartera.solicitudes_resultados sr
                ON sr.id_solicitud_resultado =
                   sc.id_solicitud_resultado

            WHERE sc.id_solicitud_credito =
                  :idSolicitudCredito

              AND sc.activo = true

              AND sr.es_final = false

            FOR UPDATE OF sc
            """;


    // =========================================================
    // SQL - PERSONA EXISTE
    // =========================================================

    private static final String SQL_EXISTE_PERSONA = """
            SELECT EXISTS (
                SELECT 1
                FROM hoja_vida.datos_personales dp
                WHERE dp.id_datos_personal = :idDatosPersonal
            )
            """;


    // =========================================================
    // SQL - DEUDOR ACTIVO POR PERSONA
    // =========================================================

    private static final String SQL_BUSCAR_ACTIVO_POR_PERSONA = """
            SELECT sd.id_solicitud_deudor
            FROM cartera.solicitudes_deudores sd
            WHERE sd.id_solicitud_credito = :idSolicitudCredito
              AND sd.id_datos_personal = :idDatosPersonal
              AND sd.activo = true
            ORDER BY sd.id_solicitud_deudor
            LIMIT 1
            """;


    // =========================================================
    // SQL - DEUDOR INACTIVO POR PERSONA
    // =========================================================

    private static final String SQL_BUSCAR_INACTIVO_POR_PERSONA = """
            SELECT sd.id_solicitud_deudor
            FROM cartera.solicitudes_deudores sd
            WHERE sd.id_solicitud_credito = :idSolicitudCredito
              AND sd.id_datos_personal = :idDatosPersonal
              AND sd.activo = false
            ORDER BY sd.id_solicitud_deudor DESC
            LIMIT 1
            """;


    // =========================================================
    // SQL - SIGUIENTE ORDEN
    // =========================================================

    private static final String SQL_SIGUIENTE_ORDEN = """
        SELECT
            COALESCE(
                MAX(sd.orden_deudor),
                0
            ) + 1

        FROM cartera.solicitudes_deudores sd

        WHERE sd.id_solicitud_credito =
              :idSolicitudCredito
        """;


    // =========================================================
    // SQL - INSERTAR CODEUDOR
    // =========================================================

    private static final String SQL_INSERTAR_CODEUDOR = """
            INSERT INTO cartera.solicitudes_deudores (
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
            VALUES (
                :idSolicitudCredito,
                :idDatosPersonal,
                :tipoDeudor,
                :ordenDeudor,
                true,
                :idUsuario,
                CURRENT_TIMESTAMP,
                :idUsuario,
                CURRENT_TIMESTAMP
            )
            RETURNING id_solicitud_deudor
            """;


    // =========================================================
    // SQL - REACTIVAR CODEUDOR
    // =========================================================

    private static final String SQL_REACTIVAR_CODEUDOR = """
            UPDATE cartera.solicitudes_deudores
            SET
                tipo_deudor = :tipoDeudor,
                orden_deudor = :ordenDeudor,

                saldo_cartera_inicio = NULL,
                dias_mora_inicio = NULL,
                cumple_mora_inicio = NULL,

                saldo_cartera_validacion = NULL,
                dias_mora_validacion = NULL,
                cumple_mora_validacion = NULL,

                activo = true,

                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP

            WHERE id_solicitud_deudor =
                  :idSolicitudDeudor
            """;


    // =========================================================
    // SQL - DESACTIVAR BIENES
    // =========================================================

    private static final String SQL_DESACTIVAR_BIENES = """
            UPDATE cartera.solicitudes_deudores_bienes
            SET
                activo = false,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_solicitud_deudor = :idSolicitudDeudor
              AND activo = true
            """;


    // =========================================================
    // SQL - DESACTIVAR CENTRALES
    // =========================================================

    private static final String SQL_DESACTIVAR_CENTRALES = """
            UPDATE cartera.solicitudes_deudores_centrales
            SET
                activo = false,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_solicitud_deudor = :idSolicitudDeudor
              AND activo = true
            """;


    // =========================================================
    // SQL - DESACTIVAR FINANCIERO
    // =========================================================

    private static final String SQL_DESACTIVAR_FINANCIERO = """
            UPDATE cartera.solicitudes_deudores_financieros
            SET
                activo = false,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_solicitud_deudor = :idSolicitudDeudor
              AND activo = true
            """;


    // =========================================================
    // SQL - DESACTIVAR CODEUDOR
    // =========================================================

    private static final String SQL_DESACTIVAR_CODEUDOR = """
            UPDATE cartera.solicitudes_deudores
            SET
                activo = false,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_solicitud_deudor = :idSolicitudDeudor
              AND activo = true
              AND UPPER(TRIM(tipo_deudor)) = :tipoCodeudor
            """;


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<SolicitudDeudorDTO>
            RESUMEN_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudDeudorDTO.class
            );

    private static final BeanPropertyRowMapper<SolicitudDeudorDetalleDTO>
            DETALLE_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudDeudorDetalleDTO.class
            );


    // =========================================================
    // DEPENDENCIA
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudDeudorRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // LISTAR
    // =========================================================

    public List<SolicitudDeudorDTO> listarPorSolicitud(
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
    // BUSCAR POR ID
    // =========================================================

    public Optional<SolicitudDeudorDetalleDTO> buscarPorId(
            Integer idSolicitudDeudor
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        );

        List<SolicitudDeudorDetalleDTO> resultados =
                jdbc.query(
                        SQL_BUSCAR_POR_ID,
                        parametros,
                        DETALLE_MAPPER
                );

        return resultados.stream().findFirst();
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

        return resultados.stream().findFirst();
    }


    // =========================================================
    // BLOQUEAR SOLICITUD EDITABLE
    // =========================================================

    public Optional<Integer> bloquearSolicitudEditable(
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
                        SQL_BLOQUEAR_SOLICITUD_EDITABLE,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt("id_agencia")
                );

        return resultados.stream().findFirst();
    }


    // =========================================================
    // EXISTE PERSONA
    // =========================================================

    public boolean existePersona(
            Integer idDatosPersonal
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        );

        Boolean existe =
                jdbc.queryForObject(
                        SQL_EXISTE_PERSONA,
                        parametros,
                        Boolean.class
                );

        return Boolean.TRUE.equals(existe);
    }


    // =========================================================
    // BUSCAR ACTIVO POR PERSONA
    // =========================================================

    public Optional<Integer> buscarActivoPorPersona(
            Integer idSolicitudCredito,
            Integer idDatosPersonal
    ) {

        MapSqlParameterSource parametros =
                parametrosSolicitudPersona(
                        idSolicitudCredito,
                        idDatosPersonal
                );

        List<Integer> resultados =
                jdbc.query(
                        SQL_BUSCAR_ACTIVO_POR_PERSONA,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt(
                                        "id_solicitud_deudor"
                                )
                );

        return resultados.stream().findFirst();
    }


    // =========================================================
    // BUSCAR INACTIVO POR PERSONA
    // =========================================================

    public Optional<Integer> buscarInactivoPorPersona(
            Integer idSolicitudCredito,
            Integer idDatosPersonal
    ) {

        MapSqlParameterSource parametros =
                parametrosSolicitudPersona(
                        idSolicitudCredito,
                        idDatosPersonal
                );

        List<Integer> resultados =
                jdbc.query(
                        SQL_BUSCAR_INACTIVO_POR_PERSONA,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt(
                                        "id_solicitud_deudor"
                                )
                );

        return resultados.stream().findFirst();
    }


    // =========================================================
    // SIGUIENTE ORDEN
    // =========================================================

    public Integer obtenerSiguienteOrden(
            Integer idSolicitudCredito
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        Integer orden =
                jdbc.queryForObject(
                        SQL_SIGUIENTE_ORDEN,
                        parametros,
                        Integer.class
                );

        return orden != null
                ? orden
                : 1;
    }


    // =========================================================
    // INSERTAR CODEUDOR
    // =========================================================

    public Integer insertarCodeudor(
            Integer idSolicitudCredito,
            Integer idDatosPersonal,
            Integer ordenDeudor,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
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
                                "tipoDeudor",
                                TIPO_CODEUDOR
                        )
                        .addValue(
                                "ordenDeudor",
                                ordenDeudor
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.queryForObject(
                SQL_INSERTAR_CODEUDOR,
                parametros,
                Integer.class
        );
    }


    // =========================================================
    // REACTIVAR CODEUDOR
    // =========================================================

    public void reactivarCodeudor(
            Integer idSolicitudDeudor,
            Integer ordenDeudor,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        )
                        .addValue(
                                "tipoDeudor",
                                TIPO_CODEUDOR
                        )
                        .addValue(
                                "ordenDeudor",
                                ordenDeudor
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        int actualizados =
                jdbc.update(
                        SQL_REACTIVAR_CODEUDOR,
                        parametros
                );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No fue posible reactivar el codeudor."
            );
        }
    }


    // =========================================================
    // DESACTIVAR DEPENDENCIAS
    // =========================================================

    public void desactivarDependencias(
            Integer idSolicitudDeudor,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
                parametrosEdicion(
                        idSolicitudDeudor,
                        idUsuario
                );

        jdbc.update(
                SQL_DESACTIVAR_BIENES,
                parametros
        );

        jdbc.update(
                SQL_DESACTIVAR_CENTRALES,
                parametros
        );

        jdbc.update(
                SQL_DESACTIVAR_FINANCIERO,
                parametros
        );
    }


    // =========================================================
    // DESACTIVAR CODEUDOR
    // =========================================================

    public boolean desactivarCodeudor(
            Integer idSolicitudDeudor,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
                parametrosEdicion(
                        idSolicitudDeudor,
                        idUsuario
                )
                        .addValue(
                                "tipoCodeudor",
                                TIPO_CODEUDOR
                        );

        int actualizados =
                jdbc.update(
                        SQL_DESACTIVAR_CODEUDOR,
                        parametros
                );

        return actualizados == 1;
    }


    // =========================================================
    // SOPORTE
    // =========================================================

    private MapSqlParameterSource parametrosSolicitudPersona(
            Integer idSolicitudCredito,
            Integer idDatosPersonal
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "idSolicitudCredito",
                        idSolicitudCredito
                )
                .addValue(
                        "idDatosPersonal",
                        idDatosPersonal
                );
    }

    private MapSqlParameterSource parametrosEdicion(
            Integer idSolicitudDeudor,
            Integer idUsuario
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "idSolicitudDeudor",
                        idSolicitudDeudor
                )
                .addValue(
                        "idUsuario",
                        idUsuario
                );
    }
}