package co.assip.erp.shared.financiero.tablaamortizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TablaAmortizacionDetalleDTO {

    private Integer numeroCuota;

    private LocalDate fechaPago;

    private BigDecimal abonoCapital;

    private BigDecimal valorIntereses;

    private BigDecimal valorSeguros;

    /**
     * Saldo después de aplicar el abono de capital
     * correspondiente a la fila.
     */
    private BigDecimal saldoCredito;

    /**
     * Capital + intereses + seguros.
     */
    private BigDecimal valorCuota;
}