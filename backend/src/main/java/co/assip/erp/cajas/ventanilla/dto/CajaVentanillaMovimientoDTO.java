package co.assip.erp.cajas.ventanilla.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CajaVentanillaMovimientoDTO {

    // =========================================================
    // MOVIMIENTO
    // =========================================================

    /**
     * Sentido del traslado:
     *
     * CAJA_A_VENTANILLA
     * VENTANILLA_A_CAJA
     */
    private String tipoMovimiento;

    /**
     * Valor físico que se trasladará.
     *
     * Debe ser mayor que cero.
     */
    private BigDecimal valor;

    // =========================================================
    // OBSERVACIÓN
    // =========================================================

    /**
     * Observación opcional del traslado.
     */
    private String observacion;
}