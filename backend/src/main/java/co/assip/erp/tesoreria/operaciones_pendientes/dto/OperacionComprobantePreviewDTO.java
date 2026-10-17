
package co.assip.erp.tesoreria.operaciones_pendientes.dto;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ASSIP ERP
 * Tesorería - Operaciones Pendientes
 *
 * Vista previa del comprobante contable.
 *
 * No genera consecutivos.
 * No registra movimientos contables.
 * No modifica saldos.
 *
 * cuentasContables:
 * Clave: id_catalogo_cuenta
 * Valor: código y nombre de la cuenta contable.
 */
public record OperacionComprobantePreviewDTO(

        Long idOperacion,

        Integer idAgencia,

        Integer idBeneficiario,

        BigDecimal valorOriginal,

        BigDecimal valorEjecutado,

        BigDecimal saldoDisponible,

        BigDecimal totalPago,

        BigDecimal saldoRestante,

        BigDecimal totalDebito,

        BigDecimal totalCredito,

        Boolean cuadrado,

        List<MovimientoContableDTO> movimientos,

        Map<Integer, String> cuentasContables

) {
}
