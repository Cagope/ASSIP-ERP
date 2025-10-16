package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.JornadaLaboralCat;
import co.assip.erp.catalogos.repository.JornadaLaboralCatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/catalogos/jornadas-laborales")
public class JornadasLaboralesController {

    private final JornadaLaboralCatRepository repo;

    public JornadasLaboralesController(JornadaLaboralCatRepository repo) {
        this.repo = repo;
    }

    public record CodigoNombreDTO(String codigo, String nombre) {}

    @GetMapping
    public List<JornadaLaboralCat> list(@RequestParam(value = "q", required = false) String q) {
        List<JornadaLaboralCat> data = (q == null || q.isBlank())
                ? repo.findAll()
                : repo.findByNombreJornadaContainingIgnoreCase(q);

        data.sort(Comparator.comparing(
                JornadaLaboralCat::getCodigoJornada,
                Comparator.nullsLast(String::compareToIgnoreCase)
        ));
        return data;
    }

    @GetMapping("/combo")
    public List<CodigoNombreDTO> combo(@RequestParam(value = "q", required = false) String q) {
        List<JornadaLaboralCat> data = (q == null || q.isBlank())
                ? repo.findAll()
                : repo.findByNombreJornadaContainingIgnoreCase(q);

        return data.stream()
                .map(it -> new CodigoNombreDTO(it.getCodigoJornada(), it.getNombreJornada()))
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
    public ResponseEntity<JornadaLaboralCat> get(@PathVariable String codigo) {
        return repo.findById(codigo).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<JornadaLaboralCat> create(@RequestBody JornadaLaboralCat body) {
        if (body.getCodigoJornada() == null || body.getCodigoJornada().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (repo.existsById(body.getCodigoJornada())) {
            return ResponseEntity.status(409).build();
        }
        var saved = repo.save(body);
        return ResponseEntity.created(URI.create("/catalogos/jornadas-laborales/" + saved.getCodigoJornada()))
                .body(saved);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<JornadaLaboralCat> update(@PathVariable String codigo, @RequestBody JornadaLaboralCat body) {
        return repo.findById(codigo).map(existing -> {
            body.setCodigoJornada(codigo);
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
