package co.assip.erp.cartera.analisis.vectorcomportamiento.actual;

import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto.VectorComportamientoDetalleDTO;
import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto.VectorComportamientoResumenDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Repositorio del proceso de análisis
 * Vector de Comportamiento Actual.
 *
 * El proceso trabaja exclusivamente con créditos
 * que actualmente tienen saldo mayor que cero.
 *
 * Para cada crédito el vector contiene:
 *
 * - Una posición ACTUAL.
 * - Hasta 12 cierres históricos mensuales.
 *
 * Por lo tanto, un crédito puede tener como máximo
 * 13 posiciones dentro del Vector de Comportamiento Actual.
 *
 * La información proviene de:
 *
 * - cartera.vw_cartera_vector_comportamiento
 * - cartera.vw_cartera_vector_comportamiento_detalle
 *
 * Toda la construcción funcional del vector se resuelve
 * en las vistas de base de datos.
 *
 * Este repositorio es exclusivamente de lectura.
 */
@Repository
public class VectorComportamientoActualRepository {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<VectorComportamientoResumenDTO>
            RESUMEN_MAPPER =
            crearMapper(
                    VectorComportamientoResumenDTO.class
            );

    private static final BeanPropertyRowMapper<VectorComportamientoDetalleDTO>
            DETALLE_MAPPER =
            crearMapper(
                    VectorComportamientoDetalleDTO.class
            );


    // =========================================================
    // SQL BASE RESUMEN
    // =========================================================

    private static final String SQL_RESUMEN_BASE = """
            SELECT
                v.id_cartera_credito,
                v.id_agencia,
                v.id_linea_credito,
                v.codigo_linea_credito,
                v.nombre_linea_credito,
                v.pagare_cartera,
                v.id_datos_personal,
                v.tipo_documento,
                v.documento,
                v.nombre_completo,

                v.telefono,
                v.celular,
                v.correo,

                v.primer_corte,
                v.ultimo_corte,
                v.cantidad_cortes_observados,

                v.mora_ultimo_corte,
                v.mora_maxima,
                v.cantidad_cortes_al_dia,
                v.cantidad_cortes_con_mora,

                v.cantidad_mora_1_30
                    AS cantidadMora1_30,

                v.cantidad_mora_31_60
                    AS cantidadMora31_60,

                v.cantidad_mora_61_90
                    AS cantidadMora61_90,

                v.cantidad_mora_91_120
                    AS cantidadMora91_120,

                v.cantidad_mora_121_150
                    AS cantidadMora121_150,

                v.cantidad_mora_151_180
                    AS cantidadMora151_180,

                v.cantidad_mora_181_360
                    AS cantidadMora181_360,

                v.cantidad_mora_mayor_360
                    AS cantidadMoraMayor360,

                v.pbb_mora,

                v.fecha_desembolso,
                v.valor_inicial_credito,
                v.valor_desembolsado,

                v.saldo_primer_corte,
                v.saldo_ultimo_corte,
                v.variacion_saldo_periodo,
                v.saldo_actual_maestro,

                v.severidad,
                v.rango_severidad,

                v.codigo_estado_cartera,
                v.descripcion_estado_cartera,

                v.codigo_estado_juridico,
                v.descripcion_estado_juridico,

                v.codigo_clasificacion_credito,
                v.descripcion_clasificacion_credito,

                v.edad_riesgo_inicial_resultado,
                v.edad_de_mora_resultado,
                v.edad_de_riesgo_resultado,
                v.edad_de_pe_resultado,
                v.edad_de_homologacion_resultado,
                v.edad_contable_resultado,

                v.codigo_metodo_calculo,

                v.vea,
                v.pi,
                v.pdi,
                v.perdida_esperada,

                v.deterioro_capital,
                v.deterioro_intereses,
                v.deterioro_otros,
                v.deterioro_total,

                v.codigo_estado_cartera_actual,
                v.credito_activo_actual,

                v.tuvo_mora_periodo,
                v.esta_en_mora_ultimo_corte,
                v.tuvo_mora_mayor_90,
                v.tiene_historia,

                v.id_cierre_cartera,
                v.id_cierre_cartera_credito,
                v.id_cierre_cartera_resultado

            FROM cartera.vw_cartera_vector_comportamiento v
            """;


    // =========================================================
    // SQL BASE DETALLE
    // =========================================================

