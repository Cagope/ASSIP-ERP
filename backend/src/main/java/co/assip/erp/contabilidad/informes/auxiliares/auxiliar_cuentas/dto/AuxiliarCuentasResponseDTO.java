
package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta general del informe de auxiliares contables por cuentas.
 * Agrupa los resultados individuales dentro del período solicitado.
 */
public record AuxiliarCuentasResponseDTO(
        Integer idAgencia,
        String cuentaInicial,
        String cuentaFinal,
        LocalDate fechaInicial,
        LocalDate fechaFinal,
        List<AuxiliarCuentaDTO> cuentas
) {
}
