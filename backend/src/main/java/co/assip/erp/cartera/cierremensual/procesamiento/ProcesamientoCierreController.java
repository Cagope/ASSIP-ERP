package co.assip.erp.cartera.cierremensual.procesamiento;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/cierre-mensual")
@RequiredArgsConstructor
public class ProcesamientoCierreController {

    private final ProcesamientoCierreService service;

    // =========================================================
    // PROCESAR CÁLCULOS DE CIERRE
    //
    // POST
    // /api/v1/cartera/cierre-mensual/{idCierreCartera}/procesar-calculos
    //
    // REQUISITOS:
    //
    // - el cierre debe existir
    // - estado_cierre = C
    // - fotografía cerrada en firme
    //
    // ORDEN:
    //
    // 1. Intereses
    // 2. Seguros
    // 3. Alivios
    // 4. Cálculos previos
    // 5. Anexo 1
    // 6. Anexo 2 / PE
    // 7. Validaciones finales
    //
    // IMPORTANTE:
    //
    // - NO recibe idUsuario desde frontend
    // - el usuario se obtiene de la sesión autenticada
    // - NO modifica la fotografía
    // - NO cambia estado_cierre
    // - permite reejecución segura según las reglas
    //   internas de cada proceso
    // =========================================================

    @PostMapping("/{idCierreCartera}/procesar-calculos")
    public ResponseEntity<
            ProcesamientoCierreService.ResultadoProcesamientoCierre
            > procesarCalculos(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.procesar(
                        idCierreCartera
                )
        );
    }
}