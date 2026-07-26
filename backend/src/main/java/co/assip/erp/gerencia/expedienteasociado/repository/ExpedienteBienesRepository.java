package co.assip.erp.gerencia.expedienteasociado.repository;

import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteBienInmuebleDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteBienInversionDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteBienMaquinariaDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteBienVehiculoDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de consulta de bienes del
 * Expediente Integral del Asociado.
 *
 * Características:
 *
 * - Solo lectura.
 * - Consulta inmuebles, vehículos, maquinaria e inversiones.
 * - Consume vistas del esquema reporting.
 * - No modifica información de Hoja de Vida.
 */
@Repository
public class ExpedienteBienesRepository {

    private static final String PARAM_ID_DATOS_PERSONAL =
            "idDatosPersonal";

    private final NamedParameterJdbcTemplate jdbc;

    public ExpedienteBienesRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // RowMapper
    // =========================================================

    private static final BeanPropertyRowMapper<ExpedienteBienInmuebleDTO>
            INMUEBLE_MAPPER =
            mapper(ExpedienteBienInmuebleDTO.class);

    private static final BeanPropertyRowMapper<ExpedienteBienVehiculoDTO>
            VEHICULO_MAPPER =
            mapper(ExpedienteBienVehiculoDTO.class);

    private static final BeanPropertyRowMapper<ExpedienteBienMaquinariaDTO>
            MAQUINARIA_MAPPER =
            mapper(ExpedienteBienMaquinariaDTO.class);

    private static final BeanPropertyRowMapper<ExpedienteBienInversionDTO>
            INVERSION_MAPPER =
            mapper(ExpedienteBienInversionDTO.class);

    private static <T> BeanPropertyRowMapper<T> mapper(
            Class<T> tipo
    ) {
        BeanPropertyRowMapper<T> mapper =
                BeanPropertyRowMapper.newInstance(tipo);

        /*
         * Permite que el DTO tenga más propiedades que las columnas
         * seleccionadas. Los atributos no disponibles quedan en null.
         */
        mapper.setCheckFullyPopulated(false);

        /*
         * Facilita el mapeo de columnas snake_case hacia propiedades
         * camelCase.
         */
        mapper.setPrimitivesDefaultedForNullValue(true);

        return mapper;
    }

    // =========================================================
    // SQL: inmuebles
    // =========================================================

