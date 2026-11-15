package co.assip.erp.ses.formato9027;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class Formato9027Repository {
    private final NamedParameterJdbcTemplate jdbc;

    public boolean existeCierre(LocalDate fecha) {
        Integer total = jdbc.queryForObject("""
                SELECT count(*) FROM cartera.cierres_cartera
                WHERE fecha_corte = :fecha AND estado_cierre = 'C'
                """, new MapSqlParameterSource("fecha", fecha), Integer.class);
        return total != null && total > 0;
    }

    /** Una fila por crédito; los bienes se ordenan por garantía asignada. */
    public List<Map<String, Object>> consultar(LocalDate fecha) {
        return jdbc.queryForList("""
                SELECT
                    v.id_cierre_cartera_credito AS id_credito_cierre,
                    v.id_cartera_credito AS id_credito,
                    v.id_agencia AS agencia,
                    v.codigo_clasificacion_credito AS clasificacion,
                    v.codigo_forma_pago AS forma_pago,
                    v.tipo_garantia AS tipo_garantia,
                    v.edad_contable_resultado AS edad_contable,
                    v.tipo_documento AS tipo_documento,
                    v.documento AS documento,
                    v.codigo_modificacion_credito AS modificacion,
                    da.codigo_agencia AS codigo_agencia,
                    v.codigo_linea_credito AS codigo_linea,
                    v.pagare_cartera AS pagare,
                    v.fecha_desembolso AS desembolso,
                    v.fecha_final AS vencimiento,
                    v.dias_mora AS dias_mora,
                    v.codigo_tipo_cuota AS tipo_cuota,
                    v.altura_cuota AS altura,
                    v.amortizacion_capital AS amortizacion,
                    v.tipo_modalidad_interes AS modalidad,
                    v.periodo_meses_interes AS periodo_interes,
                    v.tasa_efectiva_anual AS tasa_efectiva,
                    v.tasa_nominal_anual AS tasa_nominal,
                    v.valor_inicial_credito AS valor_inicial,
                    v.valor_primera_cuota AS primera_cuota,
                    v.valor_cuota AS cuota,
                    v.saldo_credito_fecha_corte AS saldo,
                    v.saldo_intereses_causados AS intereses,
                    v.valor_costas_judiciales AS costas,
                    v.valor_garantias_credito AS garantia,
                    v.deterioro_capital AS deterioro_capital,
                    v.deterioro_intereses AS deterioro_intereses,
                    v.saldo_intereses_contingentes AS contingencia,
                    v.ultima_fecha_capital AS ultimo_pago_capital,
                    v.codigo_garantia_credito AS clase_garantia,
                    v.codigo_destino_economico AS destino,
                    v.valor_aportes_credito AS aportes,
                    v.nombre_linea_credito AS linea_nombre,
                    v.numero_novaciones AS novaciones,
                    v.codigo_estado_juridico AS estado_juridico,
                    v.id_empresa_libranza AS empresa_libranza,
                    v.documento_empresa_libranza AS documento_libranza,
                    NULLIF(TRIM(CONCAT_WS(' ', dpel.nombres,
                        dpel.primer_apellido, dpel.segundo_apellido)), '') AS nombre_libranza,
                    v.credito_reestructurado AS reestructurado,
                    v.fecha_reestructuracion AS fecha_reestructuracion,
                    v.plazo AS plazo,
                    v.edad_de_riesgo_resultado AS categoria,
                    garantia_bien.id_bien AS id_bien,
                    garantia_bien.fecha_ultimo_avaluo AS fecha_avaluo
                FROM cartera.vw_cartera_resultados_mensuales_total v
                JOIN general.datos_agencias da ON da.id_agencia = v.id_agencia
                LEFT JOIN depositos.empresas_libranza el
                    ON el.id_empresa_libranza = v.id_empresa_libranza
                LEFT JOIN hoja_vida.datos_personales dpel
                    ON dpel.id_datos_personal = el.id_datos_personal
                LEFT JOIN LATERAL (
                    SELECT g.id_bien, g.fecha_ultimo_avaluo
                    FROM cartera.cierres_cartera_resultados_garantias g
                    WHERE g.id_cierre_cartera = v.id_cierre_cartera
                      AND g.id_cierre_cartera_credito = v.id_cierre_cartera_credito
                      AND g.id_bien IS NOT NULL
                    ORDER BY g.valor_garantia_credito_bien DESC NULLS LAST,
                             g.id_bien ASC
                    LIMIT 1
                ) garantia_bien ON TRUE
                WHERE v.fecha_corte = :fecha
                  AND v.estado_cierre = 'C'
                  AND v.saldo_credito_fecha_corte > 0
                ORDER BY v.id_agencia, v.codigo_linea_credito,
                         v.pagare_cartera, v.id_cierre_cartera_credito
                """, new MapSqlParameterSource("fecha", fecha));
    }
    /** Función financiera existente; no se modifica su fórmula. */
    public BigDecimal cuotaFija(int periodoMeses, int plazo,
                               BigDecimal tasa, BigDecimal valorInicial) {
        return jdbc.queryForObject("""
                SELECT cartera.fn_cuota_fija(:periodo, :plazo, :tasa, :valor)
                """, new MapSqlParameterSource()
                    .addValue("periodo", periodoMeses)
                    .addValue("plazo", plazo)
                    .addValue("tasa", tasa)
                    .addValue("valor", valorInicial), BigDecimal.class);
    }

}
