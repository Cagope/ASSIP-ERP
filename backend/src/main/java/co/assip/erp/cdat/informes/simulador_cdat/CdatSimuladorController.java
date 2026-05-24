package co.assip.erp.cdat.informes.simulador_cdat;

import co.assip.erp.cdat.informes.simulador_cdat.dto.CdatSimuladorEntradaDTO;
import co.assip.erp.cdat.informes.simulador_cdat.dto.CdatSimuladorPreviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cdat/informes/simulador-cdat")
@RequiredArgsConstructor
public class CdatSimuladorController {

    private final CdatSimuladorService service;

    @PostMapping("/preview")
    public CdatSimuladorPreviewDTO preview(
            @RequestBody CdatSimuladorEntradaDTO input
    ) {
        return service.generarPreview(input);
    }
}