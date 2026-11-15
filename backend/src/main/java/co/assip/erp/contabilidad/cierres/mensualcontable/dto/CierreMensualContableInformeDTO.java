package co.assip.erp.contabilidad.cierres.mensualcontable.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contiene la información del informe formal del cierre mensual.
 * Reutiliza el resumen y el detalle contable para impresión y Excel.
 * No modifica registros ni ejecuta operaciones de cierre.
 */
public record CierreMensualContableInformeDTO(
        CierreMensualContableResumenDTO resumen,
        List<CierreMensualContableDetalleCuentaDTO> cuentas,
        LocalDateTime fechaGeneracion,
        String usuarioGeneracion,
        LocalDateTime fechaCierre,
        String usuarioCierre
) {
}