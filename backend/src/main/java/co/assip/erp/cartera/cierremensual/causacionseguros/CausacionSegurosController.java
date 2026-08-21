package co.assip.erp.cartera.cierremensual.causacionseguros;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/causacion-seguros")
public class CausacionSegurosController {

    private final CausacionSegurosService service;

    public CausacionSegurosController(
            CausacionSegurosService service
    ) {
        this.service = service;
    }

    // =========================================================
    // EJECUTAR CAUSACION DE SEGUROS
    //
    // POST
    // /cartera/causacion-seguros/{idCierreCartera}/ejecutar
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