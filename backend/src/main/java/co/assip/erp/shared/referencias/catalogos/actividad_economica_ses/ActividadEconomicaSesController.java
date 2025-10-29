package co.assip.erp.shared.referencias.catalogos.actividad_economica_ses;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/actividades-economicas-ses")
public class ActividadEconomicaSesController {

    private final ActividadEconomicaSesService service;

    public ActividadEconomicaSesController(ActividadEconomicaSesService service) {
        this.service = service;
    }

    @GetMapping
    public List<ActividadEconomicaSes> listar() {
        return service.listar();
    }
}
