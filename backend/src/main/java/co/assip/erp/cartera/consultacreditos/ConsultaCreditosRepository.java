package co.assip.erp.cartera.consultacreditos;

import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoAlivioDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoDetalleDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoEvaluacionDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoExtractoDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoInteresDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoResumenDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoSeguroDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoResultadoMensualDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoProrrogaDTO;

/**
 * Repositorio de consulta especializada de créditos.
 *
 * Características:
 *
 * - Solo lectura.
 * - Incluye créditos vigentes y cancelados.
 * - No filtra por saldo.
 * - No recalcula saldos, intereses, seguros ni riesgos.
 * - Consulta directamente las vistas consolidadas de cartera.
 */
@Repository
public class ConsultaCreditosRepository {

    // =========================================================
    // Parámetros
    // =========================================================

    private static final String PARAM_ID_DATOS_PERSONAL =
            "idDatosPersonal";

    private static final String PARAM_ID_CARTERA_CREDITO =
            "idCarteraCredito";

    // =========================================================
    // SQL: lista de créditos del asociado
    // =========================================================

    private static final String SQL_LISTAR_CREDITOS_PERSONA = """
            SELECT
                c.id_cartera_credito,
                c.pagare_cartera,

                c.id_linea_credito,
                c.codigo_linea_credito,
                c.nombre_linea_credito
                    AS descripcion_linea_credito,

                c.codigo_estado_cartera,
                c.descripcion_estado_cartera,

                c.codigo_estado_juridico,
                c.descripcion_estado_juridico,

                c.fecha_desembolso,
                c.fecha_final,
                c.proxima_fecha_capital,

                c.valor_desembolsado,
                c.saldo_actual,

                c.edad_de_riesgo,
                c.descripcion_edad_de_riesgo,

                c.edad_de_mora,
                c.descripcion_edad_de_mora,

                COALESCE(
                    c.credito_saldado,
                    false
                ) AS credito_saldado,

                CASE
                     WHEN COALESCE(
                         c.credito_saldado,
                         false
                     ) = false
                     AND COALESCE(
                         c.saldo_actual,
                         0
                     ) > 0
                     AND c.proxima_fecha_capital IS NOT NULL
                     AND c.proxima_fecha_capital < CURRENT_DATE
                     THEN true
                     ELSE false
                END AS credito_en_mora,

                COALESCE(
                    c.riesgo_alto,
                    false
                ) AS riesgo_alto,

                COALESCE(
                    c.requiere_revision,
                    false
                ) AS requiere_revision

            FROM cartera.vw_cartera_creditos_total c

            WHERE c.id_datos_personal = :idDatosPersonal

            ORDER BY
                COALESCE(
                    c.credito_saldado,
                    false
                ),
                c.fecha_desembolso DESC NULLS LAST,
                c.id_cartera_credito DESC
            """;

    // =========================================================
    // SQL: detalle del crédito
    // =========================================================

    private static final String SQL_BUSCAR_CREDITO = """
            SELECT
                c.*
            FROM cartera.vw_cartera_creditos_total c
            WHERE c.id_cartera_credito = :idCarteraCredito
            """;

    // =========================================================
    // SQL: extracto
    // =========================================================

    private static final String SQL_LISTAR_EXTRACTO = """
            SELECT
                e.*
            FROM cartera.vw_cartera_extractos_total e
            WHERE e.id_cartera_credito = :idCarteraCredito
            ORDER BY
                e.fecha_pago DESC NULLS LAST,
                e.hora DESC NULLS LAST,
                e.id_extracto_cartera DESC
            """;

    // =========================================================
    // SQL: seguros
    // =========================================================

    private static final String SQL_LISTAR_SEGUROS = """
            SELECT
                s.*
            FROM cartera.vw_cartera_seguros_total s
            WHERE s.id_cartera_credito = :idCarteraCredito
            ORDER BY
                COALESCE(
                    s.configuracion_seguro_principal,
                    false
                ) DESC,
                COALESCE(
                    s.seguro_activo,
                    false
                ) DESC,
                s.numero_configuracion_seguro,
                s.id_credito_seguro DESC
            """;


    // =========================================================
    // SQL: movimientos de seguros
    // =========================================================

