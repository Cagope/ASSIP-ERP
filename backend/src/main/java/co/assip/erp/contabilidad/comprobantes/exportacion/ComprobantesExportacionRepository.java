package co.assip.erp.contabilidad.comprobantes.exportacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Recupera datos contables para exportación, sin modificar registros ni limitar resultados. */
@Repository
@RequiredArgsConstructor
public class ComprobantesExportacionRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    /** Cabecera, origen, importes y auditoría de un comprobante. */
    public record CabeceraExportacion(
            Integer idOrigenComprobante, Integer idAgencia, String codigoAgencia,
            String nombreAgencia, String tipoComprobante, String nombreTipoComprobante,
            String numeroComprobante, LocalDate fechaContable, String conceptoComprobante,
            String origenTipo, String moduloOrigen, String procesoOrigen, String tablaOrigen,
            Long idOrigen, String estadoOrigen, BigDecimal totalDebito,
            BigDecimal totalCredito, Long cantidadMovimientos,
            Integer idUsuarioCreacionOrigen, String usuarioCreacionOrigen,
            String nombreCreacionOrigen, LocalDateTime fechaCreacionOrigen,
            Integer idUsuarioEdicionOrigen, String usuarioEdicionOrigen,
            String nombreEdicionOrigen, LocalDateTime fechaEdicionOrigen,
            Integer idUsuarioCreacionConcepto, String usuarioCreacionConcepto,
            String nombreCreacionConcepto, LocalDateTime fechaCreacionConcepto,
            Integer idUsuarioEdicionConcepto, String usuarioEdicionConcepto,
            String nombreEdicionConcepto, LocalDateTime fechaEdicionConcepto
    ) {}

    /** Movimiento contable individual, sin multiplicar importes por controles documentales. */
    public record MovimientoExportacion(
            Integer idAuxiliarContable, Integer idAgencia, String tipoComprobante,
            String numeroComprobante, Integer idCatalogoCuenta, String codigoCuenta,
            String nombreCuenta, Integer idDatosPersonal, String documentoTercero,
            String nombreTercero, LocalDate fechaAuxiliar, String detalleMovimiento,
            String estadoMovimiento, BigDecimal valorDebito, BigDecimal valorCredito,
            BigDecimal valorBase, String origenModulo, String documentoOrigen,
            String documentoSoporte, Integer idUsuarioCreacion, String usuarioCreacion,
            String nombreCreacion, LocalDateTime fechaCreacion,
            Integer idUsuarioEdicion, String usuarioEdicion, String nombreEdicion,
            LocalDateTime fechaEdicion
    ) {}

    /** Documento de control y su auditoría, relacionado mediante el ID del auxiliar. */
    public record ControlExportacion(
            Integer idControlesAuxiliares, Integer idAuxiliarContable,
            Integer idTiposControl, String descripcionTipoControl, Boolean pideDocumento,
            String numeroDocumento, Integer idUsuarioCreacion, String usuarioCreacion,
            String nombreCreacion, LocalDateTime fechaCreacion,
            Integer idUsuarioEdicion, String usuarioEdicion, String nombreEdicion,
            LocalDateTime fechaEdicion
    ) {}

    private record ClaveComprobante(Integer agencia, String tipo, String numero) {}

    /** Aplica los filtros de consulta general a todas las cabeceras, sin LIMIT 100. */
    public List<CabeceraExportacion> buscar(
            List<Integer> agenciasAutorizadas, String tipoComprobante,
            String numeroComprobante, LocalDate fechaDesde, LocalDate fechaHasta,
            String concepto, Integer idDatosPersonal
    ) {
        if (agenciasAutorizadas == null || agenciasAutorizadas.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos una agencia autorizada.");
        }
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede superar la final.");
        }
        String sql = """
            WITH movimientos_resumen AS (
                SELECT a.id_agencia, a.tipo_comprobante, a.numero_comprobante,
                    MIN(a.fecha_auxiliar) AS fecha_contable,
                    MAX(a.fecha_auxiliar) AS fecha_final,
                    COALESCE(SUM(a.valor_debito), 0) AS total_debito,
                    COALESCE(SUM(a.valor_credito), 0) AS total_credito,
                    COUNT(a.id_auxiliar_contable) AS cantidad_movimientos
                FROM contabilidad.auxiliares_contables a
                WHERE a.id_agencia IN (:agencias)
                GROUP BY a.id_agencia, a.tipo_comprobante, a.numero_comprobante
            )
            SELECT o.id_origen_comprobante, o.id_agencia,
                ag.codigo_agencia, ag.nombre_agencia,
                TRIM(o.tipo_comprobante) AS tipo_comprobante,
                tc.nombre_tipo_comprobante,
                TRIM(o.numero_comprobante) AS numero_comprobante,
                mr.fecha_contable, c.concepto_comprobante,
                o.origen_tipo, o.modulo_origen, o.proceso_origen,
                o.tabla_origen, o.id_origen, o.estado_origen,
                COALESCE(mr.total_debito, 0) AS total_debito,
                COALESCE(mr.total_credito, 0) AS total_credito,
                COALESCE(mr.cantidad_movimientos, 0) AS cantidad_movimientos,
                o.fk_seguridad_creacion AS origen_usuario_creacion_id,
                uoc.username AS origen_usuario_creacion,
                uoc.nombre_completo AS origen_nombre_creacion,
                o.fecha_creacion AS origen_fecha_creacion,
                o.fk_seguridad_edicion AS origen_usuario_edicion_id,
                uoe.username AS origen_usuario_edicion,
                uoe.nombre_completo AS origen_nombre_edicion,
                o.fecha_edicion AS origen_fecha_edicion,
                c.fk_seguridad_creacion AS concepto_usuario_creacion_id,
                ucc.username AS concepto_usuario_creacion,
                ucc.nombre_completo AS concepto_nombre_creacion,
                c.fecha_creacion AS concepto_fecha_creacion,
                c.fk_seguridad_edicion AS concepto_usuario_edicion_id,
                uce.username AS concepto_usuario_edicion,
                uce.nombre_completo AS concepto_nombre_edicion,
                c.fecha_edicion AS concepto_fecha_edicion
            FROM contabilidad.origen_comprobantes o
            LEFT JOIN movimientos_resumen mr
                ON mr.id_agencia = o.id_agencia
                AND mr.tipo_comprobante = o.tipo_comprobante
                AND mr.numero_comprobante = o.numero_comprobante
            LEFT JOIN contabilidad.conceptos_contables c
                ON c.id_agencia = o.id_agencia
                AND c.tipo_comprobante = o.tipo_comprobante
                AND c.numero_comprobante = o.numero_comprobante
            LEFT JOIN general.datos_agencias ag ON ag.id_agencia = o.id_agencia
            LEFT JOIN contabilidad.tipos_comprobantes tc
                ON tc.id_agencia = o.id_agencia
                AND TRIM(tc.tipo_comprobante) = TRIM(o.tipo_comprobante)
            LEFT JOIN seguridad.usuarios uoc ON uoc.id_usuario = o.fk_seguridad_creacion
            LEFT JOIN seguridad.usuarios uoe ON uoe.id_usuario = o.fk_seguridad_edicion
            LEFT JOIN seguridad.usuarios ucc ON ucc.id_usuario = c.fk_seguridad_creacion
            LEFT JOIN seguridad.usuarios uce ON uce.id_usuario = c.fk_seguridad_edicion
            WHERE o.id_agencia IN (:agencias)
                AND (CAST(:tipo AS text) IS NULL OR TRIM(o.tipo_comprobante) = :tipo)
                AND (CAST(:numero AS text) IS NULL OR TRIM(o.numero_comprobante) = :numero)
                AND (CAST(:concepto AS text) IS NULL
                    OR c.concepto_comprobante ILIKE '%' || :concepto || '%')
                AND (CAST(:tercero AS integer) IS NULL OR EXISTS (
                    SELECT 1 FROM contabilidad.auxiliares_contables at
                    WHERE at.id_agencia = o.id_agencia
                        AND at.tipo_comprobante = o.tipo_comprobante
                        AND at.numero_comprobante = o.numero_comprobante
                        AND at.id_datos_personal = :tercero
                ))
                AND (CAST(:desde AS date) IS NULL OR mr.fecha_contable >= :desde)
                AND (CAST(:hasta AS date) IS NULL OR mr.fecha_final <= :hasta)
            ORDER BY mr.fecha_contable DESC NULLS LAST,
                o.tipo_comprobante, o.numero_comprobante DESC
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("agencias", agenciasAutorizadas)
                .addValue("tipo", normalizar(tipoComprobante))
                .addValue("numero", normalizar(numeroComprobante))
                .addValue("desde", fechaDesde)
                .addValue("hasta", fechaHasta)
                .addValue("concepto", normalizar(concepto))
                .addValue("tercero", idDatosPersonal);
        return jdbc.query(sql, params, (rs, rowNum) -> new CabeceraExportacion(
                entero(rs, "id_origen_comprobante"), entero(rs, "id_agencia"),
                rs.getString("codigo_agencia"), rs.getString("nombre_agencia"),
                rs.getString("tipo_comprobante"), rs.getString("nombre_tipo_comprobante"),
                rs.getString("numero_comprobante"), rs.getObject("fecha_contable", LocalDate.class),
                rs.getString("concepto_comprobante"), rs.getString("origen_tipo"),
                rs.getString("modulo_origen"), rs.getString("proceso_origen"),
                rs.getString("tabla_origen"), largo(rs, "id_origen"),
                rs.getString("estado_origen"), rs.getBigDecimal("total_debito"),
                rs.getBigDecimal("total_credito"), rs.getLong("cantidad_movimientos"),
                entero(rs, "origen_usuario_creacion_id"), rs.getString("origen_usuario_creacion"),
                rs.getString("origen_nombre_creacion"), fechaHora(rs, "origen_fecha_creacion"),
                entero(rs, "origen_usuario_edicion_id"), rs.getString("origen_usuario_edicion"),
                rs.getString("origen_nombre_edicion"), fechaHora(rs, "origen_fecha_edicion"),
                entero(rs, "concepto_usuario_creacion_id"), rs.getString("concepto_usuario_creacion"),
                rs.getString("concepto_nombre_creacion"), fechaHora(rs, "concepto_fecha_creacion"),
                entero(rs, "concepto_usuario_edicion_id"), rs.getString("concepto_usuario_edicion"),
                rs.getString("concepto_nombre_edicion"), fechaHora(rs, "concepto_fecha_edicion")
        ));
    }

    /** Recupera movimientos de todos los comprobantes seleccionados en una consulta. */
    public List<MovimientoExportacion> consultarMovimientos(List<CabeceraExportacion> comprobantes) {
        if (comprobantes == null || comprobantes.isEmpty()) {
            return List.of();
        }
        String sql = """
            WITH claves AS (
                SELECT DISTINCT agencia, tipo, numero
                FROM jsonb_to_recordset(CAST(:claves AS jsonb))
                    AS k(agencia integer, tipo text, numero text)
            )
            SELECT a.id_auxiliar_contable, a.id_agencia,
                TRIM(a.tipo_comprobante) AS tipo_comprobante,
                TRIM(a.numero_comprobante) AS numero_comprobante,
                a.id_catalogo_cuenta, cc.codigo_cuenta, cc.nombre_cuenta,
                a.id_datos_personal, dp.documento AS documento_tercero,
                NULLIF(TRIM(CONCAT_WS(' ',
                    NULLIF(TRIM(dp.nombres), ''),
                    NULLIF(TRIM(dp.primer_apellido), ''),
                    NULLIF(TRIM(dp.segundo_apellido), '')
                )), '') AS nombre_tercero,
                a.fecha_auxiliar, a.detalle_movimiento,
                TRIM(a.estado_movimiento) AS estado_movimiento,
                a.valor_debito, a.valor_credito, a.valor_base,
                a.origen_modulo, a.documento_origen, a.documento_soporte,
                a.fk_seguridad_creacion AS usuario_creacion_id,
                uc.username AS usuario_creacion, uc.nombre_completo AS nombre_creacion,
                a.fecha_creacion, a.fk_seguridad_edicion AS usuario_edicion_id,
                ue.username AS usuario_edicion, ue.nombre_completo AS nombre_edicion,
                a.fecha_edicion
            FROM contabilidad.auxiliares_contables a
            JOIN claves k ON k.agencia = a.id_agencia
                AND k.tipo = TRIM(a.tipo_comprobante)
                AND k.numero = TRIM(a.numero_comprobante)
            LEFT JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = a.id_catalogo_cuenta
                AND cc.id_agencia = a.id_agencia
            LEFT JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal = a.id_datos_personal
            LEFT JOIN seguridad.usuarios uc ON uc.id_usuario = a.fk_seguridad_creacion
            LEFT JOIN seguridad.usuarios ue ON ue.id_usuario = a.fk_seguridad_edicion
            ORDER BY a.id_agencia, a.tipo_comprobante,
                a.numero_comprobante, a.id_auxiliar_contable
            """;
        List<ClaveComprobante> claves = comprobantes.stream()
                .map(c -> new ClaveComprobante(c.idAgencia(), c.tipoComprobante(), c.numeroComprobante()))
                .distinct().toList();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("claves", json(claves));
        return jdbc.query(sql, params, (rs, rowNum) -> new MovimientoExportacion(
                entero(rs, "id_auxiliar_contable"), entero(rs, "id_agencia"),
                rs.getString("tipo_comprobante"), rs.getString("numero_comprobante"),
                entero(rs, "id_catalogo_cuenta"), rs.getString("codigo_cuenta"),
                rs.getString("nombre_cuenta"), entero(rs, "id_datos_personal"),
                rs.getString("documento_tercero"), rs.getString("nombre_tercero"),
                rs.getObject("fecha_auxiliar", LocalDate.class), rs.getString("detalle_movimiento"),
                rs.getString("estado_movimiento"), rs.getBigDecimal("valor_debito"),
                rs.getBigDecimal("valor_credito"), rs.getBigDecimal("valor_base"),
                rs.getString("origen_modulo"), rs.getString("documento_origen"),
                rs.getString("documento_soporte"), entero(rs, "usuario_creacion_id"),
                rs.getString("usuario_creacion"), rs.getString("nombre_creacion"),
                fechaHora(rs, "fecha_creacion"), entero(rs, "usuario_edicion_id"),
                rs.getString("usuario_edicion"), rs.getString("nombre_edicion"),
                fechaHora(rs, "fecha_edicion")
        ));
    }

    /** Recupera controles en bloque mediante JSON, sin duplicar valores contables. */
    public List<ControlExportacion> consultarControles(List<MovimientoExportacion> movimientos) {
        if (movimientos == null || movimientos.isEmpty()) {
            return List.of();
        }
        String sql = """
            WITH auxiliares AS (
                SELECT DISTINCT id_auxiliar_contable
                FROM jsonb_to_recordset(CAST(:auxiliares AS jsonb))
                    AS x(id_auxiliar_contable integer)
            )
            SELECT ca.id_controles_auxiliares, ca.id_auxiliar_contable,
                ca.id_tipos_control, tc.descripcion AS descripcion_tipo_control,
                tc.pide_documento, ca.numero_documento,
                ca.fk_seguridad_creacion AS usuario_creacion_id,
                uc.username AS usuario_creacion, uc.nombre_completo AS nombre_creacion,
                ca.fecha_creacion, ca.fk_seguridad_edicion AS usuario_edicion_id,
                ue.username AS usuario_edicion, ue.nombre_completo AS nombre_edicion,
                ca.fecha_edicion
            FROM contabilidad.controles_auxiliares ca
            JOIN auxiliares x ON x.id_auxiliar_contable = ca.id_auxiliar_contable
            LEFT JOIN contabilidad.tipos_control_ent_sal tc
                ON tc.id_tipos_control = ca.id_tipos_control
            LEFT JOIN seguridad.usuarios uc ON uc.id_usuario = ca.fk_seguridad_creacion
            LEFT JOIN seguridad.usuarios ue ON ue.id_usuario = ca.fk_seguridad_edicion
            ORDER BY ca.id_auxiliar_contable, ca.id_controles_auxiliares
            """;
        List<Integer> ids = movimientos.stream()
                .map(MovimientoExportacion::idAuxiliarContable)
                .distinct().toList();
        List<IdentificadorAuxiliar> auxiliares = ids.stream()
                .map(IdentificadorAuxiliar::new).toList();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("auxiliares", json(auxiliares));
        return jdbc.query(sql, params, (rs, rowNum) -> new ControlExportacion(
                entero(rs, "id_controles_auxiliares"), entero(rs, "id_auxiliar_contable"),
                entero(rs, "id_tipos_control"), rs.getString("descripcion_tipo_control"),
                (Boolean) rs.getObject("pide_documento"), rs.getString("numero_documento"),
                entero(rs, "usuario_creacion_id"), rs.getString("usuario_creacion"),
                rs.getString("nombre_creacion"), fechaHora(rs, "fecha_creacion"),
                entero(rs, "usuario_edicion_id"), rs.getString("usuario_edicion"),
                rs.getString("nombre_edicion"), fechaHora(rs, "fecha_edicion")
        ));
    }

    private record IdentificadorAuxiliar(Integer id_auxiliar_contable) {}

    /** Serializa los identificadores de manera segura mediante Jackson. */
    private String json(Object valor) {
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible preparar los identificadores de exportación.", ex);
        }
    }

    /** Recupera un entero sin convertir NULL a cero. */
    private Integer entero(ResultSet rs, String columna) throws SQLException {
        return (Integer) rs.getObject(columna);
    }

    /** Recupera identificadores largos conservando NULL. */
    private Long largo(ResultSet rs, String columna) throws SQLException {
        Number valor = (Number) rs.getObject(columna);
        return valor == null ? null : valor.longValue();
    }

    /** Conserva fecha y hora completas para los campos de auditoría. */
    private LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        return rs.getObject(columna, LocalDateTime.class);
    }

    /** Normaliza filtros opcionales sin cambiar su significado. */
    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
