package co.assip.erp.depositos.informes.inconsistencias;

import co.assip.erp.depositos.informes.inconsistencias.dto.InconsistenciasRequest;
import co.assip.erp.depositos.informes.inconsistencias.dto.InconsistenciaSaldoDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/informes/inconsistencias")
public class InconsistenciasController {

    private final InconsistenciasService service;

    public InconsistenciasController(InconsistenciasService service) {
        this.service = service;
    }

    @PostMapping
    public List<InconsistenciaSaldoDTO> consultar(@RequestBody InconsistenciasRequest request) {
        return service.consultar(request);
    }
}
