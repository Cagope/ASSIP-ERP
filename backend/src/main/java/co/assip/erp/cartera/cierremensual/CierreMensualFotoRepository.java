package co.assip.erp.cartera.cierremensual;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CierreMensualFotoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // VALIDAR SI EXISTE FOTO
    // =========================================================

    public boolean existeFoto(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1
                    FROM cartera.cierres_cartera_creditos c
                    WHERE c.id_cierre_cartera =
                          :idCierreCartera
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Boolean.class
                )
        );
    }

    // =========================================================
    // GENERAR FOTO DE CRÉDITOS
    //
    // IMPORTANTE:
    // - La foto queda autosuficiente.
    // - Se conservan IDs solamente para trazabilidad.
    // - Se congelan persona y descripciones de catálogos.
    // - Aquí NO se calculan mora, edades, aportes,
    //   garantías ni VEA.
    // =========================================================

    public int generarFotoCreditos(
            Integer idCierreCartera,
            Integer idUsuario
    ){

        String sql = """
                INSERT INTO cartera.cierres_cartera_creditos
                (
                    id_cierre_cartera,
                    id_cartera_credito,

                    id_agencia,

                    id_linea_credito,
                    codigo_linea_credito,
                    nombre_linea_credito,

                    pagare_cartera,
                    
                    id_agencia_juridica,
                    id_linea_credito_juridica,
                    pagare_juridico,

                    id_datos_personal,
                    tipo_documento,
                    documento,
                    nombres,
                    primer_apellido,
                    segundo_apellido,

                    id_cuenta_aportes,

                    tipo_comprobante,
                    numero_comprobante,

                    codigo_clasificacion_credito,
                    descripcion_clasificacion_credito,

                    codigo_garantia_credito,
                    descripcion_garantia_credito,
                    tipo_garantia,

                    codigo_subgarantia,
                    descripcion_subgarantia,

                    codigo_destino_economico,
                    descripcion_destino_economico,

                    periodo_codigo_interes,
                    tipo_modalidad_interes,
                    descripcion_modalidad_interes,
                    periodo_meses_interes,

                    amortizacion_capital,

                    codigo_tipo_cuota,
                    descripcion_tipo_cuota,

                    plazo,
                    meses_gracia_capital,
                    meses_gracia_interes,

                    codigo_forma_pago,
                    descripcion_forma_pago,

                    id_empresa_libranza,
                    documento_empresa_libranza,

                    id_ente_aprobacion,
                    nombre_ente_aprobacion,

                    codigo_estado_cartera,
                    descripcion_estado_cartera,

                    codigo_estado_juridico,
                    descripcion_estado_juridico,
                    fecha_estado_juridico,

                    valor_inicial_credito,
                    valor_desembolsado,
                    valor_base_calculo_cuota,
                    valor_primera_cuota,
                    valor_cuota,
                    saldo_actual,
                    abonos_pendientes,
                    altura_cuota,

                    tasa_nominal_anual,
                    tasa_efectiva_anual,

                    fecha_inclusion_sistema,
                    fecha_contable,
                    fecha_desembolso,
                    fecha_primera_cuota,
                    fecha_primera_cuota_capital,
                    fecha_primera_cuota_interes,
                    fecha_final,

                    ultima_fecha_capital,
                    ultima_fecha_interes,
                    ultima_fecha_mora,
                    ultima_fecha_seguro,
                    ultima_fecha_fondo,

                    proxima_fecha_capital,
                    proxima_fecha_interes,
                    proxima_fecha_seguro,
                    proxima_fecha_fondo,

                    intereses_pagados_hasta,
                    intereses_mora_hasta,

                    credito_evaluado,
                    fecha_evaluacion,
                    edad_de_riesgo,
                    edad_riesgo_inicial,
                    edad_de_mora,
                    edad_de_pe,
                    edad_de_homologacion,
                    edad_contable,
                    comentario_evaluacion,

                    codigo_modificacion_credito,
                    descripcion_modificacion_credito,

                    numero_novaciones,

                    credito_reestructurado,
                    fecha_reestructuracion,
                    edad_reestructuracion_inicial,
                    edad_reestructurado,
                    comentario_restructuracion,

                    ultima_fecha_cifin,
                    ultima_fecha_datacredito,
                    ultima_fecha_otra,

                    establecimiento,
                    comentario_general,

                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                SELECT
                    :idCierreCartera,
                    c.id_cartera_credito,

                    c.id_agencia,

                    c.id_linea_credito,
                    lc.codigo_linea_credito,
                    lc.nombre_linea_credito,

                    c.pagare_cartera,
                    
                    oj.id_agencia,
                    oj.id_linea_credito,
                    oj.numero_pagare,

                    c.id_datos_personal,

                    hv.tipo_documento,
                    hv.documento,
                    hv.nombres,
                    hv.primer_apellido,
                    hv.segundo_apellido,

                    c.id_cuenta_aportes,

                    c.tipo_comprobante,
                    c.numero_comprobante,

                    c.codigo_clasificacion_credito,
                    cla.descripcion_clasificacion_credito,

                    c.codigo_garantia_credito,
                    gar.descripcion_garantia_credito,
                    gar.tipo_garantia,

                    c.codigo_subgarantia,
                    sub.descripcion_subgarantia,

                    c.codigo_destino_economico,
                    des.descripcion_destino_economico,

                    c.periodo_codigo_interes,
                    c.tipo_modalidad_interes,
                    mi.descripcion_modalidad_interes,
                    mi.periodo_meses,

                    c.amortizacion_capital,

                    c.codigo_tipo_cuota,
                    tc.descripcion_tipo_cuota,

                    c.plazo,
                    c.meses_gracia_capital,
                    c.meses_gracia_interes,

                    c.codigo_forma_pago,
                    fp.descripcion_forma_pago,

                    c.id_empresa_libranza,
                    NULLIF(
                        TRIM(el.documento),
                        ''
                    ),

                    c.id_ente_aprobacion,
                    ea.nombre_ente_aprobacion,

                    c.codigo_estado_cartera,
                    ec.descripcion_estado_cartera,

                    c.codigo_estado_juridico,
                    ej.descripcion_estado_juridico,
                    c.fecha_estado_juridico,

                    c.valor_inicial_credito,
                    c.valor_desembolsado,
                    c.valor_base_calculo_cuota,
                    c.valor_primera_cuota,
                    c.valor_cuota,
                    c.saldo_actual,
                    c.abonos_pendientes,
                    c.altura_cuota,

                    c.tasa_nominal_anual,
                    c.tasa_efectiva_anual,

                    c.fecha_inclusion_sistema,
                    c.fecha_contable,
                    c.fecha_desembolso,
                    c.fecha_primera_cuota,
                    c.fecha_primera_cuota_capital,
                    c.fecha_primera_cuota_interes,
                    c.fecha_final,

                    c.ultima_fecha_capital,
                    c.ultima_fecha_interes,
                    c.ultima_fecha_mora,
                    c.ultima_fecha_seguro,
                    c.ultima_fecha_fondo,

                    c.proxima_fecha_capital,
                    c.proxima_fecha_interes,
                    c.proxima_fecha_seguro,
                    c.proxima_fecha_fondo,

                    c.intereses_pagados_hasta,
                    c.intereses_mora_hasta,

                    c.credito_evaluado,
                    c.fecha_evaluacion,
                    c.edad_de_riesgo,
                    c.edad_riesgo_inicial,
                    c.edad_de_mora,
                    c.edad_de_pe,
                    c.edad_de_homologacion,
                    c.edad_contable,
                    c.comentario_evaluacion,

                    c.codigo_modificacion_credito,
                    mc.descripcion_modificacion_credito,

                    c.numero_novaciones,

                    c.credito_reestructurado,
                    c.fecha_reestructuracion,
                    c.edad_reestructuracion_inicial,
                    c.edad_reestructurado,
                    c.comentario_restructuracion,

                    c.ultima_fecha_cifin,
                    c.ultima_fecha_datacredito,
                    c.ultima_fecha_otra,

                    c.establecimiento,
                    c.comentario_general,

                    :idUsuario,
                    :idUsuario

                FROM cartera.carteras_creditos c
                
                LEFT JOIN cartera.obligaciones_juridicas oj
                    ON oj.id_obligacion_juridica =
                       c.id_obligacion_juridica

                LEFT JOIN cartera.lineas_creditos lc
                    ON lc.id_linea_credito =
                       c.id_linea_credito

                LEFT JOIN cartera.clasificaciones_creditos cla
                    ON cla.codigo_clasificacion_credito =
                       c.codigo_clasificacion_credito

                LEFT JOIN cartera.garantias_creditos gar
                    ON gar.codigo_garantia_credito =
                       c.codigo_garantia_credito

                LEFT JOIN cartera.subgarantias_creditos sub
                    ON sub.codigo_subgarantia =
                       c.codigo_subgarantia

                LEFT JOIN cartera.destinos_economicos des
                    ON des.codigo_destino_economico =
                       c.codigo_destino_economico

                LEFT JOIN cartera.modalidades_intereses mi
                    ON mi.periodo_codigo =
                       c.periodo_codigo_interes
                   AND mi.tipo_modalidad =
                       c.tipo_modalidad_interes

                LEFT JOIN cartera.tipos_cuotas tc
                    ON tc.codigo_tipo_cuota =
                       c.codigo_tipo_cuota

                LEFT JOIN cartera.formas_pago fp
                    ON fp.codigo_forma_pago =
                       c.codigo_forma_pago

                LEFT JOIN cartera.estados_cartera ec
                    ON ec.codigo_estado_cartera =
                       c.codigo_estado_cartera

                LEFT JOIN cartera.estados_juridicos ej
                    ON ej.codigo_estado_juridico =
                       c.codigo_estado_juridico

                LEFT JOIN cartera.modificaciones_creditos mc
                    ON mc.codigo_modificacion_credito =
                       c.codigo_modificacion_credito

                LEFT JOIN cartera.entes_aprobacion ea
                    ON ea.id_ente_aprobacion =
                       c.id_ente_aprobacion

                LEFT JOIN depositos.empresas_libranza el
                    ON el.id_empresa_libranza =
                       c.id_empresa_libranza

                                LEFT JOIN LATERAL
                (
                    SELECT
                        hv1.tipo_documento,
                        hv1.documento,
                        hv1.nombres,
                        hv1.primer_apellido,
                        hv1.segundo_apellido

                    FROM reporting.vw_hoja_vida_general_total_reciente hv1

                    WHERE hv1.id_datos_personal =
                          c.id_datos_personal

                    LIMIT 1
                ) hv
                    ON true

                ORDER BY
                    c.id_cartera_credito
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
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

    /// =========================================================
    // CREAR BASE DE CÁLCULOS
    //
    // IMPORTANTE:
    // - solamente créditos fotografiados con saldo_actual > 0.
    // - se crea una fila por crédito activo.
    // - aquí todavía NO se ejecutan cálculos.
    // =========================================================

    public int crearResultadosBase(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.cierres_cartera_resultados
                (
                    id_cierre_cartera,
                    id_cierre_cartera_credito,

                    saldo_actual,

                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                SELECT
                    f.id_cierre_cartera,
                    f.id_cierre_cartera_credito,

                    COALESCE(
                        f.saldo_actual,
                        0
                    ),

                    :idUsuario,
                    :idUsuario

                FROM cartera.cierres_cartera_creditos f
                
                      WHERE f.id_cierre_cartera =
                            :idCierreCartera

                        AND COALESCE(
                              f.saldo_actual,
                              0
                            ) > 0

                  AND NOT EXISTS
                  (
                      SELECT 1

                      FROM cartera.cierres_cartera_resultados r

                      WHERE r.id_cierre_cartera_credito =
                            f.id_cierre_cartera_credito
                  )

                ORDER BY
                    f.id_cierre_cartera_credito
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
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
    // CONTAR FOTO
    // =========================================================

    public int contarCreditosFoto(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_creditos f
                WHERE f.id_cierre_cartera =
                      :idCierreCartera
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
    // CONTAR BASE DE CÁLCULOS
    // =========================================================

    public int contarResultadosBase(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_resultados r
                WHERE r.id_cierre_cartera =
                      :idCierreCartera
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
// ELIMINAR GARANTÍAS DE RESULTADOS
//
// Debe ejecutarse antes de eliminar la base de resultados,
// porque cierres_cartera_resultados_garantias depende de
// cierres_cartera_resultados.
// =========================================================

    public int eliminarResultadosGarantias(
            Integer idCierreCartera
    ) {

        String sql = """
            DELETE FROM cartera.cierres_cartera_resultados_garantias g
            USING cartera.cierres_cartera_resultados r
            WHERE r.id_cierre_cartera_resultado =
                  g.id_cierre_cartera_resultado
              AND r.id_cierre_cartera =
                  :idCierreCartera
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // ELIMINAR BASE DE CÁLCULOS
    //
    // Para regenerar una foto.
    // Los procesos posteriores deberán eliminar primero sus
    // propias tablas hijas antes de llegar aquí.
    // =========================================================

    public int eliminarResultadosBase(
            Integer idCierreCartera
    ) {

        String sql = """
                DELETE
                FROM cartera.cierres_cartera_resultados
                WHERE id_cierre_cartera =
                      :idCierreCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // ELIMINAR FOTO
    // =========================================================

    public int eliminarFotoCreditos(
            Integer idCierreCartera
    ) {

        String sql = """
                DELETE
                FROM cartera.cierres_cartera_creditos
                WHERE id_cierre_cartera =
                      :idCierreCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
// OBTENER SALDO TOTAL DE CARTERA ACTIVA EN LA FOTO
// =========================================================

    public java.math.BigDecimal obtenerSaldoCarteraFoto(
            Integer idCierreCartera
    ) {

        String sql = """
        SELECT
            COALESCE(
                SUM(f.saldo_actual),
                0
            )
        FROM cartera.cierres_cartera_creditos f
        WHERE f.id_cierre_cartera =
              :idCierreCartera
          AND COALESCE(f.saldo_actual, 0) > 0
        """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        java.math.BigDecimal saldo =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        java.math.BigDecimal.class
                );

        return saldo != null
                ? saldo
                : java.math.BigDecimal.ZERO;
    }

    // =========================================================
    // CONTAR CRÉDITOS DE LA FOTO CON SALDO
    // =========================================================

    public int contarCreditosFotoConSaldo(
            Integer idCierreCartera
    ) {

        String sql = """
        SELECT COUNT(*)
        FROM cartera.cierres_cartera_creditos f
        WHERE f.id_cierre_cartera = :idCierreCartera
          AND COALESCE(f.saldo_actual, 0) > 0
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

}