package co.assip.erp.cartera.originacion.impresion;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cartera/originacion/impresion")
public class OriginacionImpresionController {

    private final OriginacionExpedientePdfService pdfService;

    public OriginacionImpresionController(
            OriginacionExpedientePdfService pdfService
    ) {
        this.pdfService = pdfService;
    }

    // =========================================================
    // EXPEDIENTE DE LA SOLICITUD
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/expediente/pdf")
    public ResponseEntity<byte[]> generarExpediente(
            @PathVariable Integer idSolicitudCredito
    ) {

        return pdfService.generarPdfResponse(
                idSolicitudCredito
        );
    }

    // =========================================================
    // EXPEDIENTE HISTÓRICO DE UNA ACTUACIÓN
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/actuaciones/{idSolicitudAprobacion}/expediente/pdf")
    public ResponseEntity<byte[]> generarExpedienteActuacion(
            @PathVariable Integer idSolicitudCredito,
            @PathVariable Integer idSolicitudAprobacion
    ) {

        return pdfService.generarPdfActuacionResponse(
                idSolicitudCredito,
                idSolicitudAprobacion
        );
    }
}