    private static final String SQL_INMUEBLES = """
            WITH inmuebles AS (
                SELECT DISTINCT ON (
                    i.id_datos_personal,
                    i.id_bien
                )
                    i.*
                FROM reporting.vw_hoja_vida_bienes_inmuebles_total i
                WHERE i.id_datos_personal = :idDatosPersonal
                ORDER BY
                    i.id_datos_personal,
                    i.id_bien,
                    i.fecha_avaluo DESC NULLS LAST,
                    i.fecha_edicion DESC NULLS LAST,
                    i.fecha_creacion DESC NULLS LAST
            )

            SELECT
                i.id_bien_persona,
                i.id_datos_personal,
                i.porcentaje_propiedad,

                i.valor_propiedad_asociado
                    AS valor_participacion,

                (
                    COALESCE(i.porcentaje_propiedad, 0) >= 100
                ) AS titular_principal,

                (
                    COALESCE(i.porcentaje_propiedad, 100) < 100
                ) AS propiedad_compartida,

                i.tipo_documento,
                i.nombre_tipo_documento,
                i.documento,
                i.nombre_completo,

                i.id_bien,
                i.id_tipo_bien,
                i.codigo_tipo_bien,
                i.nombre_tipo_bien,
                i.descripcion_general,

                i.valor_comercial,
                i.valor_gravamen,

                i.valor_neto_asociado
                    AS valor_neto,

                i.id_bien_inmueble,
                i.id_tipo_inmueble,
                i.codigo_tipo_inmueble,
                i.nombre_tipo_inmueble,

                i.id_tipo_zona_inmueble,
                i.codigo_tipo_zona_inmueble,
                i.nombre_tipo_zona_inmueble,

                i.numero_matricula_inmobiliaria,
                i.cedula_catastral,
                i.numero_escritura,
                i.fecha_escritura,
                i.notaria,

                i.id_pais,
                i.nombre_pais,
                i.id_departamento,
                i.nombre_departamento,
                i.id_ciudad,
                i.nombre_ciudad,

                i.direccion,
                i.barrio_vereda,

                trim(
                    concat_ws(
                        ', ',
                        NULLIF(trim(i.direccion), ''),
                        NULLIF(trim(i.barrio_vereda), ''),
                        NULLIF(trim(i.nombre_ciudad), ''),
                        NULLIF(trim(i.nombre_departamento), ''),
                        NULLIF(trim(i.nombre_pais), '')
                    )
                ) AS ubicacion_completa,

                i.area_terreno,
                i.area_construida,

                i.id_tipo_gravamen,
                i.nombre_gravamen,

                (
                    COALESCE(i.valor_gravamen_asociado, 0) > 0
                    OR COALESCE(i.id_tipo_gravamen, 1) <> 1
                ) AS tiene_gravamen,

                CASE
                    WHEN COALESCE(i.valor_propiedad_asociado, 0) <= 0
                        THEN NULL
                    ELSE round(
                        (
                            COALESCE(
                                i.valor_gravamen_asociado,
                                0
                            )
                            /
                            NULLIF(
                                i.valor_propiedad_asociado,
                                0
                            )
                        ) * 100,
                        2
                    )
                END AS porcentaje_gravamen,

                (
                    COALESCE(i.valor_gravamen_asociado, 0)
                    >
                    COALESCE(i.valor_propiedad_asociado, 0)
                ) AS gravamen_supera_valor_comercial,

                (
                    COALESCE(i.valor_neto_asociado, 0) < 0
                ) AS valor_neto_negativo,

                i.id_bien_inmueble_avaluo
                    AS id_bien_inmueble_avaluo,

                i.fecha_avaluo,
                i.valor_avaluo_comercial,
                i.valor_avaluo_catastral,
                i.entidad_avaluadora,

                i.numero_informe
                    AS numero_avaluo,

                i.observaciones_avaluo,

                CASE
                    WHEN i.fecha_avaluo IS NULL THEN NULL
                    ELSE (
                        current_date - i.fecha_avaluo
                    )::integer
                END AS dias_desde_avaluo,

                CASE
                    WHEN i.fecha_avaluo IS NULL THEN NULL
                    ELSE (
                        extract(
                            year FROM age(
                                current_date,
                                i.fecha_avaluo
                            )
                        ) * 12
                        +
                        extract(
                            month FROM age(
                                current_date,
                                i.fecha_avaluo
                            )
                        )
                    )::integer
                END AS antiguedad_avaluo_meses,

                (i.fecha_avaluo IS NOT NULL)
                    AS tiene_avaluo,

                (
                    i.fecha_avaluo IS NOT NULL
                    AND (
                        i.fecha_vencimiento_avaluo IS NULL
                        OR i.fecha_vencimiento_avaluo >= current_date
                    )
                ) AS avaluo_vigente,

                (
                    i.fecha_vencimiento_avaluo
                    BETWEEN current_date
                        AND current_date + 60
                ) AS avaluo_proximo_vencer,

                (
                    i.fecha_vencimiento_avaluo < current_date
                ) AS avaluo_vencido,

                i.id_bien_inmueble_seguro,
                i.aseguradora,
                i.numero_poliza,
                i.valor_asegurado,
                i.fecha_inicio_seguro,
                i.fecha_vencimiento_seguro,
                i.estado_seguro,
                i.observaciones_seguro,

                CASE
                    WHEN i.fecha_vencimiento_seguro IS NULL THEN NULL
                    ELSE (
                        i.fecha_vencimiento_seguro - current_date
                    )::integer
                END AS dias_para_vencimiento_seguro,

                (i.id_bien_inmueble_seguro IS NOT NULL)
                    AS tiene_seguro,

                (
                    i.id_bien_inmueble_seguro IS NOT NULL
                    AND COALESCE(i.estado_seguro, 'A') = 'A'
                    AND (
                        i.fecha_vencimiento_seguro IS NULL
                        OR i.fecha_vencimiento_seguro >= current_date
                    )
                ) AS seguro_vigente,

                (
                    i.fecha_vencimiento_seguro
                    BETWEEN current_date
                        AND current_date + 30
                ) AS seguro_proximo_vencer,

                (
                    i.fecha_vencimiento_seguro < current_date
                ) AS seguro_vencido,

                CASE
                    WHEN COALESCE(i.valor_comercial, 0) <= 0
                        THEN NULL
                    ELSE round(
                        (
                            COALESCE(i.valor_asegurado, 0)
                            /
                            NULLIF(i.valor_comercial, 0)
                        ) * 100,
                        2
                    )
                END AS porcentaje_cobertura_seguro,

                (
                    COALESCE(i.valor_asegurado, 0)
                    >=
                    COALESCE(i.valor_comercial, 0)
                    AND COALESCE(i.valor_comercial, 0) > 0
                ) AS seguro_cubre_valor_comercial,

                (
                    NULLIF(trim(i.numero_matricula_inmobiliaria), '')
                        IS NOT NULL
                    AND NULLIF(trim(i.numero_escritura), '')
                        IS NOT NULL
                ) AS informacion_registral_completa,

                (
                    NULLIF(trim(i.direccion), '') IS NOT NULL
                    AND i.id_ciudad IS NOT NULL
                ) AS informacion_ubicacion_completa,

                (
                    COALESCE(i.valor_comercial, 0) > 0
                ) AS informacion_valoracion_completa,

                (
                    i.id_bien_inmueble_seguro IS NOT NULL
                    AND NULLIF(trim(i.numero_poliza), '') IS NOT NULL
                ) AS informacion_seguro_completa,

                i.estado_bien,

                COALESCE(i.estado_bien, 'A') <> 'I'
                    AS activo,

                COALESCE(i.valor_comercial, 0) > 0
                    AS tiene_valor_comercial,

                (
                    i.fecha_vencimiento_avaluo < current_date
                    OR i.fecha_vencimiento_seguro < current_date
                ) AS requiere_actualizacion,

                (
                    COALESCE(i.valor_neto_asociado, 0) < 0
                    OR i.fecha_vencimiento_avaluo < current_date
                    OR i.fecha_vencimiento_seguro < current_date
                ) AS requiere_revision,

                i.observaciones,
                i.fk_seguridad_creacion,
                i.fecha_creacion::date AS fecha_creacion,
                i.fk_seguridad_edicion,
                i.fecha_edicion::date AS fecha_edicion

            FROM inmuebles i

            ORDER BY
                COALESCE(i.estado_bien, 'A') <> 'A',
                i.valor_neto_asociado DESC NULLS LAST,
                i.id_bien DESC
            """;

