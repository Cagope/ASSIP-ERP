package co.assip.erp.depositos.informes.cumpleanios_asociados;

import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosRequestDTO;
import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/cumpleanios-asociados")
@RequiredArgsConstructor
public class CumpleaniosAsociadosController {

    private final CumpleaniosAsociadosService service;

    @PostMapping
    public CumpleaniosAsociadosResponseDTO consultar(
            @RequestBody CumpleaniosAsociadosRequestDTO request
    ) {
        return service.consultar(request);
    }

}