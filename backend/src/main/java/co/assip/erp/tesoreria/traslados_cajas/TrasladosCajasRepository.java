package co.assip.erp.tesoreria.traslados_cajas;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TrasladosCajasRepository {

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // USUARIO Y TERCERO CONTABLE
    // =========================================================

    public Integer obtenerIdDatosPersonalUsuario(Integer idUsuario) {

        String sql = """
            SELECT id_datos_personal
            FROM seguridad.usuarios
            WHERE id_usuario = ?
              AND activo = TRUE
              AND id_datos_personal IS NOT NULL
            """;

        List<Integer> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("id_datos_personal"),
                idUsuario
        );

        if (datos.isEmpty()) {
            throw new IllegalStateException(
                    "El usuario autenticado no tiene un tercero contable válido."
            );
        }

        return datos.get(0);
    }

    // =========================================================
    // DTO INTERNO DE CAJAS
    // =========================================================

    public record CajaMovimientoDisponible(
            Long idCaja,
            Integer idAgencia,
            String codigoCaja,
            String nombreCaja,
            String tipoCaja,
            Integer idCatalogoCuentaCaja,
            String codigoCuentaCaja,
            String nombreCuentaCaja
    ) {
    }

    // =========================================================
    // LISTAR CAJAS DISPONIBLES
    // =========================================================

    public List<CajaMovimientoDisponible> listarCajasMovimiento(
            Integer idAgencia
    ) {

        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion AS nombre_caja,
                c.tipo_caja,
                c.id_catalogo_cuenta_caja,
                cc.codigo_cuenta AS codigo_cuenta_caja,
                cc.nombre_cuenta AS nombre_cuenta_caja
            FROM cajas.cajas c
            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = c.id_catalogo_cuenta_caja
            WHERE c.id_agencia = ?
              AND c.activa = TRUE
              AND c.permite_efectivo = TRUE
              AND c.tipo_caja IN ('CONTROL', 'OPERATIVA')
              AND cc.id_agencia = c.id_agencia
              AND cc.cuenta_operable = TRUE
            ORDER BY c.tipo_caja, c.codigo_caja
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapCajaMovimiento(rs),
                idAgencia
        );
    }

    // =========================================================
    // CATÁLOGO DE CAJAS CON PROVISIÓN ABIERTA PARA TRASLADOS
    // No depende del usuario vinculado a cada caja.
    // =========================================================

    public record CajaTrasladoAbierta(
            Long idCaja,
            Integer idAgencia,
            String codigoCaja,
            String nombreCaja,
            String tipoCaja,
            Long idProvision,
            LocalDate fechaContable,
            BigDecimal efectivoDisponible
    ) {
    }

    public List<CajaTrasladoAbierta> listarCajasTrasladoAbiertas(
            Integer idAgencia,
            LocalDate fechaContable
    ) {
        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion AS nombre_caja,
                c.tipo_caja,
                p.id_provision,
                p.fecha_contable,
                COALESCE(p.efectivo_inicio, 0)
                + COALESCE((
                    SELECT SUM(CASE
                        WHEN m.naturaleza = 'INGRESO' THEN m.valor
                        WHEN m.naturaleza = 'EGRESO' THEN -m.valor
                        ELSE 0
                    END)
                    FROM cajas.movimientos_caja m
                    WHERE m.id_provision = p.id_provision
                      AND m.estado = 'REGISTRADO'
                      AND m.medio_pago = 'EFECTIVO'
                ), 0) AS efectivo_disponible
            FROM cajas.cajas c
            INNER JOIN cajas.provisiones_diarias p
                ON p.id_caja = c.id_caja
               AND p.fecha_contable = ?
               AND p.estado = 'ABIERTA'
            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = c.id_catalogo_cuenta_caja
            WHERE c.id_agencia = ?
              AND c.activa = TRUE
              AND c.permite_efectivo = TRUE
              AND c.tipo_caja IN ('CONTROL', 'OPERATIVA')
              AND cc.id_agencia = c.id_agencia
              AND cc.cuenta_operable = TRUE
            ORDER BY c.tipo_caja, c.codigo_caja
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CajaTrasladoAbierta(
                        rs.getLong("id_caja"),
                        rs.getInt("id_agencia"),
                        rs.getString("codigo_caja"),
                        rs.getString("nombre_caja"),
                        rs.getString("tipo_caja"),
                        rs.getLong("id_provision"),
                        rs.getObject("fecha_contable", LocalDate.class),
                        rs.getBigDecimal("efectivo_disponible")
                ),
                fechaContable,
                idAgencia
        );
    }

    // =========================================================
    // OBTENER CAJA CON BLOQUEO
    // =========================================================

    public CajaMovimientoDisponible obtenerCajaMovimientoConLock(
            Long idCaja,
            Integer idAgencia
    ) {

        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion AS nombre_caja,
                c.tipo_caja,
                c.id_catalogo_cuenta_caja,
                cc.codigo_cuenta AS codigo_cuenta_caja,
                cc.nombre_cuenta AS nombre_cuenta_caja
            FROM cajas.cajas c
            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = c.id_catalogo_cuenta_caja
            WHERE c.id_caja = ?
              AND c.id_agencia = ?
              AND c.activa = TRUE
              AND c.permite_efectivo = TRUE
              AND c.tipo_caja IN ('CONTROL', 'OPERATIVA')
              AND cc.id_agencia = c.id_agencia
              AND cc.cuenta_operable = TRUE
            FOR UPDATE OF c
            """;

        List<CajaMovimientoDisponible> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapCajaMovimiento(rs),
                idCaja,
                idAgencia
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    private CajaMovimientoDisponible mapCajaMovimiento(
            ResultSet rs
    ) throws SQLException {

        return new CajaMovimientoDisponible(
                rs.getLong("id_caja"),
                rs.getInt("id_agencia"),
                rs.getString("codigo_caja"),
                rs.getString("nombre_caja"),
                rs.getString("tipo_caja"),
                rs.getInt("id_catalogo_cuenta_caja"),
                rs.getString("codigo_cuenta_caja"),
                rs.getString("nombre_cuenta_caja")
        );
    }

    // =========================================================
    // REGISTRAR MOVIMIENTO FÍSICO DE CAJA
    //
    // Se utiliza dos veces por traslado:
    //
    // 1. EGRESO en caja origen.
    // 2. INGRESO en caja destino.
    //
    // Ambas provisiones deben estar ABIERTAS.
    // =========================================================

    public Long insertarMovimientoCaja(
            Long idProvision,
            Long idCaja,
            Long idDepartamentoOperativo,
            Long idOperacionCaja,
            String procesoOrigen,
            String tipoDocumento,
            String numeroDocumento,
            String codigoReferencia,
            String naturaleza,
            String medioPago,
            BigDecimal valor,
            String concepto,
            Integer idUsuario
    ) {

        if (!"INGRESO".equals(naturaleza)
                && !"EGRESO".equals(naturaleza)) {

            throw new IllegalArgumentException(
                    "La naturaleza del movimiento de caja no es válida."
            );
        }

        if (!"EFECTIVO".equals(medioPago)) {

            throw new IllegalArgumentException(
                    "Los traslados entre cajas únicamente permiten efectivo."
            );
        }

        if (valor == null
                || valor.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El valor del movimiento debe ser mayor a cero."
            );
        }

        if (idProvision == null || idCaja == null) {

            throw new IllegalArgumentException(
                    "La provisión y la caja son obligatorias."
            );
        }

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
                ?,
                NULL,
                NULL,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                'REGISTRADO',
                ?,
                ?
            FROM cajas.provisiones_diarias p
            WHERE p.id_provision = ?
              AND p.id_caja = ?
              AND p.estado = 'ABIERTA'
            RETURNING id_movimiento_caja
            """;

        List<Long> ids = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_movimiento_caja"),
                idDepartamentoOperativo,
                idOperacionCaja,
                procesoOrigen,
                tipoDocumento,
                numeroDocumento,
                codigoReferencia,
                naturaleza,
                medioPago,
                valor,
                concepto,
                idUsuario,
                idUsuario,
                idProvision,
                idCaja
        );

        if (ids.size() != 1) {

            throw new IllegalStateException(
                    "No fue posible registrar el movimiento en la caja "
                            + idCaja
                            + ". Verifique que la provisión "
                            + idProvision
                            + " permanezca ABIERTA."
            );
        }

        return ids.get(0);
    }

    // =========================================================
    // ASOCIAR COMPROBANTE CONTABLE
    // =========================================================

    public void asociarComprobanteContable(
            Long idMovimientoCaja,
            String tipoComprobante,
            String numeroComprobante,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.movimientos_caja
            SET
                tipo_comprobante = ?,
                numero_comprobante = ?,
                fk_seguridad_edicion = ?,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_movimiento_caja = ?
              AND estado = 'REGISTRADO'
              AND tipo_comprobante IS NULL
              AND numero_comprobante IS NULL
            """;

        int actualizados = jdbcTemplate.update(
                sql,
                tipoComprobante,
                numeroComprobante,
                idUsuario,
                idMovimientoCaja
        );

        if (actualizados != 1) {

            throw new IllegalStateException(
                    "No fue posible asociar el comprobante al movimiento "
                            + idMovimientoCaja
                            + ". Verifique que no tenga un comprobante previo."
            );
        }
    }

    // =========================================================
    // DTO INTERNO DE PROVISIÓN ABIERTA
    // =========================================================

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

    private CajaEfectivoDisponible mapCaja(
            ResultSet rs
    ) throws SQLException {

        return new CajaEfectivoDisponible(
                rs.getLong("id_caja"),
                rs.getInt("id_agencia"),
                rs.getString("codigo_caja"),
                rs.getString("nombre_caja"),
                rs.getInt("id_catalogo_cuenta_caja"),
                rs.getString("codigo_cuenta_caja"),
                rs.getString("nombre_cuenta_caja"),
                rs.getLong("id_provision"),
                rs.getObject("fecha_contable", LocalDate.class)
        );
    }

    // =========================================================
    // VALIDAR PROVISIÓN ABIERTA
    //
    // Consulta sin bloqueo para vista previa.
    // =========================================================

    public boolean tieneProvisionAbierta(
            Long idCaja,
            Integer idAgencia,
            LocalDate fechaContable
    ) {

        String sql = """
            SELECT 1
            FROM cajas.provisiones_diarias p
            INNER JOIN cajas.cajas c
                ON c.id_caja = p.id_caja
            WHERE p.id_caja = ?
              AND p.fecha_contable = ?
              AND p.estado = 'ABIERTA'
              AND c.id_agencia = ?
              AND c.activa = TRUE
              AND c.permite_efectivo = TRUE
              AND c.tipo_caja IN ('CONTROL', 'OPERATIVA')
            LIMIT 1
            """;

        return !jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt(1),
                idCaja,
                fechaContable,
                idAgencia
        ).isEmpty();
    }

    // =========================================================
    // OBTENER PROVISIÓN ABIERTA CON BLOQUEO
    //
    // El servicio debe llamar este método para ambas cajas
    // en orden ascendente de idCaja.
    // =========================================================

    public CajaEfectivoDisponible obtenerCajaConProvisionAbiertaConLock(
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
                ON c.id_caja = p.id_caja
            INNER JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = c.id_catalogo_cuenta_caja
            WHERE p.id_caja = ?
              AND p.fecha_contable = ?
              AND p.estado = 'ABIERTA'
              AND c.id_agencia = ?
              AND c.activa = TRUE
              AND c.permite_efectivo = TRUE
              AND c.tipo_caja IN ('CONTROL', 'OPERATIVA')
              AND cc.id_agencia = c.id_agencia
              AND cc.cuenta_operable = TRUE
            FOR UPDATE OF p
            """;

        List<CajaEfectivoDisponible> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapCaja(rs),
                idCaja,
                fechaContable,
                idAgencia
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    // =========================================================
    // DEPARTAMENTO OPERATIVO
    // =========================================================

    public Long obtenerDepartamentoOperativo(
            String codigoDepartamento
    ) {

        String sql = """
            SELECT id_departamento_operativo
            FROM cajas.departamentos_operativos
            WHERE codigo_departamento = ?
              AND estado = 'A'
            """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_departamento_operativo"),
                codigoDepartamento
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    // =========================================================
    // OPERACIÓN DE CAJA
    // =========================================================

    public Long obtenerOperacionCaja(
            String codigoOperacion
    ) {

        String sql = """
            SELECT id_operacion_caja
            FROM cajas.operaciones_caja
            WHERE codigo_operacion = ?
              AND estado = 'A'
            """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_operacion_caja"),
                codigoOperacion
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    // =========================================================
    // EFECTIVO DISPONIBLE DE LA JORNADA
    //
    // efectivo_inicio
    // + ingresos registrados en efectivo
    // - egresos registrados en efectivo
    //
    // No incluye cheques ni otros medios de pago.
    // =========================================================

    public BigDecimal obtenerEfectivoDisponibleJornada(
            Long idProvision
    ) {

        String sql = """
            SELECT
                COALESCE(p.efectivo_inicio, 0)
                + COALESCE(
                    SUM(
                        CASE
                            WHEN m.naturaleza = 'INGRESO'
                                THEN m.valor
                            WHEN m.naturaleza = 'EGRESO'
                                THEN -m.valor
                            ELSE 0
                        END
                    ),
                    0
                ) AS disponible
            FROM cajas.provisiones_diarias p
            LEFT JOIN cajas.movimientos_caja m
                ON m.id_provision = p.id_provision
               AND m.estado = 'REGISTRADO'
               AND m.medio_pago = 'EFECTIVO'
            WHERE p.id_provision = ?
              AND p.estado = 'ABIERTA'
            GROUP BY
                p.id_provision,
                p.efectivo_inicio
            """;

        List<BigDecimal> valores = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getBigDecimal("disponible"),
                idProvision
        );

        if (valores.isEmpty()) {

            throw new IllegalStateException(
                    "La jornada no está disponible o ya se encuentra cerrada."
            );
        }

        BigDecimal disponible = valores.get(0);

        return disponible != null
                ? disponible
                : BigDecimal.ZERO;
    }
}