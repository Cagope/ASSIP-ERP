package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/anexo2")
@RequiredArgsConstructor
public class Anexo2Controller {

    private final Anexo2Service service;

    @PostMapping("/{idCierreCartera}/preparar")
    public ResponseEntity<Anexo2Service.ResultadoPreparacionAnexo2> preparar(
            @PathVariable Integer idCierreCartera,
            @RequestParam Integer idUsuario
    ) {

        return ResponseEntity.ok(
                service.preparar(
                        idCierreCartera,
                        idUsuario
                )
        );
    }
}