
package co.assip.erp.contabilidad.comprobantes.consulta;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Consulta general de comprobantes contables de todos los módulos.
 * Respeta el alcance de agencias validado por el servicio.
 * No modifica registros ni habilita la edición de comprobantes automáticos.
 */
@Repository
@RequiredArgsConstructor
public class ComprobantesConsultaRepository {
    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // MODELOS DE RESPUESTA
    // =========================================================
    /** Resumen de comprobantes para resultados de búsqueda. */
    public record ResumenComprobante(
            Integer idAgencia,
            String codigoAgencia,
            String nombreAgencia,
            String tipoComprobante,
            String nombreTipoComprobante,
            String numeroComprobante,
            LocalDate fechaContable,
            String conceptoComprobante,
            String estadoOrigen,
            BigDecimal totalDebito,
            BigDecimal totalCredito,
            long cantidadMovimientos
    ) {}

    /** Cabecera del comprobante, incluyendo información de origen. */
    public record CabeceraComprobante(
            Integer idAgencia,
            String codigoAgencia,
            String nombreAgencia,
            String tipoComprobante,
            String nombreTipoComprobante,
            String numeroComprobante,
            String conceptoComprobante,
            String origenTipo,
            String moduloOrigen,
            String procesoOrigen,
            String estadoOrigen,
            LocalDate fechaContable
    ) {}

    /** Movimiento contable con información descriptiva. */
    public record MovimientoComprobante(
            Integer idAuxiliarContable,
            Integer idCatalogoCuenta,
            String codigoCuenta,
            String nombreCuenta,
            Integer idDatosPersonal,
            String documentoTercero,
            String nombreTercero,
            LocalDate fechaAuxiliar,
            String detalleMovimiento,
            String estadoMovimiento,
            BigDecimal valorDebito,
            BigDecimal valorCredito,
            BigDecimal valorBase,
            Integer idControlesAuxiliares,
            Integer idTiposControl,
            String numeroDocumento
    ) {}

