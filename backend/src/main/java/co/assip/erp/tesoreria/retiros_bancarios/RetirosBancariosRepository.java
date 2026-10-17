package co.assip.erp.tesoreria.retiros_bancarios;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Persistencia de retiros bancarios pendientes de autorización. */
@Repository
@RequiredArgsConstructor
public class RetirosBancariosRepository {

    private final JdbcTemplate jdbc;

    public record CuentaBancariaRetiro(
            Long idCuentaBancaria,
            Integer idAgencia,
            String nombreBanco,
            String numeroCuenta,
            String nombreCuenta,
            Integer idCuentaContable,
            String codigoCuentaContable,
            String nombreCuentaContable
    ) {}

    public record CajaControl(
            Long idCaja,
            Integer idAgencia,
            String codigoCaja,
            String descripcion,
            Integer idCatalogoCuentaCaja,
            String codigoCuentaCaja,
            String nombreCuentaCaja
    ) {}

    public record PartidaPendiente(
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


    /**
     * Consulta las cuentas bancarias habilitadas para retiros
     * en la agencia que realiza la operación.
     *
     * La autorización depende de cuentas_bancarias_agencias,
     * no de la agencia titular de la cuenta bancaria.
     *
     * La cuenta contable utilizada corresponde siempre
     * a la agencia que ejecuta el retiro.
     */
    private static final String SQL_CUENTAS = """
        SELECT cb.id_cuenta_bancaria,
               cba.id_agencia,
               b.nombre_banco,
               cb.numero_cuenta,
               cb.nombre_cuenta,
               cba.id_cuenta_contable,
               cc.codigo_cuenta,
               cc.nombre_cuenta AS nombre_cuenta_contable
          FROM tesoreria.cuentas_bancarias_agencias cba
          JOIN tesoreria.cuentas_bancarias cb
            ON cb.id_cuenta_bancaria = cba.id_cuenta_bancaria
          JOIN tesoreria.bancos b
            ON b.codigo_banco = cb.codigo_banco
          JOIN contabilidad.catalogo_cuentas cc
            ON cc.id_catalogo_cuenta = cba.id_cuenta_contable
         WHERE cba.id_agencia = ?
           AND cba.activo = TRUE
           AND cba.permite_retirar = TRUE
           AND cb.activo = TRUE
           AND cb.estado_cuenta = 'ACTIVA'
           AND b.activo = TRUE
           AND cc.id_agencia = cba.id_agencia
           AND cc.cuenta_operable = TRUE
        """;

    private final RowMapper<CuentaBancariaRetiro> cuentaMapper = (rs, row) ->
            new CuentaBancariaRetiro(
                    rs.getLong("id_cuenta_bancaria"),
                    rs.getInt("id_agencia"),
                    rs.getString("nombre_banco"),
                    rs.getString("numero_cuenta"),
                    rs.getString("nombre_cuenta"),
                    rs.getInt("id_cuenta_contable"),
                    rs.getString("codigo_cuenta"),
                    rs.getString("nombre_cuenta_contable")
            );

    /** Lista las cuentas de la agencia habilitadas específicamente para retiros. */
    public List<CuentaBancariaRetiro> listarCuentasRetiro(Integer idAgencia) {
        return jdbc.query(SQL_CUENTAS + " ORDER BY b.nombre_banco, cb.numero_cuenta",
                cuentaMapper, idAgencia);
    }

    /** Bloquea la configuración bancaria para validar que sigue habilitada. */
    public CuentaBancariaRetiro obtenerCuentaRetiroConLock(
            Long idCuentaBancaria, Integer idAgencia) {
        List<CuentaBancariaRetiro> filas = jdbc.query(
                SQL_CUENTAS + " AND cb.id_cuenta_bancaria = ? FOR UPDATE OF cba",
                cuentaMapper, idAgencia, idCuentaBancaria);
        return filas.isEmpty() ? null : filas.get(0);
    }

    private static final String SQL_CAJAS = """
            SELECT c.id_caja, c.id_agencia, c.codigo_caja,
                   c.descripcion, c.id_catalogo_cuenta_caja,
                   cc.codigo_cuenta,
                   cc.nombre_cuenta AS nombre_cuenta_caja
              FROM cajas.cajas c
              JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = c.id_catalogo_cuenta_caja
             WHERE c.id_agencia = ?
               AND c.tipo_caja = 'CONTROL'
               AND c.activa = TRUE
               AND c.permite_efectivo = TRUE
               AND cc.id_agencia = c.id_agencia
               AND cc.cuenta_operable = TRUE
            """;

    private final RowMapper<CajaControl> cajaMapper = (rs, row) ->
            new CajaControl(
                    rs.getLong("id_caja"),
                    rs.getInt("id_agencia"),
                    rs.getString("codigo_caja"),
                    rs.getString("descripcion"),
                    rs.getInt("id_catalogo_cuenta_caja"),
                    rs.getString("codigo_cuenta"),
                    rs.getString("nombre_cuenta_caja")
            );

    /** Lista únicamente cajas CONTROL activas y aptas para recibir efectivo. */
    public List<CajaControl> listarCajasControl(Integer idAgencia) {
        return jdbc.query(SQL_CAJAS + " ORDER BY c.codigo_caja",
                cajaMapper, idAgencia);
    }

    /** Verifica que la caja seleccionada sigue siendo CONTROL y pertenece a la agencia. */
    public CajaControl obtenerCajaControl(Long idCaja, Integer idAgencia) {
        List<CajaControl> filas = jdbc.query(
                SQL_CAJAS + " AND c.id_caja = ?",
                cajaMapper, idAgencia, idCaja);
        return filas.isEmpty() ? null : filas.get(0);
    }

    /** Obtiene el tercero contable asociado al usuario elaborador. */
    public Integer obtenerTerceroUsuario(Integer idUsuario) {
        List<Integer> filas = jdbc.query("""
                SELECT id_datos_personal
                  FROM seguridad.usuarios
                 WHERE id_usuario = ?
                   AND activo = TRUE
                   AND id_datos_personal IS NOT NULL
                """, (rs, row) -> rs.getInt(1), idUsuario);
        return filas.isEmpty() ? null : filas.get(0);
    }

    /** Resuelve el departamento operativo activo de la operación por su código. */
    public Long obtenerDepartamentoOperativo(String codigoDepartamento) {
        List<Long> filas = jdbc.query("""
                SELECT id_departamento_operativo
                  FROM cajas.departamentos_operativos
                 WHERE codigo_departamento = ?
                   AND estado = 'A'
                """, (rs, row) -> rs.getLong(1), codigoDepartamento);
        if (filas.size() > 1) {
            throw new IllegalStateException("Departamento operativo duplicado: " + codigoDepartamento);
        }
        return filas.isEmpty() ? null : filas.get(0);
    }

    /** Resuelve la operación física de caja activa por su código. */
    public Long obtenerOperacionCaja(String codigoOperacion) {
        List<Long> filas = jdbc.query("""
                SELECT id_operacion_caja
                  FROM cajas.operaciones_caja
                 WHERE codigo_operacion = ?
                   AND estado = 'A'
                """, (rs, row) -> rs.getLong(1), codigoOperacion);
        if (filas.size() > 1) {
            throw new IllegalStateException("Operación de caja duplicada: " + codigoOperacion);
        }
        return filas.isEmpty() ? null : filas.get(0);
    }

    /** Crea la cabecera PENDIENTE sin número de comprobante ni fecha contable. */
    public Long crearComprobantePendiente(
            Integer idAgencia,
            String tipoComprobante,
            LocalDate fechaDocumento,
            String concepto,
            String documentoSoporte,
            BigDecimal total,
            Integer idUsuario) {

        Objects.requireNonNull(idAgencia, "La agencia es obligatoria");
        Objects.requireNonNull(tipoComprobante, "El tipo de comprobante es obligatorio");
        Objects.requireNonNull(fechaDocumento, "La fecha del documento es obligatoria");
        Objects.requireNonNull(concepto, "El concepto es obligatorio");
        Objects.requireNonNull(total, "El total es obligatorio");
        Objects.requireNonNull(idUsuario, "El usuario es obligatorio");
        if (total.signum() <= 0) {
            throw new IllegalArgumentException("El valor del retiro debe ser positivo.");
        }

        String sql = """
                INSERT INTO tesoreria.comprobantes (
                    id_agencia, tipo_comprobante, numero_comprobante,
                    proceso_origen, fecha_documento, fecha_contable,
                    concepto, documento_soporte, total_debito, total_credito,
                    estado, fk_seguridad_creacion
                ) VALUES (?, ?, NULL, 'RETIRO_BANCARIO', ?, NULL,
                          ?, ?, ?, ?, 'PENDIENTE', ?)
                """;

        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, new String[]{"id_comprobante"});
            ps.setInt(1, idAgencia);
            ps.setString(2, tipoComprobante);
            ps.setObject(3, fechaDocumento);
            ps.setString(4, concepto);
            ps.setString(5, documentoSoporte);
            ps.setBigDecimal(6, total);
            ps.setBigDecimal(7, total);
            ps.setInt(8, idUsuario);
            return ps;
        }, keys);
        if (keys.getKey() == null) {
            throw new IllegalStateException("No se generó el ID del comprobante pendiente.");
        }
        return keys.getKey().longValue();
    }

    /** Guarda una partida contable proyectada sin afectar los auxiliares. */
    public void insertarPartida(Long idComprobante, PartidaPendiente p) {
        jdbc.update("""
                INSERT INTO tesoreria.comprobantes_detalles (
                    id_comprobante, numero_linea, id_catalogo_cuenta,
                    id_agencia, id_datos_personal, detalle_movimiento,
                    valor_debito, valor_credito, valor_base,
                    origen_modulo, documento_origen, documento_soporte
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                idComprobante, p.numeroLinea(), p.idCatalogoCuenta(),
                p.idAgencia(), p.idDatosPersonal(), p.detalleMovimiento(),
                p.valorDebito(), p.valorCredito(), p.valorBase(),
                p.origenModulo(), p.documentoOrigen(), p.documentoSoporte());
    }

    /** Guarda el movimiento físico proyectado; la provisión se asignará al autorizar. */
    public void insertarMovimientoCajaPendiente(
            Long idComprobante, MovimientoCajaPendiente m) {
        jdbc.update("""
                INSERT INTO tesoreria.comprobantes_movimientos_caja (
                    id_comprobante, numero_linea, id_caja,
                    id_departamento_operativo, id_operacion_caja,
                    modulo_origen, proceso_origen, tipo_documento,
                    numero_documento, codigo_referencia, naturaleza,
                    medio_pago, valor, concepto
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                idComprobante, m.numeroLinea(), m.idCaja(),
                m.idDepartamentoOperativo(), m.idOperacionCaja(),
                m.moduloOrigen(), m.procesoOrigen(), m.tipoDocumento(),
                m.numeroDocumento(), m.codigoReferencia(), m.naturaleza(),
                m.medioPago(), m.valor(), m.concepto());
    }
}
