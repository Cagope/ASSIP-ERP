package co.assip.erp.nomina.liquidacion_v2;

import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2PreviewResponseDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2RequestDTO;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/liquidacion-v2")
@RequiredArgsConstructor
public class LiquidacionV2PreviewController {

    private final LiquidacionV2PreviewService service;

    // =========================================================
    // 👁️ PREVIEW
    // =========================================================
    @PostMapping("/preview")
    public LiquidacionV2PreviewResponseDTO preview(
            @RequestBody LiquidacionV2RequestDTO request
    ) {
        return service.preview(request);
    }

    // =========================================================
    // 🔥 PERÍODO AUTOMÁTICO POR AGENCIA
    // =========================================================
    @GetMapping("/periodo-disponible/{idAgencia}")
    public PeriodosNominaRepository.PeriodoLabel obtenerPeriodoDisponible(
            @PathVariable Integer idAgencia
    ) {
        return service.obtenerPrimerPeriodoDisponible(idAgencia);
    }
}