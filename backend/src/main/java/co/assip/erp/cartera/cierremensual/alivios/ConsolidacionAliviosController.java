package co.assip.erp.cartera.cierremensual.alivios;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/consolidacion-alivios")
public class ConsolidacionAliviosController {

    private final ConsolidacionAliviosService service;

    public ConsolidacionAliviosController(
            ConsolidacionAliviosService service
    ) {
        this.service = service;
    }

    // =========================================================
    // CONSOLIDAR ALIVIOS DEL CIERRE
    //
    // POST
    // /cartera/consolidacion-alivios/{idCierreCartera}/ejecutar
    // =========================================================

    @PostMapping("/{idCierreCartera}/ejecutar")
    public ResponseEntity<Integer> ejecutar(
            @PathVariable Integer idCierreCartera
    ) {

        int cantidad =
                service.ejecutar(
                        idCierreCartera
                );

        return ResponseEntity.ok(
                cantidad
        );
    }
}