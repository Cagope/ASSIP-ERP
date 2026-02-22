package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.liquidacion.dto.LiquidacionRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/liquidacion/preview")
@RequiredArgsConstructor
public class LiquidacionPreviewController {

    private final LiquidacionPreviewService previewService;

    // =========================================================
    // 👁️ PREVISUALIZAR LIQUIDACIÓN DE PERÍODO
    // =========================================================
    @PostMapping
    public ResponseEntity<?> preview(
            @RequestBody LiquidacionRequestDTO request
    ) {

        return ResponseEntity.ok(
                previewService.previewPeriodo(request)
        );
    }
}
