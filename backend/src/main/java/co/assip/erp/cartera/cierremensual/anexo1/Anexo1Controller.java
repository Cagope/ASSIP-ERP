package co.assip.erp.cartera.cierremensual.anexo1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/anexo1")
@RequiredArgsConstructor
public class Anexo1Controller {

    private final Anexo1Service service;

    // =========================================================
    // CALCULAR EDAD CONTABLE
    // ALINEAMIENTO / LEY DE ARRASTRE
    //
    // POST
    // /cartera/anexo1/{idCierreCartera}/edad-contable
    // =========================================================

    @PostMapping("/{idCierreCartera}/edad-contable")
    public ResponseEntity<Integer> calcularEdadContable(
            @PathVariable Integer idCierreCartera,
            @RequestParam Integer idUsuario
    ) {

        int cantidad =
                service.calcularEdadContable(
                        idCierreCartera,
                        idUsuario
                );

        return ResponseEntity.ok(cantidad);
    }

    @PostMapping("/{idCierreCartera}/procesar")
    public ResponseEntity<Anexo1Service.ResultadoAnexo1> procesarAnexo1(
            @PathVariable Integer idCierreCartera,
            @RequestParam Integer idUsuario
    ) {

        return ResponseEntity.ok(
                service.procesarAnexo1(
                        idCierreCartera,
                        idUsuario
                )
        );
    }
}