    // =========================================================
    // SQL: vehículos
    // =========================================================

    private static final String SQL_VEHICULOS = """
            WITH vehiculos AS (
                SELECT DISTINCT ON (
                    v.id_datos_personal,
                    v.id_bien
                )
                    v.*
                FROM reporting.vw_hoja_vida_bienes_vehiculos_total v
                WHERE v.id_datos_personal = :idDatosPersonal
                ORDER BY
                    v.id_datos_personal,
                    v.id_bien,
                    v.fecha_edicion DESC NULLS LAST,
                    v.fecha_creacion DESC NULLS LAST
            )

            SELECT
                v.id_bien_persona,
                v.id_datos_personal,
                v.porcentaje_propiedad,

                v.valor_propiedad_asociado
                    AS valor_participacion,

                (
                    COALESCE(v.porcentaje_propiedad, 0) >= 100
                ) AS titular_principal,

                (
                    COALESCE(v.porcentaje_propiedad, 100) < 100
                ) AS propiedad_compartida,

                v.documento,
                v.nombre_completo,

                v.id_bien,
                v.codigo_tipo_bien,
                v.nombre_tipo_bien,
                v.descripcion_general,

                v.valor_comercial,
                v.valor_gravamen,

                v.valor_neto_asociado
                    AS valor_neto,

                v.id_bien_vehiculo,
                v.id_tipo_vehiculo,
                v.nombre_tipo_vehiculo,

                v.placa,
                v.marca,
                v.linea,
                v.modelo,
                v.color,
                v.numero_motor,
                v.numero_chasis,
                v.numero_serie,

                v.id_tipo_gravamen,
                v.nombre_gravamen,

                (
                    COALESCE(v.valor_gravamen_asociado, 0) > 0
                    OR COALESCE(v.id_tipo_gravamen, 1) <> 1
                ) AS tiene_gravamen,

                CASE
                    WHEN COALESCE(v.valor_propiedad_asociado, 0) <= 0
                        THEN NULL
                    ELSE round(
                        (
                            COALESCE(
                                v.valor_gravamen_asociado,
                                0
                            )
                            /
                            NULLIF(
                                v.valor_propiedad_asociado,
                                0
                            )
                        ) * 100,
                        2
                    )
                END AS porcentaje_gravamen,

                (
                    NULLIF(trim(v.placa), '') IS NOT NULL
                    AND NULLIF(trim(v.marca), '') IS NOT NULL
                    AND v.modelo IS NOT NULL
                ) AS informacion_identificacion_completa,

                (
                    COALESCE(v.valor_comercial, 0) > 0
                ) AS informacion_valoracion_completa,

                (
                    NULLIF(trim(v.placa), '') IS NOT NULL
                    AND NULLIF(trim(v.marca), '') IS NOT NULL
                    AND v.modelo IS NOT NULL
                    AND COALESCE(v.valor_comercial, 0) > 0
                ) AS informacion_completa,

                COALESCE(v.valor_comercial, 0) > 0
                    AS activo,

                (
                    COALESCE(v.valor_neto_asociado, 0) < 0
                ) AS requiere_revision,

                (
                    v.modelo IS NULL
                    OR COALESCE(v.valor_comercial, 0) <= 0
                ) AS requiere_actualizacion,

                v.observaciones

            FROM vehiculos v

            ORDER BY
                v.valor_neto_asociado DESC NULLS LAST,
                v.id_bien DESC
            """;

