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
    private final OriginacionPagareService pagareService;

    public OriginacionImpresionController(
            OriginacionExpedientePdfService pdfService,
            OriginacionPagareService pagareService
    ) {
        this.pdfService = pdfService;
        this.pagareService = pagareService;
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

    // =========================================================
    // DATOS DEL PAGARÉ DILIGENCIADO PARA ANGULAR
    //
    // Devuelve:
    // - Identificación del crédito y pagaré.
    // - Condiciones financieras definitivas.
    // - Fechas del crédito constituido.
    // - Deudor principal y codeudores.
    //
    // No genera PDF ni modifica información del crédito.
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/pagare/diligenciado/datos")
    public ResponseEntity<OriginacionPagareService.PagareImpresion>
    consultarDatosPagareDiligenciado(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                pagareService.obtenerPagareDiligenciado(
                        idSolicitudCredito
                )
        );
    }

    // =========================================================
    // DATOS DEL PAGARÉ EN BLANCO Y CARTA DE INSTRUCCIONES
    //
    // Ambos documentos se generan desde Angular.
    //
    // Conserva:
    // - Número del pagaré.
    // - Identificación del crédito.
    // - Deudor principal.
    // - Codeudores.
    // - Datos de contacto de los firmantes.
    //
    // No genera PDF ni modifica información del crédito.
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/pagare/en-blanco/datos")
    public ResponseEntity<OriginacionPagareService.PagareImpresion>
    consultarDatosPagareEnBlanco(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                pagareService.obtenerPagareEnBlanco(
                        idSolicitudCredito
                )
        );
    }
}