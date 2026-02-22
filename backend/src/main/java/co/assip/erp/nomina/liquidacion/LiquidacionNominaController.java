package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/liquidacion")
@RequiredArgsConstructor
public class LiquidacionNominaController {

    private final LiquidacionNominaService liquidacionService;

    // =========================================================
    // ▶️ EJECUTAR LIQUIDACIÓN DE PERÍODO
    // =========================================================
    @PostMapping("/ejecutar")
    public ResponseEntity<Void> ejecutarLiquidacion(
            @RequestBody LiquidacionRequestDTO request
    ) {

        liquidacionService.liquidarPeriodo(request);

        return ResponseEntity.noContent().build(); // 204
    }
}