    // =========================================================
    // SQL: maquinaria
    // =========================================================

    private static final String SQL_MAQUINARIA = """
            WITH maquinaria AS (
                SELECT DISTINCT ON (
                    m.id_datos_personal,
                    m.id_bien
                )
                    m.*
                FROM reporting.vw_hoja_vida_bienes_maquinaria_total m
                WHERE m.id_datos_personal = :idDatosPersonal
                ORDER BY
                    m.id_datos_personal,
                    m.id_bien,
                    m.fecha_edicion DESC NULLS LAST,
                    m.fecha_creacion DESC NULLS LAST
            )

            SELECT
                m.id_bien_persona,
                m.id_datos_personal,
                m.porcentaje_propiedad,

                m.valor_propiedad_asociado
                    AS valor_participacion,

                (
                    COALESCE(m.porcentaje_propiedad, 0) >= 100
                ) AS titular_principal,

                (
                    COALESCE(m.porcentaje_propiedad, 100) < 100
                ) AS propiedad_compartida,

                m.tipo_documento,
                m.nombre_tipo_documento,
                m.documento,
                m.nombre_completo,

                m.id_bien,
                m.id_tipo_bien,
                m.codigo_tipo_bien,
                m.nombre_tipo_bien,
                m.descripcion_general,

                m.valor_comercial,
                m.valor_gravamen,

                m.valor_neto_asociado
                    AS valor_neto,

                m.id_bien_maquinaria,
                m.id_tipo_maquinaria,
                m.nombre_tipo_maquinaria,

                m.marca,
                m.modelo,
                m.serial AS serie,
                m.descripcion_tecnica,

                m.id_tipo_gravamen,
                m.nombre_gravamen,

                (
                    COALESCE(m.valor_gravamen_asociado, 0) > 0
                    OR COALESCE(m.id_tipo_gravamen, 1) <> 1
                ) AS tiene_gravamen,

                CASE
                    WHEN COALESCE(m.valor_propiedad_asociado, 0) <= 0
                        THEN NULL
                    ELSE round(
                        (
                            COALESCE(
                                m.valor_gravamen_asociado,
                                0
                            )
                            /
                            NULLIF(
                                m.valor_propiedad_asociado,
                                0
                            )
                        ) * 100,
                        2
                    )
                END AS porcentaje_gravamen,

                (
                    COALESCE(m.valor_gravamen_asociado, 0)
                    >
                    COALESCE(m.valor_propiedad_asociado, 0)
                ) AS gravamen_supera_valor_comercial,

                (
                    COALESCE(m.valor_neto_asociado, 0) < 0
                ) AS valor_neto_negativo,

                (
                    NULLIF(trim(m.marca), '') IS NOT NULL
                    AND NULLIF(trim(m.serial), '') IS NOT NULL
                ) AS informacion_identificacion_completa,

                (
                    NULLIF(trim(m.descripcion_tecnica), '') IS NOT NULL
                ) AS informacion_tecnica_completa,

                (
                    COALESCE(m.valor_comercial, 0) > 0
                ) AS informacion_valoracion_completa,

                (
                    NULLIF(trim(m.marca), '') IS NOT NULL
                    AND NULLIF(trim(m.serial), '') IS NOT NULL
                    AND COALESCE(m.valor_comercial, 0) > 0
                ) AS informacion_completa,

                true AS activo,

                COALESCE(m.valor_comercial, 0) > 0
                    AS tiene_valor_comercial,

                (
                    NULLIF(trim(m.serial), '') IS NULL
                    OR COALESCE(m.valor_comercial, 0) <= 0
                ) AS requiere_actualizacion,

                (
                    COALESCE(m.valor_neto_asociado, 0) < 0
                    OR NULLIF(trim(m.serial), '') IS NULL
                ) AS requiere_revision,

                m.observaciones,
                m.fk_seguridad_creacion,
                m.fecha_creacion,
                m.fk_seguridad_edicion,
                m.fecha_edicion

            FROM maquinaria m

            ORDER BY
                m.valor_neto_asociado DESC NULLS LAST,
                m.id_bien DESC
            """;

