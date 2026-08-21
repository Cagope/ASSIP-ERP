package co.assip.erp.cartera.cierremensual.causacionintereses;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/causacion-intereses")
public class CausacionInteresesController {

    private final CausacionInteresesService service;

    public CausacionInteresesController(
            CausacionInteresesService service
    ) {
        this.service = service;
    }

    // =========================================================
    // EJECUTAR CAUSACIÓN DE INTERESES
    //
    // Ejemplo:
    //
    // POST
    // /api/v1/cartera/causacion-intereses/1/ejecutar
    //
    // PROCESO ACTUAL:
    //
    // 1. toma la fotografía del cierre
    // 2. usa ultima_fecha_interes
    // 3. calcula días 30/360
    // 4. calcula interés bruto
    // 5. aplica porcentaje de causación
    // 6. genera movimiento débito
    //
    // TODAVÍA NO:
    // - calcula contingentes
    // - consolida saldo al cierre
    // - genera comprobante contable
    // =========================================================

    @PostMapping("/{idCierreCartera}/ejecutar")
    public ResponseEntity<Integer> ejecutar(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.ejecutar(
                        idCierreCartera
                )
        );
    }
}