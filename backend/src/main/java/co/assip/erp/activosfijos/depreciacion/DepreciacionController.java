package co.assip.erp.activosfijos.depreciacion;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activos-fijos/depreciacion")
public class DepreciacionController {

    private final DepreciacionPreviewService previewService;
    private final DepreciacionEjecucionService ejecucionService;

    public DepreciacionController(
            DepreciacionPreviewService previewService,
            DepreciacionEjecucionService ejecucionService
    ) {
        this.previewService = previewService;
        this.ejecucionService = ejecucionService;
    }

    /**
     * PREVIEW del proceso de depreciación.
     * No escribe en base de datos.
     */
    @PostMapping("/preview")
    public ResponseEntity<DepreciacionPreviewResult> preview(
            @RequestBody DepreciacionRequestDTO request
    ) {
        DepreciacionPreviewResult result = previewService.ejecutarPreview(request);
        return ResponseEntity.ok(result);
    }

    /**
     * EJECUTA definitivamente la depreciación del período.
     * Es transaccional.
     */
    @PostMapping("/ejecutar")
    public ResponseEntity<DepreciacionEjecucionResult> ejecutar(
            @RequestBody DepreciacionRequestDTO request
    ) {
        DepreciacionEjecucionResult result = ejecucionService.ejecutar(request);
        return ResponseEntity.ok(result);
    }
}
