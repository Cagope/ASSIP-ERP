package co.assip.erp.gerencia.expedienteasociado.repository;

import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteInformacionFinancieraDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteResumenGeneralDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteSarlaftDTO;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteContactoDTO;

import java.util.Optional;

/**
 * Repositorio de consulta del dominio Persona del
 * Expediente Integral del Asociado.
 *
 * Solo lectura.
 */
@Repository
public class ExpedientePersonaRepository {

    private static final String PARAM_ID_DATOS_PERSONAL =
            "idDatosPersonal";

    private final NamedParameterJdbcTemplate jdbc;

    public ExpedientePersonaRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // RowMapper
    // =========================================================

    private static final BeanPropertyRowMapper<ExpedienteResumenGeneralDTO>
            RESUMEN_MAPPER =
            mapper(ExpedienteResumenGeneralDTO.class);

    private static final BeanPropertyRowMapper<ExpedienteContactoDTO>
            CONTACTO_MAPPER =
            mapper(ExpedienteContactoDTO.class);

    private static final BeanPropertyRowMapper<ExpedienteInformacionFinancieraDTO>
            FINANCIERO_MAPPER =
            mapper(ExpedienteInformacionFinancieraDTO.class);

    private static final BeanPropertyRowMapper<ExpedienteSarlaftDTO>
            SARLAFT_MAPPER =
            mapper(ExpedienteSarlaftDTO.class);

    private static <T> BeanPropertyRowMapper<T> mapper(
            Class<T> tipo
    ) {
        BeanPropertyRowMapper<T> mapper =
                BeanPropertyRowMapper.newInstance(tipo);

        mapper.setCheckFullyPopulated(false);
        mapper.setPrimitivesDefaultedForNullValue(true);

        return mapper;
    }

    // =========================================================
    // SQL: existencia
    // =========================================================

    private static final String SQL_EXISTE_PERSONA = """
            SELECT EXISTS (
                SELECT 1
                FROM reporting.vw_hoja_vida_general_total h
                WHERE h.id_datos_personal = :idDatosPersonal
            )
            """;

    // =========================================================
    // SQL: resumen general
    // =========================================================

