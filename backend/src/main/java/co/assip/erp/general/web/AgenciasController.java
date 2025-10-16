package co.assip.erp.general.web;

import co.assip.erp.general.dto.AgenciaDTOs.AgenciaDetailDTO;
import co.assip.erp.general.dto.AgenciaDTOs.AgenciaListItemDTO;
import co.assip.erp.general.service.AgenciaService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/general/agencias")
public class AgenciasController {

    private final AgenciaService service;

    public AgenciasController(AgenciaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AgenciaListItemDTO>> list(@RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(service.list(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgenciaDetailDTO> get(@PathVariable @Min(1) Integer id) {
        return ResponseEntity.ok(service.get(id));
    }
}
