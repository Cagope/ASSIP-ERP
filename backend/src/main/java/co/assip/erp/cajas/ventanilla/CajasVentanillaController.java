package co.assip.erp.cajas.ventanilla;

import co.assip.erp.cajas.ventanilla.dto.CajaVentanillaEstadoDTO;
import co.assip.erp.cajas.ventanilla.dto.CajaVentanillaMovimientoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cajas/ventanilla")
@RequiredArgsConstructor
public class CajasVentanillaController {

    private final CajasVentanillaService service;


    // =========================================================
    // ESTADO ACTUAL DE CAJA / VENTANILLA
    // =========================================================

    @GetMapping("/actual")
    public ResponseEntity<CajaVentanillaEstadoDTO> obtenerEstadoActual() {

        return ResponseEntity.ok(
                service.obtenerEstadoActual()
        );
    }


    // =========================================================
    // TRASLADO CAJA ↔ VENTANILLA
    // =========================================================

    @PostMapping("/trasladar")
    public ResponseEntity<CajaVentanillaEstadoDTO> trasladar(
            @RequestBody CajaVentanillaMovimientoDTO dto
    ) {

        return ResponseEntity.ok(
                service.trasladar(dto)
        );
    }
}