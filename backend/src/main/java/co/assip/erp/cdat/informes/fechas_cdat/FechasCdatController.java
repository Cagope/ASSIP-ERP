package co.assip.erp.cdat.informes.fechas_cdat;

import co.assip.erp.cdat.informes.fechas_cdat.dto.FechasCdatRequestDTO;
import co.assip.erp.cdat.informes.fechas_cdat.dto.FechasCdatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cdat/informes/fechas")
@RequiredArgsConstructor
public class FechasCdatController {

    private final FechasCdatService service;

    @PostMapping("/consultar")
    public FechasCdatResponseDTO consultar(
            @RequestBody FechasCdatRequestDTO request
    ) {

        return service.consultar(request);
    }

}