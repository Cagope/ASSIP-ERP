package co.assip.erp.shared.financiero.tablaamortizacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TablaAmortizacionRequestDTO {

    // =========================================================
    // DATOS DEL CRÉDITO
    // =========================================================

    private BigDecimal valorCredito;

    /**
     * Tasa nominal de colocación.
     * NO es la TEA.
     */
    private BigDecimal tasaColocacion;

    /**
     * Plazo total expresado en meses.
     */
    private Integer plazoMeses;

    /**
     * Periodicidad de amortización de capital en meses.
     * Ejemplo:
     * 1 = mensual
     * 3 = trimestral
     * 6 = semestral
     */
    private Integer amortizacionCapitalMeses;

    /**
     * Periodicidad de pago de intereses en meses.
     */
    private Integer periodoInteresMeses;

    /**
     * Código de modalidad de intereses.
     * Se conserva el código utilizado por cartera.
     */
    private String tipoModalidadInteres;

    /**
     * 1 = Fija
     * 2 = Variable
     * 3 = Otra, actualmente tratada como variable.
     */
    private String codigoTipoCuota;


    // =========================================================
    // FECHAS
    // =========================================================

    /**
     * Fecha de desembolso o fecha base de la simulación.
     *
     * En solicitud puede ser la fecha actual.
     * En cartera será la fecha real de desembolso.
     */
    private LocalDate fechaDesembolso;

    /**
     * Primera fecha en la que corresponde amortizar capital.
     */
    private LocalDate fechaPrimeraCuotaCapital;

    /**
     * Primera fecha en la que corresponde cobrar intereses.
     */
    private LocalDate fechaPrimeraCuotaInteres;


    // =========================================================
    // SEGUROS
    // =========================================================

    /**
     * Primer porcentaje utilizado en el cálculo histórico
     * del seguro.
     *
     * Desde solicitud inicialmente llegará en cero.
     */
    private BigDecimal porcentajeSeguroCredito;

    /**
     * Segundo porcentaje utilizado en el cálculo histórico
     * del seguro.
     *
     * Desde solicitud inicialmente llegará en cero.
     */
    private BigDecimal porcentajeSeguroEntidad;
}