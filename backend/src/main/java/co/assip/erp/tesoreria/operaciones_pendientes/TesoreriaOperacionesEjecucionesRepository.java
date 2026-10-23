
package co.assip.erp.tesoreria.operaciones_pendientes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TesoreriaOperacionesEjecucionesRepository {

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // CONSULTA DE OPERACIONES PENDIENTES
    // =========================================================

    /**
     * Consulta una obligación sin bloquearla.
     *
     * Utilizado por las consultas de solo lectura.
     */
    public Map<String, Object> buscarOperacion(
            Long idOperacion
    ) {
        String sql = """
            SELECT
                id_operacion_pendiente,
                id_agencia,
                id_datos_personal,
                modulo_origen,
                proceso_origen,
                tabla_origen,
                id_origen,
                naturaleza,
                concepto,
                valor_original,
                valor_ejecutado,
                estado
            FROM tesoreria.operaciones_pendientes
            WHERE id_operacion_pendiente = ?
            """;

        List<Map<String, Object>> resultados =
                jdbcTemplate.queryForList(sql, idOperacion);

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }

    /**
     * Bloquea la obligación para preparar ejecuciones.
     *
     * El bloqueo debe permanecer dentro de la misma
     * transacción que valida el saldo e inserta el pago.
     */
    public Map<String, Object> bloquearOperacion(
            Long idOperacion
    ) {
        String sql = """
            SELECT
                id_operacion_pendiente,
                id_agencia,
                id_datos_personal,
                modulo_origen,
                proceso_origen,
                tabla_origen,
                id_origen,
                naturaleza,
                concepto,
                valor_original,
                valor_ejecutado,
                estado
            FROM tesoreria.operaciones_pendientes
            WHERE id_operacion_pendiente = ?
            FOR UPDATE
            """;

        List<Map<String, Object>> resultados =
                jdbcTemplate.queryForList(sql, idOperacion);

        if (resultados.isEmpty()) {
            throw new IllegalArgumentException(
                    "La operación pendiente no existe."
            );
        }

        return resultados.get(0);
    }

    // =========================================================
    // CONTROL DE RESERVAS
    // =========================================================

    /**
     * Suma las ejecuciones PREPARADA.
     *
     * Estas ejecuciones reservan saldo, pero todavía
     * no representan pagos confirmados.
     */
    public BigDecimal sumarPreparadas(
            Long idOperacion
    ) {
        String sql = """
            SELECT COALESCE(SUM(valor), 0)
            FROM tesoreria.operaciones_ejecuciones
            WHERE id_operacion_pendiente = ?
              AND estado = 'PREPARADA'
            """;

        return jdbcTemplate.queryForObject(
                sql,
                BigDecimal.class,
                idOperacion
        );
    }

    // =========================================================
    // CONSULTAS DE EJECUCIONES
    // =========================================================

    /**
     * Lista todas las ejecuciones de una obligación.
     */
    public List<Map<String, Object>> listarEjecuciones(
            Long idOperacion
    ) {
        String sql = """
            SELECT
                id_operacion_ejecucion,
                id_operacion_pendiente,
                numero_linea,
                id_datos_personal_beneficiario,
                id_cuenta_bancaria,
                medio_pago,
                numero_documento,
                valor,
                estado,
                fecha_ejecucion,
                tipo_comprobante,
                numero_comprobante,
                observaciones,
                fk_seguridad_creacion,
                fecha_creacion,
                fk_seguridad_edicion,
                fecha_edicion
            FROM tesoreria.operaciones_ejecuciones
            WHERE id_operacion_pendiente = ?
            ORDER BY numero_linea
            """;

        return jdbcTemplate.queryForList(
                sql,
                idOperacion
        );
    }

    /**
     * Consulta una ejecución por su identificador.
     */
    public Map<String, Object> buscarEjecucion(
            Long idEjecucion
    ) {
        String sql = """
            SELECT
                id_operacion_ejecucion,
                id_operacion_pendiente,
                numero_linea,
                id_datos_personal_beneficiario,
                id_cuenta_bancaria,
                medio_pago,
                numero_documento,
                valor,
                estado,
                fecha_ejecucion,
                tipo_comprobante,
                numero_comprobante,
                observaciones,
                fk_seguridad_creacion,
                fecha_creacion,
                fk_seguridad_edicion,
                fecha_edicion
            FROM tesoreria.operaciones_ejecuciones
            WHERE id_operacion_ejecucion = ?
            """;

        List<Map<String, Object>> resultados =
                jdbcTemplate.queryForList(
                        sql,
                        idEjecucion
                );

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }

    // =========================================================
    // NUMERACIÓN DE EJECUCIONES
    // =========================================================

    /**
     * Obtiene el siguiente número de línea.
     *
     * Debe ejecutarse después de bloquear
     * la obligación mediante bloquearOperacion().
     *
     * La restricción UNIQUE de PostgreSQL protege
     * adicionalmente contra líneas duplicadas.
     */
    public Integer siguienteNumeroLinea(
            Long idOperacion
    ) {
        String sql = """
            SELECT COALESCE(MAX(numero_linea), 0) + 1
            FROM tesoreria.operaciones_ejecuciones
            WHERE id_operacion_pendiente = ?
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                idOperacion
        );
    }

    // =========================================================
    // PREPARACIÓN DE EJECUCIONES
    // =========================================================

    /**
     * Inserta una ejecución PREPARADA.
     *
     * No actualiza el valor ejecutado.
     * No genera comprobantes.
     * No realiza movimientos contables.
     *
     * Debe invocarse desde el servicio transaccional
     * después de validar la operación y el saldo.
     */
    public Long insertarPreparada(
            Long idOperacion,
            Integer numeroLinea,
            Integer idBeneficiario,
            Long idCuentaBancaria,
            String medioPago,
            String numeroDocumento,
            BigDecimal valor,
            String observaciones,
            Integer idUsuario
    ) {
        String sql = """
            INSERT INTO tesoreria.operaciones_ejecuciones (
                id_operacion_pendiente,
                numero_linea,
                id_datos_personal_beneficiario,
                id_cuenta_bancaria,
                medio_pago,
                numero_documento,
                valor,
                estado,
                observaciones,
                fk_seguridad_creacion
            )
            VALUES (
                ?, ?, ?, ?, ?, ?, ?,
                'PREPARADA', ?, ?
            )
            RETURNING id_operacion_ejecucion
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                idOperacion,
                numeroLinea,
                idBeneficiario,
                idCuentaBancaria,
                medioPago,
                numeroDocumento,
                valor,
                observaciones,
                idUsuario
        );
    }


    /**
     * Obtiene la cuenta contable asociada a una cuenta
     * bancaria autorizada para pagar desde la agencia.
     *
     * La autorización se valida nuevamente en backend,
     * independientemente del catálogo mostrado en Angular.
     */
    public Map<String, Object> buscarCuentaParaPago(
            Integer idAgencia,
            Long idCuentaBancaria
    ) {

        String sql = """
                SELECT
                    cb.id_cuenta_bancaria,
                    cb.numero_cuenta,
                    cb.nombre_cuenta,
                    cba.id_agencia,
                    cba.id_cuenta_contable,
                    cc.codigo_cuenta,
                    cc.nombre_cuenta AS nombre_cuenta_contable
                FROM tesoreria.cuentas_bancarias_agencias cba
                INNER JOIN tesoreria.cuentas_bancarias cb
                    ON cb.id_cuenta_bancaria =
                       cba.id_cuenta_bancaria
                INNER JOIN contabilidad.catalogo_cuentas cc
                    ON cc.id_catalogo_cuenta =
                       cba.id_cuenta_contable
                WHERE cba.id_agencia = ?
                  AND cba.id_cuenta_bancaria = ?
                  AND cba.activo = TRUE
                  AND cba.permite_pagar = TRUE
                  AND cb.activo = TRUE
                  AND cb.estado_cuenta = 'ACTIVA'
                  AND cc.id_agencia = cba.id_agencia
                  AND cc.cuenta_operable = TRUE
                """;

        List<Map<String, Object>> resultados =
                jdbcTemplate.queryForList(
                        sql,
                        idAgencia,
                        idCuentaBancaria
                );

        if (resultados.isEmpty()) {
            throw new IllegalArgumentException(
                    "La cuenta bancaria no está habilitada "
                            + "para realizar pagos desde esta agencia."
            );
        }

        return resultados.get(0);
    }


    public void confirmarEjecucion(
            Long idEjecucion,
            String tipoComprobante,
            String numeroComprobante,
            Integer idUsuario
    ) {

        int actualizadas = jdbcTemplate.update("""
            UPDATE tesoreria.operaciones_ejecuciones
               SET estado = 'CONFIRMADA',
                   fecha_ejecucion = CURRENT_TIMESTAMP,
                   tipo_comprobante = ?,
                   numero_comprobante = ?,
                   fk_seguridad_edicion = ?,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_operacion_ejecucion = ?
               AND estado = 'PREPARADA'
            """,
                tipoComprobante,
                numeroComprobante,
                idUsuario,
                idEjecucion
        );

        if (actualizadas != 1) {
            throw new IllegalStateException(
                    "La ejecución no pudo confirmarse."
            );
        }
    }

    public void actualizarSaldoOperacion(
            Long idOperacion,
            BigDecimal nuevoEjecutado,
            String estado,
            Integer idUsuario
    ) {
        int actualizadas = jdbcTemplate.update("""
            UPDATE tesoreria.operaciones_pendientes
               SET valor_ejecutado = ?,
                   estado = ?,
                   fk_seguridad_edicion = ?,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_operacion_pendiente = ?
            """,
                nuevoEjecutado,
                estado,
                idUsuario,
                idOperacion
        );

        if (actualizadas != 1) {
            throw new IllegalStateException(
                    "No se actualizó el saldo de la obligación."
            );
        }
    }

}
