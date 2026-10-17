
package co.assip.erp.tesoreria.autorizacion_comprobantes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistencia de comprobantes de Tesorería sujetos a autorización.
 *
 * No genera consecutivos ni contabiliza: esas acciones pertenecen al servicio
 * de autorización y se ejecutarán en una única transacción.
 */
@Repository
@RequiredArgsConstructor
public class AutorizacionComprobantesRepository {

    private final JdbcTemplate jdbc;

    public record Comprobante(
            Long idComprobante,
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String procesoOrigen,
            LocalDate fechaDocumento,
            LocalDate fechaContable,
            String concepto,
            String documentoSoporte,
            BigDecimal totalDebito,
            BigDecimal totalCredito,
            String estado,
            Integer usuarioCreacion,
            LocalDateTime fechaCreacion,
            Integer usuarioAutorizacion,
            LocalDateTime fechaAutorizacion,
            Integer usuarioRechazo,
            LocalDateTime fechaRechazo,
            String motivoRechazo,
            Integer usuarioAnulacion,
            LocalDateTime fechaAnulacion,
            String motivoAnulacion
    ) {}

    public record Partida(
            Long idDetalle,
            Long idComprobante,
            Integer numeroLinea,
            Integer idCatalogoCuenta,
            Integer idAgencia,
            Integer idDatosPersonal,
            String detalleMovimiento,
            BigDecimal valorDebito,
            BigDecimal valorCredito,
            BigDecimal valorBase,
            String origenModulo,
            String documentoOrigen,
            String documentoSoporte
    ) {}

    public record MovimientoCajaPendiente(
            Long idMovimientoPendiente,
            Long idComprobante,
            Integer numeroLinea,
            Long idCaja,
            Long idDepartamentoOperativo,
            Long idOperacionCaja,
            String moduloOrigen,
            String procesoOrigen,
            String tipoDocumento,
            String numeroDocumento,
            String codigoReferencia,
            String naturaleza,
            String medioPago,
            BigDecimal valor,
            String concepto
    ) {}

    public record ProvisionAbierta(
            Long idProvision,
            Long idCaja,
            LocalDate fechaContable
    ) {}

    private static final String SELECT_COMPROBANTE = """
        SELECT id_comprobante, id_agencia, tipo_comprobante, numero_comprobante,
               proceso_origen, fecha_documento, fecha_contable, concepto,
               documento_soporte, total_debito, total_credito, estado,
               fk_seguridad_creacion, fecha_creacion,
               fk_seguridad_autorizacion, fecha_autorizacion,
               fk_seguridad_rechazo, fecha_rechazo, motivo_rechazo,
               fk_seguridad_anulacion, fecha_anulacion, motivo_anulacion
        FROM tesoreria.comprobantes
        """;

    public List<Comprobante> listarPendientes(Integer idAgencia) {
        return jdbc.query(
                SELECT_COMPROBANTE + """
                    WHERE estado = 'PENDIENTE' AND id_agencia = ?
                    ORDER BY fecha_creacion, id_comprobante
                    """,
                this::mapComprobante, idAgencia);
    }

    public Comprobante buscar(Long idComprobante) {
        List<Comprobante> filas = jdbc.query(
                SELECT_COMPROBANTE + " WHERE id_comprobante = ?",
                this::mapComprobante, idComprobante);
        return filas.isEmpty() ? null : filas.get(0);
    }

    /** Bloqueo pesimista para impedir dos autorizaciones simultáneas. */
    public Comprobante bloquear(Long idComprobante) {
        List<Comprobante> filas = jdbc.query(
                SELECT_COMPROBANTE + " WHERE id_comprobante = ? FOR UPDATE",
                this::mapComprobante, idComprobante);
        return filas.isEmpty() ? null : filas.get(0);
    }