    private static final String SQL_LISTAR_MOVIMIENTOS_SEGURO = """
        SELECT
            m.*
        FROM cartera.vw_cartera_seguros_movimientos_total m
        WHERE m.id_credito_seguro = :idCreditoSeguro
        ORDER BY
            m.fecha_movimiento ASC NULLS LAST,
            m.id_credito_seguro_detalle ASC
        """;

    // =========================================================
    // SQL: alivios
    // =========================================================

    private static final String SQL_LISTAR_ALIVIOS = """
            SELECT
                a.*
            FROM cartera.vw_cartera_alivios_total a
            WHERE a.id_cartera_credito = :idCarteraCredito
            ORDER BY
                a.fecha_inicial_alivio DESC NULLS LAST,
                a.numero_periodo NULLS FIRST,
                a.id_credito_alivio DESC,
                a.id_credito_alivio_detalle
            """;

    // =========================================================
    // SQL: intereses causados
    // =========================================================

    private static final String SQL_LISTAR_INTERESES = """
            SELECT
                i.*
            FROM cartera.vw_cartera_intereses_causados_total i
            WHERE i.id_cartera_credito = :idCarteraCredito
            ORDER BY
                i.fecha_inicial_periodo DESC NULLS LAST,
                i.fecha_final_periodo DESC NULLS LAST,
                i.id_interes_causado DESC
            """;

    // =========================================================
    // SQL: evaluaciones
    // =========================================================

    private static final String SQL_LISTAR_EVALUACIONES = """
            SELECT
                e.*
            FROM cartera.vw_cartera_evaluaciones_total e
            WHERE e.id_cartera_credito = :idCarteraCredito
            ORDER BY
                e.fecha_evaluacion DESC NULLS LAST,
                e.numero_evaluacion_credito DESC NULLS LAST,
                e.id_evaluacion_cartera DESC
            """;

    // =========================================================
    // SQL: última evaluación
    // =========================================================

    private static final String SQL_BUSCAR_ULTIMA_EVALUACION = """
            SELECT
                e.*
            FROM cartera.vw_cartera_evaluaciones_total e
            WHERE e.id_cartera_credito = :idCarteraCredito
            ORDER BY
                COALESCE(
                    e.ultima_evaluacion_credito,
                    false
                ) DESC,
                e.fecha_evaluacion DESC NULLS LAST,
                e.numero_evaluacion_credito DESC NULLS LAST,
                e.id_evaluacion_cartera DESC
            LIMIT 1
            """;

    // =========================================================
    // SQL: prórrogas
    // =========================================================

    private static final String SQL_LISTAR_PRORROGAS = """
        SELECT
            p.*
        FROM cartera.vw_cartera_prorrogas_total p
        WHERE p.id_cartera_credito = :idCarteraCredito
        ORDER BY
            p.numero_prorroga DESC,
            p.fecha_prorroga DESC NULLS LAST,
            p.id_credito_prorroga DESC
        """;

    // =========================================================
    // SQL: resultados mensuales
    // =========================================================

    private static final String SQL_LISTAR_RESULTADOS_MENSUALES = """
        SELECT
            r.*
        FROM cartera.vw_cartera_resultados_mensuales_total r
        WHERE r.id_cartera_credito = :idCarteraCredito
        ORDER BY
            r.fecha_corte DESC,
            r.id_cierre_cartera DESC
        """;

    // =========================================================
    // Dependencia
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultaCreditosRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // Mapeadores
    // =========================================================

    private static final BeanPropertyRowMapper<ConsultaCreditoResumenDTO>
            RESUMEN_MAPPER =
            crearMapper(
                    ConsultaCreditoResumenDTO.class
            );

    private static final BeanPropertyRowMapper<ConsultaCreditoDetalleDTO>
            DETALLE_MAPPER =
            crearMapper(
                    ConsultaCreditoDetalleDTO.class
            );

    private static final BeanPropertyRowMapper<ConsultaCreditoExtractoDTO>
            EXTRACTO_MAPPER =
            crearMapper(
                    ConsultaCreditoExtractoDTO.class
            );

    private static final BeanPropertyRowMapper<ConsultaCreditoSeguroDTO>
            SEGURO_MAPPER =
            crearMapper(
                    ConsultaCreditoSeguroDTO.class
            );

