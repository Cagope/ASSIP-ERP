package co.assip.erp.shared.referencias.catalogos.sector_economico;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/sectores-economicos")
public class SectorEconomicoController {

    private final SectorEconomicoService service;

    public SectorEconomicoController(SectorEconomicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<SectorEconomico> listar() {
        return service.listar();
    }
}