    private static final String SQL_RESUMEN_GENERAL = """
            WITH persona AS (
                SELECT
                    h.id_datos_personal,
                    h.tipo_persona,
                    h.tipo_documento,
                    h.nombre_tipo_documento,
                    h.documento,
                    h.nombres,
                    h.primer_apellido,
                    h.segundo_apellido,

                    trim(
                        concat_ws(
                            ' ',
                            h.nombres,
                            h.primer_apellido,
                            h.segundo_apellido
                        )
                    ) AS nombre_completo,

                    h.fecha_nacimiento,

                    CASE
                        WHEN h.fecha_nacimiento IS NULL THEN NULL
                        ELSE extract(
                            year FROM age(
                                current_date,
                                h.fecha_nacimiento
                            )
                        )::integer
                    END AS edad,

                    h.nombre_genero AS genero,
                    h.nombre_estado_civil AS estado_civil,
                    h.nombre_ocupacion AS ocupacion,
                    h.nombre_escolaridad,
            
                    h.cabeza_familia,
                    h.numero_hijos,
                    h.estrato_social,
            
                    h.nombre_tipo_vivienda,
            
                    h.nombre_sector_economico,
                    h.nombre_actividad_ses,
                    h.nombre_actividad_dian,
            
                    h.pais_nacimiento,
                    h.departamento_nacimiento,
                    h.ciudad_nacimiento,

                    h.direccion_residencia AS direccion,
                    h.barrio_residencia AS barrio,
                    h.ciudad_residencia AS ciudad,
                    h.departamento_residencia AS departamento,
                    h.pais_residencia AS pais,

                    h.telefono_residencia AS telefono,
                    COALESCE(
                        NULLIF(trim(h.celular_uno), ''),
                        NULLIF(trim(h.celular_dos), '')
                    ) AS celular,

                    h.correo_principal AS correo_electronico,
                    h.nombre_empresa AS empresa,

                    h.valor_salario,
                    h.valor_pension,
                    h.ingresos_arriendo,
                    h.ingresos_comisiones,
                    h.otros_ingresos,

                    h.egresos_familiares,
                    h.egresos_arriendo,
                    h.egresos_credito,
                    h.otros_egresos,

                    h.total_activos,
                    h.total_pasivos,

                    h.asociado_peps AS pep,
                    h.familia_peps AS familiar_pep,
                    h.moneda_extranjera,
                    h.cuenta_extranjero,

                    h.fecha_apertura,
                    h.fecha_actualizacion

                FROM reporting.vw_hoja_vida_general_total h
                WHERE h.id_datos_personal = :idDatosPersonal
                LIMIT 1
            ),

            captaciones AS (
                  SELECT
                      count(*) FILTER (
                          WHERE trim(a.codigo_tipo_captacion) <> '1'
                      )::integer AS numero_cuentas_ahorro,
            
                      COALESCE(
                          sum(
                              CASE
                                  WHEN trim(a.codigo_tipo_captacion) <> '1'
                                      THEN COALESCE(a.saldo_actual_cuenta, 0)
                                  ELSE 0
                              END
                          ),
                          0
                      )::numeric AS saldo_ahorros
            
                  FROM reporting.vw_depositos_cuentas_ahorro_integral a
            
                  WHERE a.id_datos_personal = :idDatosPersonal
              ),
            
              cuenta_aportes AS (
                  SELECT
                      a.codigo_estado_cuenta
                          AS codigo_estado_asociado,
            
                      a.nombre_estado_cuenta
                          AS nombre_estado_asociado,
            
                      COALESCE(
                          a.estado_operativo,
                          false
                      ) AS activo,
            
                      a.fecha_apertura_cuenta
                          AS fecha_afiliacion,
            
                      COALESCE(
                          a.saldo_actual_cuenta,
                          0
                      )::numeric AS saldo_aportes,
            
                      a.id_agencia,
                      a.codigo_agencia,
                      a.nombre_agencia
            
                  FROM reporting.vw_depositos_cuentas_ahorro_integral a
            
                  WHERE a.id_datos_personal = :idDatosPersonal
                    AND trim(a.codigo_tipo_captacion) = '1'
            
                  ORDER BY
                      COALESCE(a.estado_operativo, false) DESC,
                      a.fecha_apertura_cuenta DESC NULLS LAST,
                      a.id_cuenta_ahorro DESC
            
                  LIMIT 1
              ),

            cdats AS (
                SELECT
                    count(*)::integer AS numero_cdats,
                    COALESCE(
                        sum(COALESCE(c.saldo_actual_cdat, 0)),
                        0
                    )::numeric AS saldo_cdats
                FROM cdat.vw_cdat_cuentas_total_extendida c
                WHERE c.id_datos_personal = :idDatosPersonal
            ),

            creditos AS (
                SELECT
                    count(*)::integer AS numero_creditos,

                    COALESCE(
                        sum(COALESCE(c.saldo_neto_pendiente, 0)),
                        0
                    )::numeric AS saldo_capital_cartera

                FROM reporting.vw_cartera_creditos_patrimonio c
                WHERE c.id_datos_personal = :idDatosPersonal
            ),

            bienes AS (
                 SELECT
                     count(*)::integer AS numero_bienes,
            
                     COALESCE(
                         sum(valor_neto),
                         0
                     )::numeric AS valor_bienes
            
                 FROM (
            
                     (
                         SELECT DISTINCT ON (
                             i.id_datos_personal,
                             i.id_bien
                         )
                             i.id_datos_personal,
                             i.id_bien,
                             COALESCE(
                                 i.valor_neto_asociado,
                                 0
                             ) AS valor_neto
            
                         FROM reporting.vw_hoja_vida_bienes_inmuebles_total i
            
                         WHERE i.id_datos_personal = :idDatosPersonal
            
                         ORDER BY
                             i.id_datos_personal,
                             i.id_bien,
                             i.fecha_avaluo DESC NULLS LAST,
                             i.fecha_edicion DESC NULLS LAST,
                             i.fecha_creacion DESC NULLS LAST
                     )
            
                     UNION ALL
            
                     (
                         SELECT DISTINCT ON (
                             v.id_datos_personal,
                             v.id_bien
                         )
                             v.id_datos_personal,
                             v.id_bien,
                             COALESCE(
                                 v.valor_neto_asociado,
                                 0
                             ) AS valor_neto
            
                         FROM reporting.vw_hoja_vida_bienes_vehiculos_total v
            
                         WHERE v.id_datos_personal = :idDatosPersonal
            
                         ORDER BY
                             v.id_datos_personal,
                             v.id_bien,
                             v.fecha_edicion DESC NULLS LAST,
                             v.fecha_creacion DESC NULLS LAST
                     )
            
                     UNION ALL
            
                     (
                         SELECT DISTINCT ON (
                             m.id_datos_personal,
                             m.id_bien
                         )
                             m.id_datos_personal,
                             m.id_bien,
                             COALESCE(
                                 m.valor_neto_asociado,
                                 0
                             ) AS valor_neto
            
                         FROM reporting.vw_hoja_vida_bienes_maquinaria_total m
            
                         WHERE m.id_datos_personal = :idDatosPersonal
            
                         ORDER BY
                             m.id_datos_personal,
                             m.id_bien,
                             m.fecha_edicion DESC NULLS LAST,
                             m.fecha_creacion DESC NULLS LAST
                     )
            
                     UNION ALL
            
                     (
                         SELECT DISTINCT ON (
                             n.id_datos_personal,
                             n.id_bien
                         )
                             n.id_datos_personal,
                             n.id_bien,
                             COALESCE(
                                 n.valor_neto_asociado,
                                 0
                             ) AS valor_neto
            
                         FROM reporting.vw_hoja_vida_bienes_inversiones_total n
            
                         WHERE n.id_datos_personal = :idDatosPersonal
            
                         ORDER BY
                             n.id_datos_personal,
                             n.id_bien,
                             n.fecha_edicion DESC NULLS LAST,
                             n.fecha_creacion DESC NULLS LAST
                     )
            
                 ) x
            )

            SELECT
                p.id_datos_personal,
                p.tipo_persona,
                p.tipo_documento,
                p.nombre_tipo_documento,
                p.documento,
                p.nombres,
                p.primer_apellido,
                p.segundo_apellido,
                p.nombre_completo,
                
                ap.codigo_estado_asociado,
                ap.nombre_estado_asociado,
                ap.activo,
            
                ap.fecha_afiliacion,
            
                CASE
                    WHEN ap.fecha_afiliacion IS NULL THEN NULL
                    ELSE (
                        current_date - ap.fecha_afiliacion
                    )::integer
                END AS antiguedad_dias,
            
                CASE
                    WHEN ap.fecha_afiliacion IS NULL THEN NULL
                    ELSE (
                        extract(
                            year FROM age(
                                current_date,
                                ap.fecha_afiliacion
                            )
                        ) * 12
                        +
                        extract(
                            month FROM age(
                                current_date,
                                ap.fecha_afiliacion
                            )
                        )
                    )::integer
                END AS antiguedad_meses,
            
                CASE
                    WHEN ap.fecha_afiliacion IS NULL THEN NULL
                    ELSE extract(
                        year FROM age(
                            current_date,
                            ap.fecha_afiliacion
                        )
                    )::integer
                END AS antiguedad_anios,
            
                ap.id_agencia,
                ap.codigo_agencia,
                ap.nombre_agencia,

                p.fecha_nacimiento,
                p.edad,
                p.genero,
                p.estado_civil,
                p.ocupacion,
                p.nombre_escolaridad,
            
                p.cabeza_familia,
                p.numero_hijos,
                p.estrato_social,
            
                p.nombre_tipo_vivienda,
            
                p.nombre_sector_economico,
                p.nombre_actividad_ses,
                p.nombre_actividad_dian,
            
                p.pais_nacimiento,
                p.departamento_nacimiento,
                p.ciudad_nacimiento,

                p.direccion,
                p.barrio,
                p.ciudad,
                p.departamento,
                p.pais,

                p.telefono,
                p.celular,
                p.correo_electronico,

                p.empresa,

                (
                    COALESCE(p.valor_salario, 0)
                  + COALESCE(p.valor_pension, 0)
                  + COALESCE(p.ingresos_arriendo, 0)
                  + COALESCE(p.ingresos_comisiones, 0)
                  + COALESCE(p.otros_ingresos, 0)
                )::numeric AS ingresos_mensuales,

                (
                    COALESCE(p.egresos_familiares, 0)
                  + COALESCE(p.egresos_arriendo, 0)
                  + COALESCE(p.egresos_credito, 0)
                  + COALESCE(p.otros_egresos, 0)
                )::numeric AS egresos_mensuales,

                COALESCE(p.total_activos, 0)
                    AS activos,

                COALESCE(p.total_pasivos, 0)
                    AS pasivos,

                (
                    COALESCE(p.total_activos, 0)
                  - COALESCE(p.total_pasivos, 0)
                )::numeric AS patrimonio,

                cap.numero_cuentas_ahorro,
                d.numero_cdats,
                c.numero_creditos,
                b.numero_bienes,

                ap.saldo_aportes,
                cap.saldo_ahorros,
                d.saldo_cdats,
                c.saldo_capital_cartera,
                b.valor_bienes,

                GREATEST(
                    (
                        COALESCE(p.total_activos, 0)
                      - COALESCE(p.total_pasivos, 0)
                    ),
                    (
                        COALESCE(ap.saldo_aportes, 0)
                      + COALESCE(cap.saldo_ahorros, 0)
                      + COALESCE(d.saldo_cdats, 0)
                      + COALESCE(b.valor_bienes, 0)
                    )
                )::numeric AS patrimonio_estimado,

                p.pep,
                p.familiar_pep,
                p.moneda_extranjera,
                p.cuenta_extranjero,

                p.fecha_actualizacion
                    AS fecha_actualizacion_hoja_vida,

                CASE
                    WHEN p.fecha_actualizacion IS NULL THEN NULL
                    ELSE (
                        current_date - p.fecha_actualizacion
                    )::integer
                END AS dias_sin_actualizar

            FROM persona p
            CROSS JOIN captaciones cap
            LEFT JOIN cuenta_aportes ap
                  ON true
            CROSS JOIN cdats d
            CROSS JOIN creditos c
            CROSS JOIN bienes b
            """;

