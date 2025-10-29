package co.assip.erp.shared.referencias.catalogos.actividad_economica_dian;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/catalogos/actividades-economicas-dian")
public class ActividadEconomicaDianController {

    private final ActividadEconomicaDianService service;

    public ActividadEconomicaDianController(ActividadEconomicaDianService service) {
        this.service = service;
    }

    @GetMapping
    public List<ActividadEconomicaDian> listar() {
        return service.listar();
    }
}
