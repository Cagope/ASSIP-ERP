
package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Movimiento individual del auxiliar contable por cuentas.
 * Incluye identificación del tercero, valores, saldo acumulado
 * y trazabilidad de creación, edición y módulo de origen.
 */
public record AuxiliarCuentaMovimientoDTO(
        Integer idAuxiliarContable,
        LocalDate fechaAuxiliar,
        String tipoComprobante,
        String numeroComprobante,
        String detalleMovimiento,
        String estadoMovimiento,
        Integer idDatosPersonal,
        String documentoTercero,
        String nombreTercero,
        BigDecimal valorDebito,
        BigDecimal valorCredito,
        BigDecimal valorBase,
        BigDecimal saldoAcumulado,
        Integer fkSeguridadCreacion,
        String usernameCreacion,
        String nombreUsuarioCreacion,
        LocalDateTime fechaCreacion,
        Integer fkSeguridadEdicion,
        String usernameEdicion,
        String nombreUsuarioEdicion,
        LocalDateTime fechaEdicion,
        String origenModulo,
        String documentoOrigen,
        String documentoSoporte
) {
}