    // =========================================================
    // SQL: contacto
    // =========================================================

    private static final String SQL_CONTACTO = """
            SELECT
                h.id_datos_personal,
                h.tipo_documento,
                h.nombre_tipo_documento,
                h.documento,
                h.nombres,
                h.primer_apellido,
                h.segundo_apellido,
                h.nombre_completo_apellidos
                    AS nombre_completo,

                h.direccion_residencia
                    AS direccion_principal,

                h.barrio
                    AS barrio_vereda,

                h.pais_residencia
                    AS nombre_pais,

                h.departamento_residencia
                    AS nombre_departamento,

                h.ciudad_residencia
                    AS nombre_ciudad,

                h.codigo_dane_residencia
                    AS codigo_ciudad,

                h.telefono
                    AS telefono_residencia,

                h.telefono_empresa
                    AS telefono_trabajo,

                h.celular_uno
                    AS celular_principal,

                h.celular_dos
                    AS celular_alterno,

                h.correo_personal
                    AS correo_principal,

                h.recibe_emails
                    AS autoriza_correo_electronico,

                h.recibe_msm
                    AS autoriza_mensajes_texto,

                h.recibe_llamadas
                    AS autoriza_llamadas_telefonicas,

                h.nombre_empresa
                    AS empresa,

                h.direccion_empresa,
                h.telefono_empresa,

                (
                    NULLIF(trim(h.direccion_residencia), '')
                    IS NOT NULL
                ) AS tiene_direccion,

                (
                    NULLIF(trim(h.telefono), '')
                    IS NOT NULL
                    OR NULLIF(trim(h.telefono_empresa), '')
                    IS NOT NULL
                ) AS tiene_telefono,

                (
                    NULLIF(trim(h.celular_uno), '')
                    IS NOT NULL
                    OR NULLIF(trim(h.celular_dos), '')
                    IS NOT NULL
                ) AS tiene_celular,

                (
                    NULLIF(trim(h.correo_personal), '')
                    IS NOT NULL
                ) AS tiene_correo

            FROM reporting.vw_hoja_vida_general_total_extendida h
            WHERE h.id_datos_personal = :idDatosPersonal
            LIMIT 1
            """;

