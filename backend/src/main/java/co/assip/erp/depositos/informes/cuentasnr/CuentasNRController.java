package co.assip.erp.depositos.informes.cuentasnr;

import co.assip.erp.depositos.informes.cuentasnr.dto.CuentasNRRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/cuentas-nr")
public class CuentasNRController {

    private final CuentasNRService service;

    public CuentasNRController(CuentasNRService service) {
        this.service = service;
    }

    @PostMapping
    public Object resolver(@RequestBody CuentasNRRequest request) {
        return service.resolver(request);
    }
}
