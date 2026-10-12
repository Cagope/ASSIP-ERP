package co.assip.erp.cajas.ventanilla.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CajaVentanillaEstadoDTO {

    // =========================================================
    // PROVISIÓN
    // =========================================================

    private Long idProvision;
    private LocalDate fechaContable;
    private String estadoProvision;

    // =========================================================
    // CAJA
    // =========================================================

    private Long idCaja;
    private Integer idAgencia;

    private String codigoCaja;
    private String descripcionCaja;
    private String tipoCaja;

    // =========================================================
    // PARÁMETROS DE VENTANILLA
    // =========================================================

    private BigDecimal fondoSugeridoVentanilla;
    private BigDecimal limiteMaximoVentanilla;

    // =========================================================
    // EFECTIVO DE LA PROVISIÓN
    // =========================================================

    private BigDecimal efectivoInicio;

    private BigDecimal entradasOperacionesEfectivo;
    private BigDecimal salidasOperacionesEfectivo;

    private BigDecimal efectivoTotal;

    // =========================================================
    // MOVIMIENTOS INTERNOS CAJA ↔ VENTANILLA
    // =========================================================

    /**
     * Total trasladado desde la caja interna
     * hacia la ventanilla durante la jornada.
     */
    private BigDecimal cajaAVentanilla;

    /**
     * Total trasladado desde la ventanilla
     * hacia la caja interna durante la jornada.
     */
    private BigDecimal ventanillaACaja;

    // =========================================================
    // SALDOS FÍSICOS ACTUALES
    // =========================================================

    /**
     * Efectivo que físicamente debe encontrarse
     * en la ventanilla.
     *
     * cajaAVentanilla
     * - ventanillaACaja
     * + entradasOperacionesEfectivo
     * - salidasOperacionesEfectivo
     */
    private BigDecimal saldoVentanilla;

    /**
     * Efectivo que permanece en la caja interna.
     *
     * efectivoTotal - saldoVentanilla
     */
    private BigDecimal saldoCajaInterna;
}