
package co.assip.erp.tesoreria.traslados_bancarios;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Persistencia de traslados bancarios.
 *
 * El traslado se prepara como comprobante PENDIENTE.
 *
 * La autorización y contabilización definitiva
 * corresponden al proceso de Autorización de Comprobantes.
 *
 * No registra movimientos físicos de caja.
 */
@Repository
@RequiredArgsConstructor
public class TrasladosBancariosRepository {

    private final JdbcTemplate jdbc;

    // =========================================================
    // MODELOS DE CONSULTA
    // =========================================================

    public record CuentaBancaria(
            Long idCuentaBancaria,
            Integer idAgencia,
            String nombreBanco,
            String numeroCuenta,
            String nombreCuenta,
            Integer idCuentaContable,
            String codigoCuentaContable,
            String nombreCuentaContable
    ) {}

    public record PartidaPendiente(
            Integer numeroLinea,
            Integer idCatalogoCuenta,
            Integer idAgencia,
            Integer idDatosPersonal,
            String detalleMovimiento,
            BigDecimal valorDebito,
            BigDecimal valorCredito,
            String documentoSoporte
    ) {}

    // =========================================================
    // CONSULTA BASE DE CUENTAS BANCARIAS
    // =========================================================

    private static final String SQL_CUENTAS = """
            SELECT
                cb.id_cuenta_bancaria,
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
              AND cb.id_agencia_titular = cba.id_agencia
              AND cba.activo = TRUE
              AND cb.activo = TRUE
              AND cb.estado_cuenta = 'ACTIVA'
              AND b.activo = TRUE
              AND cc.id_agencia = cba.id_agencia
              AND cc.cuenta_operable = TRUE
            """;

    private final RowMapper<CuentaBancaria> cuentaMapper =
            (rs, row) -> new CuentaBancaria(
                    rs.getLong("id_cuenta_bancaria"),
                    rs.getInt("id_agencia"),
                    rs.getString("nombre_banco"),
                    rs.getString("numero_cuenta"),
                    rs.getString("nombre_cuenta"),
                    rs.getInt("id_cuenta_contable"),
                    rs.getString("codigo_cuenta"),
                    rs.getString("nombre_cuenta_contable")
            );

    // =========================================================
    // CUENTAS DE ORIGEN
    // =========================================================

    /**
     * Cuentas habilitadas para trasladar recursos.
     *
     * Se utiliza permite_trasladar, no permite_retirar.
     */
    public List<CuentaBancaria> listarCuentasOrigen(
            Integer idAgencia
    ) {

        return jdbc.query(
                SQL_CUENTAS
                        + " AND cba.permite_trasladar = TRUE"
                        + " ORDER BY b.nombre_banco, cb.numero_cuenta",
                cuentaMapper,
                idAgencia
        );
    }

    // =========================================================
    // CUENTAS DE DESTINO
    // =========================================================

    /**
     * Cuentas habilitadas para recibir recursos.
     */
    public List<CuentaBancaria> listarCuentasDestino(
            Integer idAgencia
    ) {

        return jdbc.query(
                SQL_CUENTAS
                        + " AND cba.permite_consignar = TRUE"
                        + " ORDER BY b.nombre_banco, cb.numero_cuenta",
                cuentaMapper,
                idAgencia
        );
    }

    // =========================================================
    // VALIDACIÓN CON BLOQUEO
    // =========================================================

    public CuentaBancaria obtenerOrigenConLock(
            Long idCuenta,
            Integer idAgencia
    ) {

        return obtenerConLock(
                idCuenta,
                idAgencia,
                "permite_trasladar"
        );
    }

    public CuentaBancaria obtenerDestinoConLock(
            Long idCuenta,
            Integer idAgencia
    ) {

        return obtenerConLock(
                idCuenta,
                idAgencia,
                "permite_consignar"
        );
    }

    private CuentaBancaria obtenerConLock(
            Long idCuenta,
            Integer idAgencia,
            String permiso
    ) {

        if (!"permite_trasladar".equals(permiso)
                && !"permite_consignar".equals(permiso)) {

            throw new IllegalArgumentException(
                    "Permiso bancario no permitido."
            );
        }

        List<CuentaBancaria> filas = jdbc.query(
                SQL_CUENTAS
                        + " AND cba." + permiso + " = TRUE"
                        + " AND cb.id_cuenta_bancaria = ?"
                        + " FOR UPDATE OF cba",
                cuentaMapper,
                idAgencia,
                idCuenta
        );

        return filas.isEmpty() ? null : filas.get(0);
    }