    public List<Partida> listarPartidas(Long idComprobante) {
        return jdbc.query("""
                SELECT id_comprobante_detalle, id_comprobante, numero_linea,
                       id_catalogo_cuenta, id_agencia, id_datos_personal,
                       detalle_movimiento, valor_debito, valor_credito,
                       valor_base, origen_modulo, documento_origen, documento_soporte
                FROM tesoreria.comprobantes_detalles
                WHERE id_comprobante = ?
                ORDER BY numero_linea
                """, this::mapPartida, idComprobante);
    }

    public List<MovimientoCajaPendiente> listarMovimientosCaja(Long idComprobante) {
        return jdbc.query("""
                SELECT id_comprobante_movimiento_caja, id_comprobante, numero_linea,
                       id_caja, id_departamento_operativo, id_operacion_caja,
                       modulo_origen, proceso_origen, tipo_documento,
                       numero_documento, codigo_referencia, naturaleza,
                       medio_pago, valor, concepto
                FROM tesoreria.comprobantes_movimientos_caja
                WHERE id_comprobante = ?
                ORDER BY numero_linea
                """, this::mapMovimiento, idComprobante);
    }

    /**
     * Verifica la provisión en la fecha de AUTORIZACIÓN, no en la fecha
     * en que Tesorería elaboró el comprobante.
     * El bloqueo protege frente a cierres concurrentes de la provisión.
     */
    public ProvisionAbierta bloquearProvisionAbierta(
            Long idCaja, Integer idAgencia, LocalDate fechaAutorizacion) {
        List<ProvisionAbierta> filas = jdbc.query("""
                SELECT p.id_provision, p.id_caja, p.fecha_contable
                FROM cajas.provisiones_diarias p
                JOIN cajas.cajas c ON c.id_caja = p.id_caja
                WHERE p.id_caja = ?
                  AND c.id_agencia = ?
                  AND c.activa = TRUE
                  AND p.fecha_contable = ?
                  AND p.estado = 'ABIERTA'
                FOR UPDATE OF p
                """, (rs, n) -> new ProvisionAbierta(
                        rs.getLong("id_provision"),
                        rs.getLong("id_caja"),
                        rs.getDate("fecha_contable").toLocalDate()),
                idCaja, idAgencia, fechaAutorizacion);
        if (filas.size() > 1) {
            throw new IllegalStateException(
                    "Existe más de una provisión abierta para la caja y fecha.");
        }
        return filas.isEmpty() ? null : filas.get(0);
    }

    /**
     * Cambia a CONTABILIZADO únicamente desde PENDIENTE.
     * Debe invocarse en la misma transacción que genera consecutivo,
     * registra auxiliares y ejecuta movimientos físicos.
     */
    public boolean marcarContabilizado(
            Long idComprobante, String numero, LocalDate fecha,
            Integer usuarioAutorizador) {
        int filas = jdbc.update("""
                UPDATE tesoreria.comprobantes
                   SET estado = 'CONTABILIZADO',
                       numero_comprobante = ?,
                       fecha_contable = ?,
                       fk_seguridad_autorizacion = ?,
                       fecha_autorizacion = CURRENT_TIMESTAMP
                 WHERE id_comprobante = ?
                   AND estado = 'PENDIENTE'
                   AND fk_seguridad_creacion <> ?
                """, numero, fecha, usuarioAutorizador,
                idComprobante, usuarioAutorizador);
        return filas == 1;
    }

    public boolean rechazar(
            Long idComprobante, Integer usuarioAutorizador, String motivo) {
        exigirMotivo(motivo);
        return jdbc.update("""
                UPDATE tesoreria.comprobantes
                   SET estado = 'RECHAZADO',
                       fk_seguridad_rechazo = ?,
                       fecha_rechazo = CURRENT_TIMESTAMP,
                       motivo_rechazo = ?
                 WHERE id_comprobante = ?
                   AND estado = 'PENDIENTE'
                   AND fk_seguridad_creacion <> ?
                """, usuarioAutorizador, motivo.trim(),
                idComprobante, usuarioAutorizador) == 1;
    }