    // =========================================================
    // SQL: información financiera
    // =========================================================

    private static final String SQL_FINANCIERO = """
        WITH financiero AS (
            SELECT DISTINCT ON (f.id_datos_personal)
                f.*
            FROM reporting.vw_hoja_vida_financieros_total f
            WHERE f.id_datos_personal = :idDatosPersonal
            ORDER BY
                f.id_datos_personal,
                f.fecha_edicion DESC NULLS LAST,
                f.fecha_creacion DESC NULLS LAST,
                f.id_financiero DESC
        ),

        persona AS (
            SELECT
                h.id_datos_personal,

                h.nombre_actividad_ses,
                h.nombre_sector_economico,
                h.nombre_ocupacion,
                h.nombre_empresa

            FROM reporting.vw_hoja_vida_general_total h

            WHERE h.id_datos_personal = :idDatosPersonal

            LIMIT 1
        )

        SELECT
                f.id_financiero,
                f.id_datos_personal,
                
                p.nombre_actividad_ses
                    AS nombre_actividad_economica,
            
                p.nombre_sector_economico
                    AS nombre_sector_economico,
            
                p.nombre_ocupacion
                    AS ocupacion,
            
                p.nombre_empresa
                    AS empresa,

                f.valor_salario,
                f.valor_pension,
                f.ingresos_arriendo,
                f.ingresos_comisiones,
                f.otros_ingresos,
                f.comentario_otros_ingresos,

                (
                    COALESCE(f.valor_salario, 0)
                  + COALESCE(f.valor_pension, 0)
                  + COALESCE(f.ingresos_arriendo, 0)
                  + COALESCE(f.ingresos_comisiones, 0)
                  + COALESCE(f.otros_ingresos, 0)
                )::numeric AS total_ingresos,

                f.egresos_familiares,
                f.egresos_arriendo,
                f.egresos_credito,
                f.otros_egresos,
                f.comentario_otros_egresos,

                (
                    COALESCE(f.egresos_familiares, 0)
                  + COALESCE(f.egresos_arriendo, 0)
                  + COALESCE(f.egresos_credito, 0)
                  + COALESCE(f.otros_egresos, 0)
                )::numeric AS total_egresos,

                (
                    COALESCE(f.valor_salario, 0)
                  + COALESCE(f.valor_pension, 0)
                  + COALESCE(f.ingresos_arriendo, 0)
                  + COALESCE(f.ingresos_comisiones, 0)
                  + COALESCE(f.otros_ingresos, 0)
                  - COALESCE(f.egresos_familiares, 0)
                  - COALESCE(f.egresos_arriendo, 0)
                  - COALESCE(f.egresos_credito, 0)
                  - COALESCE(f.otros_egresos, 0)
                )::numeric AS disponible_mensual,

                f.total_activos,
                f.total_pasivos,

                (
                    COALESCE(f.total_activos, 0)
                  - COALESCE(f.total_pasivos, 0)
                )::numeric AS patrimonio_neto,

                f.origen_fondos,
                f.relacion_financiera,
                f.deuda_relacion_financiera,

                CASE
                    WHEN (
                        COALESCE(f.valor_salario, 0)
                      + COALESCE(f.valor_pension, 0)
                      + COALESCE(f.ingresos_arriendo, 0)
                      + COALESCE(f.ingresos_comisiones, 0)
                      + COALESCE(f.otros_ingresos, 0)
                    ) <= 0 THEN NULL

                    ELSE round(
                        (
                            (
                                COALESCE(f.egresos_familiares, 0)
                              + COALESCE(f.egresos_arriendo, 0)
                              + COALESCE(f.egresos_credito, 0)
                              + COALESCE(f.otros_egresos, 0)
                            )
                            /
                            NULLIF(
                                (
                                    COALESCE(f.valor_salario, 0)
                                  + COALESCE(f.valor_pension, 0)
                                  + COALESCE(f.ingresos_arriendo, 0)
                                  + COALESCE(f.ingresos_comisiones, 0)
                                  + COALESCE(f.otros_ingresos, 0)
                                ),
                                0
                            )
                        ) * 100,
                        2
                    )
                END AS porcentaje_egresos_ingresos,

                CASE
                    WHEN COALESCE(f.total_activos, 0) <= 0 THEN NULL
                    ELSE round(
                        (
                            COALESCE(f.total_pasivos, 0)
                            /
                            NULLIF(f.total_activos, 0)
                        ) * 100,
                        2
                    )
                END AS porcentaje_endeudamiento,

                greatest(
                    f.fecha_edicion,
                    f.fecha_creacion
                )::date AS fecha_informacion_financiera,

                CASE
                    WHEN greatest(
                        f.fecha_edicion,
                        f.fecha_creacion
                    ) IS NULL THEN NULL

                    ELSE (
                        current_date
                        -
                        greatest(
                            f.fecha_edicion,
                            f.fecha_creacion
                        )::date
                    )::integer
                END AS dias_sin_actualizar,

                CASE
                    WHEN greatest(
                        f.fecha_edicion,
                        f.fecha_creacion
                    ) IS NULL THEN false

                    WHEN current_date
                         -
                         greatest(
                             f.fecha_edicion,
                             f.fecha_creacion
                         )::date <= 365
                        THEN true

                    ELSE false
                END AS informacion_actualizada,

                (
                    (
                        COALESCE(f.valor_salario, 0)
                      + COALESCE(f.valor_pension, 0)
                      + COALESCE(f.ingresos_arriendo, 0)
                      + COALESCE(f.ingresos_comisiones, 0)
                      + COALESCE(f.otros_ingresos, 0)
                    )
                    -
                    (
                        COALESCE(f.egresos_familiares, 0)
                      + COALESCE(f.egresos_arriendo, 0)
                      + COALESCE(f.egresos_credito, 0)
                      + COALESCE(f.otros_egresos, 0)
                    )
                ) >= 0 AS capacidad_pago_suficiente,

                f.fk_seguridad_creacion,
                f.fecha_creacion::date AS fecha_creacion,
                f.fk_seguridad_edicion,
                f.fecha_edicion::date AS fecha_edicion

            FROM financiero f
            
            LEFT JOIN persona p
                   ON p.id_datos_personal =
                      f.id_datos_personal
            
            """;