    // =========================================================
    // TERCERO CONTABLE
    // =========================================================

    /**
     * Obtiene el tercero contable del usuario elaborador.
     *
     * Conserva el patrón utilizado en retiros bancarios.
     */
    public Integer obtenerTerceroUsuario(
            Integer idUsuario
    ) {

        List<Integer> filas = jdbc.query("""
                SELECT id_datos_personal
                FROM seguridad.usuarios
                WHERE id_usuario = ?
                  AND activo = TRUE
                  AND id_datos_personal IS NOT NULL
                """,
                (rs, row) -> rs.getInt(1),
                idUsuario
        );

        return filas.isEmpty() ? null : filas.get(0);
    }

    // =========================================================
    // CREACIÓN DEL COMPROBANTE PENDIENTE
    // =========================================================

    /**
     * Crea un comprobante pendiente para traslado bancario.
     *
     * Tipo: TS
     * Proceso: TRASLADO_BANCARIO
     * Estado: PENDIENTE
     *
     * No asigna consecutivo contable definitivo.
     * No asigna fecha contable.
     */
    public Long crearComprobantePendiente(
            Integer idAgencia,
            LocalDate fechaDocumento,
            String concepto,
            String documentoSoporte,
            BigDecimal valor,
            Integer idUsuario
    ) {

        Objects.requireNonNull(
                idAgencia,
                "La agencia es obligatoria."
        );

        Objects.requireNonNull(
                fechaDocumento,
                "La fecha del documento es obligatoria."
        );

        Objects.requireNonNull(
                concepto,
                "El concepto es obligatorio."
        );

        Objects.requireNonNull(
                valor,
                "El valor es obligatorio."
        );

        Objects.requireNonNull(
                idUsuario,
                "El usuario es obligatorio."
        );

        if (valor.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El valor del traslado debe ser positivo."
            );
        }

        String sql = """
                INSERT INTO tesoreria.comprobantes (
                    id_agencia,
                    tipo_comprobante,
                    numero_comprobante,
                    proceso_origen,
                    fecha_documento,
                    fecha_contable,
                    concepto,
                    documento_soporte,
                    total_debito,
                    total_credito,
                    estado,
                    fk_seguridad_creacion
                )
                VALUES (
                    ?,
                    'TS',
                    NULL,
                    'TRASLADO_BANCARIO',
                    ?,
                    NULL,
                    ?,
                    ?,
                    ?,
                    ?,
                    'PENDIENTE',
                    ?
                )
                """;

        KeyHolder keys = new GeneratedKeyHolder();

        jdbc.update(connection -> {

            PreparedStatement ps =
                    connection.prepareStatement(
                            sql,
                            new String[]{"id_comprobante"}
                    );

            ps.setInt(1, idAgencia);
            ps.setObject(2, fechaDocumento);
            ps.setString(3, concepto);
            ps.setString(4, documentoSoporte);
            ps.setBigDecimal(5, valor);
            ps.setBigDecimal(6, valor);
            ps.setInt(7, idUsuario);

            return ps;

        }, keys);

        if (keys.getKey() == null) {
            throw new IllegalStateException(
                    "No se generó el identificador "
                            + "del comprobante pendiente."
            );
        }

        return keys.getKey().longValue();
    }

    // =========================================================
    // PARTIDAS CONTABLES PENDIENTES
    // =========================================================

    /**
     * Registra una partida proyectada del comprobante.
     *
     * No afecta auxiliares contables.
     *
     * Débito: banco destino.
     * Crédito: banco origen.
     */
    public void insertarPartida(
            Long idComprobante,
            PartidaPendiente partida
    ) {

        Objects.requireNonNull(
                idComprobante,
                "El comprobante es obligatorio."
        );

        Objects.requireNonNull(
                partida,
                "La partida contable es obligatoria."
        );

        jdbc.update("""
                INSERT INTO tesoreria.comprobantes_detalles (
                    id_comprobante,
                    numero_linea,
                    id_catalogo_cuenta,
                    id_agencia,
                    id_datos_personal,
                    detalle_movimiento,
                    valor_debito,
                    valor_credito,
                    valor_base,
                    origen_modulo,
                    documento_origen,
                    documento_soporte
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                idComprobante,
                partida.numeroLinea(),
                partida.idCatalogoCuenta(),
                partida.idAgencia(),
                partida.idDatosPersonal(),
                partida.detalleMovimiento(),
                partida.valorDebito(),
                partida.valorCredito(),
                BigDecimal.ZERO,
                "TESORERIA",
                "TRASLADO_BANCARIO",
                partida.documentoSoporte()
        );
    }
}
