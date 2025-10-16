package co.assip.erp.general.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ParametroDTOs {

    public record ParametroListDTO(
            Integer idParametro,
            Integer idAgencia,
            String nombreAgencia,
            Integer codigoParametro,
            String nombreParametro,
            BigDecimal valorParametro,
            Boolean tipoValor
    ) {}

    public record ParametroDetailDTO(
            Integer idParametro,
            Integer idAgencia,
            String nombreAgencia,
            Integer codigoParametro,
            String nombreParametro,
            BigDecimal valorParametro,
            Boolean tipoValor
    ) {}

    public record ParametroCreateRequest(
            @NotNull(message = "La agencia es obligatoria")
            Integer idAgencia,

            @NotNull(message = "El código es obligatorio")
            @Min(value = 0, message = "El código no puede ser negativo")
            Integer codigoParametro,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
            String nombreParametro,

            @NotNull(message = "El valor es obligatorio")
            @Digits(integer = 16, fraction = 2)
            BigDecimal valorParametro,

            @NotNull(message = "El tipo de valor es obligatorio (true=valor, false=porcentaje)")
            Boolean tipoValor
    ) {}

    public record ParametroUpdateRequest(
            @NotNull(message = "La agencia es obligatoria")
            Integer idAgencia,

            @NotNull(message = "El código es obligatorio")
            @Min(value = 0, message = "El código no puede ser negativo")
            Integer codigoParametro,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
            String nombreParametro,

            @NotNull(message = "El valor es obligatorio")
            @Digits(integer = 16, fraction = 2)
            BigDecimal valorParametro,

            @NotNull(message = "El tipo de valor es obligatorio (true=valor, false=porcentaje)")
            Boolean tipoValor
    ) {}
}