    // =========================================================
    // SQL: inversiones
    // =========================================================

    private static final String SQL_INVERSIONES = """
            WITH inversiones AS (
                SELECT DISTINCT ON (
                    i.id_datos_personal,
                    i.id_bien
                )
                    i.*
                FROM reporting.vw_hoja_vida_bienes_inversiones_total i
                WHERE i.id_datos_personal = :idDatosPersonal
                ORDER BY
                    i.id_datos_personal,
                    i.id_bien,
                    i.fecha_edicion DESC NULLS LAST,
                    i.fecha_creacion DESC NULLS LAST
            )

            SELECT
                i.id_bien_persona,
                i.id_datos_personal,
                i.porcentaje_propiedad,

                i.valor_propiedad_asociado
                    AS valor_participacion,

                (
                    COALESCE(i.porcentaje_propiedad, 0) >= 100
                ) AS titular_principal,

                (
                    COALESCE(i.porcentaje_propiedad, 100) < 100
                ) AS propiedad_compartida,

                i.documento,
                i.nombre_completo,

                i.id_bien,
                i.id_tipo_bien,
                i.codigo_tipo_bien,
                i.nombre_tipo_bien,
                i.descripcion_general,

                i.valor_comercial,
                i.valor_gravamen,

                i.valor_neto_asociado
                    AS valor_neto,

                i.id_bien_inversion,
                i.id_tipo_inversion,
                i.nombre_tipo_inversion,

                i.entidad,
                i.numero_titulo,

                i.fecha_inversion
                    AS fecha_emision,

                i.fecha_vencimiento,

                i.valor_nominal,

                i.tasa_rendimiento
                    AS tasa,

                CASE
                    WHEN i.fecha_inversion IS NULL THEN NULL
                    ELSE greatest(
                        current_date - i.fecha_inversion,
                        0
                    )::integer
                END AS dias_vigencia,

                CASE
                    WHEN i.fecha_vencimiento IS NULL THEN NULL
                    ELSE (
                        i.fecha_vencimiento - current_date
                    )::integer
                END AS dias_para_vencimiento,

                (
                    i.fecha_vencimiento IS NULL
                    OR i.fecha_vencimiento >= current_date
                ) AS vigente,

                (
                    i.fecha_vencimiento
                    BETWEEN current_date
                        AND current_date + 30
                ) AS proximo_vencer,

                (
                    i.fecha_vencimiento < current_date
                ) AS vencido,

                i.id_tipo_gravamen,
                i.nombre_gravamen,

                (
                    COALESCE(i.valor_gravamen_asociado, 0) > 0
                    OR COALESCE(i.id_tipo_gravamen, 1) <> 1
                ) AS tiene_gravamen,

                CASE
                    WHEN COALESCE(i.valor_propiedad_asociado, 0) <= 0
                        THEN NULL
                    ELSE round(
                        (
                            COALESCE(
                                i.valor_gravamen_asociado,
                                0
                            )
                            /
                            NULLIF(
                                i.valor_propiedad_asociado,
                                0
                            )
                        ) * 100,
                        2
                    )
                END AS porcentaje_gravamen,

                (
                    NULLIF(trim(i.entidad), '') IS NOT NULL
                    AND NULLIF(trim(i.numero_titulo), '') IS NOT NULL
                ) AS informacion_general_completa,

                (
                    COALESCE(i.valor_nominal, 0) > 0
                    AND i.tasa_rendimiento IS NOT NULL
                ) AS informacion_financiera_completa,

                (
                    i.fecha_inversion IS NOT NULL
                    AND i.fecha_vencimiento IS NOT NULL
                ) AS informacion_fechas_completa,

                (
                    NULLIF(trim(i.entidad), '') IS NOT NULL
                    AND NULLIF(trim(i.numero_titulo), '') IS NOT NULL
                    AND COALESCE(i.valor_nominal, 0) > 0
                    AND i.fecha_inversion IS NOT NULL
                ) AS informacion_completa,

                (
                    i.fecha_vencimiento IS NULL
                    OR i.fecha_vencimiento >= current_date
                ) AS activo,

                (
                    i.fecha_vencimiento < current_date
                    OR COALESCE(i.valor_neto_asociado, 0) < 0
                ) AS requiere_revision,

                (
                    i.fecha_inversion IS NULL
                    OR COALESCE(i.valor_actual, 0) <= 0
                ) AS requiere_actualizacion,

                i.observaciones

            FROM inversiones i

            ORDER BY
                CASE
                    WHEN i.fecha_vencimiento IS NULL
                      OR i.fecha_vencimiento >= current_date
                        THEN 0
                    ELSE 1
                END,
                i.fecha_vencimiento ASC NULLS LAST,
                i.id_bien DESC
            """;

    // =========================================================
    // Consultas públicas
    // =========================================================

    public List<ExpedienteBienInmuebleDTO> listarBienesInmuebles(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return jdbc.query(
                SQL_INMUEBLES,
                parametros(idDatosPersonal),
                INMUEBLE_MAPPER
        );
    }

    public List<ExpedienteBienVehiculoDTO> listarBienesVehiculos(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return jdbc.query(
                SQL_VEHICULOS,
                parametros(idDatosPersonal),
                VEHICULO_MAPPER
        );
    }

    public List<ExpedienteBienMaquinariaDTO> listarBienesMaquinaria(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return jdbc.query(
                SQL_MAQUINARIA,
                parametros(idDatosPersonal),
                MAQUINARIA_MAPPER
        );
    }

    public List<ExpedienteBienInversionDTO> listarBienesInversiones(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return jdbc.query(
                SQL_INVERSIONES,
                parametros(idDatosPersonal),
                INVERSION_MAPPER
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