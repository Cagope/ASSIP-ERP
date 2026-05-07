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

    private final NamedParameterJdbcTemplate jdbc;

    public CuentaSaldoDTO obtenerCuentaConLock(Integer idCuentaAhorro) {

        String sql = """
            SELECT
                id_cuenta_ahorro,
                saldo_actual_cuenta,
                estado_cuenta_cuenta
            FROM depositos.cuentas_ahorro
            WHERE id_cuenta_ahorro = :idCuentaAhorro
            FOR UPDATE
        """;

        return jdbc.query(sql,
                new MapSqlParameterSource("idCuentaAhorro", idCuentaAhorro),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }

                    return new CuentaSaldoDTO(
                            rs.getInt("id_cuenta_ahorro"),
                            rs.getBigDecimal("saldo_actual_cuenta"),
                            rs.getString("estado_cuenta_cuenta")
                    );
                });
    }

    public void actualizarSaldo(Integer idCuentaAhorro, BigDecimal nuevoSaldo, Integer idUsuario) {

        String sql = """
            UPDATE depositos.cuentas_ahorro
               SET saldo_actual_cuenta = :nuevoSaldo,
                   fk_seguridad_edicion = :idUsuario,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_cuenta_ahorro = :idCuentaAhorro
        """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("nuevoSaldo", nuevoSaldo)
                .addValue("idUsuario", idUsuario)
                .addValue("idCuentaAhorro", idCuentaAhorro));
    }

    public void insertarExtracto(DepositosMovimientoDTO dto, Integer idUsuario) {

        String sql = """
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

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idCuentaAhorro", dto.getIdCuentaAhorro())
                .addValue("fechaMovimiento", dto.getFechaMovimiento())
                .addValue("tipoComprobante", dto.getTipoComprobante())
                .addValue("numeroComprobante", dto.getNumeroComprobante())
                .addValue("tipoMovimiento", dto.getTipoMovimiento())
                .addValue("valorDebito", nvl(dto.getValorDebito()))
                .addValue("valorCredito", nvl(dto.getValorCredito()))
                .addValue("modulo", dto.getModulo())
                .addValue("tarjeta", dto.getTarjeta() == null ? "N" : dto.getTarjeta())
                .addValue("establecimiento", dto.getEstablecimiento())
                .addValue("idUsuario", idUsuario));
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record CuentaSaldoDTO(
            Integer idCuentaAhorro,
            BigDecimal saldoActual,
            String estadoCuenta
    ) {
    }

    public List<CuentaSaldoDTO> obtenerCuentasConLock(List<Integer> idsCuentas) {

        String sql = """
        SELECT
            id_cuenta_ahorro,
            saldo_actual_cuenta,
            estado_cuenta_cuenta
        FROM depositos.cuentas_ahorro
        WHERE id_cuenta_ahorro IN (:ids)
        FOR UPDATE
    """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("ids", idsCuentas),
                (rs, rowNum) -> new CuentaSaldoDTO(
                        rs.getInt("id_cuenta_ahorro"),
                        rs.getBigDecimal("saldo_actual_cuenta"),
                        rs.getString("estado_cuenta_cuenta")
                )
        );
    }

    public void actualizarSaldoBatch(List<CuentaSaldoDTO> cuentas, Integer idUsuario) {

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

    public void insertarExtractosBatch(List<DepositosMovimientoDTO> movimientos, Integer idUsuario) {

        String sql = """
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

        MapSqlParameterSource[] batch = movimientos.stream()
                .map(m -> new MapSqlParameterSource()
                        .addValue("idCuentaAhorro", m.getIdCuentaAhorro())
                        .addValue("fechaMovimiento", m.getFechaMovimiento())
                        .addValue("tipoComprobante", m.getTipoComprobante())
                        .addValue("numeroComprobante", m.getNumeroComprobante())
                        .addValue("tipoMovimiento", m.getTipoMovimiento())
                        .addValue("valorDebito", nvl(m.getValorDebito()))
                        .addValue("valorCredito", nvl(m.getValorCredito()))
                        .addValue("modulo", m.getModulo())
                        .addValue("tarjeta", m.getTarjeta() == null ? "N" : m.getTarjeta())
                        .addValue("establecimiento", m.getEstablecimiento())
                        .addValue("idUsuario", idUsuario))
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate(sql, batch);
    }

}