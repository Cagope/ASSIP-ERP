package co.assip.erp.cartera.cierremensual.hojavida;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CierreHojaVidaCarteraRepository {

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // BUSCAR ID DE CIERRE DE HOJA DE VIDA POR FECHA
    // =========================================================

    public Optional<Integer> buscarIdPorFecha(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT
                    chv.id_cierre_hoja_vida

                FROM hoja_vida.cierres_hoja_vida chv

                WHERE chv.fecha_corte =
                      :fechaCorte

                LIMIT 1
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        List<Integer> lista =
                jdbc.query(
                        sql,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt(
                                        "id_cierre_hoja_vida"
                                )
                );

        return lista.stream().findFirst();
    }


    // =========================================================
    // BUSCAR ESTADO DE CIERRE DE HOJA DE VIDA
    // =========================================================

    public Optional<String> buscarEstado(
            Integer idCierreHojaVida
    ) {

        String sql = """
                SELECT
                    chv.estado

                FROM hoja_vida.cierres_hoja_vida chv

                WHERE chv.id_cierre_hoja_vida =
                      :idCierreHojaVida
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        );

        List<String> lista =
                jdbc.query(
                        sql,
                        parametros,
                        (rs, rowNum) ->
                                rs.getString(
                                        "estado"
                                )
                );

        return lista.stream().findFirst();
    }


    // =========================================================
    // CREAR CABECERA DE PRECierre DE HOJA DE VIDA
    //
    // P = Precierre / fotografía regenerable
    //
    // Esta cabecera es generada automáticamente desde
    // el cierre mensual de cartera.
    // =========================================================

    public Integer crearCabecera(
            LocalDate fechaCorte,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO hoja_vida.cierres_hoja_vida
                (
                    fecha_corte,
                    fecha_fotografia,
                    version_hoja_vida,
                    estado,
                    cantidad_personas,
                    observaciones,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    :fechaCorte,
                    CURRENT_TIMESTAMP,
                    'HV-1.0',
                    'P',
                    0,
                    'Fotografía de Hoja de Vida generada automáticamente como insumo del cierre mensual de cartera.',
                    :idUsuario,
                    :idUsuario
                )

                RETURNING
                    id_cierre_hoja_vida
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.queryForObject(
                sql,
                parametros,
                Integer.class
        );
    }


    // =========================================================
    // PREPARAR CABECERA PARA UN NUEVO PRECierre
    //
    // Solamente debe usarse cuando estado = P.
    //
    // - conserva id_cierre_hoja_vida
    // - conserva fecha_corte
    // - reinicia cantidad_personas
    // - actualiza fecha_fotografia
    // =========================================================

    public int prepararCabeceraPrecierre(
            Integer idCierreHojaVida,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE hoja_vida.cierres_hoja_vida

                   SET cantidad_personas =
                           0,

                       fecha_fotografia =
                           CURRENT_TIMESTAMP,

                       estado =
                           'P',

                       observaciones =
                           'Fotografía de Hoja de Vida generada automáticamente como insumo del cierre mensual de cartera.',

                       fk_seguridad_edicion =
                           :idUsuario,

                       fecha_edicion =
                           CURRENT_TIMESTAMP

                 WHERE id_cierre_hoja_vida =
                       :idCierreHojaVida

                   AND estado =
                       'P'
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
    // ELIMINAR PERSONAS DEL PRECierre
    //
    // IMPORTANTE:
    //
    // Cuando incorporemos bienes, el orden completo será:
    //
    // 1. bienes_personas
    // 2. bienes
    // 3. personas
    //
    // Por ahora este método maneja únicamente personas.
    // =========================================================

    public int eliminarPersonas(
            Integer idCierreHojaVida
    ) {

        String sql = """
                DELETE FROM hoja_vida.cierres_hoja_vida_personas

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
    // GENERAR FOTOGRAFÍA DE PERSONAS
    //
    // POBLACIÓN:
    //
    // Únicamente personas presentes en la fotografía
    // del cierre de cartera recibido.
    //
    // No depende del cierre mensual de depósitos.
    //
    // Se congela:
    //
    // - información básica
    // - última ubicación disponible
    // - último registro financiero disponible
    // - totales financieros calculados
    // =========================================================

    public int generarPersonas(
            Integer idCierreHojaVida,
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO hoja_vida.cierres_hoja_vida_personas
                (
                    id_cierre_hoja_vida,
                    id_datos_personal,

                    tipo_documento,
                    documento,
                    tipo_persona,
                    tiene_rut,
                    digito_verificacion,

                    nombres,
                    primer_apellido,
                    segundo_apellido,

                    fecha_nacimiento,
                    fecha_apertura,
                    fecha_actualizacion,

                    codigo_genero,
                    codigo_estado_civil,
                    codigo_escolaridad,
                    cabeza_familia,
                    estrato_social,
                    codigo_tipo_vivienda,
                    numero_hijos,
                    codigo_ocupacion,
                    codigo_sector_economico,
                    codigo_actividad_ses,
                    codigo_actividad_dian,

                    direccion,
                    barrio,
                    telefono,
                    celular_uno,
                    celular_dos,
                    correo,

                    id_pais,
                    id_departamento,
                    id_ciudad,
                    id_zona,
                    id_sub_zona,

                    valor_salario,
                    valor_pension,
                    ingresos_arriendo,
                    ingresos_comisiones,
                    otros_ingresos,

                    egresos_familiares,
                    egresos_arriendo,
                    egresos_credito,
                    otros_egresos,

                    total_activos,
                    total_pasivos,
                    deuda_relacion_financiera,
                    origen_fondos,
                    relacion_financiera,

                    ingresos_totales,
                    egresos_totales,
                    patrimonio_total,
                    ingreso_disponible,

                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )

                SELECT
                    :idCierreHojaVida,
                    dp.id_datos_personal,

                    dp.tipo_documento,
                    dp.documento,
                    dp.tipo_persona,
                    COALESCE(
                        dp.tiene_rut,
                        FALSE
                    ),
                    dp.digito_verificacion,

                    dp.nombres,
                    dp.primer_apellido,
                    dp.segundo_apellido,

                    dp.fecha_nacimiento,
                    dp.fecha_apertura,
                    dp.fecha_actualizacion,

                    dp.codigo_genero,
                    dp.codigo_estado_civil,
                    dp.codigo_escolaridad,
                    dp.cabeza_familia,
                    dp.estrato_social,
                    dp.codigo_tipo_vivienda,
                    dp.numero_hijos,
                    dp.codigo_ocupacion,
                    dp.codigo_sector_economico,
                    dp.codigo_actividad_ses,
                    dp.codigo_actividad_dian,

                    ubicacion.direccion,
                    ubicacion.barrio,
                    ubicacion.telefono,
                    ubicacion.celular_uno,
                    ubicacion.celular_dos,
                    ubicacion.correo,

                    ubicacion.id_pais,
                    ubicacion.id_departamento,
                    ubicacion.id_ciudad,
                    ubicacion.id_zona,
                    ubicacion.id_sub_zona,

                    COALESCE(
                        financiero.valor_salario,
                        0
                    ),

                    COALESCE(
                        financiero.valor_pension,
                        0
                    ),

                    COALESCE(
                        financiero.ingresos_arriendo,
                        0
                    ),

                    COALESCE(
                        financiero.ingresos_comisiones,
                        0
                    ),

                    COALESCE(
                        financiero.otros_ingresos,
                        0
                    ),

                    COALESCE(
                        financiero.egresos_familiares,
                        0
                    ),

                    COALESCE(
                        financiero.egresos_arriendo,
                        0
                    ),

                    COALESCE(
                        financiero.egresos_credito,
                        0
                    ),

                    COALESCE(
                        financiero.otros_egresos,
                        0
                    ),

                    COALESCE(
                        financiero.total_activos,
                        0
                    ),

                    COALESCE(
                        financiero.total_pasivos,
                        0
                    ),

                    COALESCE(
                        financiero.deuda_relacion_financiera,
                        0
                    ),

                    financiero.origen_fondos,
                    financiero.relacion_financiera,

                    (
                        COALESCE(
                            financiero.valor_salario,
                            0
                        )
                        +
                        COALESCE(
                            financiero.valor_pension,
                            0
                        )
                        +
                        COALESCE(
                            financiero.ingresos_arriendo,
                            0
                        )
                        +
                        COALESCE(
                            financiero.ingresos_comisiones,
                            0
                        )
                        +
                        COALESCE(
                            financiero.otros_ingresos,
                            0
                        )
                    ) AS ingresos_totales,

                    (
                        COALESCE(
                            financiero.egresos_familiares,
                            0
                        )
                        +
                        COALESCE(
                            financiero.egresos_arriendo,
                            0
                        )
                        +
                        COALESCE(
                            financiero.egresos_credito,
                            0
                        )
                        +
                        COALESCE(
                            financiero.otros_egresos,
                            0
                        )
                    ) AS egresos_totales,

                    (
                        COALESCE(
                            financiero.total_activos,
                            0
                        )
                        -
                        COALESCE(
                            financiero.total_pasivos,
                            0
                        )
                    ) AS patrimonio_total,

                    (
                        COALESCE(
                            financiero.valor_salario,
                            0
                        )
                        +
                        COALESCE(
                            financiero.valor_pension,
                            0
                        )
                        +
                        COALESCE(
                            financiero.ingresos_arriendo,
                            0
                        )
                        +
                        COALESCE(
                            financiero.ingresos_comisiones,
                            0
                        )
                        +
                        COALESCE(
                            financiero.otros_ingresos,
                            0
                        )

                        -

                        COALESCE(
                            financiero.egresos_familiares,
                            0
                        )
                        -
                        COALESCE(
                            financiero.egresos_arriendo,
                            0
                        )
                        -
                        COALESCE(
                            financiero.egresos_credito,
                            0
                        )
                        -
                        COALESCE(
                            financiero.otros_egresos,
                            0
                        )
                    ) AS ingreso_disponible,

                    :idUsuario,
                    :idUsuario

                FROM hoja_vida.datos_personales dp

                INNER JOIN
                (
                    SELECT DISTINCT
                        cc.id_datos_personal

                    FROM cartera.cierres_cartera_creditos cc

                    WHERE cc.id_cierre_cartera =
                          :idCierreCartera

                      AND cc.id_datos_personal
                          IS NOT NULL

                      AND COALESCE(
                              cc.saldo_actual,
                              0
                          ) > 0
                ) personas_cartera

                    ON personas_cartera.id_datos_personal =
                       dp.id_datos_personal


                -- =================================================
                -- ÚLTIMA UBICACIÓN DISPONIBLE
                -- =================================================

                LEFT JOIN LATERAL
                (
                    SELECT
                        u.direccion,
                        u.barrio,
                        u.telefono,
                        u.celular_uno,
                        u.celular_dos,
                        u.correo,

                        u.id_pais,
                        u.id_departamento,
                        u.id_ciudad,
                        u.id_zona,
                        u.id_sub_zona

                    FROM hoja_vida.ubicaciones u

                    WHERE u.id_datos_personal =
                          dp.id_datos_personal

                    ORDER BY
                        u.fecha_edicion DESC NULLS LAST,
                        u.fecha_creacion DESC NULLS LAST,
                        u.id_ubicacion DESC

                    LIMIT 1

                ) ubicacion
                    ON TRUE


                -- =================================================
                -- ÚLTIMO REGISTRO FINANCIERO DISPONIBLE
                -- =================================================

                LEFT JOIN LATERAL
                (
                    SELECT
                        f.valor_salario,
                        f.valor_pension,
                        f.ingresos_arriendo,
                        f.ingresos_comisiones,
                        f.otros_ingresos,

                        f.egresos_familiares,
                        f.egresos_arriendo,
                        f.egresos_credito,
                        f.otros_egresos,

                        f.total_activos,
                        f.total_pasivos,
                        f.deuda_relacion_financiera,
                        f.origen_fondos,
                        f.relacion_financiera

                    FROM hoja_vida.financieros f

                    WHERE f.id_datos_personal =
                          dp.id_datos_personal

                    ORDER BY
                        f.fecha_edicion DESC NULLS LAST,
                        f.fecha_creacion DESC NULLS LAST,
                        f.id_financiero DESC

                    LIMIT 1

                ) financiero
                    ON TRUE

                ORDER BY
                    dp.id_datos_personal
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
    // CONTAR PERSONAS ESPERADAS DESDE FOTO DE CARTERA
    // =========================================================

    public int contarPersonasEsperadas(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM
                (
                    SELECT DISTINCT
                        cc.id_datos_personal

                    FROM cartera.cierres_cartera_creditos cc

                    WHERE cc.id_cierre_cartera =
                          :idCierreCartera

                      AND cc.id_datos_personal
                          IS NOT NULL

                      AND COALESCE(
                              cc.saldo_actual,
                              0
                          ) > 0
                ) x
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
    // CONTAR PERSONAS FOTOGRAFIADAS
    // =========================================================

    public int contarPersonasFotografiadas(
            Integer idCierreHojaVida
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM hoja_vida.cierres_hoja_vida_personas

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
    // ACTUALIZAR RESULTADO DEL PRECierre
    //
    // La fotografía continúa en P.
    // Todavía NO se marca definitiva.
    // =========================================================

    public int actualizarCantidadPersonasPrecierre(
            Integer idCierreHojaVida,
            Integer cantidadPersonas,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE hoja_vida.cierres_hoja_vida

                   SET cantidad_personas =
                           :cantidadPersonas,

                       fecha_fotografia =
                           CURRENT_TIMESTAMP,

                       fk_seguridad_edicion =
                           :idUsuario,

                       fecha_edicion =
                           CURRENT_TIMESTAMP

                 WHERE id_cierre_hoja_vida =
                       :idCierreHojaVida

                   AND estado =
                       'P'
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreHojaVida",
                                idCierreHojaVida
                        )
                        .addValue(
                                "cantidadPersonas",
                                cantidadPersonas
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
    // MARCAR FOTOGRAFÍA DE HOJA DE VIDA COMO DEFINITIVA
    //
    // P -> D
    //
    // Debe llamarse únicamente cuando la fotografía
    // de cartera pase de P -> C.
    // =========================================================

    public int finalizarDefinitivo(
            Integer idCierreHojaVida,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE hoja_vida.cierres_hoja_vida

                   SET estado =
                           'D',

                       fk_seguridad_edicion =
                           :idUsuario,

                       fecha_edicion =
                           CURRENT_TIMESTAMP

                 WHERE id_cierre_hoja_vida =
                       :idCierreHojaVida

                   AND estado =
                       'P'
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
}