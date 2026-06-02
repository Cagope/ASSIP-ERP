package co.assip.erp.cajas.recaudos_convenios;

import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioConvenioDTO;
import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioDTO;
import co.assip.erp.cajas.recaudos_convenios.dto.RecaudoConvenioRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cajas/recaudos_convenios")
@RequiredArgsConstructor
public class RecaudosConveniosController {

    private final RecaudosConveniosService service;

    @GetMapping("/convenios-activos")
    public List<RecaudoConvenioConvenioDTO> listarConveniosActivos(
            @RequestParam Integer idAgencia
    ) {
        return service.listarConveniosActivos(idAgencia);
    }

    @GetMapping
    public List<RecaudoConvenioDTO> listarPorProvision(
            @RequestParam Long idProvision
    ) {
        return service.listarPorProvision(idProvision);
    }

    @PostMapping("/aplicar")
    public RecaudoConvenioDTO aplicar(
            @RequestBody RecaudoConvenioRequestDTO request
    ) {
        return service.aplicar(request);
    }
}