package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.TiposContratoCat;
import co.assip.erp.catalogos.repository.TiposContratoCatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/catalogos/tipos-contratos")
public class TiposContratoController {

    private final TiposContratoCatRepository repo;

    public TiposContratoController(TiposContratoCatRepository repo) {
        this.repo = repo;
    }

    public record CodigoNombreDTO(String codigo, String nombre) {}

    @GetMapping
    public List<TiposContratoCat> list(@RequestParam(value = "q", required = false) String q) {
        List<TiposContratoCat> data = (q == null || q.isBlank())
                ? repo.findAll()
                : repo.findByNombreTipoContratoContainingIgnoreCase(q);

        data.sort(Comparator.comparing(
                TiposContratoCat::getCodigoTipoContrato,
                Comparator.nullsLast(String::compareToIgnoreCase)
        ));
        return data;
    }

    @GetMapping("/combo")
    public List<CodigoNombreDTO> combo(@RequestParam(value = "q", required = false) String q) {
        List<TiposContratoCat> data = (q == null || q.isBlank())
                ? repo.findAll()
                : repo.findByNombreTipoContratoContainingIgnoreCase(q);

        return data.stream()
                .map(it -> new CodigoNombreDTO(it.getCodigoTipoContrato(), it.getNombreTipoContrato()))
                .sorted(
                        Comparator.comparing(
                                CodigoNombreDTO::nombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                        ).thenComparing(
                                CodigoNombreDTO::codigo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                        )
                )
                .toList();
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<TiposContratoCat> get(@PathVariable String codigo) {
        return repo.findById(codigo).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TiposContratoCat> create(@RequestBody TiposContratoCat body) {
        if (body.getCodigoTipoContrato() == null || body.getCodigoTipoContrato().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (repo.existsById(body.getCodigoTipoContrato())) {
            return ResponseEntity.status(409).build();
        }
        var saved = repo.save(body);
        return ResponseEntity.created(URI.create("/catalogos/tipos-contratos/" + saved.getCodigoTipoContrato()))
                .body(saved);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<TiposContratoCat> update(@PathVariable String codigo, @RequestBody TiposContratoCat body) {
        return repo.findById(codigo).map(existing -> {
            body.setCodigoTipoContrato(codigo);
            return ResponseEntity.ok(repo.save(body));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> delete(@PathVariable String codigo) {
        if (!repo.existsById(codigo)) return ResponseEntity.notFound().build();
        repo.deleteById(codigo);
        return ResponseEntity.noContent().build();
    }
}
