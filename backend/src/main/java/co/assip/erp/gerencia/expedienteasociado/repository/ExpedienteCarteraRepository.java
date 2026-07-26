package co.assip.erp.gerencia.expedienteasociado.repository;

import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteCreditoDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteGarantiaDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de consulta del bloque de cartera
 * del Expediente Integral del Asociado.
 *
 * Características:
 *
 * - Solo lectura.
 * - Consulta créditos desde las vistas de reporting.
 * - No modifica información de cartera.
 * - Las garantías permanecen pendientes de implementación definitiva.
 */
@Repository
public class ExpedienteCarteraRepository {

    private static final String PARAM_ID_DATOS_PERSONAL =
            "idDatosPersonal";

    private final NamedParameterJdbcTemplate jdbc;

    public ExpedienteCarteraRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // RowMapper
    // =========================================================

    private static final BeanPropertyRowMapper<ExpedienteCreditoDTO>
            CREDITO_MAPPER =
            mapper(ExpedienteCreditoDTO.class);

    private static <T> BeanPropertyRowMapper<T> mapper(
            Class<T> tipo
    ) {
        BeanPropertyRowMapper<T> mapper =
                BeanPropertyRowMapper.newInstance(tipo);

        /*
         * Permite que el DTO tenga más propiedades que las columnas
         * seleccionadas. Los atributos no disponibles quedan en null.
         */
        mapper.setCheckFullyPopulated(false);

        /*
         * Facilita el mapeo de columnas snake_case hacia propiedades
         * camelCase.
         */
        mapper.setPrimitivesDefaultedForNullValue(true);

        return mapper;
    }

    // =========================================================
    // SQL: créditos
    // =========================================================