    // =========================================================
    // BÚSQUEDA GENERAL DE COMPROBANTES
    // =========================================================
    /**
     * Busca comprobantes de todos los módulos y estados.
     * Incluye registros sin auxiliares y limita a 100 resultados.
     */
    public List<ResumenComprobante> buscar(
            List<Integer> agenciasAutorizadas,
            String tipoComprobante,
            String numeroComprobante,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String concepto,
            Integer idDatosPersonal
    ) {
        if (agenciasAutorizadas == null || agenciasAutorizadas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe existir al menos una agencia autorizada."
            );
        }
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede superar la final."
            );
        }
        String sql = """
            SELECT
                o.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,
                TRIM(o.tipo_comprobante) AS tipo_comprobante,
                tc.nombre_tipo_comprobante,
                TRIM(o.numero_comprobante) AS numero_comprobante,
                MIN(a.fecha_auxiliar) AS fecha_contable,
                c.concepto_comprobante,
                o.estado_origen,
                COALESCE(SUM(a.valor_debito), 0) AS total_debito,
                COALESCE(SUM(a.valor_credito), 0) AS total_credito,
                COUNT(a.id_auxiliar_contable) AS cantidad_movimientos
            FROM contabilidad.origen_comprobantes o
            LEFT JOIN contabilidad.conceptos_contables c
                ON c.id_agencia = o.id_agencia
                AND c.tipo_comprobante = o.tipo_comprobante
                AND c.numero_comprobante = o.numero_comprobante
            LEFT JOIN contabilidad.auxiliares_contables a
                ON a.id_agencia = o.id_agencia
                AND a.tipo_comprobante = o.tipo_comprobante
                AND a.numero_comprobante = o.numero_comprobante
            LEFT JOIN general.datos_agencias ag
                ON ag.id_agencia = o.id_agencia
            LEFT JOIN contabilidad.tipos_comprobantes tc
                ON tc.id_agencia = o.id_agencia
                AND TRIM(tc.tipo_comprobante) = TRIM(o.tipo_comprobante)
            WHERE o.id_agencia IN (:agencias)
                AND (
                    CAST(:tipo AS text) IS NULL
                    OR TRIM(o.tipo_comprobante) = :tipo
                )
                AND (
                    CAST(:numero AS text) IS NULL
                    OR TRIM(o.numero_comprobante) = :numero
                )
                AND (
                    CAST(:concepto AS text) IS NULL
                    OR c.concepto_comprobante ILIKE '%' || :concepto || '%'
                )
                AND (
                    CAST(:tercero AS integer) IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM contabilidad.auxiliares_contables at
                        WHERE at.id_agencia = o.id_agencia
                            AND at.tipo_comprobante = o.tipo_comprobante
                            AND at.numero_comprobante = o.numero_comprobante
                            AND at.id_datos_personal = :tercero
                    )
                )
            GROUP BY
                o.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,
                o.tipo_comprobante,
                tc.nombre_tipo_comprobante,
                o.numero_comprobante,
                c.concepto_comprobante,
                o.estado_origen
            HAVING (
                CAST(:desde AS date) IS NULL
                OR MIN(a.fecha_auxiliar) >= :desde
            )
            AND (
                CAST(:hasta AS date) IS NULL
                OR MAX(a.fecha_auxiliar) <= :hasta
            )
            ORDER BY
                MIN(a.fecha_auxiliar) DESC NULLS LAST,
                o.tipo_comprobante,
                o.numero_comprobante DESC
            LIMIT 100
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("agencias", agenciasAutorizadas)
                .addValue("tipo", normalizar(tipoComprobante))
                .addValue("numero", normalizar(numeroComprobante))
                .addValue("desde", fechaDesde)
                .addValue("hasta", fechaHasta)
                .addValue("concepto", normalizar(concepto))
                .addValue("tercero", idDatosPersonal);
        return jdbc.query(sql, params, (rs, rowNum) -> new ResumenComprobante(
                rs.getInt("id_agencia"),
                rs.getString("codigo_agencia"),
                rs.getString("nombre_agencia"),
                rs.getString("tipo_comprobante"),
                rs.getString("nombre_tipo_comprobante"),
                rs.getString("numero_comprobante"),
                rs.getObject("fecha_contable", LocalDate.class),
                rs.getString("concepto_comprobante"),
                rs.getString("estado_origen"),
                rs.getBigDecimal("total_debito"),
                rs.getBigDecimal("total_credito"),
                rs.getLong("cantidad_movimientos")
        ));
    }

    // =========================================================
    // CABECERA DEL COMPROBANTE
    // =========================================================
    /**
     * Recupera la cabecera de cualquier comprobante registrado.
     * No exige origen manual ni movimientos activos.
     */
    public Optional<CabeceraComprobante> consultarCabecera(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante
    ) {
        String sql = """
            SELECT
                o.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,
                TRIM(o.tipo_comprobante) AS tipo_comprobante,
                tc.nombre_tipo_comprobante,
                TRIM(o.numero_comprobante) AS numero_comprobante,
                c.concepto_comprobante,
                o.origen_tipo,
                o.modulo_origen,
                o.proceso_origen,
                o.estado_origen,
                MIN(a.fecha_auxiliar) AS fecha_contable
            FROM contabilidad.origen_comprobantes o
            LEFT JOIN contabilidad.conceptos_contables c
                ON c.id_agencia = o.id_agencia
                AND c.tipo_comprobante = o.tipo_comprobante
                AND c.numero_comprobante = o.numero_comprobante
            LEFT JOIN contabilidad.auxiliares_contables a
                ON a.id_agencia = o.id_agencia
                AND a.tipo_comprobante = o.tipo_comprobante
                AND a.numero_comprobante = o.numero_comprobante
            LEFT JOIN general.datos_agencias ag
                ON ag.id_agencia = o.id_agencia
            LEFT JOIN contabilidad.tipos_comprobantes tc
                ON tc.id_agencia = o.id_agencia
                AND TRIM(tc.tipo_comprobante) = TRIM(o.tipo_comprobante)
            WHERE o.id_agencia = :agencia
                AND TRIM(o.tipo_comprobante) = :tipo
                AND TRIM(o.numero_comprobante) = :numero
            GROUP BY
                o.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,
                o.tipo_comprobante,
                tc.nombre_tipo_comprobante,
                o.numero_comprobante,
                c.concepto_comprobante,
                o.origen_tipo,
                o.modulo_origen,
                o.proceso_origen,
                o.estado_origen
            """;
        List<CabeceraComprobante> resultado = jdbc.query(
                sql,
                clave(idAgencia, tipoComprobante, numeroComprobante),
                (rs, rowNum) -> new CabeceraComprobante(
                        rs.getInt("id_agencia"),
                        rs.getString("codigo_agencia"),
                        rs.getString("nombre_agencia"),
                        rs.getString("tipo_comprobante"),
                        rs.getString("nombre_tipo_comprobante"),
                        rs.getString("numero_comprobante"),
                        rs.getString("concepto_comprobante"),
                        rs.getString("origen_tipo"),
                        rs.getString("modulo_origen"),
                        rs.getString("proceso_origen"),
                        rs.getString("estado_origen"),
                        rs.getObject("fecha_contable", LocalDate.class)
                )
        );
        return resultado.stream().findFirst();
    }

    // =========================================================
    // MOVIMIENTOS CONTABLES
    // =========================================================
    /**
     * Recupera los movimientos de cualquier comprobante.
     * Incluye movimientos no activos para consultar su historial.
     */
    public List<MovimientoComprobante> consultarMovimientos(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante
    ) {
        String sql = """
            SELECT
                a.id_auxiliar_contable,
                a.id_catalogo_cuenta,
                cc.codigo_cuenta,
                cc.nombre_cuenta,
                a.id_datos_personal,
                dp.documento AS documento_tercero,
                NULLIF(
                    TRIM(
                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(dp.nombres), ''),
                            NULLIF(TRIM(dp.primer_apellido), ''),
                            NULLIF(TRIM(dp.segundo_apellido), '')
                        )
                    ),
                    ''
                ) AS nombre_tercero,
                a.fecha_auxiliar,
                a.detalle_movimiento,
                TRIM(a.estado_movimiento) AS estado_movimiento,
                a.valor_debito,
                a.valor_credito,
                a.valor_base,
                ca.id_controles_auxiliares,
                ca.id_tipos_control,
                ca.numero_documento
            FROM contabilidad.auxiliares_contables a
            LEFT JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = a.id_catalogo_cuenta
                AND cc.id_agencia = a.id_agencia
            LEFT JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal = a.id_datos_personal
            LEFT JOIN contabilidad.controles_auxiliares ca
                ON ca.id_auxiliar_contable = a.id_auxiliar_contable
            WHERE a.id_agencia = :agencia
                AND TRIM(a.tipo_comprobante) = :tipo
                AND TRIM(a.numero_comprobante) = :numero
            ORDER BY
                a.id_auxiliar_contable,
                ca.id_controles_auxiliares
            """;
        return jdbc.query(
                sql,
                clave(idAgencia, tipoComprobante, numeroComprobante),
                (rs, rowNum) -> new MovimientoComprobante(
                        rs.getInt("id_auxiliar_contable"),
                        rs.getInt("id_catalogo_cuenta"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta"),
                        (Integer) rs.getObject("id_datos_personal"),
                        rs.getString("documento_tercero"),
                        rs.getString("nombre_tercero"),
                        rs.getObject("fecha_auxiliar", LocalDate.class),
                        rs.getString("detalle_movimiento"),
                        rs.getString("estado_movimiento"),
                        rs.getBigDecimal("valor_debito"),
                        rs.getBigDecimal("valor_credito"),
                        rs.getBigDecimal("valor_base"),
                        (Integer) rs.getObject("id_controles_auxiliares"),
                        (Integer) rs.getObject("id_tipos_control"),
                        rs.getString("numero_documento")
                )
        );
    }

    // =========================================================
    // PARÁMETROS DE CONSULTA
    // =========================================================
    /** Construye los parámetros de identificación del comprobante. */
    private MapSqlParameterSource clave(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante
    ) {
        return new MapSqlParameterSource()
                .addValue("agencia", idAgencia)
                .addValue("tipo", normalizar(tipoComprobante))
                .addValue("numero", normalizar(numeroComprobante));
    }

    /** Normaliza los filtros opcionales sin modificar los datos originales. */
    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
