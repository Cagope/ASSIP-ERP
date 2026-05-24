package co.assip.erp.shared.cuentas_ahorro.extracto;

import co.assip.erp.shared.cuentas_ahorro.extracto.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shared/cuentas-ahorro/extracto")
@RequiredArgsConstructor
public class ExtractoCuentaSharedController {

    private final ExtractoCuentaSharedService service;

    @PostMapping
    public ExtractoCuentaSharedResponseDTO consultar(
            @RequestBody ExtractoCuentaSharedRequestDTO request
    ) {
        return service.consultar(request);
    }
}