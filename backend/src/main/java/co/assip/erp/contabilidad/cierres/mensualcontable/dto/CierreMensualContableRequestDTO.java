package co.assip.erp.contabilidad.cierres.mensualcontable.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Recibe los parámetros del cierre mensual.
 * Valida la agencia, el año y el mes solicitados.
 * La autorización de agencia se verifica en el servicio.
 */
public record CierreMensualContableRequestDTO(
        @NotNull(message = "La agencia es obligatoria.")
        @Min(value = 1, message = "La agencia no es válida.")
        Integer idAgencia,

        @Min(value = 1900, message = "El año no es válido.")
        @Max(value = 9999, message = "El año no es válido.")
        int ano,

        @Min(value = 1, message = "El mes debe estar entre 1 y 12.")
        @Max(value = 12, message = "El mes debe estar entre 1 y 12.")
        int mes
) {
}