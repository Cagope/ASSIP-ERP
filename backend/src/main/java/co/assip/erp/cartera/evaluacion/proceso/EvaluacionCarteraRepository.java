package co.assip.erp.cartera.evaluacion.proceso;

import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCarteraDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCreditoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EvaluacionCarteraRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // SQL BASE
    // =========================================================

    private static final String SELECT_BASE = """
            SELECT
                e.id_evaluacion_cartera
                    AS idEvaluacionCartera,

                e.fecha_corte
                    AS fechaCorte,

                e.fecha_ejecucion
                    AS fechaEjecucion,

                e.estado,

                e.version_metodologia
                    AS versionMetodologia,

                e.cantidad_creditos
                    AS cantidadCreditos,

                e.cantidad_asociados
                    AS cantidadAsociados,

                e.saldo_total_evaluado
                    AS saldoTotalEvaluado,

                e.cantidad_recalificados
                    AS cantidadRecalificados,

                e.cantidad_habilitados
                    AS cantidadHabilitados,

                e.cantidad_mantenidos
                    AS cantidadMantenidos,

                e.fecha_comite_riesgos
                    AS fechaComiteRiesgos,

                e.numero_acta_riesgos
                    AS numeroActaRiesgos,

                e.fecha_consejo
                    AS fechaConsejo,

                e.numero_acta_consejo
                    AS numeroActaConsejo,

                e.observaciones,

                e.fk_seguridad_creacion
                    AS fkSeguridadCreacion,

                e.fecha_creacion
                    AS fechaCreacion,

                e.fk_seguridad_edicion
                    AS fkSeguridadEdicion,

                e.fecha_edicion
                    AS fechaEdicion

            FROM cartera.evaluaciones_cartera e
            """;

    // =========================================================
    // LISTAR
    // =========================================================

    public List<EvaluacionCarteraDTO> listar() {

        String sql = SELECT_BASE + """
                
                ORDER BY
                    e.fecha_corte DESC,
                    e.id_evaluacion_cartera DESC
                """;

        return jdbc.query(
                sql,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionCarteraDTO.class
                )
        );
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<EvaluacionCarteraDTO> buscarPorId(
            Integer idEvaluacionCartera
    ) {

        String sql = SELECT_BASE + """
                
                WHERE e.id_evaluacion_cartera =
                      :idEvaluacionCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        );

        List<EvaluacionCarteraDTO> resultado =
                jdbc.query(
                        sql,
                        parametros,
                        BeanPropertyRowMapper.newInstance(
                                EvaluacionCarteraDTO.class
                        )
                );

        return resultado.stream().findFirst();
    }

    // =========================================================
    // BUSCAR POR FECHA DE CORTE
    // =========================================================

    public Optional<EvaluacionCarteraDTO> buscarPorFechaCorte(
            LocalDate fechaCorte
    ) {

        String sql = SELECT_BASE + """
                
                WHERE e.fecha_corte =
                      :fechaCorte
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        List<EvaluacionCarteraDTO> resultado =
                jdbc.query(
                        sql,
                        parametros,
                        BeanPropertyRowMapper.newInstance(
                                EvaluacionCarteraDTO.class
                        )
                );

        return resultado.stream().findFirst();
    }

    // =========================================================
    // VALIDAR CIERRES DE CARTERA POR FECHA
    // =========================================================

    public boolean existenCierresPorFecha(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1

                    FROM cartera.cierres_cartera c

                    WHERE c.fecha_corte =
                          :fechaCorte
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
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
// VALIDAR CRÉDITOS EVALUABLES EN LOS CIERRES DE LA FECHA
// Solo créditos con saldo_actual > 0
// =========================================================

    public boolean existenCreditosPorFecha(
            LocalDate fechaCorte
    ) {

        String sql = """
            SELECT EXISTS
            (
                SELECT 1

                FROM cartera.cierres_cartera c

                INNER JOIN cartera.cierres_cartera_creditos cc
                    ON cc.id_cierre_cartera =
                       c.id_cierre_cartera

                WHERE c.fecha_corte =
                      :fechaCorte

                  AND COALESCE(
                          cc.saldo_actual,
                          0
                      ) > 0
            )
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
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
    // VALIDAR EVALUACIÓN POR FECHA
    // =========================================================

    public boolean existeEvaluacionPorFecha(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1

                    FROM cartera.evaluaciones_cartera e

                    WHERE e.fecha_corte =
                          :fechaCorte
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
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
    // CREAR
    // =========================================================

    public Integer crear(
            EvaluacionCarteraDTO dto,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.evaluaciones_cartera
                (
                    fecha_corte,
                    fecha_ejecucion,
                    estado,
                    version_metodologia,
                    cantidad_creditos,
                    cantidad_asociados,
                    saldo_total_evaluado,
                    cantidad_recalificados,
                    cantidad_habilitados,
                    cantidad_mantenidos,
                    fecha_comite_riesgos,
                    numero_acta_riesgos,
                    fecha_consejo,
                    numero_acta_consejo,
                    observaciones,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    :fechaCorte,
                    CURRENT_TIMESTAMP,
                    'P',
                    :versionMetodologia,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    :fechaComiteRiesgos,
                    :numeroActaRiesgos,
                    :fechaConsejo,
                    :numeroActaConsejo,
                    :observaciones,
                    :idUsuario,
                    :idUsuario
                )
                RETURNING id_evaluacion_cartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                dto.getFechaCorte()
                        )
                        .addValue(
                                "versionMetodologia",
                                dto.getVersionMetodologia()
                        )
                        .addValue(
                                "fechaComiteRiesgos",
                                dto.getFechaComiteRiesgos()
                        )
                        .addValue(
                                "numeroActaRiesgos",
                                dto.getNumeroActaRiesgos()
                        )
                        .addValue(
                                "fechaConsejo",
                                dto.getFechaConsejo()
                        )
                        .addValue(
                                "numeroActaConsejo",
                                dto.getNumeroActaConsejo()
                        )
                        .addValue(
                                "observaciones",
                                dto.getObservaciones()
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
    // ACTUALIZAR DATOS GENERALES Y ACTAS
    // =========================================================

    public int actualizar(
            Integer idEvaluacionCartera,
            EvaluacionCarteraDTO dto,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.evaluaciones_cartera

                SET
                    version_metodologia =
                        :versionMetodologia,

                    fecha_comite_riesgos =
                        :fechaComiteRiesgos,

                    numero_acta_riesgos =
                        :numeroActaRiesgos,

                    fecha_consejo =
                        :fechaConsejo,

                    numero_acta_consejo =
                        :numeroActaConsejo,

                    observaciones =
                        :observaciones,

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                WHERE id_evaluacion_cartera =
                      :idEvaluacionCartera

                  AND estado = 'P'
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        )
                        .addValue(
                                "versionMetodologia",
                                dto.getVersionMetodologia()
                        )
                        .addValue(
                                "fechaComiteRiesgos",
                                dto.getFechaComiteRiesgos()
                        )
                        .addValue(
                                "numeroActaRiesgos",
                                dto.getNumeroActaRiesgos()
                        )
                        .addValue(
                                "fechaConsejo",
                                dto.getFechaConsejo()
                        )
                        .addValue(
                                "numeroActaConsejo",
                                dto.getNumeroActaConsejo()
                        )
                        .addValue(
                                "observaciones",
                                dto.getObservaciones()
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
    // MARCAR DEFINITIVA
    // =========================================================

    public int marcarDefinitiva(
            Integer idEvaluacionCartera,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.evaluaciones_cartera

                SET
                    estado = 'D',

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                WHERE id_evaluacion_cartera =
                      :idEvaluacionCartera

                  AND estado = 'P'
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
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
    // OBTENER CRÉDITOS BASE DE TODAS LAS AGENCIAS
    //
    // Usa:
    // - créditos del cierre de cartera de la fecha evaluada;
    // - únicamente créditos con saldo_actual > 0;
    // - fotografía definitiva de Hoja de Vida del mismo corte.
    //
    // Este método solo obtiene información general del crédito.
    // Los datos particulares de cada criterio se consultan desde
    // el repositorio propio del criterio correspondiente.
    // =========================================================

    public List<EvaluacionCreditoDTO> obtenerCreditos(
            LocalDate fechaCorte
    ) {

        String sql = """
            SELECT
                cc.id_cierre_cartera
                    AS idCierreCartera,

                cc.id_cierre_cartera_credito
                    AS idCierreCarteraCredito,

                cc.id_cartera_credito
                    AS idCarteraCredito,

                cc.id_datos_personal
                    AS idDatosPersonal,

                hv.documento,

                hv.nombres,

                hv.primer_apellido
                    AS primerApellido,

                hv.segundo_apellido
                    AS segundoApellido,

                cc.pagare_cartera
                    AS pagareCartera,

                cc.codigo_clasificacion_credito
                    AS codigoClasificacionCredito,

                cc.saldo_actual
                    AS saldoActual,

                cc.credito_evaluado
                    AS creditoEvaluado,

                cc.edad_de_mora
                    AS edadMora,

                cc.edad_de_riesgo
                    AS edadRiesgoAnterior,

                cc.edad_riesgo_inicial
                    AS edadRiesgoInicial

            FROM cartera.cierres_cartera cierre

            INNER JOIN cartera.cierres_cartera_creditos cc
                ON cc.id_cierre_cartera =
                   cierre.id_cierre_cartera

            INNER JOIN hoja_vida.cierres_hoja_vida cierre_hv
                ON cierre_hv.fecha_corte =
                   cierre.fecha_corte

               AND cierre_hv.estado = 'D'

            INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                ON hv.id_cierre_hoja_vida =
                   cierre_hv.id_cierre_hoja_vida

               AND hv.id_datos_personal =
                   cc.id_datos_personal

            WHERE cierre.fecha_corte =
                  :fechaCorte

              AND COALESCE(
                      cc.saldo_actual,
                      0
                  ) > 0

            ORDER BY
                cc.id_agencia,
                hv.primer_apellido,
                hv.segundo_apellido,
                hv.nombres,
                cc.pagare_cartera
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionCreditoDTO.class
                )
        );
    }

    // =========================================================
    // VALIDAR CIERRE DEFINITIVO DE HOJA DE VIDA
    // =========================================================

    public boolean existeCierreHojaVidaDefinitivo(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1

                    FROM hoja_vida.cierres_hoja_vida cierre_hv

                    WHERE cierre_hv.fecha_corte =
                          :fechaCorte

                      AND cierre_hv.estado = 'D'
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
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
    // CONTAR CRÉDITOS SIN PERSONA EN EL CIERRE DE HOJA DE VIDA
    // Solo valida créditos que hacen parte de la evaluación:
    // saldo_actual > 0
    // =========================================================

    public int contarCreditosSinCierreHojaVida(
            LocalDate fechaCorte
    ) {

        String sql = """
            SELECT COUNT(*)

            FROM cartera.cierres_cartera cierre

            INNER JOIN cartera.cierres_cartera_creditos cc
                ON cc.id_cierre_cartera =
                   cierre.id_cierre_cartera

            LEFT JOIN hoja_vida.cierres_hoja_vida cierre_hv
                ON cierre_hv.fecha_corte =
                   cierre.fecha_corte

               AND cierre_hv.estado = 'D'

            LEFT JOIN hoja_vida.cierres_hoja_vida_personas hv
                ON hv.id_cierre_hoja_vida =
                   cierre_hv.id_cierre_hoja_vida

               AND hv.id_datos_personal =
                   cc.id_datos_personal

            WHERE cierre.fecha_corte =
                  :fechaCorte

              AND COALESCE(
                      cc.saldo_actual,
                      0
                  ) > 0

              AND hv.id_cierre_hoja_vida_persona
                  IS NULL
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad == null
                ? 0
                : cantidad;
    }

    // =========================================================
    // CONTAR CRÉDITOS EVALUABLES DEL CORTE
    // Solo créditos con saldo_actual > 0
    // =========================================================

    public int contarCreditosPorFecha(
            LocalDate fechaCorte
    ) {

        String sql = """
            SELECT COUNT(*)

            FROM cartera.cierres_cartera cierre

            INNER JOIN cartera.cierres_cartera_creditos cc
                ON cc.id_cierre_cartera =
                   cierre.id_cierre_cartera

            WHERE cierre.fecha_corte =
                  :fechaCorte

              AND COALESCE(
                      cc.saldo_actual,
                      0
                  ) > 0
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad == null
                ? 0
                : cantidad;
    }

    // =========================================================
// CONTAR ASOCIADOS EVALUABLES DEL CORTE
// Solo asociados con créditos cuyo saldo_actual > 0
// =========================================================

    public int contarAsociadosPorFecha(
            LocalDate fechaCorte
    ) {

        String sql = """
            SELECT COUNT(
                DISTINCT cc.id_datos_personal
            )

            FROM cartera.cierres_cartera cierre

            INNER JOIN cartera.cierres_cartera_creditos cc
                ON cc.id_cierre_cartera =
                   cierre.id_cierre_cartera

            WHERE cierre.fecha_corte =
                  :fechaCorte

              AND COALESCE(
                      cc.saldo_actual,
                      0
                  ) > 0
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad == null
                ? 0
                : cantidad;
    }

    // =========================================================
// OBTENER SALDO TOTAL EVALUABLE DEL CORTE
// Solo créditos con saldo_actual > 0
// =========================================================

    public BigDecimal obtenerSaldoTotalPorFecha(
            LocalDate fechaCorte
    ) {

        String sql = """
            SELECT COALESCE(
                SUM(cc.saldo_actual),
                0
            )

            FROM cartera.cierres_cartera cierre

            INNER JOIN cartera.cierres_cartera_creditos cc
                ON cc.id_cierre_cartera =
                   cierre.id_cierre_cartera

            WHERE cierre.fecha_corte =
                  :fechaCorte

              AND COALESCE(
                      cc.saldo_actual,
                      0
                  ) > 0
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        BigDecimal saldo =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        BigDecimal.class
                );

        return saldo == null
                ? BigDecimal.ZERO
                : saldo;
    }

    // =========================================================
    // ACTUALIZAR TOTALES DE LA EVALUACIÓN
    // =========================================================

    public int actualizarTotales(
            Integer idEvaluacionCartera,
            Integer cantidadCreditos,
            Integer cantidadAsociados,
            BigDecimal saldoTotalEvaluado,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.evaluaciones_cartera

                SET
                    cantidad_creditos =
                        :cantidadCreditos,

                    cantidad_asociados =
                        :cantidadAsociados,

                    saldo_total_evaluado =
                        :saldoTotalEvaluado,

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                WHERE id_evaluacion_cartera =
                      :idEvaluacionCartera

                  AND estado = 'P'
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        )
                        .addValue(
                                "cantidadCreditos",
                                cantidadCreditos
                        )
                        .addValue(
                                "cantidadAsociados",
                                cantidadAsociados
                        )
                        .addValue(
                                "saldoTotalEvaluado",
                                saldoTotalEvaluado
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