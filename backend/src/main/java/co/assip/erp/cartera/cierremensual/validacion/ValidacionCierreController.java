package co.assip.erp.cartera.cierremensual.validacion;

import co.assip.erp.cartera.cierremensual.validacion.dto.ResultadoValidacionCierreDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/cierre-mensual/validacion")
@RequiredArgsConstructor
public class ValidacionCierreController {

    private final ValidacionCierreService service;


    // =========================================================
    // VALIDAR CIERRE
    //
    // Este endpoint:
    //
    // - NO modifica datos.
    // - NO finaliza el cierre.
    // - NO genera comprobantes.
    // - NO genera informes.
    //
    // Solamente ejecuta las validaciones finales
    // actualmente implementadas.
    //
    // Ejemplo:
    //
    // GET
    // /api/v1/cartera/cierre-mensual/validacion/41
    //
    // =========================================================

    @GetMapping("/{idCierreCartera}")
    public ResponseEntity<ResultadoValidacionCierreDTO> validar(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.validar(
                        idCierreCartera
                )
        );
    }
}