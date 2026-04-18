package co.assip.erp.nomina.liquidacion_v2;

import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2RequestDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/liquidacion-v2")
public class LiquidacionV2Controller {

    private final LiquidacionV2Service service;

    public LiquidacionV2Controller(LiquidacionV2Service service) {
        this.service = service;
    }

    @PostMapping
    public void liquidar(@RequestBody LiquidacionV2RequestDTO request) {
        service.liquidarPeriodo(request);
    }
}