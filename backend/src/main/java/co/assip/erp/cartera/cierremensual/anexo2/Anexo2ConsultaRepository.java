package co.assip.erp.cartera.cierremensual.anexo2;

import co.assip.erp.cartera.cierremensual.anexo2.dto.DetalleAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.MoraAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.ResumenAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.TrabajoAnexo2DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class Anexo2ConsultaRepository {

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // MODELOS PE PROCESADOS EN EL CIERRE
    // =========================================================

    public List<Integer> obtenerModelosDelCierre(
            Integer idCierreCartera
    ) {

        return jdbcTemplate.queryForList(
                """
                SELECT DISTINCT
                    id_modelo_pe

                FROM cartera.pe_resultados

                WHERE id_cierre_cartera = ?

                ORDER BY id_modelo_pe
                """,
                Integer.class,
                idCierreCartera
        );
    }

    // =========================================================
    // HOJA: RESULTADO
    // =========================================================

    public List<DetalleAnexo2DTO> obtenerDetalle(
            Integer idCierreCartera,
            Integer idModeloPe
    ) {

        String sql = """
                SELECT
                    c.id_cierre_cartera
                        AS "idCierreCartera",

                    c.fecha_corte
                        AS "fechaCorte",

                    f.id_cartera_credito
                        AS "idCarteraCredito",

                    f.id_cierre_cartera_credito
                        AS "idCierreCarteraCredito",

                    r.id_cierre_cartera_resultado
                        AS "idCierreCarteraResultado",

                    f.id_agencia
                        AS "idAgencia",

                    f.id_datos_personal
                        AS "idDatosPersonal",

                    f.pagare_cartera
                        AS "pagareCartera",

                    f.tipo_documento
                        AS "tipoDocumento",

                    f.documento
                        AS "documento",

                    f.nombres
                        AS "nombres",

                    f.primer_apellido
                        AS "primerApellido",

                    f.segundo_apellido
                        AS "segundoApellido",

                    TRIM(
                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(f.nombres), ''),
                            NULLIF(TRIM(f.primer_apellido), ''),
                            NULLIF(TRIM(f.segundo_apellido), '')
                        )
                    )
                        AS "nombreCompleto",

                    pe.id_modelo_pe
                        AS "idModeloPe",

                    pe.id_modelo_pe::varchar
                        AS "codigoModeloPe",

                    pe.nombre_modelo_pe
                        AS "nombreModeloPe",

                    f.id_linea_credito
                        AS "idLineaCredito",

                    f.codigo_linea_credito
                        AS "codigoLineaCredito",

                    f.nombre_linea_credito
                        AS "nombreLineaCredito",

                    f.codigo_clasificacion_credito
                        AS "codigoClasificacionCredito",

                    f.descripcion_clasificacion_credito
                        AS "descripcionClasificacionCredito",

                    pe.tipo_persona
                        AS "tipoPersona",

                    f.codigo_garantia_credito
                        AS "codigoGarantiaCredito",

                    f.descripcion_garantia_credito
                        AS "descripcionGarantiaCredito",

                    f.tipo_garantia
                        AS "tipoGarantia",

                    COALESCE(
                        r.valor_garantias_credito,
                        0
                    )
                        AS "valorGarantiasCredito",

                    COALESCE(
                        r.porcentaje_garantias_credito,
                        0
                    )
                        AS "porcentajeGarantiasCredito",

                    f.fecha_desembolso
                        AS "fechaDesembolso",

                    f.fecha_final
                        AS "fechaVencimiento",

                    COALESCE(
                        pe.saldo_actual,
                        0
                    )
                        AS "saldoCapital",

                    COALESCE(
                        pe.saldo_intereses_causados,
                        0
                    )
                        AS "saldoIntereses",

                    COALESCE(
                        pe.valor_otros_conceptos,
                        0
                    )
                        AS "saldoOtrosConceptos",

                    COALESCE(
                        pe.saldo_aportes_fecha_corte,
                        0
                    )
                        AS "saldoAportes",

                    COALESCE(
                        pe.valor_aportes_credito,
                        0
                    )
                        AS "valorAportesAplicados",

                    COALESCE(
                        pe.vea,
                        0
                    )
                        AS "vea",

                    COALESCE(
                        pe.pi,
                        0
                    )
                        AS "pi",

                    COALESCE(
                        pe.pdi,
                        0
                    )
                        AS "pdi",

                    COALESCE(
                        pe.perdida_esperada,
                        0
                    )
                        AS "perdidaEsperada",

                    COALESCE(
                        pe.porcentaje_perdida,
                        0
                    )
                        AS "porcentajePerdidaEsperada",

                    COALESCE(
                        pe.deterioro_capital_pe,
                        0
                    )
                        AS "deterioroCapital",

                    COALESCE(
                        pe.deterioro_intereses_pe,
                        0
                    )
                        AS "deterioroIntereses",

                    COALESCE(
                        pe.deterioro_otros_pe,
                        0
                    )
                        AS "deterioroOtros",

                    pe.dias_mora_actual
                        AS "diasMora",

                    r.edad_riesgo_inicial
                        AS "edadRiesgoInicial",

                    pe.edad_mora_entrada_pe
                        AS "edadDeMora",

                    pe.edad_riesgo_entrada_pe
                        AS "edadDeRiesgo",

                    pe.calificacion_pe
                        AS "edadPe",

                    pe.edad_homologada_individual
                        AS "edadHomologada",

                    pe.edad_contable_pe
                        AS "edadContable",

                    pe.codigo_forma_pago
                        AS "codigoFormaPago",

                    f.codigo_estado_juridico
                        AS "codigoEstadoJuridico",

                    (
                        pe.id_empresa_libranza IS NOT NULL
                        OR pe.codigo_forma_pago = 'L'
                    )
                        AS "esLibranza",

                    r.es_reestructurado
                        AS "esReestructurado",

                    r.codigo_metodo_calculo
                        AS "codigoMetodoCalculo"

                FROM cartera.pe_resultados pe

                INNER JOIN cartera.cierres_cartera c
                    ON c.id_cierre_cartera =
                       pe.id_cierre_cartera

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       pe.id_cierre_cartera_credito

                INNER JOIN cartera.cierres_cartera_resultados r
                    ON r.id_cierre_cartera =
                       pe.id_cierre_cartera

                   AND r.id_cierre_cartera_credito =
                       pe.id_cierre_cartera_credito

                WHERE pe.id_cierre_cartera = ?
                  AND pe.id_modelo_pe = ?

                ORDER BY
                    f.documento,
                    f.pagare_cartera,
                    f.id_cartera_credito
                """;

        return jdbcTemplate.query(
                sql,
                DataClassRowMapper.newInstance(
                        DetalleAnexo2DTO.class
                ),
                idCierreCartera,
                idModeloPe
        );
    }

    // =========================================================
    // HOJA: HOJA_TRABAJO
    //
    // Se consulta exclusivamente la trazabilidad persistida
    // en cartera.pe_resultados.
    //
    // No depende de tmp_pe_variables.
    // =========================================================

    public List<TrabajoAnexo2DTO> obtenerTrabajo(
            Integer idCierreCartera,
            Integer idModeloPe
    ) {

        String sql = """
                SELECT
                    pe.id_cierre_cartera
                        AS "idCierreCartera",

                    c.fecha_corte
                        AS "fechaCorte",

                    pe.id_cartera_credito
                        AS "idCarteraCredito",

                    pe.id_cierre_cartera_credito
                        AS "idCierreCarteraCredito",

                    f.id_datos_personal
                        AS "idDatosPersonal",

                    f.pagare_cartera
                        AS "pagareCartera",

                    f.documento
                        AS "documento",

                    TRIM(
                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(f.nombres), ''),
                            NULLIF(TRIM(f.primer_apellido), ''),
                            NULLIF(TRIM(f.segundo_apellido), '')
                        )
                    )
                        AS "nombreCompleto",

                    pe.id_modelo_pe
                        AS "idModeloPe",

                    pe.nombre_modelo_pe
                        AS "nombreModeloPe",

                    pe.codigo_clasificacion_credito
                        AS "codigoClasificacionCredito",

                    pe.codigo_forma_pago
                        AS "codigoFormaPago",

                    pe.id_empresa_libranza
                        AS "idEmpresaLibranza",

                    pe.tipo_persona
                        AS "tipoPersona",

                    pe.codigo_garantia_credito
                        AS "codigoGarantiaCredito",

                    pe.fecha_desembolso
                        AS "fechaDesembolso",

                    pe.dias_mora_actual
                        AS "diasMoraActual",

                    pe.edad_mora_entrada_pe
                        AS "edadMoraEntradaPe",

                    pe.edad_riesgo_entrada_pe
                        AS "edadRiesgoEntradaPe",

                    pe.saldo_actual
                        AS "saldoActual",

                    pe.saldo_intereses_causados
                        AS "saldoInteresesCausados",

                    pe.saldo_aportes_fecha_corte
                        AS "saldoAportesFechaCorte",

                    pe.valor_aportes_credito
                        AS "valorAportesCredito",

                    pe.valor_costas_judiciales
                        AS "valorCostasJudiciales",

                    pe.valor_otros_conceptos
                        AS "valorOtrosConceptos",

                    pe.mora_max_3m
                        AS "moraMax3m",

                    pe.mora_max_12m
                        AS "moraMax12m",

                    pe.mora_max_24m
                        AS "moraMax24m",

                    pe.mora_max_36m
                        AS "moraMax36m",

                    pe.cantidad_mora_31_60_3m
                        AS "cantidadMora3160_3m",

                    pe.ea
                        AS "ea",

                    pe.ea_contenido
                        AS "eaContenido",

                    pe.fe
                        AS "fe",

                    pe.fe_contenido
                        AS "feContenido",

                    pe.valcuota
                        AS "valcuota",

                    pe.valcuota_contenido
                        AS "valcuotaContenido",

                    pe.fondplazo
                        AS "fondplazo",

                    pe.fondplazo_contenido
                        AS "fondplazoContenido",

                    pe.mora1230
                        AS "mora1230",

                    pe.mora1230_contenido
                        AS "mora1230Contenido",

                    pe.mora1260
                        AS "mora1260",

                    pe.mora1260_contenido
                        AS "mora1260Contenido",

                    pe.sinmora
                        AS "sinmora",

                    pe.sinmora_contenido
                        AS "sinmoraContenido",

                    pe.mora2430n
                        AS "mora2430n",

                    pe.mora2430n_contenido
                        AS "mora2430nContenido",

                    pe.mora315
                        AS "mora315",

                    pe.mora315_contenido
                        AS "mora315Contenido",

                    pe.mortrim
                        AS "mortrim",

                    pe.mortrim_contenido
                        AS "mortrimContenido",

                    pe.mora3660
                        AS "mora3660",

                    pe.mora3660_mora_max_36m
                        AS "mora3660MoraMax36m",

                    pe.mora3660_mora_max_24m
                        AS "mora3660MoraMax24m",

                    pe.beta_intercepto
                        AS "betaIntercepto",

                    pe.beta_ea
                        AS "betaEa",

                    pe.beta_fe
                        AS "betaFe",

                    pe.beta_valcuota
                        AS "betaValcuota",

                    pe.beta_fondplazo
                        AS "betaFondplazo",

                    pe.beta_mora1230
                        AS "betaMora1230",

                    pe.beta_mora1260
                        AS "betaMora1260",

                    pe.beta_sinmora
                        AS "betaSinmora",

                    pe.beta_mora2430n
                        AS "betaMora2430n",

                    pe.beta_mora315
                        AS "betaMora315",

                    pe.beta_mortrim
                        AS "betaMortrim",

                    pe.beta_mora3660
                        AS "betaMora3660",

                    pe.aporte_z_intercepto
                        AS "aporteZIntercepto",

                    pe.aporte_z_ea
                        AS "aporteZEa",

                    pe.aporte_z_fe
                        AS "aporteZFe",

                    pe.aporte_z_valcuota
                        AS "aporteZValcuota",

                    pe.aporte_z_fondplazo
                        AS "aporteZFondplazo",

                    pe.aporte_z_mora1230
                        AS "aporteZMora1230",

                    pe.aporte_z_mora1260
                        AS "aporteZMora1260",

                    pe.aporte_z_sinmora
                        AS "aporteZSinmora",

                    pe.aporte_z_mora2430n
                        AS "aporteZMora2430n",

                    pe.aporte_z_mora315
                        AS "aporteZMora315",

                    pe.aporte_z_mortrim
                        AS "aporteZMortrim",

                    pe.aporte_z_mora3660
                        AS "aporteZMora3660",

                    pe.z
                        AS "z",

                    pe.puntaje
                        AS "puntaje",

                    pe.calificacion_modelo
                        AS "calificacionModelo",

                    pe.dias_default_modelo
                        AS "diasDefaultModelo",

                    pe.default_pe
                        AS "defaultPe",

                    pe.calificacion_pe
                        AS "calificacionPe",

                    pe.edad_deterioro
                        AS "edadDeterioro",

                    pe.tipo_entidad_pe
                        AS "tipoEntidadPe",

                    pe.calificacion_base_pi
                        AS "calificacionBasePi",

                    pe.pi
                        AS "pi",

                    pe.base_vea_capital
                        AS "baseVeaCapital",

                    pe.base_vea_intereses
                        AS "baseVeaIntereses",

                    pe.base_vea_costas_judiciales
                        AS "baseVeaCostasJudiciales",

                    pe.base_vea_otros
                        AS "baseVeaOtros",

                    pe.base_vea_aportes
                        AS "baseVeaAportes",

                    pe.base_vea_ahorro_permanente
                        AS "baseVeaAhorroPermanente",

                    pe.vea_bruto
                        AS "veaBruto",

                    pe.vea_deducciones
                        AS "veaDeducciones",

                    pe.vea
                        AS "vea",

                    pe.codigo_garantia_pdi
                        AS "codigoGarantiaPdi",

                    pe.nombre_garantia_pdi
                        AS "nombreGarantiaPdi",

                    pe.valor_garantia
                        AS "valorGarantia",

                    pe.porcentaje_garantia_reconocido
                        AS "porcentajeGarantiaReconocido",

                    pe.valor_garantia_reconocido
                        AS "valorGarantiaReconocido",

                    pe.dias_mora_pdi
                        AS "diasMoraPdi",

                    pe.tramo_pdi
                        AS "tramoPdi",

                    pe.dias_desde_pdi
                        AS "diasDesdePdi",

                    pe.dias_hasta_pdi
                        AS "diasHastaPdi",

                    pe.pdi
                        AS "pdi",

                    pe.perdida_esperada
                        AS "perdidaEsperada",

                    pe.porcentaje_perdida
                        AS "porcentajePerdida",

                    pe.base_deterioro_capital
                        AS "baseDeterioroCapital",

                    pe.base_deterioro_intereses
                        AS "baseDeterioroIntereses",

                    pe.base_deterioro_otros
                        AS "baseDeterioroOtros",

                    pe.base_deterioro_total
                        AS "baseDeterioroTotal",

                    pe.porcentaje_deterioro_capital
                        AS "porcentajeDeterioroCapital",

                    pe.porcentaje_deterioro_intereses
                        AS "porcentajeDeterioroIntereses",

                    pe.porcentaje_deterioro_otros
                        AS "porcentajeDeterioroOtros",

                    pe.valor_perdida_capital
                        AS "valorPerdidaCapital",

                    pe.valor_perdida_intereses
                        AS "valorPerdidaIntereses",

                    pe.valor_perdida_otros
                        AS "valorPerdidaOtros",

                    pe.deterioro_capital_pe
                        AS "deterioroCapitalPe",

                    pe.deterioro_intereses_pe
                        AS "deterioroInteresesPe",

                    pe.deterioro_otros_pe
                        AS "deterioroOtrosPe",

                    pe.deterioro_total_pe
                        AS "deterioroTotalPe",

                    pe.dias_mora_homologacion
                        AS "diasMoraHomologacion",

                    pe.edad_homologada_individual
                        AS "edadHomologadaIndividual",

                    pe.edad_contable_pe
                        AS "edadContablePe"

                FROM cartera.pe_resultados pe

                INNER JOIN cartera.cierres_cartera c
                    ON c.id_cierre_cartera =
                       pe.id_cierre_cartera

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       pe.id_cierre_cartera_credito

                WHERE pe.id_cierre_cartera = ?
                  AND pe.id_modelo_pe = ?

                ORDER BY
                    f.documento,
                    f.pagare_cartera,
                    pe.id_cartera_credito
                """;

        return jdbcTemplate.query(
                sql,
                DataClassRowMapper.newInstance(
                        TrabajoAnexo2DTO.class
                ),
                idCierreCartera,
                idModeloPe
        );
    }

    // =========================================================
    // HOJA: MORA
    //
    // Se reconstruye el mismo vector histórico utilizado por PE.
    //
    // Máximo:
    //
    // corte seleccionado
    // + 39 meses anteriores
    //
    // La salida permanece normalizada:
    //
    // crédito + periodo + fecha + mora
    //
    // El Excel hará posteriormente el pivote horizontal.
    // =========================================================

    public List<MoraAnexo2DTO> obtenerMora(
            Integer idCierreCartera,
            Integer idModeloPe
    ) {

        String sql = """
                WITH parametros AS
                (
                    SELECT
                        id_cierre_cartera,
                        fecha_corte

                    FROM cartera.cierres_cartera

                    WHERE id_cierre_cartera = ?
                ),

                cierres_40 AS
                (
                    SELECT
                        ch.id_cierre_cartera,
                        ch.fecha_corte,

                        ROW_NUMBER() OVER (
                            ORDER BY ch.fecha_corte
                        )::integer
                            AS numero_periodo

                    FROM cartera.cierres_cartera ch

                    CROSS JOIN parametros p

                    WHERE ch.fecha_corte BETWEEN
                          (
                              p.fecha_corte
                              - INTERVAL '39 months'
                          )::date
                          AND p.fecha_corte
                ),

                poblacion AS
                (
                    SELECT
                        pe.id_cierre_cartera,
                        pe.id_cierre_cartera_credito,
                        pe.id_cartera_credito,
                        pe.id_modelo_pe,
                        pe.nombre_modelo_pe,

                        f.pagare_cartera,
                        f.documento,

                        TRIM(
                            CONCAT_WS(
                                ' ',
                                NULLIF(TRIM(f.nombres), ''),
                                NULLIF(TRIM(f.primer_apellido), ''),
                                NULLIF(TRIM(f.segundo_apellido), '')
                            )
                        )
                            AS nombre_completo

                    FROM cartera.pe_resultados pe

                    INNER JOIN cartera.cierres_cartera_creditos f
                        ON f.id_cierre_cartera_credito =
                           pe.id_cierre_cartera_credito

                    WHERE pe.id_cierre_cartera = ?
                      AND pe.id_modelo_pe = ?
                )

                SELECT
                    p.id_cierre_cartera
                        AS "idCierreCartera",

                    pc.fecha_corte
                        AS "fechaCorte",

                    p.id_cartera_credito
                        AS "idCarteraCredito",

                    p.id_cierre_cartera_credito
                        AS "idCierreCarteraCredito",

                    p.pagare_cartera
                        AS "pagareCartera",

                    p.documento
                        AS "documento",

                    p.nombre_completo
                        AS "nombreCompleto",

                    p.id_modelo_pe
                        AS "idModeloPe",

                    p.nombre_modelo_pe
                        AS "nombreModeloPe",

                    h.numero_periodo
                        AS "periodo",

                    h.fecha_corte
                        AS "fechaReferencia",

                    COALESCE(
                        rh.dias_mora,
                        0
                    )::integer
                        AS "diasMora"

                FROM poblacion p

                INNER JOIN parametros pc
                    ON pc.id_cierre_cartera =
                       p.id_cierre_cartera

                CROSS JOIN cierres_40 h

                LEFT JOIN cartera.cierres_cartera_creditos fh
                    ON fh.id_cierre_cartera =
                       h.id_cierre_cartera

                   AND fh.id_cartera_credito =
                       p.id_cartera_credito

                LEFT JOIN cartera.cierres_cartera_resultados rh
                    ON rh.id_cierre_cartera_credito =
                       fh.id_cierre_cartera_credito

                ORDER BY
                    p.documento,
                    p.pagare_cartera,
                    h.numero_periodo
                """;

        return jdbcTemplate.query(
                sql,
                DataClassRowMapper.newInstance(
                        MoraAnexo2DTO.class
                ),
                idCierreCartera,
                idCierreCartera,
                idModeloPe
        );
    }

    // =========================================================
    // HOJA: RESUMEN
    //
    // CALIFICACION
    // CANTIDAD
    // % CANTIDAD
    // SALDO
    // % SALDO
    // PE
    // % PE
    // =========================================================

    public List<ResumenAnexo2DTO> obtenerResumen(
            Integer idCierreCartera,
            Integer idModeloPe
    ) {

        String sql = """
                WITH detalle AS
                (
                    SELECT
                        pe.id_cierre_cartera,
                        pe.id_modelo_pe,
                        pe.nombre_modelo_pe,

                        COALESCE(
                            pe.edad_contable_pe,
                            'A'
                        )
                            AS calificacion,

                        COUNT(*)::integer
                            AS cantidad_creditos,

                        SUM(
                            COALESCE(
                                pe.saldo_actual,
                                0
                            )
                        )
                            AS saldo_capital,

                        SUM(
                            COALESCE(
                                pe.perdida_esperada,
                                0
                            )
                        )
                            AS perdida_esperada

                    FROM cartera.pe_resultados pe

                    WHERE pe.id_cierre_cartera = ?
                      AND pe.id_modelo_pe = ?

                    GROUP BY
                        pe.id_cierre_cartera,
                        pe.id_modelo_pe,
                        pe.nombre_modelo_pe,
                        COALESCE(
                            pe.edad_contable_pe,
                            'A'
                        )
                ),

                totales AS
                (
                    SELECT
                        SUM(cantidad_creditos)
                            AS total_cantidad,

                        SUM(saldo_capital)
                            AS total_saldo,

                        SUM(perdida_esperada)
                            AS total_pe

                    FROM detalle
                )

                SELECT
                    d.id_cierre_cartera
                        AS "idCierreCartera",

                    c.fecha_corte
                        AS "fechaCorte",

                    d.id_modelo_pe
                        AS "idModeloPe",

                    d.nombre_modelo_pe
                        AS "nombreModeloPe",

                    d.calificacion
                        AS "calificacion",

                    d.cantidad_creditos
                        AS "cantidadCreditos",

                    CASE
                        WHEN t.total_cantidad > 0
                            THEN ROUND(
                                (
                                    d.cantidad_creditos::numeric
                                    * 100
                                )
                                /
                                t.total_cantidad,
                                2
                            )

                        ELSE 0
                    END
                        AS "porcentajeCantidad",

                    d.saldo_capital
                        AS "saldoCapital",

                    CASE
                        WHEN t.total_saldo <> 0
                            THEN ROUND(
                                (
                                    d.saldo_capital
                                    * 100
                                )
                                /
                                t.total_saldo,
                                2
                            )

                        ELSE 0
                    END
                        AS "porcentajeSaldo",

                    d.perdida_esperada
                        AS "perdidaEsperada",

                    CASE
                        WHEN t.total_pe <> 0
                            THEN ROUND(
                                (
                                    d.perdida_esperada
                                    * 100
                                )
                                /
                                t.total_pe,
                                2
                            )

                        ELSE 0
                    END
                        AS "porcentajePerdidaEsperada"

                FROM detalle d

                INNER JOIN cartera.cierres_cartera c
                    ON c.id_cierre_cartera =
                       d.id_cierre_cartera

                CROSS JOIN totales t

                ORDER BY
                    CASE d.calificacion
                        WHEN 'A' THEN 1
                        WHEN 'B' THEN 2
                        WHEN 'C' THEN 3
                        WHEN 'D' THEN 4
                        WHEN 'E' THEN 5
                        ELSE 6
                    END
                """;

        return jdbcTemplate.query(
                sql,
                DataClassRowMapper.newInstance(
                        ResumenAnexo2DTO.class
                ),
                idCierreCartera,
                idModeloPe
        );
    }
}