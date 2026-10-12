package co.assip.erp.tesoreria.consignaciones_bancarias;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConsignacionBancariaRepository {

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // PARÁMETROS OPERATIVOS DE CAJA
    // =========================================================

    public Long obtenerDepartamentoConsignacionesBancarias() {

        String sql = """
            SELECT id_departamento_operativo
            FROM cajas.departamentos_operativos
            WHERE codigo_departamento = '19'
              AND estado = 'A'
            LIMIT 1
        """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        rs.getLong("id_departamento_operativo")
        );

        return datos.isEmpty()
                ? null
                : datos.get(0);
    }

    public Long obtenerOperacionConsignacionBanco() {

        String sql = """
            SELECT id_operacion_caja
            FROM cajas.operaciones_caja
            WHERE codigo_operacion = 'CONSIGNACION_BANCO'
              AND estado = 'A'
            LIMIT 1
        """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        rs.getLong("id_operacion_caja")
        );

        return datos.isEmpty()
                ? null
                : datos.get(0);
    }

    // =========================================================
    // CUENTAS BANCARIAS
    // =========================================================

    public List<CuentaBancariaConsignacion> listarCuentasBancarias(
            Integer idAgencia
    ) {

        String sql = """
            SELECT
                cb.id_cuenta_bancaria,
                cb.codigo_banco,
                b.nombre_banco,
                cb.numero_cuenta,
                cb.nombre_cuenta,
                cb.codigo_tipo_cuenta,
                tcb.nombre_tipo_cuenta,

                cba.id_cuenta_contable,

                cc.codigo_cuenta AS codigo_cuenta_contable,
                cc.nombre_cuenta AS nombre_cuenta_contable

            FROM tesoreria.cuentas_bancarias cb

            INNER JOIN tesoreria.cuentas_bancarias_agencias cba
                ON cba.id_cuenta_bancaria =
                   cb.id_cuenta_bancaria

            INNER JOIN tesoreria.bancos b
                ON b.codigo_banco =
                   cb.codigo_banco

            INNER JOIN tesoreria.tipos_cuentas_bancarias tcb
                ON tcb.codigo_tipo_cuenta =
                   cb.codigo_tipo_cuenta

            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta =
                   cba.id_cuenta_contable

            WHERE cba.id_agencia = ?

              AND cba.activo = true
              AND cba.permite_consultar = true
              AND cba.permite_consignar = true

              AND cb.activo = true
              AND cb.estado_cuenta = 'ACTIVA'

              AND b.activo = true
              AND tcb.activo = true

              AND cc.id_agencia = cba.id_agencia
              AND cc.cuenta_operable = true

            ORDER BY
                b.nombre_banco,
                cb.nombre_cuenta,
                cb.numero_cuenta
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        mapCuentaBancaria(rs),
                idAgencia
        );
    }

    public CuentaBancariaConsignacion obtenerCuentaBancariaConLock(
            Long idCuentaBancaria,
            Integer idAgencia
    ) {

        String sql = """
            SELECT
                cb.id_cuenta_bancaria,
                cb.codigo_banco,
                b.nombre_banco,
                cb.numero_cuenta,
                cb.nombre_cuenta,
                cb.codigo_tipo_cuenta,
                tcb.nombre_tipo_cuenta,

                cba.id_cuenta_contable,

                cc.codigo_cuenta AS codigo_cuenta_contable,
                cc.nombre_cuenta AS nombre_cuenta_contable

            FROM tesoreria.cuentas_bancarias_agencias cba

            INNER JOIN tesoreria.cuentas_bancarias cb
                ON cb.id_cuenta_bancaria =
                   cba.id_cuenta_bancaria

            INNER JOIN tesoreria.bancos b
                ON b.codigo_banco =
                   cb.codigo_banco

            INNER JOIN tesoreria.tipos_cuentas_bancarias tcb
                ON tcb.codigo_tipo_cuenta =
                   cb.codigo_tipo_cuenta

            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta =
                   cba.id_cuenta_contable

            WHERE cba.id_cuenta_bancaria = ?
              AND cba.id_agencia = ?

              AND cba.activo = true
              AND cba.permite_consignar = true

              AND cb.activo = true
              AND cb.estado_cuenta = 'ACTIVA'

              AND b.activo = true
              AND tcb.activo = true

              AND cc.id_agencia = cba.id_agencia
              AND cc.cuenta_operable = true

            FOR UPDATE OF cba
        """;

        List<CuentaBancariaConsignacion> datos =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                mapCuentaBancaria(rs),
                        idCuentaBancaria,
                        idAgencia
                );

        return datos.isEmpty()
                ? null
                : datos.get(0);
    }

    // =========================================================
    // CHEQUES DISPONIBLES
    // =========================================================

    public List<ChequeDisponible> listarChequesRecibidos(
            Integer idAgencia
    ) {

        String sql = """
            SELECT
                cr.id_cheque_recibido,
                cr.id_movimiento_caja,
                cr.id_caja,

                c.codigo_caja,
                c.descripcion AS nombre_caja,
                c.id_agencia,
                c.id_catalogo_cuenta_caja,

                cc.codigo_cuenta AS codigo_cuenta_caja,
                cc.nombre_cuenta AS nombre_cuenta_caja,

                cr.fecha_recibido,
                cr.codigo_banco,

                b.nombre_banco,

                cr.numero_cheque,
                cr.valor_cheque,

                cr.numero_documento,
                cr.codigo_referencia,
                cr.modulo_origen,
                cr.proceso_origen,
                cr.observacion

            FROM cajas.cheques_recibidos cr

            INNER JOIN cajas.cajas c
                ON c.id_caja =
                   cr.id_caja

            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta =
                   c.id_catalogo_cuenta_caja

            LEFT JOIN tesoreria.bancos b
                ON b.codigo_banco =
                   cr.codigo_banco

            WHERE c.id_agencia = ?
              AND c.activa = true

              AND cr.estado_cheque = 'RECIBIDO'

              AND c.id_catalogo_cuenta_caja IS NOT NULL

              AND cc.id_agencia = c.id_agencia
              AND cc.cuenta_operable = true

            ORDER BY
                cr.fecha_recibido,
                c.codigo_caja,
                cr.id_cheque_recibido
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        mapCheque(rs),
                idAgencia
        );
    }

    /**
     * Bloquea el cheque físico.
     *
     * No depende de que la provisión donde ingresó el cheque
     * continúe abierta.
     *
     * El cheque puede haber sido recibido días antes.
     */
    public ChequeDisponible obtenerChequeConLock(
            Long idChequeRecibido,
            Integer idAgencia
    ) {

        String sql = """
            SELECT
                cr.id_cheque_recibido,
                cr.id_movimiento_caja,
                cr.id_caja,

                c.codigo_caja,
                c.descripcion AS nombre_caja,
                c.id_agencia,
                c.id_catalogo_cuenta_caja,

                cc.codigo_cuenta AS codigo_cuenta_caja,
                cc.nombre_cuenta AS nombre_cuenta_caja,

                cr.fecha_recibido,
                cr.codigo_banco,

                b.nombre_banco,

                cr.numero_cheque,
                cr.valor_cheque,

                cr.numero_documento,
                cr.codigo_referencia,
                cr.modulo_origen,
                cr.proceso_origen,
                cr.observacion

            FROM cajas.cheques_recibidos cr

            INNER JOIN cajas.cajas c
                ON c.id_caja =
                   cr.id_caja

            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta =
                   c.id_catalogo_cuenta_caja

            LEFT JOIN tesoreria.bancos b
                ON b.codigo_banco =
                   cr.codigo_banco

            WHERE cr.id_cheque_recibido = ?
              AND cr.estado_cheque = 'RECIBIDO'

              AND c.id_agencia = ?
              AND c.activa = true

              AND c.id_catalogo_cuenta_caja IS NOT NULL

              AND cc.id_agencia = c.id_agencia
              AND cc.cuenta_operable = true

            FOR UPDATE OF cr
        """;

        List<ChequeDisponible> datos =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                mapCheque(rs),
                        idChequeRecibido,
                        idAgencia
                );

        return datos.isEmpty()
                ? null
                : datos.get(0);
    }

    // =========================================================
    // PROVISIONES DE CAJA PARA LA FECHA DE CONSIGNACIÓN
    // =========================================================

    /**
     * Lista las cajas de una agencia que tienen una provisión
     * creada para la fecha indicada.
     *
     * La provisión puede estar ABIERTA o CERRADA.
     *
     * Para Tesorería lo importante es que exista la jornada
     * operativa de Caja para esa fecha.
     */
    public List<CajaEfectivoDisponible> listarCajasConProvision(
            Integer idAgencia,
            LocalDate fechaContable
    ) {

        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion AS nombre_caja,

                c.id_catalogo_cuenta_caja,

                cc.codigo_cuenta AS codigo_cuenta_caja,
                cc.nombre_cuenta AS nombre_cuenta_caja,

                p.id_provision,
                p.fecha_contable

            FROM cajas.cajas c

            INNER JOIN cajas.provisiones_diarias p
                ON p.id_caja =
                   c.id_caja
               AND p.fecha_contable = ?

            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta =
                   c.id_catalogo_cuenta_caja

            WHERE c.id_agencia = ?
              AND c.activa = true

              AND c.id_catalogo_cuenta_caja IS NOT NULL

              AND cc.id_agencia = c.id_agencia
              AND cc.cuenta_operable = true

            ORDER BY
                c.codigo_caja
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        mapCaja(rs),
                fechaContable,
                idAgencia
        );
    }

    /**
     * Obtiene y bloquea la provisión existente de una caja
     * para la fecha de la consignación.
     *
     * IMPORTANTE:
     *
     * No exige estado ABIERTA.
     *
     * La provisión solamente debe existir.
     */
    public CajaEfectivoDisponible obtenerCajaConProvisionConLock(
            Long idCaja,
            Integer idAgencia,
            LocalDate fechaContable
    ) {

        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion AS nombre_caja,

                c.id_catalogo_cuenta_caja,

                cc.codigo_cuenta AS codigo_cuenta_caja,
                cc.nombre_cuenta AS nombre_cuenta_caja,

                p.id_provision,
                p.fecha_contable

            FROM cajas.provisiones_diarias p

            INNER JOIN cajas.cajas c
                ON c.id_caja =
                   p.id_caja

            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta =
                   c.id_catalogo_cuenta_caja

            WHERE p.id_caja = ?
              AND p.fecha_contable = ?

              AND c.id_agencia = ?
              AND c.activa = true

              AND c.id_catalogo_cuenta_caja IS NOT NULL

              AND cc.id_agencia = c.id_agencia
              AND cc.cuenta_operable = true

            FOR UPDATE OF p
        """;

        List<CajaEfectivoDisponible> datos =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                mapCaja(rs),
                        idCaja,
                        fechaContable,
                        idAgencia
                );

        return datos.isEmpty()
                ? null
                : datos.get(0);
    }

    // =========================================================
    // MOVIMIENTOS FÍSICOS DE CAJA
    // =========================================================

    /**
     * Registra la salida física del cheque o efectivo.
     *
     * La provisión debe existir, pero no se exige que esté ABIERTA.
     */
    public Long insertarMovimientoCaja(
            Long idProvision,
            Long idCaja,
            Long idDepartamentoOperativo,
            Long idOperacionCaja,
            String numeroConsignacion,
            String codigoReferencia,
            String medioPago,
            BigDecimal valor,
            String concepto,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cajas.movimientos_caja (
                id_provision,
                id_caja,
                fecha_contable,

                id_departamento_operativo,
                id_operacion_caja,

                modulo_origen,
                proceso_origen,
                tabla_origen,
                id_origen,

                tipo_documento,
                numero_documento,

                codigo_referencia,

                naturaleza,
                medio_pago,
                valor,
                concepto,

                estado,

                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            SELECT
                p.id_provision,
                p.id_caja,
                p.fecha_contable,

                ?,
                ?,

                'TESORERIA',
                'CONSIGNACION_BANCARIA',
                NULL,
                NULL,

                'CB',
                ?,

                ?,

                'EGRESO',
                ?,
                ?,
                ?,

                'REGISTRADO',

                ?,
                ?

            FROM cajas.provisiones_diarias p

            WHERE p.id_provision = ?
              AND p.id_caja = ?

            RETURNING id_movimiento_caja
        """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        rs.getLong("id_movimiento_caja"),

                idDepartamentoOperativo,
                idOperacionCaja,

                numeroConsignacion,
                codigoReferencia,

                medioPago,
                valor,
                concepto,

                idUsuario,
                idUsuario,

                idProvision,
                idCaja
        );

        if (datos.isEmpty()) {
            throw new IllegalStateException(
                    "No fue posible registrar el movimiento de salida "
                            + "de la caja "
                            + idCaja
                            + "."
            );
        }

        return datos.get(0);
    }

    // =========================================================
    // ASOCIACIÓN CAJA ↔ CONTABILIDAD
    // =========================================================

    public void asociarComprobanteContable(
            Long idMovimientoCaja,
            String numeroComprobante,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.movimientos_caja
            SET tipo_comprobante = 'CE',
                numero_comprobante = ?,
                fk_seguridad_edicion = ?,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_movimiento_caja = ?
              AND estado = 'REGISTRADO'
        """;

        int actualizados =
                jdbcTemplate.update(
                        sql,
                        numeroComprobante,
                        idUsuario,
                        idMovimientoCaja
                );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No fue posible asociar el comprobante CE "
                            + "al movimiento de caja "
                            + idMovimientoCaja
                            + "."
            );
        }
    }

    // =========================================================
    // ESTADO DEL CHEQUE
    // =========================================================

    public void marcarChequeConsignado(
            Long idChequeRecibido,
            LocalDate fechaConsignacion,
            String numeroConsignacion,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.cheques_recibidos
            SET estado_cheque = 'CONSIGNADO',
                fecha_estado = ?,
                fecha_consignacion = ?,
                numero_consignacion = ?,
                fk_seguridad_edicion = ?,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_cheque_recibido = ?
              AND estado_cheque = 'RECIBIDO'
        """;

        int actualizados =
                jdbcTemplate.update(
                        sql,
                        fechaConsignacion,
                        fechaConsignacion,
                        numeroConsignacion,
                        idUsuario,
                        idChequeRecibido
                );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "El cheque "
                            + idChequeRecibido
                            + " ya no se encuentra disponible "
                            + "para consignación."
            );
        }
    }

    // =========================================================
    // MAPPERS
    // =========================================================

    private CuentaBancariaConsignacion mapCuentaBancaria(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        return new CuentaBancariaConsignacion(
                rs.getLong("id_cuenta_bancaria"),
                rs.getInt("codigo_banco"),
                rs.getString("nombre_banco"),
                rs.getString("numero_cuenta"),
                rs.getString("nombre_cuenta"),
                rs.getString("codigo_tipo_cuenta"),
                rs.getString("nombre_tipo_cuenta"),
                rs.getInt("id_cuenta_contable"),
                rs.getString("codigo_cuenta_contable"),
                rs.getString("nombre_cuenta_contable")
        );
    }

    private ChequeDisponible mapCheque(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        return new ChequeDisponible(
                rs.getLong("id_cheque_recibido"),
                rs.getLong("id_movimiento_caja"),
                rs.getLong("id_caja"),

                rs.getString("codigo_caja"),
                rs.getString("nombre_caja"),

                rs.getInt("id_agencia"),

                rs.getInt("id_catalogo_cuenta_caja"),
                rs.getString("codigo_cuenta_caja"),
                rs.getString("nombre_cuenta_caja"),

                rs.getObject(
                        "fecha_recibido",
                        LocalDate.class
                ),

                rs.getInt("codigo_banco"),
                rs.getString("nombre_banco"),
                rs.getString("numero_cheque"),

                nvl(
                        rs.getBigDecimal("valor_cheque")
                ),

                rs.getString("numero_documento"),
                rs.getString("codigo_referencia"),
                rs.getString("modulo_origen"),
                rs.getString("proceso_origen"),
                rs.getString("observacion")
        );
    }

    private CajaEfectivoDisponible mapCaja(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        return new CajaEfectivoDisponible(
                rs.getLong("id_caja"),
                rs.getInt("id_agencia"),

                rs.getString("codigo_caja"),
                rs.getString("nombre_caja"),

                rs.getInt("id_catalogo_cuenta_caja"),
                rs.getString("codigo_cuenta_caja"),
                rs.getString("nombre_cuenta_caja"),

                rs.getLong("id_provision"),

                rs.getObject(
                        "fecha_contable",
                        LocalDate.class
                )
        );
    }

    // =========================================================
    // RECORDS
    // =========================================================

    public record CuentaBancariaConsignacion(
            Long idCuentaBancaria,
            Integer codigoBanco,
            String nombreBanco,
            String numeroCuenta,
            String nombreCuenta,
            String codigoTipoCuenta,
            String nombreTipoCuenta,
            Integer idCuentaContable,
            String codigoCuentaContable,
            String nombreCuentaContable
    ) {
    }

    public record ChequeDisponible(
            Long idChequeRecibido,
            Long idMovimientoCajaOrigen,
            Long idCaja,

            String codigoCaja,
            String nombreCaja,

            Integer idAgencia,

            Integer idCuentaContableCaja,
            String codigoCuentaContableCaja,
            String nombreCuentaContableCaja,

            LocalDate fechaRecibido,

            Integer codigoBancoCheque,
            String nombreBancoCheque,
            String numeroCheque,

            BigDecimal valorCheque,

            String numeroDocumentoOrigen,
            String codigoReferencia,
            String moduloOrigen,
            String procesoOrigen,
            String observacion
    ) {
    }

    public record CajaEfectivoDisponible(
            Long idCaja,
            Integer idAgencia,

            String codigoCaja,
            String nombreCaja,

            Integer idCuentaContableCaja,
            String codigoCuentaContableCaja,
            String nombreCuentaContableCaja,

            Long idProvision,
            LocalDate fechaContable
    ) {
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private BigDecimal nvl(
            BigDecimal valor
    ) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }
}