    private static final BeanPropertyRowMapper<
            ConsultaCreditoSeguroDTO.MovimientoSeguroDTO>
            MOVIMIENTO_SEGURO_MAPPER =
            crearMapper(
                    ConsultaCreditoSeguroDTO.MovimientoSeguroDTO.class
            );

    private static final BeanPropertyRowMapper<ConsultaCreditoAlivioDTO>
            ALIVIO_MAPPER =
            crearMapper(
                    ConsultaCreditoAlivioDTO.class
            );

    private static final BeanPropertyRowMapper<ConsultaCreditoInteresDTO>
            INTERES_MAPPER =
            crearMapper(
                    ConsultaCreditoInteresDTO.class
            );

    private static final BeanPropertyRowMapper<ConsultaCreditoEvaluacionDTO>
            EVALUACION_MAPPER =
            crearMapper(
                    ConsultaCreditoEvaluacionDTO.class
            );

    private static final BeanPropertyRowMapper<ConsultaCreditoProrrogaDTO>
            PRORROGA_MAPPER =
            crearMapper(
                    ConsultaCreditoProrrogaDTO.class
            );

    private static final BeanPropertyRowMapper<ConsultaCreditoResultadoMensualDTO>
            RESULTADO_MENSUAL_MAPPER =
            crearMapper(
                    ConsultaCreditoResultadoMensualDTO.class
            );

    private static <T> BeanPropertyRowMapper<T> crearMapper(
            Class<T> tipo
    ) {

        BeanPropertyRowMapper<T> mapper =
                BeanPropertyRowMapper.newInstance(
                        tipo
                );

        /*
         * Los DTO contienen exclusivamente información funcional.
         * Las vistas pueden tener columnas adicionales de validación,
         * auditoría o diagnóstico que no necesitan exponerse.
         */
        mapper.setCheckFullyPopulated(false);

        /*
         * Permite mapear valores nulos hacia propiedades envolventes
         * sin forzar valores primitivos.
         */
        mapper.setPrimitivesDefaultedForNullValue(true);

        return mapper;
    }

    // =========================================================
    // Lista de créditos del asociado
    // =========================================================

    /**
     * Lista todos los créditos asociados a una persona.
     *
     * Incluye:
     *
     * - Créditos vigentes.
     * - Créditos con saldo.
     * - Créditos saldados.
     * - Créditos cancelados.
     * - Créditos jurídicos.
     *
     * No se aplica filtro por saldo.
     */
    public List<ConsultaCreditoResumenDTO> listarPorPersona(
            Integer idDatosPersonal
    ) {

        validarIdDatosPersonal(
                idDatosPersonal
        );

        return jdbc.query(
                SQL_LISTAR_CREDITOS_PERSONA,
                parametrosPersona(
                        idDatosPersonal
                ),
                RESUMEN_MAPPER
        );
    }

    // =========================================================
    // Detalle
    // =========================================================

    public Optional<ConsultaCreditoDetalleDTO> buscarPorId(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        List<ConsultaCreditoDetalleDTO> resultados =
                jdbc.query(
                        SQL_BUSCAR_CREDITO,
                        parametrosCredito(
                                idCarteraCredito
                        ),
                        DETALLE_MAPPER
                );

        return primerResultado(
                resultados
        );
    }

    // =========================================================
    // Extracto
    // =========================================================

    public List<ConsultaCreditoExtractoDTO> listarExtracto(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return jdbc.query(
                SQL_LISTAR_EXTRACTO,
                parametrosCredito(
                        idCarteraCredito
                ),
                EXTRACTO_MAPPER
        );
    }

    // =========================================================
    // Seguros
    // =========================================================

    public List<ConsultaCreditoSeguroDTO> listarSeguros(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        List<ConsultaCreditoSeguroDTO> seguros =
                jdbc.query(
                        SQL_LISTAR_SEGUROS,
                        parametrosCredito(
                                idCarteraCredito
                        ),
                        SEGURO_MAPPER
                );

        for (ConsultaCreditoSeguroDTO seguro : seguros) {

            if (seguro.getIdCreditoSeguro() == null) {
                continue;
            }

            List<ConsultaCreditoSeguroDTO.MovimientoSeguroDTO> movimientos =
                    jdbc.query(
                            SQL_LISTAR_MOVIMIENTOS_SEGURO,
                            new MapSqlParameterSource()
                                    .addValue(
                                            "idCreditoSeguro",
                                            seguro.getIdCreditoSeguro()
                                    ),
                            MOVIMIENTO_SEGURO_MAPPER
                    );

            seguro.setMovimientos(
                    movimientos
            );
        }

        return seguros;
    }

