package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.TipoPepsCat;
import co.assip.erp.catalogos.repository.TipoPepsCatalogoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RUTA: /catalogos/tipos-peps  →  [{ "tipo_peps": "...", "nombre_tipo_peps": "..." }, ...]
@RestController
@RequestMapping("/catalogos/tipos-peps")
public class TiposPepsCatalogoController {

    private record TipoPepsDTO(String tipo_peps, String nombre_tipo_peps) {}

    private final TipoPepsCatalogoRepository repo;

    public TiposPepsCatalogoController(TipoPepsCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<TipoPepsDTO>> list() {
        List<TipoPepsDTO> out = repo.findAllByOrderByNombreTipoPepsAsc()
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(out);
    }

    private TipoPepsDTO toDto(TipoPepsCat e) {
        return new TipoPepsDTO(e.getTipoPeps(), e.getNombreTipoPeps());
    }
}
