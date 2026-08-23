package co.assip.erp.cartera.cierremensual.cuadre;

import co.assip.erp.cartera.cierremensual.cuadre.dto.CuadreCierreDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/cierre-mensual/cuadre")
@RequiredArgsConstructor
public class CuadreCierreController {

    private final CuadreCierreService service;


    // =========================================================
    // CUADRE CONSOLIDADO FINAL DEL CIERRE
    //
    // GET
    // /api/v1/cartera/cierre-mensual/cuadre/{idCierreCartera}
    //
    // IMPORTANTE:
    //
    // - NO recalcula cartera.
    // - NO modifica resultados.
    // - NO modifica PE.
    // - NO cambia estados.
    // - NO genera comprobantes.
    //
    // Solamente consolida y valida:
    //
    // 1. TOTAL = A1 + PE
    // 2. PE general = pe_resultados
    // 3. deterioro PE = pérdida esperada
    //
    // =========================================================

    @GetMapping("/{idCierreCartera}")
    public ResponseEntity<CuadreCierreDTO> cuadrar(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.cuadrar(
                        idCierreCartera
                )
        );
    }
}