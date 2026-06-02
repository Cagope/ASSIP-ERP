package co.assip.erp.cajas.cierre_recaudos_convenios;

import co.assip.erp.cajas.cierre_recaudos_convenios.dto.CierreRecaudosConveniosPreviewDTO;
import co.assip.erp.cajas.cierre_recaudos_convenios.dto.CierreRecaudosConveniosRequestDTO;
import co.assip.erp.cajas.cierre_recaudos_convenios.dto.CierreRecaudosConveniosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cajas/cierre_recaudos_convenios")
@RequiredArgsConstructor
public class CierreRecaudosConveniosController {

    private final CierreRecaudosConveniosService service;

    @PostMapping("/preview")
    public CierreRecaudosConveniosPreviewDTO preview(
            @RequestBody CierreRecaudosConveniosRequestDTO request
    ) {
        return service.preview(request);
    }

    @PostMapping("/aplicar")
    public CierreRecaudosConveniosResponseDTO aplicar(
            @RequestBody CierreRecaudosConveniosRequestDTO request
    ) {
        return service.aplicar(request);
    }
}