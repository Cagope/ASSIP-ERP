package co.assip.erp.contabilidad.comprobantes.exportacion;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

/**
 * Expone los datos para exportar comprobantes contables a Excel.
 * El servicio valida el permiso de exportación y el alcance de agencias.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/comprobantes/exportacion")
public class ComprobantesExportacionController {
    private final ComprobantesExportacionService service;

    /** Recupera todos los comprobantes filtrados y su detalle sin límite de 100. */
    @GetMapping("/consulta")
    public ComprobantesExportacionService.DatosExportacion exportarConsulta(
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) String tipoComprobante,
            @RequestParam(required = false) String numeroComprobante,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String concepto,
            @RequestParam(required = false) Integer idDatosPersonal
    ) {
        return service.exportarConsulta(idAgencia, tipoComprobante, numeroComprobante,
                fechaDesde, fechaHasta, concepto, idDatosPersonal);
    }

    /** Recupera un comprobante y todos sus movimientos y controles documentales. */
    @GetMapping("/comprobante/{idAgencia}/{tipoComprobante}/{numeroComprobante}")
    public ComprobantesExportacionService.DatosExportacion exportarComprobante(
            @PathVariable Integer idAgencia,
            @PathVariable String tipoComprobante,
            @PathVariable String numeroComprobante
    ) {
        return service.exportarComprobante(idAgencia, tipoComprobante, numeroComprobante);
    }
}
