package co.assip.erp.depositos.movimientos;

import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DepositosMovimientoRepository {

    private static final String TARJETA_NO = "N";

    private final NamedParameterJdbcTemplate jdbc;

    public CuentaSaldoDTO obtenerCuentaConLock(Integer idCuentaAhorro) {

        validarCuenta(idCuentaAhorro);

        String sql = """
            SELECT
                ca.id_cuenta_ahorro,
                ca.saldo_actual_cuenta,
                ca.estado_cuenta_cuenta,
                COALESCE(ea.operativo, false) AS estado_operativo
            FROM depositos.cuentas_ahorro ca
            LEFT JOIN depositos.estados_ahorros ea
                   ON ea.codigo_estado_ahorro = ca.estado_cuenta_cuenta
            WHERE ca.id_cuenta_ahorro = :idCuentaAhorro
            FOR UPDATE OF ca
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("idCuentaAhorro", idCuentaAhorro),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }

                    return mapCuentaSaldo(rs);
                }
        );
    }

    public List<CuentaSaldoDTO> obtenerCuentasConLock(List<Integer> idsCuentas) {

        if (idsCuentas == null || idsCuentas.isEmpty()) {
            throw new RuntimeException("No se recibieron cuentas para bloqueo.");
        }

        String sql = """
            SELECT
                ca.id_cuenta_ahorro,
                ca.saldo_actual_cuenta,
                ca.estado_cuenta_cuenta,
                COALESCE(ea.operativo, false) AS estado_operativo
            FROM depositos.cuentas_ahorro ca
            LEFT JOIN depositos.estados_ahorros ea
                   ON ea.codigo_estado_ahorro = ca.estado_cuenta_cuenta
            WHERE ca.id_cuenta_ahorro IN (:ids)
            FOR UPDATE OF ca
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("ids", idsCuentas),
                (rs, rowNum) -> mapCuentaSaldo(rs)
        );
    }

    public void actualizarSaldo(
            Integer idCuentaAhorro,
            BigDecimal nuevoSaldo,
            Integer idUsuario
    ) {

        validarCuenta(idCuentaAhorro);
        validarUsuario(idUsuario);

        if (nuevoSaldo == null) {
            throw new RuntimeException("El nuevo saldo es obligatorio.");
        }

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "No se puede actualizar una cuenta con saldo negativo."
            );
        }

        String sql = """
            UPDATE depositos.cuentas_ahorro
               SET saldo_actual_cuenta = :nuevoSaldo,
                   fk_seguridad_edicion = :idUsuario,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_cuenta_ahorro = :idCuentaAhorro
        """;

        int updated = jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("nuevoSaldo", nuevoSaldo)
                        .addValue("idUsuario", idUsuario)
                        .addValue("idCuentaAhorro", idCuentaAhorro)
        );

        if (updated <= 0) {
            throw new RuntimeException(
                    "No fue posible actualizar el saldo de la cuenta."
            );
        }
    }

    public void actualizarSaldoBatch(
            List<CuentaSaldoDTO> cuentas,
            Integer idUsuario
    ) {

        validarUsuario(idUsuario);

        if (cuentas == null || cuentas.isEmpty()) {
            throw new RuntimeException(
                    "No se recibieron cuentas para actualizar saldo."
            );
        }

        cuentas.forEach(c -> {

            validarCuenta(c.idCuentaAhorro());

            if (c.saldoActual() == null) {
                throw new RuntimeException(
                        "El saldo nuevo es obligatorio para la cuenta "
                                + c.idCuentaAhorro()
                );
            }

            if (c.saldoActual().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "La cuenta "
                                + c.idCuentaAhorro()
                                + " quedaría con saldo negativo."
                );
            }
        });

        String sql = """
            UPDATE depositos.cuentas_ahorro
               SET saldo_actual_cuenta = :saldoNuevo,
                   fk_seguridad_edicion = :idUsuario,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_cuenta_ahorro = :idCuentaAhorro
        """;

        MapSqlParameterSource[] batch = cuentas.stream()
                .map(c -> new MapSqlParameterSource()
                        .addValue("idCuentaAhorro", c.idCuentaAhorro())
                        .addValue("saldoNuevo", c.saldoActual())
                        .addValue("idUsuario", idUsuario))
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate(sql, batch);
    }

    public void insertarExtracto(
            DepositosMovimientoDTO dto,
            Integer idUsuario
    ) {

        validarMovimiento(dto);
        validarUsuario(idUsuario);

        String sql = insertExtractoSql();

        int inserted = jdbc.update(
                sql,
                buildExtractoParams(dto, idUsuario)
        );

        if (inserted <= 0) {
            throw new RuntimeException(
                    "No fue posible registrar el extracto."
            );
        }
    }

    public void insertarExtractosBatch(
            List<DepositosMovimientoDTO> movimientos,
            Integer idUsuario
    ) {

        validarUsuario(idUsuario);

        if (movimientos == null || movimientos.isEmpty()) {
            throw new RuntimeException(
                    "No se recibieron extractos para registrar."
            );
        }

        movimientos.forEach(this::validarMovimiento);

        String sql = insertExtractoSql();

        MapSqlParameterSource[] batch = movimientos.stream()
                .map(m -> buildExtractoParams(m, idUsuario))
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate(sql, batch);
    }

    private String insertExtractoSql() {

        return """
            INSERT INTO depositos.extractos_cuentas_ahorros (
                id_cuenta_ahorro,
                fecha_movimiento,
                hora_movimiento,
                tipo_comprobante,
                numero_comprobante,
                tipo_movimiento,
                valor_debito,
                valor_credito,
                modulo,
                tarjeta,
                establecimiento,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                :idCuentaAhorro,
                :fechaMovimiento,
                CURRENT_TIME,
                :tipoComprobante,
                :numeroComprobante,
                :tipoMovimiento,
                :valorDebito,
                :valorCredito,
                :modulo,
                :tarjeta,
                :establecimiento,
                :idUsuario,
                :idUsuario
            )
        """;
    }

    private MapSqlParameterSource buildExtractoParams(
            DepositosMovimientoDTO dto,
            Integer idUsuario
    ) {

        return new MapSqlParameterSource()
                .addValue("idCuentaAhorro", dto.getIdCuentaAhorro())
                .addValue("fechaMovimiento", dto.getFechaMovimiento())
                .addValue("tipoComprobante", trim(dto.getTipoComprobante()))
                .addValue("numeroComprobante", trim(dto.getNumeroComprobante()))
                .addValue("tipoMovimiento", trim(dto.getTipoMovimiento()))
                .addValue("valorDebito", nvl(dto.getValorDebito()))
                .addValue("valorCredito", nvl(dto.getValorCredito()))
                .addValue("modulo", trim(dto.getModulo()))
                .addValue(
                        "tarjeta",
                        isBlank(dto.getTarjeta())
                                ? TARJETA_NO
                                : trim(dto.getTarjeta())
                )
                .addValue("establecimiento", trim(dto.getEstablecimiento()))
                .addValue("idUsuario", idUsuario);
    }

    private CuentaSaldoDTO mapCuentaSaldo(java.sql.ResultSet rs)
            throws java.sql.SQLException {

        return new CuentaSaldoDTO(
                rs.getInt("id_cuenta_ahorro"),
                nvl(rs.getBigDecimal("saldo_actual_cuenta")),
                rs.getString("estado_cuenta_cuenta"),
                rs.getBoolean("estado_operativo")
        );
    }

    private void validarMovimiento(DepositosMovimientoDTO dto) {

        if (dto == null) {
            throw new RuntimeException(
                    "El movimiento de depósitos es obligatorio."
            );
        }

        validarCuenta(dto.getIdCuentaAhorro());

        if (dto.getFechaMovimiento() == null) {
            throw new RuntimeException(
                    "La fecha del movimiento es obligatoria."
            );
        }

        if (isBlank(dto.getTipoMovimiento())) {
            throw new RuntimeException(
                    "El tipo de movimiento es obligatorio."
            );
        }

        if (isBlank(dto.getModulo())) {
            throw new RuntimeException(
                    "El módulo es obligatorio."
            );
        }

        BigDecimal debito = nvl(dto.getValorDebito());
        BigDecimal credito = nvl(dto.getValorCredito());

        if (debito.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "No se permiten débitos negativos."
            );
        }

        if (credito.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "No se permiten créditos negativos."
            );
        }

        if (debito.compareTo(BigDecimal.ZERO) <= 0
                && credito.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "El movimiento no tiene valor."
            );
        }

        if (debito.compareTo(BigDecimal.ZERO) > 0
                && credito.compareTo(BigDecimal.ZERO) > 0) {

            throw new RuntimeException(
                    "El movimiento no puede tener débito y crédito al mismo tiempo."
            );
        }
    }

    private void validarCuenta(Integer idCuentaAhorro) {

        if (idCuentaAhorro == null) {
            throw new RuntimeException(
                    "La cuenta de ahorro es obligatoria."
            );
        }
    }

    private void validarUsuario(Integer idUsuario) {

        if (idUsuario == null) {
            throw new RuntimeException(
                    "El usuario es obligatorio."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record CuentaSaldoDTO(
            Integer idCuentaAhorro,
            BigDecimal saldoActual,
            String estadoCuenta,
            Boolean estadoOperativo
    ) {
    }
}