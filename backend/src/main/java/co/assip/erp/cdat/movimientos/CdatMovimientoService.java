package co.assip.erp.cdat.movimientos;

import co.assip.erp.cdat.movimientos.dto.CdatMovimientoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class CdatMovimientoService {

    private final NamedParameterJdbcTemplate jdbc;

    public void registrarCredito(CdatMovimientoDTO dto, Integer idUsuario) {
        validarBase(dto, idUsuario);

        if (!mayorCero(dto.getValorCredito())) {
            throw new RuntimeException("El valor crédito del movimiento CDAT debe ser mayor a cero.");
        }

        insertarExtracto(dto, idUsuario);
        actualizarSaldoCredito(dto, idUsuario);
    }

    public void registrarDebito(CdatMovimientoDTO dto, Integer idUsuario) {
        validarBase(dto, idUsuario);

        if (!mayorCero(dto.getValorDebito())) {
            throw new RuntimeException("El valor débito del movimiento CDAT debe ser mayor a cero.");
        }

        insertarExtracto(dto, idUsuario);
        actualizarSaldoDebito(dto, idUsuario);
    }

    private void insertarExtracto(CdatMovimientoDTO dto, Integer idUsuario) {
        jdbc.update("""
            INSERT INTO cdat.extractos_cuentas_cdats (
                id_cuenta_cdat,
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
            ) VALUES (
                :idCuentaCdat,
                :fechaMovimiento,
                :horaMovimiento,
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
        """, params(dto, idUsuario));
    }

    private void actualizarSaldoCredito(CdatMovimientoDTO dto, Integer idUsuario) {
        jdbc.update("""
            UPDATE cdat.cuentas_cdats
               SET saldo_actual_cdat = saldo_actual_cdat + :valorCredito,
                   fk_seguridad_edicion = :idUsuario,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_cuenta_cdat = :idCuentaCdat
        """, params(dto, idUsuario));
    }

    private void actualizarSaldoDebito(CdatMovimientoDTO dto, Integer idUsuario) {
        int rows = jdbc.update("""
            UPDATE cdat.cuentas_cdats
               SET saldo_actual_cdat = saldo_actual_cdat - :valorDebito,
                   fk_seguridad_edicion = :idUsuario,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_cuenta_cdat = :idCuentaCdat
               AND saldo_actual_cdat >= :valorDebito
        """, params(dto, idUsuario));

        if (rows == 0) {
            throw new RuntimeException("El CDAT no tiene saldo suficiente o no existe.");
        }
    }

    private MapSqlParameterSource params(CdatMovimientoDTO dto, Integer idUsuario) {
        return new MapSqlParameterSource()
                .addValue("idCuentaCdat", dto.getIdCuentaCdat())
                .addValue("fechaMovimiento", dto.getFechaMovimiento())
                .addValue("horaMovimiento", dto.getHoraMovimiento() != null ? dto.getHoraMovimiento() : LocalTime.now())
                .addValue("tipoComprobante", dto.getTipoComprobante())
                .addValue("numeroComprobante", dto.getNumeroComprobante())
                .addValue("tipoMovimiento", dto.getTipoMovimiento())
                .addValue("valorDebito", nvl(dto.getValorDebito()))
                .addValue("valorCredito", nvl(dto.getValorCredito()))
                .addValue("modulo", dto.getModulo() != null ? dto.getModulo() : "11")
                .addValue("tarjeta", dto.getTarjeta() != null ? dto.getTarjeta() : "N")
                .addValue("establecimiento", dto.getEstablecimiento())
                .addValue("idUsuario", idUsuario);
    }

    private void validarBase(CdatMovimientoDTO dto, Integer idUsuario) {
        if (dto.getIdCuentaCdat() == null) {
            throw new RuntimeException("La cuenta CDAT es obligatoria.");
        }

        if (dto.getFechaMovimiento() == null) {
            throw new RuntimeException("La fecha del movimiento CDAT es obligatoria.");
        }

        if (dto.getTipoMovimiento() == null || dto.getTipoMovimiento().isBlank()) {
            throw new RuntimeException("El tipo de movimiento CDAT es obligatorio.");
        }

        if (idUsuario == null) {
            throw new RuntimeException("El usuario del movimiento CDAT es obligatorio.");
        }
    }

    private boolean mayorCero(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}