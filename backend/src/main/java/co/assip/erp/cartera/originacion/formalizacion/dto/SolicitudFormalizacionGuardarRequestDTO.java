package co.assip.erp.cartera.originacion.formalizacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudFormalizacionGuardarRequestDTO {

    // =========================================================
    // VALOR Y PLAZO DEFINITIVOS
    // =========================================================

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 16, fraction = 2)
    private BigDecimal valorFormalizado;

    @NotNull
    @Min(1)
    private Integer plazoFormalizado;

    // =========================================================
    // FORMA DE PAGO
    // =========================================================

    @NotNull
    @Size(min = 1, max = 2)
    private String codigoFormaPagoFormalizada;

    // =========================================================
    // MODALIDAD DE INTERÉS
    // =========================================================

    @NotNull
    @Size(min = 1, max = 1)
    private String periodoCodigoInteresFormalizado;

    @NotNull
    @Size(min = 1, max = 1)
    private String tipoModalidadInteresFormalizado;

    // =========================================================
    // AMORTIZACIÓN Y TIPO DE CUOTA
    // =========================================================

    @NotNull
    @Min(1)
    private Integer amortizacionCapitalFormalizada;

    @NotNull
    @Size(min = 1, max = 1)
    private String codigoTipoCuotaFormalizada;

    // =========================================================
    // PERIODOS DE GRACIA
    // =========================================================

    @NotNull
    @Min(0)
    private Integer mesesGraciaCapitalFormalizados;

    @NotNull
    @Min(0)
    private Integer mesesGraciaInteresFormalizados;

    // =========================================================
    // TASA NOMINAL DEFINITIVA
    // =========================================================

    @NotNull
    @DecimalMin(value = "0.0000", inclusive = true)
    @Digits(integer = 5, fraction = 4)
    private BigDecimal tasaNominalFormalizada;

    // =========================================================
    // CONCEPTO FINAL DE FORMALIZACIÓN
    // =========================================================

    @Size(max = 1000)
    private String conceptoFormalizacion;
}