    private static final String SQL_CREDITOS = """
            SELECT
                c.id_datos_personal,

                c.asociado_tipo_documento
                    AS tipo_documento,

                c.asociado_nombre_tipo_documento
                    AS nombre_tipo_documento,

                c.asociado_documento
                    AS documento,

                c.asociado_nombres
                    AS nombres,

                c.asociado_primer_apellido
                    AS primer_apellido,

                c.asociado_segundo_apellido
                    AS segundo_apellido,

                c.asociado_nombre_completo_apellidos
                    AS nombre_completo,

                c.id_cartera_credito
                    AS id_credito,

                c.pagare_cartera
                    AS numero_credito,

                c.pagare_cartera
                    AS referencia_credito,

                c.id_linea_credito,
                c.codigo_linea_credito,
                c.nombre_linea_credito,

                c.codigo_clasificacion_credito,
                c.descripcion_clasificacion_credito
                    AS nombre_clasificacion_credito,

                c.codigo_estado_cartera,
                c.descripcion_estado_cartera
                    AS nombre_estado_cartera,

                (
                    COALESCE(c.saldo_neto_pendiente, 0) > 0
                    AND COALESCE(c.credito_saldado, false) = false
                ) AS vigente,

                COALESCE(c.credito_saldado, false)
                    AS cancelado,

                COALESCE(c.credito_en_mora, false)
                    AS en_mora,

                COALESCE(c.credito_reestructurado, false)
                    AS reestructurado,

                COALESCE(c.credito_novado, false)
                    AS novado,

                c.fecha_desembolso,

                c.fecha_primera_cuota
                    AS fecha_primer_vencimiento,

                c.fecha_final
                    AS fecha_ultimo_vencimiento,

                c.proxima_fecha_capital
                    AS fecha_proxima_cuota,

                c.dias_desde_desembolso,

                c.dias_para_proximo_capital
                    AS dias_para_proxima_cuota,

                c.valor_inicial_credito
                    AS valor_aprobado,

                c.valor_desembolsado,

                c.plazo
                    AS plazo_inicial,

                c.plazo
                    AS plazo_actual,

                c.altura_cuota
                    AS numero_cuotas_pagadas,

                c.cuotas_pendientes_estimadas
                    AS numero_cuotas_pendientes,

                c.codigo_tipo_cuota,

                c.descripcion_tipo_cuota
                    AS nombre_tipo_cuota,

                c.codigo_forma_pago,

                c.descripcion_forma_pago
                    AS nombre_forma_pago,

                c.valor_primera_cuota
                    AS valor_cuota_inicial,

                c.valor_cuota
                    AS valor_cuota_actual,

                c.tipo_modalidad_interes
                    AS codigo_modalidad_interes,

                c.descripcion_modalidad_interes
                    AS nombre_modalidad_interes,

                c.tasa_nominal_anual
                    AS tasa_nominal,

                c.tasa_efectiva_anual,

                c.saldo_actual
                    AS saldo_capital,

                c.saldo_neto_pendiente
                    AS saldo_total,

                c.edad_riesgo_inicial,

                c.edad_de_riesgo
                    AS edad_riesgo_actual,

                c.edad_de_mora
                    AS edad_riesgo_evaluada,

                c.edad_contable
                    AS edad_riesgo_final,

                c.descripcion_edad_de_riesgo
                    AS nombre_calificacion,

                c.fecha_evaluacion
                    AS fecha_ultima_evaluacion,

                c.codigo_garantia_credito
                    AS codigo_tipo_garantia_credito,

                c.descripcion_garantia_credito
                    AS nombre_tipo_garantia_credito,

                (
                    c.tipo_garantia IS NOT NULL
                    AND upper(trim(c.tipo_garantia)) <> 'P'
                ) AS tiene_garantia_real,

                c.patrimonio_respaldable_erp
                    AS valor_garantias,

                c.patrimonio_porcentaje_cobertura_credito
                    AS porcentaje_cobertura_garantias,

                (
                    COALESCE(
                        c.patrimonio_porcentaje_cobertura_credito,
                        0
                    ) >= 100
                ) AS garantia_suficiente,

                (
                    COALESCE(
                        c.patrimonio_porcentaje_cobertura_credito,
                        0
                    ) < 100
                ) AS garantia_insuficiente,

                c.patrimonio_saldo_ahorros_activos
                    AS valor_ahorros_respaldo,

                c.patrimonio_saldo_cdat_vigentes
                    AS valor_cdat_respaldo,

                c.patrimonio_financiero_erp
                    AS total_respaldos_liquidos,

                c.codigo_modificacion_credito,

                c.descripcion_modificacion_credito
                    AS nombre_modificacion_credito,

                c.fecha_reestructuracion
                    AS fecha_ultima_modificacion,

                c.numero_novaciones
                    AS cantidad_modificaciones,

                c.codigo_estado_juridico,

                c.descripcion_estado_juridico
                    AS nombre_estado_juridico,

                c.en_cobro_juridico
                    AS juridico,

                c.financiero_ingresos_totales
                    AS ingresos_mensuales,

                c.financiero_egresos_totales
                    AS egresos_mensuales,

                c.financiero_disponibilidad_mensual
                    AS capacidad_pago_mensual,

                c.financiero_patrimonio_neto
                    AS patrimonio_neto,

                c.financiero_porcentaje_cuotas_ingresos
                    AS porcentaje_cuota_ingresos,

                c.financiero_capacidad_pago
                    AS calificacion_capacidad_pago,

                c.patrimonio_nivel_respaldo
                    AS nivel_cobertura_patrimonial,

                c.patrimonio_nivel_alerta
                    AS nivel_alerta,

                c.patrimonio_motivo_alerta
                    AS resumen_alertas,

                c.requiere_revision,

                c.fk_seguridad_creacion,
                c.fecha_creacion::date AS fecha_creacion,
                c.fk_seguridad_edicion,
                c.fecha_edicion::date AS fecha_edicion

            FROM reporting.vw_cartera_creditos_patrimonio c
            WHERE c.id_datos_personal = :idDatosPersonal

            ORDER BY
                COALESCE(c.credito_saldado, false),
                c.fecha_desembolso DESC NULLS LAST,
                c.id_cartera_credito DESC
            """;

    // =========================================================
    // Consultas públicas
    // =========================================================

    public List<ExpedienteCreditoDTO> listarCreditos(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return jdbc.query(
                SQL_CREDITOS,
                parametros(idDatosPersonal),
                CREDITO_MAPPER
        );
    }

    /**
     * Las garantías definitivas se incorporarán cuando estén terminados:
     *
     * - proceso de evaluación de cartera;
     * - pérdida esperada;
     * - admisibilidad;
     * - cobertura reconocida;
     * - vinculación definitiva crédito-bien.
     *
     * Por ahora se retorna una lista vacía para no publicar información
     * incompleta o calculada con reglas provisionales.
     */
    public List<ExpedienteGarantiaDTO> listarGarantias(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return List.of();
    }

    // =========================================================
    // Utilidades internas
    // =========================================================

    private MapSqlParameterSource parametros(
            Long idDatosPersonal
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        PARAM_ID_DATOS_PERSONAL,
                        idDatosPersonal
                );
    }

    private void validarIdDatosPersonal(
            Long idDatosPersonal
    ) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException(
                    "El idDatosPersonal debe ser mayor que cero."
            );
        }
    }
}