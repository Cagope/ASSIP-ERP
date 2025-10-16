package co.assip.erp.general.web;

import co.assip.erp.general.dto.DirectivoDTOs.DirectivoDetailDTO;
import co.assip.erp.general.dto.DirectivoDTOs.DirectivoListItemDTO;
import co.assip.erp.general.service.DirectivoService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/general/directivos")
public class DirectivosController {
    private final DirectivoService service;
    public DirectivosController(DirectivoService service){ this.service = service; }

    @GetMapping
    public ResponseEntity<List<DirectivoListItemDTO>> list(@RequestParam(value="q", required=false) String q) {
        return ResponseEntity.ok(service.list(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectivoDetailDTO> get(@PathVariable @Min(1) Integer id) {
        return ResponseEntity.ok(service.get(id));
    }
}
