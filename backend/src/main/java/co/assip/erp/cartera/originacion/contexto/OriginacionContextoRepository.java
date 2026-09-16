package co.assip.erp.cartera.originacion.contexto;

import co.assip.erp.cartera.originacion.contexto.dto.OriginacionContextoDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public class OriginacionContextoRepository {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<
            OriginacionContextoDTO.InformacionEconomicaDTO>
            INFORMACION_ECONOMICA_MAPPER =
            mapper(
                    OriginacionContextoDTO
                            .InformacionEconomicaDTO.class
            );


    private static final BeanPropertyRowMapper<
            OriginacionContextoDTO.DepositoDTO>
            DEPOSITO_MAPPER =
            mapper(
                    OriginacionContextoDTO
                            .DepositoDTO.class
            );


    private static final BeanPropertyRowMapper<
            OriginacionContextoDTO.CarteraDTO>
            CARTERA_MAPPER =
            mapper(
                    OriginacionContextoDTO
                            .CarteraDTO.class
            );


    private static final BeanPropertyRowMapper<
            OriginacionContextoDTO.CodeudaDTO>
            CODEUDA_MAPPER =
            mapper(
                    OriginacionContextoDTO
                            .CodeudaDTO.class
            );


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public OriginacionContextoRepository(
            NamedParameterJdbcTemplate jdbc
    ) {

        this.jdbc = jdbc;
    }


    // =========================================================
    // INFORMACIÓN ECONÓMICA
    // =========================================================

    private static final String SQL_INFORMACION_ECONOMICA = """
            WITH financiero AS
            (
                SELECT DISTINCT ON (
                    f.id_datos_personal
                )

                    f.id_financiero,
                    f.id_datos_personal,

                    f.valor_salario,
                    f.valor_pension,
                    f.ingresos_arriendo,
                    f.ingresos_comisiones,
                    f.otros_ingresos,
                    f.comentario_otros_ingresos,

                    f.egresos_familiares,
                    f.egresos_arriendo,
                    f.egresos_credito,
                    f.otros_egresos,
                    f.comentario_otros_egresos,

                    f.total_activos,
                    f.total_pasivos,

                    f.origen_fondos,
                    f.relacion_financiera,
                    f.deuda_relacion_financiera,

                    f.fecha_creacion,
                    f.fecha_edicion

                FROM reporting.vw_hoja_vida_financieros_total f

                WHERE f.id_datos_personal =
                      :idDatosPersonal

                ORDER BY
                    f.id_datos_personal,
                    f.fecha_edicion DESC NULLS LAST,
                    f.fecha_creacion DESC NULLS LAST,
                    f.id_financiero DESC
            ),

            persona AS
            (
                SELECT
                    p.id_datos_personal,

                    p.nombre_actividad_ses,
                    p.nombre_sector_economico,
                    p.nombre_ocupacion

                FROM reporting.vw_datos_personales_operativa p

                WHERE p.id_datos_personal =
                      :idDatosPersonal

                LIMIT 1
            ),

            empresa AS
            (
                SELECT
                    h.id_datos_personal,
                    h.nombre_empresa

                FROM reporting.vw_hoja_vida_general_total h

                WHERE h.id_datos_personal =
                      :idDatosPersonal

                LIMIT 1
            )

            SELECT
                f.id_financiero,
                f.id_datos_personal,

                p.nombre_actividad_ses
                    AS nombre_actividad_economica,

                p.nombre_sector_economico,

                p.nombre_ocupacion
                    AS ocupacion,

                e.nombre_empresa
                    AS empresa,

                f.valor_salario,
                f.valor_pension,
                f.ingresos_arriendo,
                f.ingresos_comisiones,
                f.otros_ingresos,
                f.comentario_otros_ingresos,

                (
                    COALESCE(
                        f.valor_salario,
                        0
                    )
                    +
                    COALESCE(
                        f.valor_pension,
                        0
                    )
                    +
                    COALESCE(
                        f.ingresos_arriendo,
                        0
                    )
                    +
                    COALESCE(
                        f.ingresos_comisiones,
                        0
                    )
                    +
                    COALESCE(
                        f.otros_ingresos,
                        0
                    )
                )::numeric
                    AS total_ingresos,

                f.egresos_familiares,
                f.egresos_arriendo,
                f.egresos_credito,
                f.otros_egresos,
                f.comentario_otros_egresos,

                (
                    COALESCE(
                        f.egresos_familiares,
                        0
                    )
                    +
                    COALESCE(
                        f.egresos_arriendo,
                        0
                    )
                    +
                    COALESCE(
                        f.egresos_credito,
                        0
                    )
                    +
                    COALESCE(
                        f.otros_egresos,
                        0
                    )
                )::numeric
                    AS total_egresos,

                f.total_activos,
                f.total_pasivos,

                (
                    COALESCE(
                        f.total_activos,
                        0
                    )
                    -
                    COALESCE(
                        f.total_pasivos,
                        0
                    )
                )::numeric
                    AS patrimonio,

                f.origen_fondos,
                f.relacion_financiera,
                f.deuda_relacion_financiera,

                f.fecha_creacion,
                f.fecha_edicion

            FROM financiero f

            LEFT JOIN persona p
                ON p.id_datos_personal =
                   f.id_datos_personal

            LEFT JOIN empresa e
                ON e.id_datos_personal =
                   f.id_datos_personal

            LIMIT 1
            """;


    // =========================================================
    // DEPÓSITOS
    //
    // Contexto integral del asociado.
    // No se limita por agencia.
    // =========================================================

    private static final String SQL_DEPOSITOS = """
            SELECT
                a.id_cuenta_ahorro,

                a.id_agencia,
                a.id_datos_personal,

                a.id_forma_ahorro,

                a.codigo_forma_ahorro
                    AS codigo_forma,

                a.nombre_forma_ahorro
                    AS nombre_forma,

                a.codigo_cuenta,

                a.fecha_apertura_cuenta
                    AS fecha_apertura,

                mov.fecha_ultimo_movimiento,

                COALESCE(
                    mov.entradas_ultimo_ano,
                    0
                )::numeric(18,2)
                    AS entradas_ultimo_ano,

                COALESCE(
                    mov.salidas_ultimo_ano,
                    0
                )::numeric(18,2)
                    AS salidas_ultimo_ano,

                COALESCE(
                    a.saldo_actual_cuenta,
                    0
                )::numeric(18,2)
                    AS saldo,

                a.codigo_estado_cuenta
                    AS codigo_estado,

                a.nombre_estado_cuenta
                    AS nombre_estado,

                COALESCE(
                    a.estado_operativo,
                    false
                )
                    AS activa

            FROM reporting.vw_depositos_cuentas_ahorro_integral a

            LEFT JOIN LATERAL
            (
                SELECT
                    MAX(
                        m.fecha_movimiento
                    )::date
                        AS fecha_ultimo_movimiento,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN
                                    m.fecha_movimiento >=
                                        CURRENT_DATE
                                        - INTERVAL '1 year'

                                    AND
                                    m.fecha_movimiento <=
                                        CURRENT_DATE

                                    AND COALESCE(
                                        m.valor_credito,
                                        0
                                    ) > 0

                                THEN m.valor_credito

                                ELSE 0
                            END
                        ),
                        0
                    )::numeric(18,2)
                        AS entradas_ultimo_ano,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN
                                    m.fecha_movimiento >=
                                        CURRENT_DATE
                                        - INTERVAL '1 year'

                                    AND
                                    m.fecha_movimiento <=
                                        CURRENT_DATE

                                    AND COALESCE(
                                        m.valor_debito,
                                        0
                                    ) > 0

                                THEN m.valor_debito

                                ELSE 0
                            END
                        ),
                        0
                    )::numeric(18,2)
                        AS salidas_ultimo_ano

                FROM depositos.extractos_cuentas_ahorros m

                WHERE m.id_cuenta_ahorro =
                      a.id_cuenta_ahorro

            ) mov
                ON true

            WHERE a.id_datos_personal =
                  :idDatosPersonal
    
              AND COALESCE(
                      a.saldo_actual_cuenta,
                      0
                  ) > 0
    
            ORDER BY
                CASE
                    WHEN COALESCE(
                        a.estado_operativo,
                        false
                    )
                        THEN 0

                    ELSE 1
                END,

                a.id_agencia,
                a.fecha_apertura_cuenta DESC NULLS LAST,
                a.id_cuenta_ahorro DESC
            """;


    // =========================================================
    // CARTERA HISTÓRICA
    //
    // La cartera actual NO se consulta aquí.
    // Se toma del Vector de Comportamiento Actual.
    //
    // El histórico corresponde al asociado completo.
    // No se limita por agencia.
    // =========================================================

    private static final String SQL_CARTERA_HISTORICA = """
            SELECT
                c.id_cartera_credito,

                c.id_agencia,

                c.id_linea_credito,
                c.codigo_linea_credito,
                c.nombre_linea_credito,

                c.pagare_cartera,

                c.fecha_desembolso,

                COALESCE(
                    c.saldo_actual,
                    0
                )::numeric
                    AS saldo_actual,

                vector.dias_mora,

                c.codigo_estado_cartera,

                c.descripcion_estado_cartera
                    AS nombre_estado_cartera,

                (
                    COALESCE(
                        c.credito_saldado,
                        false
                    ) = false

                    AND COALESCE(
                        c.saldo_actual,
                        0
                    ) > 0
                )
                    AS vigente

            FROM cartera.vw_cartera_creditos_total c

            LEFT JOIN cartera.vw_cartera_vector_comportamiento_detalle vector
                ON vector.id_cartera_credito =
                   c.id_cartera_credito

               AND vector.tipo_posicion =
                   'ACTUAL'

            WHERE c.id_datos_personal =
                  :idDatosPersonal

            ORDER BY
                CASE
                    WHEN COALESCE(
                        c.saldo_actual,
                        0
                    ) > 0
                        THEN 0

                    ELSE 1
                END,

                c.fecha_desembolso DESC NULLS LAST,
                c.id_cartera_credito DESC
            """;


    // =========================================================
    // CODEUDAS ACTUALES
    //
    // Obligaciones de terceros en las cuales el asociado
    // aparece como fiador/codeudor.
    //
    // No se limita por agencia.
    // =========================================================

    private static final String SQL_CODEUDAS_ACTUALES = """
            SELECT
                fiador.id_obligacion_fiador,
                fiador.id_obligacion_juridica,

                credito.id_cartera_credito,

                credito.id_agencia,

                credito.id_linea_credito,

                linea.codigo_linea_credito,

                linea.nombre_linea_credito,

                credito.pagare_cartera,

                credito.id_datos_personal
                    AS id_deudor_principal,

                persona.documento
                    AS documento_deudor_principal,

                persona.nombre_completo_apellidos
                    AS deudor_principal,

                credito.fecha_desembolso,

                COALESCE(
                    credito.saldo_actual,
                    0
                )::numeric
                    AS saldo_actual,

                vector.dias_mora,

                credito.codigo_estado_cartera,

                estado.descripcion_estado_cartera
                    AS nombre_estado_cartera,

                true
                    AS vigente

            FROM cartera.obligaciones_fiadores fiador

            INNER JOIN cartera.obligaciones_juridicas obligacion
                ON obligacion.id_obligacion_juridica =
                   fiador.id_obligacion_juridica

            INNER JOIN cartera.carteras_creditos credito
                ON credito.id_obligacion_juridica =
                   obligacion.id_obligacion_juridica

            LEFT JOIN cartera.lineas_creditos linea
                ON linea.id_linea_credito =
                   credito.id_linea_credito

            LEFT JOIN cartera.estados_cartera estado
                ON estado.codigo_estado_cartera =
                   credito.codigo_estado_cartera

            LEFT JOIN reporting.vw_datos_personales_operativa persona
                ON persona.id_datos_personal =
                   credito.id_datos_personal

            LEFT JOIN cartera.vw_cartera_vector_comportamiento_detalle vector
                ON vector.id_cartera_credito =
                   credito.id_cartera_credito

               AND vector.tipo_posicion =
                   'ACTUAL'

            WHERE fiador.id_datos_personal =
                  :idDatosPersonal

              AND credito.id_datos_personal <>
                  fiador.id_datos_personal

              AND COALESCE(
                      credito.saldo_actual,
                      0
                  ) > 0

            ORDER BY
                credito.id_agencia,
                credito.fecha_desembolso DESC NULLS LAST,
                credito.id_cartera_credito DESC
            """;


    // =========================================================
    // CODEUDAS HISTÓRICAS
    //
    // Histórico integral del asociado.
    // No se limita por agencia.
    // =========================================================

    private static final String SQL_CODEUDAS_HISTORICAS = """
            SELECT
                fiador.id_obligacion_fiador,
                fiador.id_obligacion_juridica,

                credito.id_cartera_credito,

                credito.id_agencia,

                credito.id_linea_credito,

                linea.codigo_linea_credito,

                linea.nombre_linea_credito,

                credito.pagare_cartera,

                credito.id_datos_personal
                    AS id_deudor_principal,

                persona.documento
                    AS documento_deudor_principal,

                persona.nombre_completo_apellidos
                    AS deudor_principal,

                credito.fecha_desembolso,

                COALESCE(
                    credito.saldo_actual,
                    0
                )::numeric
                    AS saldo_actual,

                vector.dias_mora,

                credito.codigo_estado_cartera,

                estado.descripcion_estado_cartera
                    AS nombre_estado_cartera,

                (
                    COALESCE(
                        credito.saldo_actual,
                        0
                    ) > 0
                )
                    AS vigente

            FROM cartera.obligaciones_fiadores fiador

            INNER JOIN cartera.obligaciones_juridicas obligacion
                ON obligacion.id_obligacion_juridica =
                   fiador.id_obligacion_juridica

            INNER JOIN cartera.carteras_creditos credito
                ON credito.id_obligacion_juridica =
                   obligacion.id_obligacion_juridica

            LEFT JOIN cartera.lineas_creditos linea
                ON linea.id_linea_credito =
                   credito.id_linea_credito

            LEFT JOIN cartera.estados_cartera estado
                ON estado.codigo_estado_cartera =
                   credito.codigo_estado_cartera

            LEFT JOIN reporting.vw_datos_personales_operativa persona
                ON persona.id_datos_personal =
                   credito.id_datos_personal

            LEFT JOIN cartera.vw_cartera_vector_comportamiento_detalle vector
                ON vector.id_cartera_credito =
                   credito.id_cartera_credito

               AND vector.tipo_posicion =
                   'ACTUAL'

            WHERE fiador.id_datos_personal =
                  :idDatosPersonal

              AND credito.id_datos_personal <>
                  fiador.id_datos_personal

            ORDER BY
                CASE
                    WHEN COALESCE(
                        credito.saldo_actual,
                        0
                    ) > 0
                        THEN 0

                    ELSE 1
                END,

                credito.id_agencia,
                credito.fecha_desembolso DESC NULLS LAST,
                credito.id_cartera_credito DESC
            """;


    // =========================================================
    // CONSULTA INFORMACIÓN ECONÓMICA
    // =========================================================

    public Optional<OriginacionContextoDTO.InformacionEconomicaDTO>
    buscarInformacionEconomica(
            Integer idDatosPersonal
    ) {

        MapSqlParameterSource parametros =
                parametros(
                        idDatosPersonal
                );


        return jdbc.query(
                        SQL_INFORMACION_ECONOMICA,
                        parametros,
                        INFORMACION_ECONOMICA_MAPPER
                )
                .stream()
                .findFirst();
    }


    // =========================================================
    // CONSULTA DEPÓSITOS
    // =========================================================

    public List<OriginacionContextoDTO.DepositoDTO>
    listarDepositos(
            Integer idDatosPersonal
    ) {

        return jdbc.query(
                SQL_DEPOSITOS,
                parametros(
                        idDatosPersonal
                ),
                DEPOSITO_MAPPER
        );
    }


    // =========================================================
    // CARTERA HISTÓRICA
    // =========================================================

    public List<OriginacionContextoDTO.CarteraDTO>
    listarCarteraHistorica(
            Integer idDatosPersonal
    ) {

        return jdbc.query(
                SQL_CARTERA_HISTORICA,
                parametros(
                        idDatosPersonal
                ),
                CARTERA_MAPPER
        );
    }


    // =========================================================
    // CODEUDAS ACTUALES
    // =========================================================

    public List<OriginacionContextoDTO.CodeudaDTO>
    listarCodeudasActuales(
            Integer idDatosPersonal
    ) {

        return jdbc.query(
                SQL_CODEUDAS_ACTUALES,
                parametros(
                        idDatosPersonal
                ),
                CODEUDA_MAPPER
        );
    }


    // =========================================================
    // CODEUDAS HISTÓRICAS
    // =========================================================

    public List<OriginacionContextoDTO.CodeudaDTO>
    listarCodeudasHistoricas(
            Integer idDatosPersonal
    ) {

        return jdbc.query(
                SQL_CODEUDAS_HISTORICAS,
                parametros(
                        idDatosPersonal
                ),
                CODEUDA_MAPPER
        );
    }


    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource parametros(
            Integer idDatosPersonal
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "idDatosPersonal",
                        idDatosPersonal
                );
    }


    // =========================================================
    // MAPPER
    // =========================================================

    private static <T> BeanPropertyRowMapper<T> mapper(
            Class<T> tipo
    ) {

        BeanPropertyRowMapper<T> mapper =
                BeanPropertyRowMapper.newInstance(
                        tipo
                );


        mapper.setCheckFullyPopulated(
                false
        );


        mapper.setPrimitivesDefaultedForNullValue(
                true
        );


        return mapper;
    }
}