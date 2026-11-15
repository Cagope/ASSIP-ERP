package co.assip.erp.contabilidad.cierres.diario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Recibe la agencia y fecha para cerrar o reabrir un día contable.
 * La justificación es obligatoria únicamente para la reapertura.
 */
public record CierreDiarioRequestDTO(
        @NotNull @Positive Integer idAgencia,
        @NotNull LocalDate fecha,
        String justificacion
) {
}
