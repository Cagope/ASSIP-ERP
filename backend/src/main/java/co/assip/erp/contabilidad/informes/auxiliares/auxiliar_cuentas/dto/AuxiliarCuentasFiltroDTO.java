
package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto;

import java.time.LocalDate;

/**
 * Filtros para generar el informe de auxiliares contables por cuentas.
 * El rango incluye las cuentas operables descendientes de las cuentas seleccionadas.
 */
public record AuxiliarCuentasFiltroDTO(
        Integer idAgencia,
        String cuentaInicial,
        String cuentaFinal,
        LocalDate fechaInicial,
        LocalDate fechaFinal
) {
}
