package co.assip.erp.cartera.cierremensual.hojavida.bienes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CierreHojaVidaBienesRepository {

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // ELIMINAR RELACIONES BIEN - PERSONA DEL PRECierre
    //
    // Debe ejecutarse ANTES de eliminar los bienes.
    // =========================================================

    public int eliminarBienesPersonas(
            Integer idCierreHojaVida
    ) {

        String sql = """
                DELETE FROM hoja_vida.cierres_hoja_vida_bienes_personas cbp

                WHERE cbp.id_cierre_hoja_vida_bien IN
                (
                    SELECT
                        cb.id_cierre_hoja_vida_bien

                    FROM hoja_vida.cierres_hoja_vida_bienes cb

                    WHERE cb.id_cierre_hoja_vida =
                          :idCierreHojaVida
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }


    // =========================================================
    // ELIMINAR BIENES DEL PRECierre
    //
    // La cabecera hoja_vida.cierres_hoja_vida se conserva.
    // =========================================================

    public int eliminarBienes(
            Integer idCierreHojaVida
    ) {

        String sql = """
                DELETE FROM hoja_vida.cierres_hoja_vida_bienes

                WHERE id_cierre_hoja_vida =
                      :idCierreHojaVida
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }


    // =========================================================
    // CONTAR BIENES ESPERADOS
    //
    // IMPORTANTE:
    //
    // Se replica la MISMA población utilizada por el cálculo
    // posterior de prorrateo de garantías:
    //
    // cierres_cartera_creditos
    //      -> obligaciones_juridicas
    //      -> obligaciones_fiadores
    //      -> obligaciones_fiadores_bienes
    //
    // REGLAS:
    //
    // - crédito pertenece al cierre recibido
    // - saldo_actual > 0
    // - codigo_garantia_credito = '2'
    // - se cuenta cada id_bien una sola vez
    //
    // =========================================================

    public int contarBienesEsperados(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM
                (
                    SELECT DISTINCT
                        ofb.id_bien

                    FROM cartera.cierres_cartera_creditos f

                    INNER JOIN cartera.obligaciones_juridicas oj
                        ON oj.id_agencia =
                           f.id_agencia_juridica

                       AND oj.id_linea_credito =
                           f.id_linea_credito_juridica

                       AND oj.numero_pagare =
                           f.pagare_juridico

                    INNER JOIN cartera.obligaciones_fiadores ofi
                        ON ofi.id_obligacion_juridica =
                           oj.id_obligacion_juridica

                    INNER JOIN cartera.obligaciones_fiadores_bienes ofb
                        ON ofb.id_obligacion_fiador =
                           ofi.id_obligacion_fiador

                    WHERE f.id_cierre_cartera =
                          :idCierreCartera

                      AND COALESCE(
                              f.saldo_actual,
                              0
                          ) > 0

                      AND f.codigo_garantia_credito =
                          '2'

                      AND ofb.id_bien IS NOT NULL

                ) bienes
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


    // =========================================================
    // GENERAR FOTOGRAFÍA DE BIENES
    //
    // Se fotografía únicamente el universo de bienes requerido
    // por las garantías reales de los créditos del cierre.
    //
    // Se congela:
    //
    // 1. Bien general.
    // 2. Tipo de bien.
    // 3. Gravamen.
    // 4. Detalle inmueble.
    // 5. Último avalúo de inmueble.
    // 6. Detalle vehículo.
    // 7. Detalle maquinaria.
    // 8. Detalle inversión.
    // 9. Último seguro disponible según tipo de bien.
    //
    // IMPORTANTE:
    //
    // La distribución del valor del bien entre créditos NO se
    // calcula aquí. Esa responsabilidad continúa siendo de:
    //
    // cartera.cierres_cartera_resultados_garantias
    //
    // =========================================================

    public int generarBienes(
            Integer idCierreHojaVida,
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
                WITH bienes_requeridos AS
                (
                    SELECT DISTINCT
                        ofb.id_bien

                    FROM cartera.cierres_cartera_creditos f

                    INNER JOIN cartera.obligaciones_juridicas oj
                        ON oj.id_agencia =
                           f.id_agencia_juridica

                       AND oj.id_linea_credito =
                           f.id_linea_credito_juridica

                       AND oj.numero_pagare =
                           f.pagare_juridico

                    INNER JOIN cartera.obligaciones_fiadores ofi
                        ON ofi.id_obligacion_juridica =
                           oj.id_obligacion_juridica

                    INNER JOIN cartera.obligaciones_fiadores_bienes ofb
                        ON ofb.id_obligacion_fiador =
                           ofi.id_obligacion_fiador

                    WHERE f.id_cierre_cartera =
                          :idCierreCartera

                      AND COALESCE(
                              f.saldo_actual,
                              0
                          ) > 0

                      AND f.codigo_garantia_credito =
                          '2'

                      AND ofb.id_bien IS NOT NULL
                )

                INSERT INTO hoja_vida.cierres_hoja_vida_bienes
                (
                    id_cierre_hoja_vida,

                    id_bien,
                    id_tipo_bien,
                    codigo_tipo_bien,
                    nombre_tipo_bien,

                    descripcion_general,
                    valor_comercial,
                    valor_gravamen,
                    fecha_adquisicion,
                    estado_bien,
                    fecha_estado,

                    id_tipo_gravamen,
                    nombre_tipo_gravamen,

                    -- =========================================
                    -- INMUEBLE
                    -- =========================================

                    id_bien_inmueble,
                    id_tipo_inmueble,
                    codigo_tipo_inmueble,
                    nombre_tipo_inmueble,

                    numero_matricula_inmobiliaria,
                    cedula_catastral,

                    id_pais,
                    id_departamento,
                    id_ciudad,

                    direccion,
                    barrio_vereda,

                    area_terreno,
                    area_construida,

                    numero_escritura,
                    fecha_escritura,
                    fecha_registro_escritura,
                    notaria,

                    id_pais_notaria,
                    id_departamento_notaria,
                    id_ciudad_notaria,

                    oficina_registro,
                    id_tipo_zona_inmueble,

                    -- =========================================
                    -- AVALÚO
                    -- =========================================

                    id_bien_inmueble_avaluo,
                    fecha_ultimo_avaluo,
                    fecha_vencimiento_avaluo,
                    vigencia_avaluo_anios,

                    valor_avaluo_comercial,
                    valor_avaluo_catastral,
                    valor_terreno,
                    valor_construccion,
                    valor_cultivos,
                    valor_otros_avaluo,

                    entidad_avaluadora,
                    numero_informe_avaluo,

                    -- =========================================
                    -- VEHÍCULO
                    -- =========================================

                    id_bien_vehiculo,
                    id_tipo_vehiculo,
                    nombre_tipo_vehiculo,

                    placa,
                    marca_vehiculo,
                    linea_vehiculo,
                    modelo_vehiculo,
                    color_vehiculo,

                    numero_motor,
                    numero_chasis,
                    numero_serie,

                    -- =========================================
                    -- MAQUINARIA
                    -- =========================================

                    id_bien_maquinaria,
                    id_tipo_maquinaria,
                    nombre_tipo_maquinaria,

                    marca_maquinaria,
                    modelo_maquinaria,
                    serial_maquinaria,
                    referencia_maquinaria,

                    descripcion_tecnica_maquinaria,
                    ubicacion_maquinaria,
                    estado_operativo_maquinaria,

                    -- =========================================
                    -- INVERSIÓN
                    -- =========================================

                    id_bien_inversion,
                    id_tipo_inversion,
                    nombre_tipo_inversion,

                    entidad_inversion,
                    numero_titulo,

                    fecha_inversion,
                    fecha_vencimiento_inversion,

                    valor_nominal_inversion,
                    valor_actual_inversion,
                    tasa_rendimiento_inversion,

                    -- =========================================
                    -- SEGURO
                    -- =========================================

                    tipo_origen_seguro,
                    id_registro_seguro,

                    aseguradora,
                    numero_poliza,

                    valor_asegurado,
                    fecha_inicio_seguro,
                    fecha_vencimiento_seguro,
                    estado_seguro,

                    -- =========================================
                    -- AUDITORÍA
                    -- =========================================

                    fecha_fotografia,
                    fk_seguridad_creacion,
                    fecha_creacion
                )

                SELECT
                    :idCierreHojaVida,

                    b.id_bien,
                    b.id_tipo_bien,

                    tb.codigo_tipo_bien,
                    tb.nombre_tipo_bien,

                    b.descripcion_general,

                    COALESCE(
                        b.valor_comercial,
                        0
                    ),

                    COALESCE(
                        b.valor_gravamen,
                        0
                    ),

                    b.fecha_adquisicion,
                    b.estado_bien,
                    b.fecha_estado,

                    COALESCE(
                        bi.id_tipo_gravamen,
                        bv.id_tipo_gravamen,
                        bm.id_tipo_gravamen,
                        binv.id_tipo_gravamen
                    ) AS id_tipo_gravamen,

                    tg.nombre_gravamen
                        AS nombre_tipo_gravamen,

                    -- =========================================
                    -- INMUEBLE
                    -- =========================================

                    bi.id_bien_inmueble,
                    bi.id_tipo_inmueble,

                    ti.codigo_tipo_inmueble,
                    ti.nombre_tipo_inmueble,

                    bi.numero_matricula_inmobiliaria,
                    bi.cedula_catastral,

                    bi.id_pais,
                    bi.id_departamento,
                    bi.id_ciudad,

                    bi.direccion,
                    bi.barrio_vereda,

                    bi.area_terreno,
                    bi.area_construida,

                    bi.numero_escritura,
                    bi.fecha_escritura,
                    bi.fecha_registro_escritura,
                    bi.notaria,

                    bi.id_pais_notaria,
                    bi.id_departamento_notaria,
                    bi.id_ciudad_notaria,

                    bi.oficina_registro,
                    bi.id_tipo_zona_inmueble,

                    -- =========================================
                    -- ÚLTIMO AVALÚO DEL INMUEBLE
                    -- =========================================

                    avaluo.id_bien_inmueble_avaluo,

                    avaluo.fecha_avaluo
                        AS fecha_ultimo_avaluo,

                    avaluo.fecha_vencimiento_avaluo,

                    avaluo.vigencia_anios
                        AS vigencia_avaluo_anios,

                    COALESCE(
                        avaluo.valor_avaluo_comercial,
                        0
                    ),

                    COALESCE(
                        avaluo.valor_avaluo_catastral,
                        0
                    ),

                    COALESCE(
                        avaluo.valor_terreno,
                        0
                    ),

                    COALESCE(
                        avaluo.valor_construccion,
                        0
                    ),

                    COALESCE(
                        avaluo.valor_cultivos,
                        0
                    ),

                    COALESCE(
                        avaluo.valor_otros,
                        0
                    ) AS valor_otros_avaluo,

                    avaluo.entidad_avaluadora,

                    avaluo.numero_informe
                        AS numero_informe_avaluo,

                    -- =========================================
                    -- VEHÍCULO
                    -- =========================================

                    bv.id_bien_vehiculo,
                    bv.id_tipo_vehiculo,
                    tv.nombre_tipo_vehiculo,

                    bv.placa,

                    bv.marca
                        AS marca_vehiculo,

                    bv.linea
                        AS linea_vehiculo,

                    bv.modelo
                        AS modelo_vehiculo,

                    bv.color
                        AS color_vehiculo,

                    bv.numero_motor,
                    bv.numero_chasis,
                    bv.numero_serie,

                    -- =========================================
                    -- MAQUINARIA
                    -- =========================================

                    bm.id_bien_maquinaria,
                    bm.id_tipo_maquinaria,
                    tm.nombre_tipo_maquinaria,

                    bm.marca
                        AS marca_maquinaria,

                    bm.modelo
                        AS modelo_maquinaria,

                    bm.serial
                        AS serial_maquinaria,

                    bm.referencia
                        AS referencia_maquinaria,

                    bm.descripcion_tecnica
                        AS descripcion_tecnica_maquinaria,

                    bm.ubicacion
                        AS ubicacion_maquinaria,

                    bm.estado_operativo
                        AS estado_operativo_maquinaria,

                    -- =========================================
                    -- INVERSIÓN
                    -- =========================================

                    binv.id_bien_inversion,
                    binv.id_tipo_inversion,
                    tinv.nombre_tipo_inversion,

                    binv.entidad
                        AS entidad_inversion,

                    binv.numero_titulo,

                    binv.fecha_inversion,

                    binv.fecha_vencimiento
                        AS fecha_vencimiento_inversion,

                    COALESCE(
                        binv.valor_nominal,
                        0
                    ) AS valor_nominal_inversion,

                    COALESCE(
                        binv.valor_actual,
                        0
                    ) AS valor_actual_inversion,

                    binv.tasa_rendimiento
                        AS tasa_rendimiento_inversion,

                    -- =========================================
                    -- ÚLTIMO SEGURO
                    -- =========================================

                    seguro.tipo_origen_seguro,
                    seguro.id_registro_seguro,

                    seguro.aseguradora,
                    seguro.numero_poliza,

                    COALESCE(
                        seguro.valor_asegurado,
                        0
                    ),

                    seguro.fecha_inicio_seguro,
                    seguro.fecha_vencimiento_seguro,
                    seguro.estado_seguro,

                    CURRENT_TIMESTAMP,
                    :idUsuario,
                    CURRENT_TIMESTAMP

                FROM bienes_requeridos br

                INNER JOIN hoja_vida.bienes b
                    ON b.id_bien =
                       br.id_bien

                INNER JOIN hoja_vida.tipos_bienes tb
                    ON tb.id_tipo_bien =
                       b.id_tipo_bien


                -- =============================================
                -- INMUEBLE
                -- =============================================

                LEFT JOIN hoja_vida.bienes_inmuebles bi
                    ON bi.id_bien =
                       b.id_bien

                LEFT JOIN hoja_vida.tipos_inmuebles ti
                    ON ti.id_tipo_inmueble =
                       bi.id_tipo_inmueble


                -- =============================================
                -- VEHÍCULO
                -- =============================================

                LEFT JOIN hoja_vida.bienes_vehiculos bv
                    ON bv.id_bien =
                       b.id_bien

                LEFT JOIN hoja_vida.tipos_vehiculos tv
                    ON tv.id_tipo_vehiculo =
                       bv.id_tipo_vehiculo


                -- =============================================
                -- MAQUINARIA
                -- =============================================

                LEFT JOIN hoja_vida.bienes_maquinaria bm
                    ON bm.id_bien =
                       b.id_bien

                LEFT JOIN hoja_vida.tipos_maquinaria tm
                    ON tm.id_tipo_maquinaria =
                       bm.id_tipo_maquinaria


                -- =============================================
                -- INVERSIÓN
                -- =============================================

                LEFT JOIN hoja_vida.bienes_inversiones binv
                    ON binv.id_bien =
                       b.id_bien

                LEFT JOIN hoja_vida.tipos_inversiones tinv
                    ON tinv.id_tipo_inversion =
                       binv.id_tipo_inversion


                -- =============================================
                -- GRAVAMEN
                -- =============================================

                LEFT JOIN hoja_vida.tipos_gravamenes tg
                    ON tg.id_tipo_gravamen =
                       COALESCE(
                           bi.id_tipo_gravamen,
                           bv.id_tipo_gravamen,
                           bm.id_tipo_gravamen,
                           binv.id_tipo_gravamen
                       )


                -- =============================================
                -- ÚLTIMO AVALÚO DEL INMUEBLE
                -- =============================================

                LEFT JOIN LATERAL
                (
                    SELECT
                        a.id_bien_inmueble_avaluo,
                        a.fecha_avaluo,
                        a.vigencia_anios,
                        a.fecha_vencimiento_avaluo,

                        a.valor_avaluo_comercial,
                        a.valor_avaluo_catastral,

                        a.valor_terreno,
                        a.valor_construccion,
                        a.valor_cultivos,
                        a.valor_otros,

                        a.entidad_avaluadora,
                        a.numero_informe

                    FROM hoja_vida.bienes_inmuebles_avaluos a

                    WHERE a.id_bien =
                          b.id_bien

                    ORDER BY
                        a.fecha_avaluo DESC NULLS LAST,
                        a.fecha_edicion DESC NULLS LAST,
                        a.fecha_creacion DESC NULLS LAST,
                        a.id_bien_inmueble_avaluo DESC

                    LIMIT 1

                ) avaluo
                    ON bi.id_bien_inmueble IS NOT NULL


                -- =============================================
                -- ÚLTIMO SEGURO SEGÚN TIPO DE BIEN
                -- =============================================

                LEFT JOIN LATERAL
                (
                    SELECT
                        s.tipo_origen_seguro,
                        s.id_registro_seguro,

                        s.aseguradora,
                        s.numero_poliza,

                        s.valor_asegurado,
                        s.fecha_inicio_seguro,
                        s.fecha_vencimiento_seguro,
                        s.estado_seguro,

                        s.fecha_creacion,
                        s.fecha_edicion

                    FROM
                    (
                        SELECT
                            'INMUEBLE'::varchar
                                AS tipo_origen_seguro,

                            si.id_bien_inmueble_seguro
                                AS id_registro_seguro,

                            si.aseguradora,
                            si.numero_poliza,
                            si.valor_asegurado,
                            si.fecha_inicio_seguro,
                            si.fecha_vencimiento_seguro,
                            si.estado_seguro,

                            si.fecha_creacion,
                            si.fecha_edicion

                        FROM hoja_vida.bienes_inmuebles_seguros si

                        WHERE si.id_bien =
                              b.id_bien


                        UNION ALL


                        SELECT
                            'VEHICULO'::varchar
                                AS tipo_origen_seguro,

                            sv.id_bien_vehiculo_seguro
                                AS id_registro_seguro,

                            sv.aseguradora,
                            sv.numero_poliza,
                            sv.valor_asegurado,
                            sv.fecha_inicio_seguro,
                            sv.fecha_vencimiento_seguro,
                            sv.estado_seguro,

                            sv.fecha_creacion,
                            sv.fecha_edicion

                        FROM hoja_vida.bienes_vehiculos_seguros sv

                        WHERE sv.id_bien =
                              b.id_bien


                        UNION ALL


                        SELECT
                            'MAQUINARIA'::varchar
                                AS tipo_origen_seguro,

                            sm.id_bien_maquinaria_seguro
                                AS id_registro_seguro,

                            sm.aseguradora,
                            sm.numero_poliza,
                            sm.valor_asegurado,
                            sm.fecha_inicio_seguro,
                            sm.fecha_vencimiento_seguro,
                            sm.estado_seguro,

                            sm.fecha_creacion,
                            sm.fecha_edicion

                        FROM hoja_vida.bienes_maquinaria_seguros sm

                        WHERE sm.id_bien =
                              b.id_bien

                    ) s

                    ORDER BY
                        s.fecha_vencimiento_seguro DESC NULLS LAST,
                        s.fecha_edicion DESC NULLS LAST,
                        s.fecha_creacion DESC NULLS LAST,
                        s.id_registro_seguro DESC

                    LIMIT 1

                ) seguro
                    ON TRUE

                ORDER BY
                    b.id_bien
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        )
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }


    // =========================================================
    // CONTAR BIENES FOTOGRAFIADOS
    // =========================================================

    public int contarBienesFotografiados(
            Integer idCierreHojaVida
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM hoja_vida.cierres_hoja_vida_bienes

                WHERE id_cierre_hoja_vida =
                      :idCierreHojaVida
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


    // =========================================================
    // GENERAR FOTOGRAFÍA DE RELACIONES BIEN - PERSONA
    //
    // Una vez fotografiado el bien, se congelan TODOS sus
    // propietarios registrados y su porcentaje de propiedad.
    //
    // No se restringe al deudor ni al fiador porque la
    // fotografía debe conservar la composición completa
    // de propiedad del bien en ese momento.
    // =========================================================

    public int generarBienesPersonas(
            Integer idCierreHojaVida,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO hoja_vida.cierres_hoja_vida_bienes_personas
                (
                    id_cierre_hoja_vida_bien,
                    id_bien,
                    id_datos_personal,
                    porcentaje_propiedad,

                    fk_seguridad_creacion,
                    fecha_creacion
                )

                SELECT
                    cb.id_cierre_hoja_vida_bien,
                    cb.id_bien,
                    bp.id_datos_personal,

                    COALESCE(
                        bp.porcentaje_propiedad,
                        100
                    ),

                    :idUsuario,
                    CURRENT_TIMESTAMP

                FROM hoja_vida.cierres_hoja_vida_bienes cb

                INNER JOIN hoja_vida.bienes_personas bp
                    ON bp.id_bien =
                       cb.id_bien

                WHERE cb.id_cierre_hoja_vida =
                      :idCierreHojaVida

                ORDER BY
                    cb.id_bien,
                    bp.id_datos_personal
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }


    // =========================================================
    // CONTAR RELACIONES BIEN - PERSONA ESPERADAS
    //
    // Se cuentan las relaciones operativas correspondientes
    // únicamente a los bienes que ya fueron fotografiados.
    // =========================================================

    public int contarBienesPersonasEsperadas(
            Integer idCierreHojaVida
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM hoja_vida.cierres_hoja_vida_bienes cb

                INNER JOIN hoja_vida.bienes_personas bp
                    ON bp.id_bien =
                       cb.id_bien

                WHERE cb.id_cierre_hoja_vida =
                      :idCierreHojaVida
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


    // =========================================================
    // CONTAR RELACIONES BIEN - PERSONA FOTOGRAFIADAS
    // =========================================================

    public int contarBienesPersonasFotografiadas(
            Integer idCierreHojaVida
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM hoja_vida.cierres_hoja_vida_bienes_personas cbp

                INNER JOIN hoja_vida.cierres_hoja_vida_bienes cb
                    ON cb.id_cierre_hoja_vida_bien =
                       cbp.id_cierre_hoja_vida_bien

                WHERE cb.id_cierre_hoja_vida =
                      :idCierreHojaVida
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


    // =========================================================
    // CONTAR BIENES SIN PROPIETARIO
    //
    // Este control NO modifica nada.
    //
    // Un bien utilizado como garantía debería conservar al
    // menos una relación en bienes_personas.
    // =========================================================

    public int contarBienesSinPropietario(
            Integer idCierreHojaVida
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM hoja_vida.cierres_hoja_vida_bienes cb

                WHERE cb.id_cierre_hoja_vida =
                      :idCierreHojaVida

                  AND NOT EXISTS
                  (
                      SELECT 1

                      FROM hoja_vida.cierres_hoja_vida_bienes_personas cbp

                      WHERE cbp.id_cierre_hoja_vida_bien =
                            cb.id_cierre_hoja_vida_bien
                  )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }

    // =========================================================
// CONTAR RELACIONES BIEN - PERSONA FOTOGRAFIADAS
// =========================================================

    public int contarBienesPersonasFotografiados(
            Integer idCierreHojaVida
    ) {

        String sql = """
            SELECT COUNT(*)

            FROM hoja_vida.cierres_hoja_vida_bienes_personas cbp

            INNER JOIN hoja_vida.cierres_hoja_vida_bienes cb
                ON cb.id_cierre_hoja_vida_bien =
                   cbp.id_cierre_hoja_vida_bien

            WHERE cb.id_cierre_hoja_vida =
                  :idCierreHojaVida
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }
}