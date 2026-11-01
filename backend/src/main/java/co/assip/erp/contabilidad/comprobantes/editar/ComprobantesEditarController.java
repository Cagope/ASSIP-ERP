
package co.assip.erp.contabilidad.comprobantes.editar;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expone la modificación de comprobantes manuales existentes. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/comprobantes")
public class ComprobantesEditarController {

    private final ComprobantesEditarService service;

    /**
     * Actualiza un comprobante manual sin cambiar su agencia, tipo, número ni fecha.
     * El servicio valida permisos, período abierto, movimientos y auditoría.
     */
    @PutMapping("/{idAgencia}/{tipoComprobante}/{numeroComprobante}")
    public ResponseEntity<ComprobanteEditadoResponse> editar(
            @PathVariable Integer idAgencia,
            @PathVariable String tipoComprobante,
            @PathVariable String numeroComprobante,
            @RequestBody ComprobantesEditarService.SolicitudEdicion solicitud,
            HttpServletRequest request
    ) {
        service.editar(idAgencia, tipoComprobante, numeroComprobante, solicitud, request);

        return ResponseEntity.ok(new ComprobanteEditadoResponse(
                idAgencia,
                tipoComprobante.trim().toUpperCase(),
                numeroComprobante.trim(),
                "Comprobante actualizado correctamente."
        ));
    }

    /** Identifica el comprobante actualizado y confirma la operación. */
    public record ComprobanteEditadoResponse(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String mensaje
    ) {}
}
