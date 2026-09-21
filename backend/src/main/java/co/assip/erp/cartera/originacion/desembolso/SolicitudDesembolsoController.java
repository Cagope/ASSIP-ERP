package co.assip.erp.cartera.originacion.desembolso;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/originacion/desembolso")
public class SolicitudDesembolsoController {

    private final SolicitudDesembolsoService service;

    public SolicitudDesembolsoController(
            SolicitudDesembolsoService service
    ) {
        this.service = service;
    }

    // =========================================================
    // VALIDACIONES FINALES DEL DESEMBOLSO
    //
    // Verifica:
    //
    // - Estado de aprobación de la solicitud.
    // - Deudor principal y codeudores.
    // - Mora en obligaciones propias.
    // - Mora en obligaciones donde figuran como codeudores.
    // - Aportes vigentes y reciprocidad.
    //
    // Actualiza:
    //
    // - Validación de mora por deudor/codeudor.
    // - Validación de aportes de la solicitud.
    //
    // NO asigna pagaré.
    // NO constituye crédito.
    // NO cambia el estado de aprobación.
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/validar")
    public ResponseEntity<
            SolicitudDesembolsoService.ResultadoValidacionDesembolso
            > validar(
            @PathVariable Integer idSolicitudCredito
    ) {

        SolicitudDesembolsoService.ResultadoValidacionDesembolso resultado =
                service.validar(idSolicitudCredito);

        return ResponseEntity.ok(resultado);
    }
}