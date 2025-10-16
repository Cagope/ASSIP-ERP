package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.TiposEmpresaCat;
import co.assip.erp.catalogos.repository.TiposEmpresaCatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/catalogos/tipos-empresas")
public class TiposEmpresaController {

    private final TiposEmpresaCatRepository repo;

    public TiposEmpresaController(TiposEmpresaCatRepository repo) {
        this.repo = repo;
    }

    public record CodigoNombreDTO(String codigo, String nombre) {}

    @GetMapping
    public List<TiposEmpresaCat> list(@RequestParam(value = "q", required = false) String q) {
        List<TiposEmpresaCat> data = (q == null || q.isBlank())
                ? repo.findAll()
                : repo.findByNombreTipoEmpresaContainingIgnoreCase(q);

        data.sort(Comparator.comparing(
                TiposEmpresaCat::getCodigoTipoEmpresa,
                Comparator.nullsLast(String::compareToIgnoreCase)
        ));
        return data;
    }

    @GetMapping("/combo")
    public List<CodigoNombreDTO> combo(@RequestParam(value = "q", required = false) String q) {
        List<TiposEmpresaCat> data = (q == null || q.isBlank())
                ? repo.findAll()
                : repo.findByNombreTipoEmpresaContainingIgnoreCase(q);

        return data.stream()
                .map(it -> new CodigoNombreDTO(it.getCodigoTipoEmpresa(), it.getNombreTipoEmpresa()))
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
    public ResponseEntity<TiposEmpresaCat> get(@PathVariable String codigo) {
        return repo.findById(codigo).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TiposEmpresaCat> create(@RequestBody TiposEmpresaCat body) {
        if (body.getCodigoTipoEmpresa() == null || body.getCodigoTipoEmpresa().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (repo.existsById(body.getCodigoTipoEmpresa())) {
            return ResponseEntity.status(409).build();
        }
        var saved = repo.save(body);
        return ResponseEntity.created(URI.create("/catalogos/tipos-empresas/" + saved.getCodigoTipoEmpresa()))
                .body(saved);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<TiposEmpresaCat> update(@PathVariable String codigo, @RequestBody TiposEmpresaCat body) {
        return repo.findById(codigo).map(existing -> {
            body.setCodigoTipoEmpresa(codigo);
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
