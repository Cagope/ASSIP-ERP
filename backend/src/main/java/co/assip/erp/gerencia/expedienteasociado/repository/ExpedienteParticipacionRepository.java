package co.assip.erp.gerencia.expedienteasociado.repository;

import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteParticipacionInstitucionalDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExpedienteParticipacionRepository
        extends ExpedienteRepositorySupport {

    private static final
    BeanPropertyRowMapper<ExpedienteParticipacionInstitucionalDTO>
            PARTICIPACION_MAPPER =
            mapper(ExpedienteParticipacionInstitucionalDTO.class);

    private static final String SQL_PARTICIPACION_INSTITUCIONAL = """

            /* =====================================================
               1. CARGOS DIRECTIVOS ACTIVOS DEL ASOCIADO
               ===================================================== */
            SELECT
                'DIRECTIVO'::varchar
                    AS tipo_participacion,

                EXISTS (
                    SELECT 1
                    FROM general.privilegiados p
                    WHERE p.id_directivo = d.id_directivo
                )::boolean
                    AS es_privilegiado,

                d.id_directivo::bigint
                    AS id_directivo,

                d.codigo_tipo_directivo::varchar
                    AS codigo_tipo_directivo,

                td.nombre_tipo_directivo::varchar
                    AS nombre_tipo_directivo,

                d.calidad_directivo::varchar
                    AS calidad_directivo,

                CASE
                    WHEN d.calidad_directivo = '1'
                        THEN 'Principal'
                    WHEN d.calidad_directivo = '2'
                        THEN 'Suplente'
                    ELSE d.calidad_directivo
                END::varchar
                    AS nombre_calidad_directivo,

                d.estado_directivo::varchar
                    AS estado_directivo,

                CASE
                    WHEN d.estado_directivo = '1'
                        THEN 'Activo'
                    WHEN d.estado_directivo = '0'
                        THEN 'Inactivo'
                    ELSE d.estado_directivo
                END::varchar
                    AS nombre_estado_directivo,

                d.acta_asamblea::varchar
                    AS acta_asamblea,

                d.fecha_asamblea::date
                    AS fecha_asamblea,

                d.resolucion_ses::varchar
                    AS resolucion_ses,

                d.fecha_resolucion::date
                    AS fecha_resolucion,

                d.fecha_retiro::date
                    AS fecha_retiro,

                NULL::bigint
                    AS id_comite,

                NULL::varchar
                    AS nombre_comite,

                NULL::bigint
                    AS id_comite_detalle,

                NULL::varchar
                    AS codigo_cargo_comite,

                NULL::varchar
                    AS nombre_cargo_comite,

                NULL::varchar
                    AS numero_acta,

                NULL::date
                    AS fecha_nombramiento,

                NULL::bigint
                    AS id_privilegiado,

                NULL::bigint
                    AS id_persona_relacionada,

                NULL::varchar
                    AS documento_relacionado,

                NULL::varchar
                    AS nombre_relacionado,

                NULL::varchar
                    AS codigo_parentesco,

                NULL::varchar
                    AS nombre_parentesco,

                10::integer
                    AS orden

            FROM general.directivos d

            LEFT JOIN catalogos.tipos_directivos td
                   ON td.codigo_tipo_directivo =
                      d.codigo_tipo_directivo

            WHERE d.id_datos_personal = :idDatosPersonal
              AND d.estado_directivo = '1'
              AND d.fecha_retiro IS NULL


            UNION ALL


            /* =====================================================
               2. COMITÉS ACTIVOS DEL ASOCIADO
               ===================================================== */
            SELECT
                'COMITE'::varchar
                    AS tipo_participacion,

                false::boolean
                    AS es_privilegiado,

                NULL::bigint
                    AS id_directivo,

                NULL::varchar
                    AS codigo_tipo_directivo,

                NULL::varchar
                    AS nombre_tipo_directivo,

                NULL::varchar
                    AS calidad_directivo,

                NULL::varchar
                    AS nombre_calidad_directivo,

                NULL::varchar
                    AS estado_directivo,

                NULL::varchar
                    AS nombre_estado_directivo,

                NULL::varchar
                    AS acta_asamblea,

                NULL::date
                    AS fecha_asamblea,

                NULL::varchar
                    AS resolucion_ses,

                NULL::date
                    AS fecha_resolucion,

                cd.fecha_retiro::date
                    AS fecha_retiro,

                c.id_comite::bigint
                    AS id_comite,

                c.nombre_comite::varchar
                    AS nombre_comite,

                cd.id_comite_detalle::bigint
                    AS id_comite_detalle,

                cd.codigo_cargo_comite::varchar
                    AS codigo_cargo_comite,

                cc.nombre_cargo_comite::varchar
                    AS nombre_cargo_comite,

                cd.numero_acta::varchar
                    AS numero_acta,

                cd.fecha_nombramiento::date
                    AS fecha_nombramiento,

                NULL::bigint
                    AS id_privilegiado,

                NULL::bigint
                    AS id_persona_relacionada,

                NULL::varchar
                    AS documento_relacionado,

                NULL::varchar
                    AS nombre_relacionado,

                NULL::varchar
                    AS codigo_parentesco,

                NULL::varchar
                    AS nombre_parentesco,

                20::integer
                    AS orden

            FROM general.comites_detalle cd

            INNER JOIN general.comites c
                    ON c.id_comite = cd.id_comite

            LEFT JOIN catalogos.cargos_comites cc
                   ON cc.codigo_cargo_comite =
                      cd.codigo_cargo_comite

            WHERE cd.id_datos_personal = :idDatosPersonal
              AND cd.activo = true
              AND cd.fecha_retiro IS NULL
              AND c.activo = true


            UNION ALL


            /* =====================================================
               3. ASOCIADO RELACIONADO CON UN DIRECTIVO
               ===================================================== */
            SELECT
                'PERSONA_RELACIONADA'::varchar
                    AS tipo_participacion,

                true::boolean
                    AS es_privilegiado,

                d.id_directivo::bigint
                    AS id_directivo,

                d.codigo_tipo_directivo::varchar
                    AS codigo_tipo_directivo,

                td.nombre_tipo_directivo::varchar
                    AS nombre_tipo_directivo,

                d.calidad_directivo::varchar
                    AS calidad_directivo,

                CASE
                    WHEN d.calidad_directivo = '1'
                        THEN 'Principal'
                    WHEN d.calidad_directivo = '2'
                        THEN 'Suplente'
                    ELSE d.calidad_directivo
                END::varchar
                    AS nombre_calidad_directivo,

                d.estado_directivo::varchar
                    AS estado_directivo,

                CASE
                    WHEN d.estado_directivo = '1'
                        THEN 'Activo'
                    WHEN d.estado_directivo = '0'
                        THEN 'Inactivo'
                    ELSE d.estado_directivo
                END::varchar
                    AS nombre_estado_directivo,

                d.acta_asamblea::varchar
                    AS acta_asamblea,

                d.fecha_asamblea::date
                    AS fecha_asamblea,

                d.resolucion_ses::varchar
                    AS resolucion_ses,

                d.fecha_resolucion::date
                    AS fecha_resolucion,

                d.fecha_retiro::date
                    AS fecha_retiro,

                NULL::bigint
                    AS id_comite,

                NULL::varchar
                    AS nombre_comite,

                NULL::bigint
                    AS id_comite_detalle,

                NULL::varchar
                    AS codigo_cargo_comite,

                NULL::varchar
                    AS nombre_cargo_comite,

                NULL::varchar
                    AS numero_acta,

                NULL::date
                    AS fecha_nombramiento,

                p.id_privilegiado::bigint
                    AS id_privilegiado,

                p.id_datos_personal::bigint
                    AS id_persona_relacionada,

                dp.documento::varchar
                    AS documento_relacionado,

                COALESCE(
                    NULLIF(
                        TRIM(dp.nombre_completo_nombres),
                        ''
                    ),
                    NULLIF(
                        TRIM(
                            CONCAT_WS(
                                ' ',
                                dp.nombres,
                                dp.primer_apellido,
                                dp.segundo_apellido
                            )
                        ),
                        ''
                    )
                )::varchar
                    AS nombre_relacionado,

                p.codigo_parentesco::varchar
                    AS codigo_parentesco,

                pa.nombre_parentesco::varchar
                    AS nombre_parentesco,

                30::integer
                    AS orden

            FROM general.privilegiados p

            INNER JOIN general.directivos d
                    ON d.id_directivo = p.id_directivo

            LEFT JOIN catalogos.tipos_directivos td
                   ON td.codigo_tipo_directivo =
                      d.codigo_tipo_directivo

            LEFT JOIN reporting.vw_datos_personales_operativa dp
                   ON dp.id_datos_personal =
                      d.id_datos_personal

            LEFT JOIN catalogos.parentescos pa
                   ON pa.codigo_parentesco =
                      p.codigo_parentesco

            WHERE p.id_datos_personal =
                  :idDatosPersonal

              AND d.estado_directivo = '1'
              AND d.fecha_retiro IS NULL


            ORDER BY
                orden,
                nombre_tipo_directivo,
                nombre_comite,
                nombre_parentesco,
                id_directivo,
                id_comite,
                id_privilegiado
            """;

    public ExpedienteParticipacionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        super(jdbc);
    }

    public List<ExpedienteParticipacionInstitucionalDTO>
    listarParticipacionInstitucional(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return jdbc.query(
                SQL_PARTICIPACION_INSTITUCIONAL,
                parametros(idDatosPersonal),
                PARTICIPACION_MAPPER
        );
    }
}