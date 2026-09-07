package co.assip.erp.depositos.analisis.concentracion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ConcentracionDepositosResumenDTO {

    private LocalDate fechaCorte;

    private Integer cantidadCuentas;
    private Integer cantidadAsociados;

    /*
     * Captaciones: ahorros diferentes de aportes sociales.
     */
    private BigDecimal saldoCaptaciones;

    /*
     * Aportes sociales, informados separadamente.
     */
    private BigDecimal saldoAportes;

    /*
     * Total general = captaciones + aportes.
     */
    private BigDecimal saldoTotal;

    /*
     * Indicadores calculados sobre la población
     * seleccionada para el análisis.
     */
    private BigDecimal saldoPromedioPorCuenta;
    private BigDecimal saldoPromedioPorAsociado;
    private BigDecimal mayorSaldoAsociado;
}