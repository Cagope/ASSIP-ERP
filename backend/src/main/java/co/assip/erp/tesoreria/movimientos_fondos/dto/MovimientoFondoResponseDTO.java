
package co.assip.erp.tesoreria.movimientos_fondos.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado de un movimiento de fondos aplicado.
 *
 * Operaciones:
 * - CONSIGNACION
 * - RETIRO
 * - TRASLADO_CAJAS
 * - TRASLADO_BANCARIO
 *
 * La información corresponde a un movimiento
 * registrado y contabilizado correctamente.
 */
@Getter
@AllArgsConstructor
public class MovimientoFondoResponseDTO {

    // =========================================================
    // IDENTIFICACIÓN DEL MOVIMIENTO
    // =========================================================

    private Long idMovimientoFondo;

    private MovimientoFondoRequestDTO.TipoMovimiento tipoMovimiento;

    private Integer idAgencia;

    private LocalDate fechaMovimiento;

    private String numeroSoporte;

    private String observacion;

    // =========================================================
    // CUENTA BANCARIA ORIGEN
    // =========================================================

    private Long idCuentaBancariaOrigen;

    private Integer codigoBancoOrigen;

    private String nombreBancoOrigen;

    private String numeroCuentaOrigen;

    private String nombreCuentaOrigen;

    // =========================================================
    // CUENTA BANCARIA DESTINO
    // =========================================================

    private Long idCuentaBancariaDestino;

    private Integer codigoBancoDestino;

    private String nombreBancoDestino;

    private String numeroCuentaDestino;

    private String nombreCuentaDestino;

    // =========================================================
    // CAJAS
    // =========================================================

    private Long idCajaOrigen;

    private String nombreCajaOrigen;

    private Long idCajaDestino;

    private String nombreCajaDestino;

    private MovimientoFondoRequestDTO.ModalidadTrasladoCaja
            modalidadTrasladoCaja;

    // =========================================================
    // VALORES DEL MOVIMIENTO
    // =========================================================

    /**
     * Cantidad de cheques consignados.
     * Para las demás operaciones será cero.
     */
    private Integer cantidadCheques;

    /**
     * Valor de cheques consignados.
     */
    private BigDecimal valorCheques;

    /**
     * Valor de efectivo consignado.
     */
    private BigDecimal valorEfectivo;

    /**
     * Valor total del movimiento.
     */
    private BigDecimal valorTotal;

    // =========================================================
    // COMPROBANTE CONTABLE
    // =========================================================

    /**
     * Tipo de comprobante generado.
     */
    private String tipoComprobante;

    /**
     * Consecutivo del comprobante contable.
     */
    private String numeroComprobante;

    /**
     * Concepto registrado en contabilidad.
     */
    private String conceptoComprobante;
}