    // =========================================================
    // Alivios
    // =========================================================

    public List<ConsultaCreditoAlivioDTO> listarAlivios(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return jdbc.query(
                SQL_LISTAR_ALIVIOS,
                parametrosCredito(
                        idCarteraCredito
                ),
                ALIVIO_MAPPER
        );
    }

    // =========================================================
    // Intereses causados
    // =========================================================

    public List<ConsultaCreditoInteresDTO> listarInteresesCausados(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return jdbc.query(
                SQL_LISTAR_INTERESES,
                parametrosCredito(
                        idCarteraCredito
                ),
                INTERES_MAPPER
        );
    }

    // =========================================================
    // Prórrogas
    // =========================================================

    public List<ConsultaCreditoProrrogaDTO> listarProrrogas(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return jdbc.query(
                SQL_LISTAR_PRORROGAS,
                parametrosCredito(
                        idCarteraCredito
                ),
                PRORROGA_MAPPER
        );
    }

    // =========================================================
    // Evaluaciones
    // =========================================================

    public List<ConsultaCreditoEvaluacionDTO> listarEvaluaciones(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return jdbc.query(
                SQL_LISTAR_EVALUACIONES,
                parametrosCredito(
                        idCarteraCredito
                ),
                EVALUACION_MAPPER
        );
    }

    // =========================================================
    // Resultados mensuales de cartera
    // =========================================================

    public List<ConsultaCreditoResultadoMensualDTO> listarResultadosMensuales(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return jdbc.query(
                SQL_LISTAR_RESULTADOS_MENSUALES,
                parametrosCredito(
                        idCarteraCredito
                ),
                RESULTADO_MENSUAL_MAPPER
        );
    }


    public Optional<ConsultaCreditoEvaluacionDTO> buscarUltimaEvaluacion(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        List<ConsultaCreditoEvaluacionDTO> resultados =
                jdbc.query(
                        SQL_BUSCAR_ULTIMA_EVALUACION,
                        parametrosCredito(
                                idCarteraCredito
                        ),
                        EVALUACION_MAPPER
                );

        return primerResultado(
                resultados
        );
    }

    // =========================================================
    // Existencia
    // =========================================================

    public boolean existeCredito(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM cartera.vw_cartera_creditos_total c
                    WHERE c.id_cartera_credito = :idCarteraCredito
                )
                """;

        Boolean existe =
                jdbc.queryForObject(
                        sql,
                        parametrosCredito(
                                idCarteraCredito
                        ),
                        Boolean.class
                );

        return Boolean.TRUE.equals(
                existe
        );
    }

    // =========================================================
    // Parámetros
    // =========================================================

    private MapSqlParameterSource parametrosPersona(
            Integer idDatosPersonal
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        PARAM_ID_DATOS_PERSONAL,
                        idDatosPersonal
                );
    }

    private MapSqlParameterSource parametrosCredito(
            Integer idCarteraCredito
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        PARAM_ID_CARTERA_CREDITO,
                        idCarteraCredito
                );
    }

    // =========================================================
    // Utilidades
    // =========================================================

    private <T> Optional<T> primerResultado(
            List<T> resultados
    ) {

        if (
                resultados == null
                        || resultados.isEmpty()
        ) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                resultados.get(0)
        );
    }

    // =========================================================
    // Validaciones
    // =========================================================

    private void validarIdDatosPersonal(
            Integer idDatosPersonal
    ) {

        if (
                idDatosPersonal == null
                        || idDatosPersonal <= 0
        ) {
            throw new IllegalArgumentException(
                    "El idDatosPersonal debe ser mayor que cero."
            );
        }
    }

    private void validarIdCarteraCredito(
            Integer idCarteraCredito
    ) {

        if (
                idCarteraCredito == null
                        || idCarteraCredito <= 0
        ) {
            throw new IllegalArgumentException(
                    "El idCarteraCredito debe ser mayor que cero."
            );
        }
    }
}