    // =========================================================
    // SQL: SARLAFT
    // =========================================================

    // =========================================================
// SQL: SARLAFT
// =========================================================

    private static final String SQL_SARLAFT = """
        SELECT
            -- =================================================
            -- Identificación del asociado
            -- =================================================

            s.id_datos_personal,
            s.tipo_documento,
            s.nombre_tipo_documento,
            s.documento,
            s.tipo_persona,

            s.nombres,
            s.primer_apellido,
            s.segundo_apellido,
            s.nombre_completo,

            s.fecha_nacimiento,

            -- =================================================
            -- Estado de actualización
            -- =================================================

            s.fecha_actualizacion,
            s.fecha_creacion_datos,
            s.fecha_edicion_datos,

            -- =================================================
            -- Información económica
            -- =================================================

            s.codigo_ocupacion,
            s.nombre_ocupacion,

            s.codigo_sector_economico,
            s.nombre_sector_economico,

            s.codigo_actividad_ses,
            s.nombre_actividad_ses,

            s.codigo_actividad_dian,
            s.nombre_actividad_dian,

            s.origen_fondos,

            -- =================================================
            -- Persona Expuesta Políticamente
            -- =================================================

            s.asociado_peps,
            s.tipo_peps,
            s.nombre_tipo_peps,
            s.observaciones_peps,
            s.fecha_inicial_peps,
            s.fecha_final_peps,

            -- =================================================
            -- Familiar relacionado con PEP
            -- =================================================

            s.familia_peps,
            s.tipo_familia_peps,
            s.cedula_familia_peps,
            s.codigo_parentesco,
            s.nombre_parentesco,
            s.nombre_familia_peps,

            -- =================================================
            -- Operaciones en moneda extranjera
            -- =================================================

            s.moneda_extranjera,
            s.observacion_moneda_extranjera,

            -- =================================================
            -- Cuenta en el exterior
            -- =================================================

            s.cuenta_extranjero,
            s.tipo_moneda_extranjera,
            s.numero_cuenta_extranjero,
            s.nombre_banco_extranjero,
            s.ciudad_cuenta_extranjero,
            s.pais_cuenta_extranjero,

            -- =================================================
            -- Residencia fiscal / FATCA / CRS
            -- =================================================

            s.id_residencia_fiscal,
            s.tiene_informacion_residencia_fiscal,

            s.ciudadano_estados_unidos,
            s.residente_fiscal_estados_unidos,
            s.residente_fiscal_exterior,

            s.pais_residencia_fiscal,
            s.numero_identificacion_fiscal,
            s.tipo_identificacion_fiscal,
            s.ciudad_residencia_fiscal,
            s.direccion_residencia_fiscal,

            s.observaciones_residencia_fiscal,

            s.fecha_creacion_residencia_fiscal,
            s.fecha_edicion_residencia_fiscal,

            -- =================================================
            -- Condiciones de protección
            -- =================================================

            s.id_condicion_proteccion,
            s.tiene_informacion_condiciones_proteccion,

            s.administra_recursos_publicos,
            s.grupo_proteccion_especial_constitucional,
            s.persona_mayor_60_anos,
            s.discapacidad_fisica,
            s.victima_conflicto_armado,
            s.pobreza_extrema,
            s.poblacion_indigena,
            s.poblacion_afrodescendiente,
            s.poblacion_lgbtiq_mas,
            s.pertenece_grupo_proteccion_constitucional,

            s.observaciones_condiciones_proteccion,

            s.fecha_creacion_condiciones_proteccion,
            s.fecha_edicion_condiciones_proteccion

        FROM reporting.vw_expediente_sarlaft s

        WHERE s.id_datos_personal = :idDatosPersonal

        LIMIT 1
        """;

