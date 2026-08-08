package co.assip.erp.cartera.evaluacion.proceso.resultados;

import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoCreditoDTO;
import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoDetalleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoHojaVidaDTO;
import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoMorosidadDTO;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EvaluacionResultadosRepository {

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // LISTAR RESULTADOS CONSOLIDADOS DE UNA EVALUACIÓN
    // =========================================================

    public List<EvaluacionResultadoCreditoDTO> listarResultados(
            Integer idEvaluacionCartera
    ) {

        String sql = """
                SELECT
                    ec.id_evaluacion_cartera_credito
                        AS idEvaluacionCarteraCredito,

                    ec.id_evaluacion_cartera
                        AS idEvaluacionCartera,

                    ec.id_cierre_cartera_credito
                        AS idCierreCarteraCredito,

                    ec.id_cartera_credito
                        AS idCarteraCredito,

                    cc.id_datos_personal
                        AS idDatosPersonal,

                    hv.documento,

                    hv.nombres,

                    hv.primer_apellido
                        AS primerApellido,

                    hv.segundo_apellido
                        AS segundoApellido,

                    cc.id_agencia
                        AS idAgencia,

                    ag.codigo_agencia
                        AS codigoAgencia,

                    ag.nombre_agencia
                        AS nombreAgencia,

                    cc.id_linea_credito
                        AS idLineaCredito,

                    lc.codigo_linea_credito
                        AS codigoLineaCredito,

                    lc.nombre_linea_credito
                        AS nombreLineaCredito,

                    cc.pagare_cartera
                        AS pagareCartera,

                    cc.codigo_clasificacion_credito
                        AS codigoClasificacionCredito,

                    cc.valor_inicial_credito
                        AS valorInicialCredito,

                    cc.saldo_actual
                        AS saldoActual,

                    ec.puntaje_total
                        AS puntajeTotal,

                    ec.edad_mora
                        AS edadMora,

                    ec.edad_riesgo_anterior
                        AS edadRiesgoAnterior,

                    ec.edad_riesgo_inicial
                        AS edadRiesgoInicial,

                    ec.edad_riesgo_calculada
                        AS edadRiesgoCalculada,

                    ec.edad_riesgo_arrastre
                        AS edadRiesgoArrastre,

                    ec.edad_riesgo_final
                        AS edadRiesgoFinal,

                    ec.accion_evaluacion
                        AS accionEvaluacion,

                    ec.comentario_evaluacion
                        AS comentarioEvaluacion

                FROM cartera.evaluaciones_cartera_creditos ec

                INNER JOIN cartera.cierres_cartera_creditos cc
                    ON cc.id_cierre_cartera_credito =
                       ec.id_cierre_cartera_credito

                INNER JOIN cartera.cierres_cartera cierre
                    ON cierre.id_cierre_cartera =
                       cc.id_cierre_cartera

                INNER JOIN cartera.evaluaciones_cartera e
                    ON e.id_evaluacion_cartera =
                       ec.id_evaluacion_cartera

                LEFT JOIN general.datos_agencias ag
                    ON ag.id_agencia =
                       cc.id_agencia

                LEFT JOIN cartera.lineas_creditos lc
                    ON lc.id_linea_credito =
                       cc.id_linea_credito

                INNER JOIN hoja_vida.cierres_hoja_vida cierre_hv
                    ON cierre_hv.fecha_corte =
                       cierre.fecha_corte

                   AND cierre_hv.estado = 'D'

                INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                    ON hv.id_cierre_hoja_vida =
                       cierre_hv.id_cierre_hoja_vida

                   AND hv.id_datos_personal =
                       cc.id_datos_personal

                WHERE ec.id_evaluacion_cartera =
                      :idEvaluacionCartera

                ORDER BY
                    hv.primer_apellido,
                    hv.segundo_apellido,
                    hv.nombres,
                    cc.pagare_cartera,
                    ec.id_evaluacion_cartera_credito
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        );

        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionResultadoCreditoDTO.class
                )
        );
    }


    // =========================================================
    // LISTAR DETALLE DE CRITERIOS
    //
    // Consulta individual utilizada por:
    // - detalle del crédito;
    // - impresión individual.
    // =========================================================

    public List<EvaluacionResultadoDetalleDTO> listarDetalle(
            Integer idEvaluacionCarteraCredito
    ) {

        String sql = """
                SELECT
                    d.id_evaluacion_cartera_credito_detalle
                        AS idEvaluacionCarteraCreditoDetalle,

                    d.id_evaluacion_cartera_credito
                        AS idEvaluacionCarteraCredito,

                    d.id_evaluacion_criterio
                        AS idEvaluacionCriterio,

                    c.codigo_criterio
                        AS codigoCriterio,

                    c.nombre_criterio
                        AS nombreCriterio,

                    c.orden_evaluacion
                        AS ordenEvaluacion,

                    c.puntaje_maximo
                        AS puntajeMaximo,

                    d.id_evaluacion_criterio_regla
                        AS idEvaluacionCriterioRegla,

                    r.codigo_regla
                        AS codigoRegla,

                    r.nombre_regla
                        AS nombreRegla,

                    d.valor_resultado
                        AS valorResultado,

                    d.codigo_resultado
                        AS codigoResultado,

                    d.descripcion_resultado
                        AS descripcionResultado,

                    d.puntaje_obtenido
                        AS puntajeObtenido,

                    d.observaciones

                FROM cartera.evaluaciones_cartera_creditos_detalle d

                INNER JOIN cartera.evaluacion_criterios c
                    ON c.id_evaluacion_criterio =
                       d.id_evaluacion_criterio

                INNER JOIN cartera.evaluacion_criterios_reglas r
                    ON r.id_evaluacion_criterio_regla =
                       d.id_evaluacion_criterio_regla

                WHERE d.id_evaluacion_cartera_credito =
                      :idEvaluacionCarteraCredito

                ORDER BY
                    c.orden_evaluacion,
                    c.codigo_criterio,
                    d.id_evaluacion_cartera_credito_detalle
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCarteraCredito",
                                idEvaluacionCarteraCredito
                        );

        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionResultadoDetalleDTO.class
                )
        );
    }


    // =========================================================
    // LISTAR DETALLE MASIVO POR ACCIÓN
    //
    // Utilizado exclusivamente para preparar la impresión
    // masiva de resultados.
    //
    // R = Reclasificar
    // H = Habilitar
    // M = Mantener
    //
    // IMPORTANTE:
    //
    // Este método reemplaza miles de solicitudes HTTP
    // individuales por una sola consulta.
    //
    // Ejemplo:
    // 2.662 créditos x 10 criterios = aprox. 26.620 filas.
    // =========================================================

    public List<EvaluacionResultadoDetalleDTO> listarDetallePorAccion(
            Integer idEvaluacionCartera,
            String accionEvaluacion
    ) {

        String sql = """
                SELECT
                    d.id_evaluacion_cartera_credito_detalle
                        AS idEvaluacionCarteraCreditoDetalle,

                    d.id_evaluacion_cartera_credito
                        AS idEvaluacionCarteraCredito,

                    d.id_evaluacion_criterio
                        AS idEvaluacionCriterio,

                    c.codigo_criterio
                        AS codigoCriterio,

                    c.nombre_criterio
                        AS nombreCriterio,

                    c.orden_evaluacion
                        AS ordenEvaluacion,

                    c.puntaje_maximo
                        AS puntajeMaximo,

                    d.id_evaluacion_criterio_regla
                        AS idEvaluacionCriterioRegla,

                    r.codigo_regla
                        AS codigoRegla,

                    r.nombre_regla
                        AS nombreRegla,

                    d.valor_resultado
                        AS valorResultado,

                    d.codigo_resultado
                        AS codigoResultado,

                    d.descripcion_resultado
                        AS descripcionResultado,

                    d.puntaje_obtenido
                        AS puntajeObtenido,

                    d.observaciones

                FROM cartera.evaluaciones_cartera_creditos_detalle d

                INNER JOIN cartera.evaluaciones_cartera_creditos ec
                    ON ec.id_evaluacion_cartera_credito =
                       d.id_evaluacion_cartera_credito

                INNER JOIN cartera.evaluacion_criterios c
                    ON c.id_evaluacion_criterio =
                       d.id_evaluacion_criterio

                INNER JOIN cartera.evaluacion_criterios_reglas r
                    ON r.id_evaluacion_criterio_regla =
                       d.id_evaluacion_criterio_regla

                WHERE ec.id_evaluacion_cartera =
                      :idEvaluacionCartera

                  AND ec.accion_evaluacion =
                      :accionEvaluacion

                ORDER BY
                    d.id_evaluacion_cartera_credito,
                    c.orden_evaluacion,
                    c.codigo_criterio,
                    d.id_evaluacion_cartera_credito_detalle
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        )
                        .addValue(
                                "accionEvaluacion",
                                accionEvaluacion
                        );

        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionResultadoDetalleDTO.class
                )
        );
    }

    // =========================================================
    // LISTAR FOTOGRAFÍA DE HOJA DE VIDA
    //
    // Devuelve una fila por asociado incluido en la evaluación.
    //
    // La información corresponde a la fotografía definitiva
    // de Hoja de Vida de la misma fecha de corte utilizada
    // por la evaluación.
    //
    // No consulta datos vigentes.
    // =========================================================

    public List<EvaluacionResultadoHojaVidaDTO> listarFotoHojaVida(
            Integer idEvaluacionCartera
    ) {

        String sql = """
            SELECT DISTINCT

                hv.id_cierre_hoja_vida_persona
                    AS idCierreHojaVidaPersona,

                hv.id_cierre_hoja_vida
                    AS idCierreHojaVida,

                hv.id_datos_personal
                    AS idDatosPersonal,


                -- =================================================
                -- IDENTIFICACIÓN
                -- =================================================

                hv.tipo_documento
                    AS tipoDocumento,

                hv.documento,

                hv.tipo_persona
                    AS tipoPersona,

                hv.tiene_rut
                    AS tieneRut,

                hv.digito_verificacion
                    AS digitoVerificacion,

                hv.nombres,

                hv.primer_apellido
                    AS primerApellido,

                hv.segundo_apellido
                    AS segundoApellido,


                -- =================================================
                -- FECHAS
                -- =================================================

                hv.fecha_nacimiento
                    AS fechaNacimiento,

                hv.fecha_apertura
                    AS fechaApertura,

                hv.fecha_actualizacion
                    AS fechaActualizacion,


                -- =================================================
                -- INFORMACIÓN PERSONAL
                -- =================================================

                hv.codigo_genero
                    AS codigoGenero,

                hv.codigo_estado_civil
                    AS codigoEstadoCivil,

                hv.codigo_escolaridad
                    AS codigoEscolaridad,

                hv.cabeza_familia
                    AS cabezaFamilia,

                hv.estrato_social
                    AS estratoSocial,

                hv.codigo_tipo_vivienda
                    AS codigoTipoVivienda,

                hv.numero_hijos
                    AS numeroHijos,

                hv.codigo_ocupacion
                    AS codigoOcupacion,

                hv.codigo_sector_economico
                    AS codigoSectorEconomico,

                hv.codigo_actividad_ses
                    AS codigoActividadSes,

                hv.codigo_actividad_dian
                    AS codigoActividadDian,


                -- =================================================
                -- UBICACIÓN
                -- =================================================

                hv.direccion,

                hv.barrio,

                hv.telefono,

                hv.celular_uno
                    AS celularUno,

                hv.celular_dos
                    AS celularDos,

                hv.correo,

                hv.id_pais
                    AS idPais,

                hv.id_departamento
                    AS idDepartamento,

                hv.id_ciudad
                    AS idCiudad,

                hv.id_zona
                    AS idZona,

                hv.id_sub_zona
                    AS idSubZona,


                -- =================================================
                -- INGRESOS
                -- =================================================

                hv.valor_salario
                    AS valorSalario,

                hv.valor_pension
                    AS valorPension,

                hv.ingresos_arriendo
                    AS ingresosArriendo,

                hv.ingresos_comisiones
                    AS ingresosComisiones,

                hv.otros_ingresos
                    AS otrosIngresos,

                hv.ingresos_totales
                    AS ingresosTotales,


                -- =================================================
                -- EGRESOS
                -- =================================================

                hv.egresos_familiares
                    AS egresosFamiliares,

                hv.egresos_arriendo
                    AS egresosArriendo,

                hv.egresos_credito
                    AS egresosCredito,

                hv.otros_egresos
                    AS otrosEgresos,

                hv.egresos_totales
                    AS egresosTotales,


                -- =================================================
                -- PATRIMONIO
                -- =================================================

                hv.total_activos
                    AS totalActivos,

                hv.total_pasivos
                    AS totalPasivos,

                hv.patrimonio_total
                    AS patrimonioTotal,


                -- =================================================
                -- INFORMACIÓN FINANCIERA COMPLEMENTARIA
                -- =================================================

                hv.deuda_relacion_financiera
                    AS deudaRelacionFinanciera,

                hv.origen_fondos
                    AS origenFondos,

                hv.relacion_financiera
                    AS relacionFinanciera,

                hv.ingreso_disponible
                    AS ingresoDisponible


            FROM cartera.evaluaciones_cartera_creditos ec


            INNER JOIN cartera.cierres_cartera_creditos cc
                ON cc.id_cierre_cartera_credito =
                   ec.id_cierre_cartera_credito


            INNER JOIN cartera.cierres_cartera cierre
                ON cierre.id_cierre_cartera =
                   cc.id_cierre_cartera


            INNER JOIN hoja_vida.cierres_hoja_vida cierre_hv
                ON cierre_hv.fecha_corte =
                   cierre.fecha_corte

               AND cierre_hv.estado = 'D'


            INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                ON hv.id_cierre_hoja_vida =
                   cierre_hv.id_cierre_hoja_vida

               AND hv.id_datos_personal =
                   cc.id_datos_personal


            WHERE ec.id_evaluacion_cartera =
                  :idEvaluacionCartera


            ORDER BY
                hv.documento,
                hv.id_datos_personal
            """;


        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        );


        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionResultadoHojaVidaDTO.class
                )
        );
    }

    // =========================================================
    // LISTAR INSUMO DE MOROSIDAD DEL EXTRACTO
    //
    // Devuelve los comprobantes utilizados como insumo para
    // evaluar el criterio 401 - Servicio de la deuda.
    //
    // IMPORTANTE:
    //
    // Se conserva exactamente la misma regla del criterio 401:
    //
    // - último año contado desde la fecha de corte;
    // - consolidación por:
    //      id_cartera_credito
    //      + tipo_comprobante
    //      + numero_comprobante;
    //
    // - suma del capital;
    // - suma del valor de mora;
    // - máximo número de días de mora.
    //
    // Solo se incluyen créditos pertenecientes a la evaluación.
    // =========================================================

    public List<EvaluacionResultadoMorosidadDTO> listarMorosidadExtracto(
            Integer idEvaluacionCartera
    ) {

        String sql = """
            WITH evaluacion AS
            (
                SELECT
                    e.id_evaluacion_cartera,

                    e.fecha_corte,

                    (
                        e.fecha_corte
                        - INTERVAL '1 year'
                        + INTERVAL '1 day'
                    )::date
                        AS fecha_inicial

                FROM cartera.evaluaciones_cartera e

                WHERE e.id_evaluacion_cartera =
                      :idEvaluacionCartera
            ),

            pagos_consolidados AS
            (
                SELECT
                    ec.id_cartera_credito,

                    ec.tipo_comprobante,

                    ec.numero_comprobante,

                    MIN(ec.fecha_contable)
                        AS fecha_contable_desde,

                    MAX(ec.fecha_contable)
                        AS fecha_contable_hasta,

                    SUM(
                        COALESCE(
                            ec.valor_capital,
                            0
                        )
                    )
                        AS valor_capital,

                    SUM(
                        COALESCE(
                              ec.valor_interes_mora,
                            0
                        )
                    )
                        AS valor_mora,

                    MAX(ec.dias_mora)
                        AS dias_mora

                FROM cartera.extractos_cartera ec

                CROSS JOIN evaluacion e

                WHERE ec.fecha_contable
                      BETWEEN e.fecha_inicial
                          AND e.fecha_corte

                GROUP BY
                    ec.id_cartera_credito,
                    ec.tipo_comprobante,
                    ec.numero_comprobante
            )

            SELECT
                cc.id_cierre_cartera_credito
                    AS idCierreCarteraCredito,

                cc.id_cartera_credito
                    AS idCarteraCredito,

                cc.id_datos_personal
                    AS idDatosPersonal,


                -- =================================================
                -- ASOCIADO
                -- =================================================

                hv.documento,

                hv.nombres,

                hv.primer_apellido
                    AS primerApellido,

                hv.segundo_apellido
                    AS segundoApellido,


                -- =================================================
                -- AGENCIA
                -- =================================================

                cc.id_agencia
                    AS idAgencia,

                ag.codigo_agencia
                    AS codigoAgencia,

                ag.nombre_agencia
                    AS nombreAgencia,


                -- =================================================
                -- LÍNEA
                -- =================================================

                cc.id_linea_credito
                    AS idLineaCredito,

                lc.codigo_linea_credito
                    AS codigoLineaCredito,

                lc.nombre_linea_credito
                    AS nombreLineaCredito,

                cc.pagare_cartera
                    AS pagareCartera,


                -- =================================================
                -- COMPROBANTE
                -- =================================================

                pc.tipo_comprobante
                    AS tipoComprobante,

                pc.numero_comprobante
                    AS numeroComprobante,

                pc.fecha_contable_desde
                    AS fechaContableDesde,

                pc.fecha_contable_hasta
                    AS fechaContableHasta,


                -- =================================================
                -- VALORES
                -- =================================================

                pc.valor_capital
                    AS valorCapital,

                pc.valor_mora
                    AS valorMora,

                pc.dias_mora
                    AS diasMora


            FROM cartera.evaluaciones_cartera_creditos er


            INNER JOIN cartera.cierres_cartera_creditos cc
                ON cc.id_cierre_cartera_credito =
                   er.id_cierre_cartera_credito


            INNER JOIN cartera.cierres_cartera cierre
                ON cierre.id_cierre_cartera =
                   cc.id_cierre_cartera


            INNER JOIN evaluacion e
                ON e.id_evaluacion_cartera =
                   er.id_evaluacion_cartera


            INNER JOIN hoja_vida.cierres_hoja_vida cierre_hv
                ON cierre_hv.fecha_corte =
                   cierre.fecha_corte

               AND cierre_hv.estado = 'D'


            INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                ON hv.id_cierre_hoja_vida =
                   cierre_hv.id_cierre_hoja_vida

               AND hv.id_datos_personal =
                   cc.id_datos_personal


            LEFT JOIN general.datos_agencias ag
                ON ag.id_agencia =
                   cc.id_agencia


            LEFT JOIN cartera.lineas_creditos lc
                ON lc.id_linea_credito =
                   cc.id_linea_credito


            INNER JOIN pagos_consolidados pc
                ON pc.id_cartera_credito =
                   cc.id_cartera_credito


            WHERE er.id_evaluacion_cartera =
                  :idEvaluacionCartera


            ORDER BY
                ag.codigo_agencia,
                hv.documento,
                lc.codigo_linea_credito,
                cc.pagare_cartera,
                pc.fecha_contable_desde,
                pc.tipo_comprobante,
                pc.numero_comprobante
            """;


        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        );


        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionResultadoMorosidadDTO.class
                )
        );
    }


    // =========================================================
    // BUSCAR RESULTADO CONSOLIDADO POR ID
    // =========================================================

    public EvaluacionResultadoCreditoDTO buscarResultadoPorId(
            Integer idEvaluacionCarteraCredito
    ) {

        String sql = """
                SELECT
                    ec.id_evaluacion_cartera_credito
                        AS idEvaluacionCarteraCredito,

                    ec.id_evaluacion_cartera
                        AS idEvaluacionCartera,

                    ec.id_cierre_cartera_credito
                        AS idCierreCarteraCredito,

                    ec.id_cartera_credito
                        AS idCarteraCredito,

                    cc.id_datos_personal
                        AS idDatosPersonal,

                    hv.documento,

                    hv.nombres,

                    hv.primer_apellido
                        AS primerApellido,

                    hv.segundo_apellido
                        AS segundoApellido,

                    cc.id_agencia
                        AS idAgencia,

                    ag.codigo_agencia
                        AS codigoAgencia,

                    ag.nombre_agencia
                        AS nombreAgencia,

                    cc.id_linea_credito
                        AS idLineaCredito,

                    lc.codigo_linea_credito
                        AS codigoLineaCredito,

                    lc.nombre_linea_credito
                        AS nombreLineaCredito,

                    cc.pagare_cartera
                        AS pagareCartera,

                    cc.codigo_clasificacion_credito
                        AS codigoClasificacionCredito,

                    cc.valor_inicial_credito
                        AS valorInicialCredito,

                    cc.saldo_actual
                        AS saldoActual,

                    ec.puntaje_total
                        AS puntajeTotal,

                    ec.edad_mora
                        AS edadMora,

                    ec.edad_riesgo_anterior
                        AS edadRiesgoAnterior,

                    ec.edad_riesgo_inicial
                        AS edadRiesgoInicial,

                    ec.edad_riesgo_calculada
                        AS edadRiesgoCalculada,

                    ec.edad_riesgo_arrastre
                        AS edadRiesgoArrastre,

                    ec.edad_riesgo_final
                        AS edadRiesgoFinal,

                    ec.accion_evaluacion
                        AS accionEvaluacion,

                    ec.comentario_evaluacion
                        AS comentarioEvaluacion

                FROM cartera.evaluaciones_cartera_creditos ec

                INNER JOIN cartera.cierres_cartera_creditos cc
                    ON cc.id_cierre_cartera_credito =
                       ec.id_cierre_cartera_credito

                INNER JOIN cartera.cierres_cartera cierre
                    ON cierre.id_cierre_cartera =
                       cc.id_cierre_cartera

                LEFT JOIN general.datos_agencias ag
                    ON ag.id_agencia =
                       cc.id_agencia

                LEFT JOIN cartera.lineas_creditos lc
                    ON lc.id_linea_credito =
                       cc.id_linea_credito

                INNER JOIN hoja_vida.cierres_hoja_vida cierre_hv
                    ON cierre_hv.fecha_corte =
                       cierre.fecha_corte

                   AND cierre_hv.estado = 'D'

                INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                    ON hv.id_cierre_hoja_vida =
                       cierre_hv.id_cierre_hoja_vida

                   AND hv.id_datos_personal =
                       cc.id_datos_personal

                WHERE ec.id_evaluacion_cartera_credito =
                      :idEvaluacionCarteraCredito
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCarteraCredito",
                                idEvaluacionCarteraCredito
                        );

        List<EvaluacionResultadoCreditoDTO> resultados =
                jdbc.query(
                        sql,
                        parametros,
                        BeanPropertyRowMapper.newInstance(
                                EvaluacionResultadoCreditoDTO.class
                        )
                );

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }
}