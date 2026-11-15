package co.assip.erp.contabilidad.cierres.mensualcontable.dto;

import java.time.LocalDate;

/**
 * Representa un día que impide confirmar el cierre mensual.
 * Identifica fechas abiertas o sin registro de cierre diario.
 */
public record CierreMensualContableDiaPendienteDTO(
        LocalDate fecha,
        String estado
) {
}