    // =========================================================
    // Consultas públicas
    // =========================================================

    public boolean existePersona(Long idDatosPersonal) {
        validarIdDatosPersonal(idDatosPersonal);

        Boolean existe = jdbc.queryForObject(
                SQL_EXISTE_PERSONA,
                parametros(idDatosPersonal),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    public Optional<ExpedienteResumenGeneralDTO> obtenerResumenGeneral(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return consultarOpcional(
                SQL_RESUMEN_GENERAL,
                parametros(idDatosPersonal),
                RESUMEN_MAPPER
        );
    }

    public Optional<ExpedienteContactoDTO> obtenerContacto(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return consultarOpcional(
                SQL_CONTACTO,
                parametros(idDatosPersonal),
                CONTACTO_MAPPER
        );
    }

    public Optional<ExpedienteInformacionFinancieraDTO>
    obtenerInformacionFinanciera(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return consultarOpcional(
                SQL_FINANCIERO,
                parametros(idDatosPersonal),
                FINANCIERO_MAPPER
        );
    }

    public Optional<ExpedienteSarlaftDTO> obtenerSarlaft(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return consultarOpcional(
                SQL_SARLAFT,
                parametros(idDatosPersonal),
                SARLAFT_MAPPER
        );
    }

    // =========================================================
    // Utilidades internas
    // =========================================================

    private MapSqlParameterSource parametros(
            Long idDatosPersonal
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        PARAM_ID_DATOS_PERSONAL,
                        idDatosPersonal
                );
    }

    private <T> Optional<T> consultarOpcional(
            String sql,
            MapSqlParameterSource parametros,
            BeanPropertyRowMapper<T> mapper
    ) {
        try {
            T resultado = jdbc.queryForObject(
                    sql,
                    parametros,
                    mapper
            );

            return Optional.ofNullable(resultado);

        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private void validarIdDatosPersonal(
            Long idDatosPersonal
    ) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException(
                    "El idDatosPersonal debe ser mayor que cero."
            );
        }
    }
}