    /**
     * La validación de permisos del usuario corresponde al servicio.
     * Se permite anular un PENDIENTE, nunca un CONTABILIZADO.
     */
    public boolean anular(
            Long idComprobante, Integer usuario, String motivo) {
        exigirMotivo(motivo);
        return jdbc.update("""
                UPDATE tesoreria.comprobantes
                   SET estado = 'ANULADO',
                       fk_seguridad_anulacion = ?,
                       fecha_anulacion = CURRENT_TIMESTAMP,
                       motivo_anulacion = ?
                 WHERE id_comprobante = ?
                   AND estado = 'PENDIENTE'
                """, usuario, motivo.trim(), idComprobante) == 1;
    }

    private static void exigirMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo.");
        }
        if (motivo.trim().length() > 500) {
            throw new IllegalArgumentException(
                    "El motivo no puede superar 500 caracteres.");
        }
    }

    private Comprobante mapComprobante(ResultSet r, int n) throws SQLException {
        return new Comprobante(
                r.getLong("id_comprobante"),
                r.getInt("id_agencia"),
                r.getString("tipo_comprobante"),
                r.getString("numero_comprobante"),
                r.getString("proceso_origen"),
                date(r, "fecha_documento"),
                date(r, "fecha_contable"),
                r.getString("concepto"),
                r.getString("documento_soporte"),
                r.getBigDecimal("total_debito"),
                r.getBigDecimal("total_credito"),
                r.getString("estado"),
                r.getInt("fk_seguridad_creacion"),
                timestamp(r, "fecha_creacion"),
                nullableInt(r, "fk_seguridad_autorizacion"),
                timestamp(r, "fecha_autorizacion"),
                nullableInt(r, "fk_seguridad_rechazo"),
                timestamp(r, "fecha_rechazo"),
                r.getString("motivo_rechazo"),
                nullableInt(r, "fk_seguridad_anulacion"),
                timestamp(r, "fecha_anulacion"),
                r.getString("motivo_anulacion"));
    }

    private Partida mapPartida(ResultSet r, int n) throws SQLException {
        return new Partida(
                r.getLong("id_comprobante_detalle"),
                r.getLong("id_comprobante"),
                r.getInt("numero_linea"),
                r.getInt("id_catalogo_cuenta"),
                r.getInt("id_agencia"),
                nullableInt(r, "id_datos_personal"),
                r.getString("detalle_movimiento"),
                r.getBigDecimal("valor_debito"),
                r.getBigDecimal("valor_credito"),
                r.getBigDecimal("valor_base"),
                r.getString("origen_modulo"),
                r.getString("documento_origen"),
                r.getString("documento_soporte"));
    }

    private MovimientoCajaPendiente mapMovimiento(ResultSet r, int n)
            throws SQLException {
        return new MovimientoCajaPendiente(
                r.getLong("id_comprobante_movimiento_caja"),
                r.getLong("id_comprobante"),
                r.getInt("numero_linea"),
                r.getLong("id_caja"),
                r.getLong("id_departamento_operativo"),
                r.getLong("id_operacion_caja"),
                r.getString("modulo_origen"),
                r.getString("proceso_origen"),
                r.getString("tipo_documento"),
                r.getString("numero_documento"),
                r.getString("codigo_referencia"),
                r.getString("naturaleza"),
                r.getString("medio_pago"),
                r.getBigDecimal("valor"),
                r.getString("concepto"));
    }

    private static Integer nullableInt(ResultSet r, String column)
            throws SQLException {
        int value = r.getInt(column);
        return r.wasNull() ? null : value;
    }

    private static LocalDate date(ResultSet r, String column)
            throws SQLException {
        java.sql.Date value = r.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private static LocalDateTime timestamp(ResultSet r, String column)
            throws SQLException {
        java.sql.Timestamp value = r.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }
}
