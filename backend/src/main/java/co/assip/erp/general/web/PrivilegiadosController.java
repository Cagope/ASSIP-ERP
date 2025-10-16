package co.assip.erp.general.web;

import co.assip.erp.general.dto.PrivilegiadoDTOs.PrivilegiadoDetailDTO;
import co.assip.erp.general.dto.PrivilegiadoDTOs.PrivilegiadoListItemDTO;
import co.assip.erp.general.service.PrivilegiadoService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/general/privilegiados")
public class PrivilegiadosController {

    private final PrivilegiadoService service;

    public PrivilegiadosController(PrivilegiadoService service) {
        this.service = service;
    }

    // Lista por directivo
    @GetMapping
    public ResponseEntity<List<PrivilegiadoListItemDTO>> listByDirectivo(
            @RequestParam("idDirectivo") @Min(1) Integer idDirectivo
    ) {
        return ResponseEntity.ok(service.listByDirectivo(idDirectivo));
    }

    // Detalle por id de privilegiado
    @GetMapping("/{id}")
    public ResponseEntity<PrivilegiadoDetailDTO> get(@PathVariable @Min(1) Integer id) {
        return ResponseEntity.ok(service.get(id));
    }
}
