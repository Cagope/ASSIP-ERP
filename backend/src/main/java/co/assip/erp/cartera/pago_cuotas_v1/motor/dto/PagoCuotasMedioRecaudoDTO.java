package co.assip.erp.cartera.pago_cuotas_v1.motor.dto;

import java.math.BigDecimal;

/**
 * Selección del medio de recaudo para un pago de cartera.
 * No ejecuta movimientos ni genera comprobantes.
 *
 * CAJA: requiere idCaja.
 * AHORROS: requiere idCuentaAhorro.
 * CXP: requiere idCuentaPorPagar (catálogo contable por agencia).
 * La elegibilidad y los permisos se validan en el servicio, no en este DTO.
 */
public record PagoCuotasMedioRecaudoDTO(
        TipoMedioRecaudo tipoMedio,
        BigDecimal valor,
        Long idCaja,
        Long idCuentaAhorro,
        Long idCuentaPorPagar
) {
    public enum TipoMedioRecaudo {
        CAJA,
        AHORROS,
        CXP
    }
}
