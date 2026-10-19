
package co.assip.erp.depositos.retiro_asociado;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RetiroAsociadoRepository {

    private static final Set<String> ESTADOS_FINALES =
            Set.of("C", "R");

    private final NamedParameterJdbcTemplate jdbc;

    public record CuentaRetiro(
            Integer idCuentaAhorro,
            Integer idDatosPersonal,
            Integer idAgencia,
            Integer idFormaAhorro,
            String codigoCuenta,
            String codigoForma,
            String nombreForma,
            String tipoCaptacion,
            BigDecimal saldoActual,
            String estadoCuenta,
            Boolean estadoOperativo,
            LocalDate fechaEstadoCuenta
    ) {}

    /**
     * Consulta todas las cuentas del asociado, incluyendo
     * las que tienen saldo cero y estados no operativos.
     *
     * La selección de cuentas con saldo positivo se realiza
     * posteriormente en el servicio.
     */
    public List<CuentaRetiro> consultarCuentas(
            Integer idDatosPersonal
    ) {
        validarAsociado(idDatosPersonal);

        String sql = """
            SELECT
                ca.id_cuenta_ahorro,
                ca.id_datos_personal,
                ca.id_agencia,
                ca.id_forma_ahorro,
                TRIM(ca.codigo_cuenta) AS codigo_cuenta,
                TRIM(fa.codigo_forma) AS codigo_forma,
                fa.nombre_forma,
                TRIM(fa.tipo_captacion_forma) AS tipo_captacion,
                ca.saldo_actual_cuenta AS saldo_actual,
                TRIM(ca.estado_cuenta_cuenta) AS estado_cuenta,
                ea.operativo AS estado_operativo,
                ca.fecha_estado_cuenta
            FROM depositos.cuentas_ahorro ca
            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro
            LEFT JOIN depositos.estados_ahorros ea
                ON ea.codigo_estado_ahorro = ca.estado_cuenta_cuenta
            WHERE ca.id_datos_personal = :idDatosPersonal
            ORDER BY
                ca.id_agencia,
                fa.codigo_forma,
                ca.id_cuenta_ahorro
            """;

        return jdbc.query(
                sql,
                parametrosAsociado(idDatosPersonal),
                this::mapearCuenta
        );
    }

    /**
     * Bloquea las cuentas del asociado para impedir
     * modificaciones concurrentes durante el retiro.
     *
     * Debe ejecutarse dentro de una transacción.
     */
    public List<CuentaRetiro> bloquearCuentas(
            Integer idDatosPersonal
    ) {
        validarAsociado(idDatosPersonal);

        String sql = """
            SELECT
                ca.id_cuenta_ahorro,
                ca.id_datos_personal,
                ca.id_agencia,
                ca.id_forma_ahorro,
                TRIM(ca.codigo_cuenta) AS codigo_cuenta,
                TRIM(fa.codigo_forma) AS codigo_forma,
                fa.nombre_forma,
                TRIM(fa.tipo_captacion_forma) AS tipo_captacion,
                ca.saldo_actual_cuenta AS saldo_actual,
                TRIM(ca.estado_cuenta_cuenta) AS estado_cuenta,
                ea.operativo AS estado_operativo,
                ca.fecha_estado_cuenta
            FROM depositos.cuentas_ahorro ca
            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro
            LEFT JOIN depositos.estados_ahorros ea
                ON ea.codigo_estado_ahorro = ca.estado_cuenta_cuenta
            WHERE ca.id_datos_personal = :idDatosPersonal
            ORDER BY ca.id_cuenta_ahorro
            FOR UPDATE OF ca
            """;

        return jdbc.query(
                sql,
                parametrosAsociado(idDatosPersonal),
                this::mapearCuenta
        );
    }

    /**
     * Actualiza el estado y el comentario de una cuenta
     * después de descargar completamente su saldo.
     *
     * No modifica el indicador cuenta_activa.
     */
    public void actualizarEstadoRetiro(
            Integer idCuentaAhorro,
            String estadoFinal,
            LocalDate fechaProceso,
            String comentarioCuentas,
            Integer idUsuario
    ) {
        if (idCuentaAhorro == null || idCuentaAhorro <= 0
                || idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "La cuenta y el usuario son obligatorios."
            );
        }

        if (estadoFinal == null
                || !ESTADOS_FINALES.contains(estadoFinal)) {
            throw new IllegalArgumentException(
                    "El estado final debe ser C (cancelado) "
                            + "o R (retirado)."
            );
        }

        if (fechaProceso == null) {
            throw new IllegalArgumentException(
                    "Debe informar la fecha del proceso."
            );
        }

        if (comentarioCuentas == null
                || comentarioCuentas.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe registrar el motivo del retiro del asociado."
            );
        }

        String comentario = comentarioCuentas.trim();

        if (comentario.length() > 500) {
            throw new IllegalArgumentException(
                    "El comentario no puede superar 500 caracteres."
            );
        }

        String sql = """
            UPDATE depositos.cuentas_ahorro
            SET
                estado_cuenta_cuenta = :estadoFinal,
                fecha_estado_cuenta = :fechaProceso,
                comentario_cuentas = :comentarioCuentas,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_cuenta_ahorro = :idCuentaAhorro
              AND saldo_actual_cuenta = 0
            """;

        int actualizados = jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuentaAhorro", idCuentaAhorro)
                        .addValue("estadoFinal", estadoFinal)
                        .addValue("fechaProceso", fechaProceso)
                        .addValue("comentarioCuentas", comentario)
                        .addValue("idUsuario", idUsuario)
        );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No fue posible actualizar el estado de la cuenta "
                            + idCuentaAhorro
                            + ". Verifique que su saldo sea cero."
            );
        }
    }

    private void validarAsociado(Integer idDatosPersonal) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException(
                    "El asociado es obligatorio."
            );
        }
    }

    private MapSqlParameterSource parametrosAsociado(
            Integer idDatosPersonal
    ) {
        return new MapSqlParameterSource()
                .addValue("idDatosPersonal", idDatosPersonal);
    }

    private CuentaRetiro mapearCuenta(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        java.sql.Date fecha =
                rs.getDate("fecha_estado_cuenta");

        return new CuentaRetiro(
                rs.getInt("id_cuenta_ahorro"),
                rs.getInt("id_datos_personal"),
                (Integer) rs.getObject("id_agencia"),
                (Integer) rs.getObject("id_forma_ahorro"),
                rs.getString("codigo_cuenta"),
                rs.getString("codigo_forma"),
                rs.getString("nombre_forma"),
                rs.getString("tipo_captacion"),
                rs.getBigDecimal("saldo_actual"),
                rs.getString("estado_cuenta"),
                rs.getObject("estado_operativo", Boolean.class),
                fecha == null ? null : fecha.toLocalDate()
        );
    }
}