    private static final String SQL_DETALLE_BASE = """
            SELECT

                -- =================================================
                -- POSICIÓN DEL VECTOR
                -- =================================================

                d.posicion_vector,
                d.tipo_posicion,
                d.periodo_vector,
                d.fecha_referencia,

                -- =================================================
                -- IDENTIFICACIÓN
                -- =================================================

                d.id_cartera_credito,
                d.id_agencia,
                d.id_linea_credito,
                d.codigo_linea_credito,
                d.nombre_linea_credito,
                d.pagare_cartera,
                d.id_datos_personal,
                d.tipo_documento,
                d.documento,
                d.nombre_completo,

                -- =================================================
                -- CORTE HISTÓRICO
                -- =================================================

                d.id_cierre_cartera,
                d.id_cierre_cartera_credito,
                d.id_cierre_cartera_resultado,

                d.fecha_corte,
                d.anio_corte,
                d.mes_corte,
                d.periodo_corte,

                -- =================================================
                -- ESTADO
                -- =================================================

                d.codigo_estado_cartera,
                d.descripcion_estado_cartera,

                d.codigo_estado_juridico,
                d.descripcion_estado_juridico,

                d.codigo_clasificacion_credito,
                d.descripcion_clasificacion_credito,

                -- =================================================
                -- MORA
                -- =================================================

                d.dias_mora,
                d.rango_mora,
                d.orden_rango_mora,
                d.tiene_mora,

                -- =================================================
                -- EDADES
                -- =================================================

                d.edad_riesgo_inicial_resultado,
                d.edad_de_mora_resultado,
                d.edad_de_riesgo_resultado,
                d.edad_de_pe_resultado,
                d.edad_de_homologacion_resultado,
                d.edad_contable_resultado,

                -- =================================================
                -- ORIGINACIÓN
                -- =================================================

                d.valor_inicial_credito,
                d.valor_desembolsado,

                -- =================================================
                -- SALDOS
                -- =================================================

                d.saldo_actual_fotografia,
                d.saldo_actual_resultado,
                d.saldo_credito_fecha_corte,

                -- =================================================
                -- MODELO / PÉRDIDA ESPERADA
                -- =================================================

                d.codigo_metodo_calculo,

                d.vea,
                d.pi,
                d.pdi,
                d.perdida_esperada,

                -- =================================================
                -- DETERIORO
                -- =================================================

                d.deterioro_capital,
                d.deterioro_intereses,
                d.deterioro_otros,
                d.deterioro_total,

                -- =================================================
                -- APORTES
                -- =================================================

                d.saldo_aportes_fecha_corte,
                d.porcentaje_aportes_credito,
                d.valor_aportes_credito,

                -- =================================================
                -- GARANTÍAS
                -- =================================================

                d.cantidad_bienes_garantia,
                d.valor_garantias_total,
                d.porcentaje_garantias_credito,
                d.valor_garantias_credito,

                -- =================================================
                -- ESTADO ACTUAL DEL MAESTRO
                -- =================================================

                d.saldo_actual_maestro,
                d.codigo_estado_cartera_actual,
                d.credito_activo_actual

            FROM cartera.vw_cartera_vector_comportamiento_detalle d
            """;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public VectorComportamientoActualRepository(
            NamedParameterJdbcTemplate jdbc
    ) {

        this.jdbc =
                jdbc;
    }


    // =========================================================
    // CARTERA ACTIVA - RESUMEN
    // =========================================================

    /**
     * Lista el resumen del Vector de Comportamiento
     * para todos los créditos actualmente con saldo.
     *
     * La población ya viene restringida desde la vista
     * a créditos cuyo saldo actual en el maestro
     * es mayor que cero.
     *
     * Los indicadores históricos se calculan sobre
     * un máximo de 12 cierres por crédito.
     */
    public List<VectorComportamientoResumenDTO>
    listarResumenCarteraActiva() {

        String sql = SQL_RESUMEN_BASE + """

                ORDER BY
                    v.id_agencia,
                    v.documento,
                    v.pagare_cartera,
                    v.id_cartera_credito
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                RESUMEN_MAPPER
        );
    }


    // =========================================================
    // ASOCIADO - RESUMEN
    // =========================================================

    /**
     * Lista todos los créditos actualmente con saldo
     * de un asociado, con sus indicadores resumidos.
     */
    public List<VectorComportamientoResumenDTO>
    listarResumenPorPersona(
            Integer idDatosPersonal
    ) {

        String sql = SQL_RESUMEN_BASE + """

                WHERE v.id_datos_personal =
                      :idDatosPersonal

                ORDER BY
                    v.pagare_cartera,
                    v.id_cartera_credito
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        );

