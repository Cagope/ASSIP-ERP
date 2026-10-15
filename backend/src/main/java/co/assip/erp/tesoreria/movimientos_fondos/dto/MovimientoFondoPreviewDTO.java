
package co.assip.erp.tesoreria.movimientos_fondos.dto;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Vista previa contable de un movimiento de fondos.
 *
 * Permite revisar el comprobante antes de ejecutar
 * la operación y registrar los movimientos definitivos.
 *
 * Reutiliza MovimientoContablePreviewDTO, utilizado
 * por los demás módulos del ERP.
 */
@Getter
@Builder
public class MovimientoFondoPreviewDTO {

    // =========================================================
    // IDENTIFICACIÓN DE LA OPERACIÓN
    // =========================================================

    private MovimientoFondoRequestDTO.TipoMovimiento tipoMovimiento;

    private MovimientoFondoRequestDTO.ModalidadTrasladoCaja
            modalidadTrasladoCaja;

    private Integer idAgencia;

    private LocalDate fechaMovimiento;

    private String numeroSoporte;

    // =========================================================
    // ORIGEN Y DESTINO
    // =========================================================

    private Long idCuentaBancariaOrigen;

    private Long idCuentaBancariaDestino;

    private Long idCajaOrigen;

    private Long idCajaDestino;

    // =========================================================
    // INFORMACIÓN DEL COMPROBANTE
    // =========================================================

    /**
     * Tipo de comprobante contable.
     *
     * Se determina desde la lógica contable
     * existente para cada operación.
     */
    private String tipoComprobante;

    /**
     * Concepto que se utilizará al contabilizar.
     */
    private String conceptoComprobante;

    // =========================================================
    // MOVIMIENTOS CONTABLES
    // =========================================================

    /**
     * Detalle del comprobante:
     *
     * - Cuenta contable
     * - Tercero
     * - Agencia
     * - Fecha
     * - Concepto del movimiento
     * - Débito
     * - Crédito
     *
     * No se generan consecutivos ni se registran
     * movimientos definitivos durante el preview.
     */
    private List<MovimientoContablePreviewDTO> movimientos;

    // =========================================================
    // TOTALES CONTABLES
    // =========================================================

    private BigDecimal totalDebito;

    private BigDecimal totalCredito;

    private BigDecimal diferencia;

    private Boolean cuadrado;

    // =========================================================
    // VALORES DE LA OPERACIÓN
    // =========================================================

    /**
     * Consignaciones con cheques.
     * Cero para las demás operaciones.
     */
    private BigDecimal valorCheques;

    /**
     * Consignaciones en efectivo.
     * Cero para las demás operaciones.
     */
    private BigDecimal valorEfectivo;

    /**
     * Valor total de la operación.
     */
    private BigDecimal valorTotal;
}
