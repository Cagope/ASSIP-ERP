package co.assip.erp.cartera.originacion.financiero;

import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroDTO;
import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroDetalleDTO;
import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroGuardarRequestDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudFinancieroRepository {

    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    private static final String SQL_LISTAR_POR_SOLICITUD = """
            SELECT
                sf.id_solicitud_deudor_financiero,
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

                dp.tipo_persona AS tipo_persona,
                sf.fecha_fotografia,

                sf.ingresos_totales_natural,
                sf.egresos_totales_natural,

                sf.ingresos_totales_juridica,
                sf.egresos_totales_juridica,

                sf.total_activos,
                sf.total_pasivos,
                sf.patrimonio_total,

                sf.activo

            FROM cartera.solicitudes_deudores sd

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal =
                   sd.id_datos_personal

            LEFT JOIN cartera.solicitudes_deudores_financieros sf
                ON sf.id_solicitud_deudor =
                   sd.id_solicitud_deudor

            WHERE sd.id_solicitud_credito =
                  :idSolicitudCredito

              AND sd.activo = true

            ORDER BY
                sd.orden_deudor,
                sf.id_solicitud_deudor_financiero
            """;


    // =========================================================
    // DETALLE POR DEUDOR
    // =========================================================
    //
    // Si existe registro financiero de la solicitud:
    // devuelve la información de la solicitud.
    //
    // Si todavía no existe:
    // precarga la información disponible en Hoja de Vida.
    //
    // Para una nueva solicitud:
    //
    // ingreso_independiente = 0
    // otros_ingresos = hoja_vida.financieros.otros_ingresos
    //
    // =========================================================

    private static final String SQL_BUSCAR_POR_DEUDOR = """
            SELECT
                sf.id_solicitud_deudor_financiero,
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

                dp.tipo_persona AS tipo_persona,

                sf.fecha_fotografia,

                COALESCE(
                    sf.codigo_ocupacion,
                    dp.codigo_ocupacion
                ) AS codigo_ocupacion,

                COALESCE(
                    sf.codigo_sector_economico,
                    dp.codigo_sector_economico
                ) AS codigo_sector_economico,

                COALESCE(
                    sf.codigo_actividad_ses,
                    dp.codigo_actividad_ses
                ) AS codigo_actividad_ses,

                COALESCE(
                    sf.codigo_actividad_dian,
                    dp.codigo_actividad_dian
                ) AS codigo_actividad_dian,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.valor_salario
                    ELSE COALESCE(hf.valor_salario, 0)
                END AS valor_salario,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.valor_pension
                    ELSE COALESCE(hf.valor_pension, 0)
                END AS valor_pension,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.ingreso_independiente
                    ELSE 0
                END AS ingreso_independiente,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.ingresos_arriendo
                    ELSE COALESCE(hf.ingresos_arriendo, 0)
                END AS ingresos_arriendo,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.ingresos_comisiones
                    ELSE COALESCE(hf.ingresos_comisiones, 0)
                END AS ingresos_comisiones,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.otros_ingresos
                    ELSE COALESCE(hf.otros_ingresos, 0)
                END AS otros_ingresos,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.comentario_otros_ingresos
                    ELSE hf.comentario_otros_ingresos
                END AS comentario_otros_ingresos,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.ingresos_totales_natural
                    ELSE
                          COALESCE(hf.valor_salario, 0)
                        + COALESCE(hf.valor_pension, 0)
                        + COALESCE(hf.ingresos_arriendo, 0)
                        + COALESCE(hf.ingresos_comisiones, 0)
                        + COALESCE(hf.otros_ingresos, 0)
                END AS ingresos_totales_natural,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.egresos_familiares
                    ELSE COALESCE(hf.egresos_familiares, 0)
                END AS egresos_familiares,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.egresos_arriendo
                    ELSE COALESCE(hf.egresos_arriendo, 0)
                END AS egresos_arriendo,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.egresos_credito
                    ELSE COALESCE(hf.egresos_credito, 0)
                END AS egresos_credito,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.otros_egresos
                    ELSE COALESCE(hf.otros_egresos, 0)
                END AS otros_egresos,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.comentario_otros_egresos
                    ELSE hf.comentario_otros_egresos
                END AS comentario_otros_egresos,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.egresos_totales_natural
                    ELSE
                          COALESCE(hf.egresos_familiares, 0)
                        + COALESCE(hf.egresos_arriendo, 0)
                        + COALESCE(hf.egresos_credito, 0)
                        + COALESCE(hf.otros_egresos, 0)
                END AS egresos_totales_natural,

                sf.declara_renta,
                sf.anio_declaracion,
                sf.fecha_presentacion_declaracion,

                COALESCE(
                    sf.ingresos_operacionales,
                    0
                ) AS ingresos_operacionales,

                COALESCE(
                    sf.ingresos_no_operacionales,
                    0
                ) AS ingresos_no_operacionales,

                COALESCE(
                    sf.ingresos_totales_juridica,
                    0
                ) AS ingresos_totales_juridica,

                COALESCE(
                    sf.costos,
                    0
                ) AS costos,

                COALESCE(
                    sf.gastos_operacionales,
                    0
                ) AS gastos_operacionales,

                COALESCE(
                    sf.gastos_financieros,
                    0
                ) AS gastos_financieros,

                COALESCE(
                    sf.otros_gastos,
                    0
                ) AS otros_gastos,

                COALESCE(
                    sf.egresos_totales_juridica,
                    0
                ) AS egresos_totales_juridica,

                COALESCE(
                    sf.activo_corriente,
                    0
                ) AS activo_corriente,

                COALESCE(
                    sf.pasivo_corriente,
                    0
                ) AS pasivo_corriente,

                COALESCE(
                    sf.utilidad_operacional,
                    0
                ) AS utilidad_operacional,

                COALESCE(
                    sf.utilidad_neta,
                    0
                ) AS utilidad_neta,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.total_activos
                    ELSE COALESCE(hf.total_activos, 0)
                END AS total_activos,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.total_pasivos
                    ELSE COALESCE(hf.total_pasivos, 0)
                END AS total_pasivos,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.patrimonio_total
                    ELSE
                          COALESCE(hf.total_activos, 0)
                        - COALESCE(hf.total_pasivos, 0)
                END AS patrimonio_total,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.origen_fondos
                    ELSE hf.origen_fondos
                END AS origen_fondos,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.relacion_financiera
                    ELSE hf.relacion_financiera
                END AS relacion_financiera,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NOT NULL
                        THEN sf.deuda_relacion_financiera
                    ELSE COALESCE(hf.deuda_relacion_financiera, 0)
                END AS deuda_relacion_financiera,

                COALESCE(
                    sf.activo,
                    true
                ) AS activo,

                sf.fecha_creacion,
                sf.fecha_edicion,

                CASE
                    WHEN sf.id_solicitud_deudor_financiero IS NULL
                        THEN true

                    ELSE NOT (
                        sf.codigo_ocupacion
                            IS DISTINCT FROM
                        dp.codigo_ocupacion

                        OR sf.codigo_sector_economico
                            IS DISTINCT FROM
                        dp.codigo_sector_economico

                        OR sf.codigo_actividad_ses
                            IS DISTINCT FROM
                        dp.codigo_actividad_ses

                        OR sf.codigo_actividad_dian
                            IS DISTINCT FROM
                        dp.codigo_actividad_dian

                        OR (
                            dp.tipo_persona = '1'
                            AND (
                                COALESCE(sf.valor_salario, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.valor_salario, 0)

                                OR COALESCE(sf.valor_pension, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.valor_pension, 0)

                                OR COALESCE(sf.ingresos_arriendo, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.ingresos_arriendo, 0)

                                OR COALESCE(sf.ingresos_comisiones, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.ingresos_comisiones, 0)

                                OR (
                                      COALESCE(sf.ingreso_independiente, 0)
                                    + COALESCE(sf.otros_ingresos, 0)
                                )
                                    IS DISTINCT FROM
                                COALESCE(hf.otros_ingresos, 0)

                                OR sf.comentario_otros_ingresos
                                    IS DISTINCT FROM
                                hf.comentario_otros_ingresos

                                OR COALESCE(sf.egresos_familiares, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.egresos_familiares, 0)

                                OR COALESCE(sf.egresos_arriendo, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.egresos_arriendo, 0)

                                OR COALESCE(sf.egresos_credito, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.egresos_credito, 0)

                                OR COALESCE(sf.otros_egresos, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.otros_egresos, 0)

                                OR sf.comentario_otros_egresos
                                    IS DISTINCT FROM
                                hf.comentario_otros_egresos

                                OR COALESCE(sf.total_activos, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.total_activos, 0)

                                OR COALESCE(sf.total_pasivos, 0)
                                    IS DISTINCT FROM
                                COALESCE(hf.total_pasivos, 0)

                                OR sf.origen_fondos
                                    IS DISTINCT FROM
                                hf.origen_fondos

                                OR sf.relacion_financiera
                                    IS DISTINCT FROM
                                hf.relacion_financiera

                                OR COALESCE(
                                    sf.deuda_relacion_financiera,
                                    0
                                )
                                    IS DISTINCT FROM
                                COALESCE(
                                    hf.deuda_relacion_financiera,
                                    0
                                )
                            )
                        )
                    )
                END AS sincronizado_hoja_vida

            FROM cartera.solicitudes_deudores sd

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal =
                   sd.id_datos_personal

            LEFT JOIN cartera.solicitudes_deudores_financieros sf
                ON sf.id_solicitud_deudor =
                   sd.id_solicitud_deudor

            LEFT JOIN hoja_vida.financieros hf
                ON hf.id_datos_personal =
                   sd.id_datos_personal

            WHERE sd.id_solicitud_deudor =
                  :idSolicitudDeudor

              AND sd.activo = true
            """;


    // =========================================================
    // OBTENER AGENCIA DEL DEUDOR
    // =========================================================

    private static final String SQL_OBTENER_AGENCIA_DEUDOR = """
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
    // OBTENER AGENCIA SOLICITUD
    // =========================================================

    private static final String SQL_OBTENER_AGENCIA_SOLICITUD = """
            SELECT
                sc.id_agencia

            FROM cartera.solicitudes_creditos sc

            WHERE sc.id_solicitud_credito =
                  :idSolicitudCredito

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
    // GUARDAR INFORMACIÓN FINANCIERA
    // =========================================================
    //
    // Reglas:
    //
    // - tipo_persona siempre proviene de datos_personales.
    // - se guarda una única fila por id_solicitud_deudor.
    // - los cuatro códigos se conservan en la solicitud.
    // - para persona natural se sincroniza hoja_vida.financieros.
    // - ingreso_independiente se consolida en otros_ingresos
    //   de Hoja de Vida.
    // - datos_personales.fecha_actualizacion cambia solamente
    //   cuando realmente cambia información en Hoja de Vida.
    //
    // =========================================================

    private static final String SQL_GUARDAR = """
            WITH contexto AS (
                SELECT
                    sd.id_solicitud_deudor,
                    sd.id_datos_personal,
                    dp.tipo_persona

                FROM cartera.solicitudes_deudores sd

                INNER JOIN hoja_vida.datos_personales dp
                    ON dp.id_datos_personal =
                       sd.id_datos_personal

                WHERE sd.id_solicitud_deudor =
                      :idSolicitudDeudor

                  AND sd.activo = true

                  AND dp.tipo_persona IN ('1', '2')
            ),

            financiero_hoja_vida AS (
                INSERT INTO hoja_vida.financieros (
                    id_datos_personal,

                    valor_salario,
                    valor_pension,
                    ingresos_arriendo,
                    ingresos_comisiones,
                    otros_ingresos,
                    comentario_otros_ingresos,

                    egresos_familiares,
                    egresos_arriendo,
                    egresos_credito,
                    otros_egresos,
                    comentario_otros_egresos,

                    total_activos,
                    total_pasivos,

                    origen_fondos,
                    relacion_financiera,
                    deuda_relacion_financiera,

                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )

                SELECT
                    c.id_datos_personal,

                    COALESCE(:valorSalario, 0),
                    COALESCE(:valorPension, 0),
                    COALESCE(:ingresosArriendo, 0),
                    COALESCE(:ingresosComisiones, 0),

                    COALESCE(:ingresoIndependiente, 0)
                    + COALESCE(:otrosIngresos, 0),

                    :comentarioOtrosIngresos,

                    COALESCE(:egresosFamiliares, 0),
                    COALESCE(:egresosArriendo, 0),
                    COALESCE(:egresosCredito, 0),
                    COALESCE(:otrosEgresos, 0),

                    :comentarioOtrosEgresos,

                    COALESCE(:totalActivos, 0),
                    COALESCE(:totalPasivos, 0),

                    :origenFondos,
                    :relacionFinanciera,
                    COALESCE(:deudaRelacionFinanciera, 0),

                    :idUsuario,
                    CURRENT_TIMESTAMP,
                    :idUsuario,
                    CURRENT_TIMESTAMP

                FROM contexto c

                WHERE c.tipo_persona = '1'

                ON CONFLICT (id_datos_personal)
                DO UPDATE
                SET
                    valor_salario =
                        EXCLUDED.valor_salario,

                    valor_pension =
                        EXCLUDED.valor_pension,

                    ingresos_arriendo =
                        EXCLUDED.ingresos_arriendo,

                    ingresos_comisiones =
                        EXCLUDED.ingresos_comisiones,

                    otros_ingresos =
                        EXCLUDED.otros_ingresos,

                    comentario_otros_ingresos =
                        EXCLUDED.comentario_otros_ingresos,

                    egresos_familiares =
                        EXCLUDED.egresos_familiares,

                    egresos_arriendo =
                        EXCLUDED.egresos_arriendo,

                    egresos_credito =
                        EXCLUDED.egresos_credito,

                    otros_egresos =
                        EXCLUDED.otros_egresos,

                    comentario_otros_egresos =
                        EXCLUDED.comentario_otros_egresos,

                    total_activos =
                        EXCLUDED.total_activos,

                    total_pasivos =
                        EXCLUDED.total_pasivos,

                    origen_fondos =
                        EXCLUDED.origen_fondos,

                    relacion_financiera =
                        EXCLUDED.relacion_financiera,

                    deuda_relacion_financiera =
                        EXCLUDED.deuda_relacion_financiera,

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                WHERE
                       hoja_vida.financieros.valor_salario
                           IS DISTINCT FROM
                       EXCLUDED.valor_salario

                    OR hoja_vida.financieros.valor_pension
                           IS DISTINCT FROM
                       EXCLUDED.valor_pension

                    OR hoja_vida.financieros.ingresos_arriendo
                           IS DISTINCT FROM
                       EXCLUDED.ingresos_arriendo

                    OR hoja_vida.financieros.ingresos_comisiones
                           IS DISTINCT FROM
                       EXCLUDED.ingresos_comisiones

                    OR hoja_vida.financieros.otros_ingresos
                           IS DISTINCT FROM
                       EXCLUDED.otros_ingresos

                    OR hoja_vida.financieros.comentario_otros_ingresos
                           IS DISTINCT FROM
                       EXCLUDED.comentario_otros_ingresos

                    OR hoja_vida.financieros.egresos_familiares
                           IS DISTINCT FROM
                       EXCLUDED.egresos_familiares

                    OR hoja_vida.financieros.egresos_arriendo
                           IS DISTINCT FROM
                       EXCLUDED.egresos_arriendo

                    OR hoja_vida.financieros.egresos_credito
                           IS DISTINCT FROM
                       EXCLUDED.egresos_credito

                    OR hoja_vida.financieros.otros_egresos
                           IS DISTINCT FROM
                       EXCLUDED.otros_egresos

                    OR hoja_vida.financieros.comentario_otros_egresos
                           IS DISTINCT FROM
                       EXCLUDED.comentario_otros_egresos

                    OR hoja_vida.financieros.total_activos
                           IS DISTINCT FROM
                       EXCLUDED.total_activos

                    OR hoja_vida.financieros.total_pasivos
                           IS DISTINCT FROM
                       EXCLUDED.total_pasivos

                    OR hoja_vida.financieros.origen_fondos
                           IS DISTINCT FROM
                       EXCLUDED.origen_fondos

                    OR hoja_vida.financieros.relacion_financiera
                           IS DISTINCT FROM
                       EXCLUDED.relacion_financiera

                    OR hoja_vida.financieros.deuda_relacion_financiera
                           IS DISTINCT FROM
                       EXCLUDED.deuda_relacion_financiera

                RETURNING id_datos_personal
            ),

            datos_personales_actualizados AS (
                UPDATE hoja_vida.datos_personales dp

                SET
                    codigo_ocupacion =
                        :codigoOcupacion,

                    codigo_sector_economico =
                        :codigoSectorEconomico,

                    codigo_actividad_ses =
                        :codigoActividadSes,

                    codigo_actividad_dian =
                        :codigoActividadDian,

                    fecha_actualizacion =
                        CURRENT_DATE,

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                FROM contexto c

                WHERE dp.id_datos_personal =
                      c.id_datos_personal

                  AND (
                         dp.codigo_ocupacion
                             IS DISTINCT FROM
                         :codigoOcupacion

                      OR dp.codigo_sector_economico
                             IS DISTINCT FROM
                         :codigoSectorEconomico

                      OR dp.codigo_actividad_ses
                             IS DISTINCT FROM
                         :codigoActividadSes

                      OR dp.codigo_actividad_dian
                             IS DISTINCT FROM
                         :codigoActividadDian

                      OR EXISTS (
                          SELECT 1

                          FROM financiero_hoja_vida fhv

                          WHERE fhv.id_datos_personal =
                                dp.id_datos_personal
                      )
                  )

                RETURNING dp.id_datos_personal
            ),

            solicitud_guardada AS (
                INSERT INTO cartera.solicitudes_deudores_financieros (
                    id_solicitud_deudor,
                    tipo_persona,
                    fecha_fotografia,

                    codigo_ocupacion,
                    codigo_sector_economico,
                    codigo_actividad_ses,
                    codigo_actividad_dian,

                    valor_salario,
                    valor_pension,
                    ingreso_independiente,
                    ingresos_arriendo,
                    ingresos_comisiones,
                    otros_ingresos,
                    comentario_otros_ingresos,

                    egresos_familiares,
                    egresos_arriendo,
                    egresos_credito,
                    otros_egresos,
                    comentario_otros_egresos,

                    declara_renta,
                    anio_declaracion,
                    fecha_presentacion_declaracion,

                    ingresos_operacionales,
                    ingresos_no_operacionales,

                    costos,
                    gastos_operacionales,
                    gastos_financieros,
                    otros_gastos,

                    activo_corriente,
                    pasivo_corriente,
                    utilidad_operacional,
                    utilidad_neta,

                    total_activos,
                    total_pasivos,

                    origen_fondos,
                    relacion_financiera,
                    deuda_relacion_financiera,

                    activo,

                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )

                SELECT
                    c.id_solicitud_deudor,
                    c.tipo_persona,
                    CURRENT_TIMESTAMP,

                    :codigoOcupacion,
                    :codigoSectorEconomico,
                    :codigoActividadSes,
                    :codigoActividadDian,

                    COALESCE(:valorSalario, 0),
                    COALESCE(:valorPension, 0),
                    COALESCE(:ingresoIndependiente, 0),
                    COALESCE(:ingresosArriendo, 0),
                    COALESCE(:ingresosComisiones, 0),
                    COALESCE(:otrosIngresos, 0),
                    :comentarioOtrosIngresos,

                    COALESCE(:egresosFamiliares, 0),
                    COALESCE(:egresosArriendo, 0),
                    COALESCE(:egresosCredito, 0),
                    COALESCE(:otrosEgresos, 0),
                    :comentarioOtrosEgresos,

                    :declaraRenta,
                    :anioDeclaracion,
                    :fechaPresentacionDeclaracion,

                    COALESCE(:ingresosOperacionales, 0),
                    COALESCE(:ingresosNoOperacionales, 0),

                    COALESCE(:costos, 0),
                    COALESCE(:gastosOperacionales, 0),
                    COALESCE(:gastosFinancieros, 0),
                    COALESCE(:otrosGastos, 0),

                    COALESCE(:activoCorriente, 0),
                    COALESCE(:pasivoCorriente, 0),
                    COALESCE(:utilidadOperacional, 0),
                    COALESCE(:utilidadNeta, 0),

                    COALESCE(:totalActivos, 0),
                    COALESCE(:totalPasivos, 0),

                    :origenFondos,
                    :relacionFinanciera,
                    COALESCE(:deudaRelacionFinanciera, 0),

                    true,

                    :idUsuario,
                    CURRENT_TIMESTAMP,
                    :idUsuario,
                    CURRENT_TIMESTAMP

                FROM contexto c

                ON CONFLICT (id_solicitud_deudor)
                DO UPDATE
                SET
                    tipo_persona =
                        EXCLUDED.tipo_persona,

                    fecha_fotografia =
                        CURRENT_TIMESTAMP,

                    codigo_ocupacion =
                        EXCLUDED.codigo_ocupacion,

                    codigo_sector_economico =
                        EXCLUDED.codigo_sector_economico,

                    codigo_actividad_ses =
                        EXCLUDED.codigo_actividad_ses,

                    codigo_actividad_dian =
                        EXCLUDED.codigo_actividad_dian,

                    valor_salario =
                        EXCLUDED.valor_salario,

                    valor_pension =
                        EXCLUDED.valor_pension,

                    ingreso_independiente =
                        EXCLUDED.ingreso_independiente,

                    ingresos_arriendo =
                        EXCLUDED.ingresos_arriendo,

                    ingresos_comisiones =
                        EXCLUDED.ingresos_comisiones,

                    otros_ingresos =
                        EXCLUDED.otros_ingresos,

                    comentario_otros_ingresos =
                        EXCLUDED.comentario_otros_ingresos,

                    egresos_familiares =
                        EXCLUDED.egresos_familiares,

                    egresos_arriendo =
                        EXCLUDED.egresos_arriendo,

                    egresos_credito =
                        EXCLUDED.egresos_credito,

                    otros_egresos =
                        EXCLUDED.otros_egresos,

                    comentario_otros_egresos =
                        EXCLUDED.comentario_otros_egresos,

                    declara_renta =
                        EXCLUDED.declara_renta,

                    anio_declaracion =
                        EXCLUDED.anio_declaracion,

                    fecha_presentacion_declaracion =
                        EXCLUDED.fecha_presentacion_declaracion,

                    ingresos_operacionales =
                        EXCLUDED.ingresos_operacionales,

                    ingresos_no_operacionales =
                        EXCLUDED.ingresos_no_operacionales,

                    costos =
                        EXCLUDED.costos,

                    gastos_operacionales =
                        EXCLUDED.gastos_operacionales,

                    gastos_financieros =
                        EXCLUDED.gastos_financieros,

                    otros_gastos =
                        EXCLUDED.otros_gastos,

                    activo_corriente =
                        EXCLUDED.activo_corriente,

                    pasivo_corriente =
                        EXCLUDED.pasivo_corriente,

                    utilidad_operacional =
                        EXCLUDED.utilidad_operacional,

                    utilidad_neta =
                        EXCLUDED.utilidad_neta,

                    total_activos =
                        EXCLUDED.total_activos,

                    total_pasivos =
                        EXCLUDED.total_pasivos,

                    origen_fondos =
                        EXCLUDED.origen_fondos,

                    relacion_financiera =
                        EXCLUDED.relacion_financiera,

                    deuda_relacion_financiera =
                        EXCLUDED.deuda_relacion_financiera,

                    activo =
                        true,

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                RETURNING id_solicitud_deudor_financiero
            )

            SELECT
                id_solicitud_deudor_financiero

            FROM solicitud_guardada
            """;


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<SolicitudFinancieroDTO>
            RESUMEN_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudFinancieroDTO.class
            );

    private static final BeanPropertyRowMapper<SolicitudFinancieroDetalleDTO>
            DETALLE_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudFinancieroDetalleDTO.class
            );


    // =========================================================
    // DEPENDENCIA
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudFinancieroRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    public List<SolicitudFinancieroDTO> listarPorSolicitud(
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
    // BUSCAR POR DEUDOR
    // =========================================================

    public Optional<SolicitudFinancieroDetalleDTO> buscarPorDeudor(
            Integer idSolicitudDeudor
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudDeudor",
                                idSolicitudDeudor
                        );

        List<SolicitudFinancieroDetalleDTO> resultados =
                jdbc.query(
                        SQL_BUSCAR_POR_DEUDOR,
                        parametros,
                        DETALLE_MAPPER
                );

        return resultados.stream().findFirst();
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
                        SQL_OBTENER_AGENCIA_DEUDOR,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt("id_agencia")
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
                        SQL_OBTENER_AGENCIA_SOLICITUD,
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

        return resultados.stream().findFirst();
    }


    // =========================================================
    // GUARDAR INFORMACIÓN FINANCIERA
    // =========================================================

    public Integer guardar(
            SolicitudFinancieroGuardarRequestDTO request,
            Integer idUsuario
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()

                        .addValue(
                                "idSolicitudDeudor",
                                request.getIdSolicitudDeudor()
                        )

                        // -----------------------------------------
                        // ACTIVIDAD ECONÓMICA
                        // -----------------------------------------

                        .addValue(
                                "codigoOcupacion",
                                normalizarTexto(
                                        request.getCodigoOcupacion()
                                )
                        )

                        .addValue(
                                "codigoSectorEconomico",
                                normalizarTexto(
                                        request.getCodigoSectorEconomico()
                                )
                        )

                        .addValue(
                                "codigoActividadSes",
                                normalizarTexto(
                                        request.getCodigoActividadSes()
                                )
                        )

                        .addValue(
                                "codigoActividadDian",
                                normalizarTexto(
                                        request.getCodigoActividadDian()
                                )
                        )


                        // -----------------------------------------
                        // PERSONA NATURAL - INGRESOS
                        // -----------------------------------------

                        .addValue(
                                "valorSalario",
                                request.getValorSalario()
                        )

                        .addValue(
                                "valorPension",
                                request.getValorPension()
                        )

                        .addValue(
                                "ingresoIndependiente",
                                request.getIngresoIndependiente()
                        )

                        .addValue(
                                "ingresosArriendo",
                                request.getIngresosArriendo()
                        )

                        .addValue(
                                "ingresosComisiones",
                                request.getIngresosComisiones()
                        )

                        .addValue(
                                "otrosIngresos",
                                request.getOtrosIngresos()
                        )

                        .addValue(
                                "comentarioOtrosIngresos",
                                normalizarTexto(
                                        request.getComentarioOtrosIngresos()
                                )
                        )


                        // -----------------------------------------
                        // PERSONA NATURAL - EGRESOS
                        // -----------------------------------------

                        .addValue(
                                "egresosFamiliares",
                                request.getEgresosFamiliares()
                        )

                        .addValue(
                                "egresosArriendo",
                                request.getEgresosArriendo()
                        )

                        .addValue(
                                "egresosCredito",
                                request.getEgresosCredito()
                        )

                        .addValue(
                                "otrosEgresos",
                                request.getOtrosEgresos()
                        )

                        .addValue(
                                "comentarioOtrosEgresos",
                                normalizarTexto(
                                        request.getComentarioOtrosEgresos()
                                )
                        )


                        // -----------------------------------------
                        // DECLARACIÓN DE RENTA
                        // -----------------------------------------

                        .addValue(
                                "declaraRenta",
                                request.getDeclaraRenta()
                        )

                        .addValue(
                                "anioDeclaracion",
                                request.getAnioDeclaracion()
                        )

                        .addValue(
                                "fechaPresentacionDeclaracion",
                                request.getFechaPresentacionDeclaracion()
                        )


                        // -----------------------------------------
                        // PERSONA JURÍDICA
                        // -----------------------------------------

                        .addValue(
                                "ingresosOperacionales",
                                request.getIngresosOperacionales()
                        )

                        .addValue(
                                "ingresosNoOperacionales",
                                request.getIngresosNoOperacionales()
                        )

                        .addValue(
                                "costos",
                                request.getCostos()
                        )

                        .addValue(
                                "gastosOperacionales",
                                request.getGastosOperacionales()
                        )

                        .addValue(
                                "gastosFinancieros",
                                request.getGastosFinancieros()
                        )

                        .addValue(
                                "otrosGastos",
                                request.getOtrosGastos()
                        )

                        .addValue(
                                "activoCorriente",
                                request.getActivoCorriente()
                        )

                        .addValue(
                                "pasivoCorriente",
                                request.getPasivoCorriente()
                        )

                        .addValue(
                                "utilidadOperacional",
                                request.getUtilidadOperacional()
                        )

                        .addValue(
                                "utilidadNeta",
                                request.getUtilidadNeta()
                        )


                        // -----------------------------------------
                        // BALANCE
                        // -----------------------------------------

                        .addValue(
                                "totalActivos",
                                request.getTotalActivos()
                        )

                        .addValue(
                                "totalPasivos",
                                request.getTotalPasivos()
                        )


                        // -----------------------------------------
                        // INFORMACIÓN COMPLEMENTARIA
                        // -----------------------------------------

                        .addValue(
                                "origenFondos",
                                normalizarTexto(
                                        request.getOrigenFondos()
                                )
                        )

                        .addValue(
                                "relacionFinanciera",
                                normalizarTexto(
                                        request.getRelacionFinanciera()
                                )
                        )

                        .addValue(
                                "deudaRelacionFinanciera",
                                request.getDeudaRelacionFinanciera()
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