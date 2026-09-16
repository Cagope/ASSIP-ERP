package co.assip.erp.shared.financiero.tablaamortizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TablaAmortizacionResponseDTO {

    // =========================================================
    // PARÁMETROS UTILIZADOS
    // =========================================================

    private BigDecimal valorCredito;

    private BigDecimal tasaColocacion;

    private Integer plazoMeses;

    private Integer amortizacionCapitalMeses;

    private Integer periodoInteresMeses;

    private String tipoModalidadInteres;

    private String codigoTipoCuota;

    private LocalDate fechaDesembolso;

    private LocalDate fechaPrimeraCuotaCapital;

    private LocalDate fechaPrimeraCuotaInteres;


    // =========================================================
    // RESULTADO
    // =========================================================

    private Integer cantidadCuotas;

    /**
     * Para cuota fija corresponde a la cuota financiera
     * calculada antes de seguros.
     *
     * Para cuota variable corresponde al capital periódico
     * normal.
     */
    private BigDecimal valorCuotaProyectada;

    private BigDecimal totalCapital;

    private BigDecimal totalIntereses;

    private BigDecimal totalSeguros;

    private BigDecimal totalPagado;

    private List<TablaAmortizacionDetalleDTO> detalle;
}