        return jdbc.query(
                sql,
                params,
                RESUMEN_MAPPER
        );
    }


    // =========================================================
    // CRÉDITO - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen del Vector de Comportamiento
     * para un crédito específico actualmente con saldo.
     */
    public List<VectorComportamientoResumenDTO>
    listarResumenPorCredito(
            Integer idCarteraCredito
    ) {

        String sql = SQL_RESUMEN_BASE + """

                WHERE v.id_cartera_credito =
                      :idCarteraCredito
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        );

        return jdbc.query(
                sql,
                params,
                RESUMEN_MAPPER
        );
    }


    // =========================================================
    // CARTERA ACTIVA - DETALLE
    // =========================================================

    /**
     * Lista el Vector de Comportamiento detallado
     * de todos los créditos actualmente con saldo.
     *
     * Cada crédito contiene:
     *
     * - Posición 1: estado ACTUAL.
     * - Posiciones 2 a 13: hasta 12 cierres históricos,
     *   ordenados desde el más reciente hacia el más antiguo.
     *
     * Este método será útil para:
     *
     * - Exportación masiva.
     * - Análisis estadístico.
     * - Consulta del Vector de Comportamiento.
     */
    public List<VectorComportamientoDetalleDTO>
    listarDetalleCarteraActiva() {

        String sql = SQL_DETALLE_BASE + """

                ORDER BY
                    d.id_agencia,
                    d.documento,
                    d.id_cartera_credito,
                    d.posicion_vector
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                DETALLE_MAPPER
        );
    }


    // =========================================================
    // ASOCIADO - DETALLE
    // =========================================================

    /**
     * Lista el Vector de Comportamiento detallado
     * de todos los créditos actualmente con saldo
     * de un asociado.
     *
     * Dentro de cada crédito:
     *
     * - ACTUAL aparece primero.
     * - Después aparecen hasta 12 cierres históricos
     *   desde el más reciente hacia el más antiguo.
     */
    public List<VectorComportamientoDetalleDTO>
    listarDetallePorPersona(
            Integer idDatosPersonal
    ) {

        String sql = SQL_DETALLE_BASE + """

                WHERE d.id_datos_personal =
                      :idDatosPersonal

                ORDER BY
                    d.id_cartera_credito,
                    d.posicion_vector
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        );

        return jdbc.query(
                sql,
                params,
                DETALLE_MAPPER
        );
    }


    // =========================================================
    // CRÉDITO - DETALLE
    // =========================================================

    /**
     * Lista el Vector de Comportamiento completo
     * de un crédito actualmente con saldo.
     *
     * El resultado contiene:
     *
     * - ACTUAL en posición 1.
     * - Hasta 12 cierres históricos en posiciones 2 a 13.
     */
    public List<VectorComportamientoDetalleDTO>
    listarDetallePorCredito(
            Integer idCarteraCredito
    ) {

        String sql = SQL_DETALLE_BASE + """

                WHERE d.id_cartera_credito =
                      :idCarteraCredito

                ORDER BY
                    d.posicion_vector
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        );

        return jdbc.query(
                sql,
                params,
                DETALLE_MAPPER
        );
    }


    // =========================================================
    // EXISTENCIA EN VECTOR ACTUAL
    // =========================================================

    /**
     * Verifica si el crédito hace parte de la población
     * del Vector de Comportamiento Actual.
     *
     * La vista únicamente contiene créditos cuyo saldo
     * actual en el maestro es mayor que cero.
     */
    public boolean existeCreditoEnVector(
            Integer idCarteraCredito
    ) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1

                    FROM cartera.vw_cartera_vector_comportamiento v

                    WHERE v.id_cartera_credito =
                          :idCarteraCredito
                )
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        );

        Boolean existe =
                jdbc.queryForObject(
                        sql,
                        params,
                        Boolean.class
                );

        return Boolean.TRUE.equals(
                existe
        );
    }


    // =========================================================
    // UTILIDADES
    // =========================================================

    private static <T>
    BeanPropertyRowMapper<T> crearMapper(
            Class<T> tipo
    ) {

        BeanPropertyRowMapper<T> mapper =
                new BeanPropertyRowMapper<>(
                        tipo
                );

        mapper.setPrimitivesDefaultedForNullValue(
                true
        );

        return mapper;
    }
}