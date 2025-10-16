package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.DepartamentoCat;
import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.DepartamentoCatalogoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.stream.Stream;

@RestController
@RequestMapping("/catalogos/departamentos")
public class DepartamentosCatalogoController {

    private final DepartamentoCatalogoRepository repo;

    public DepartamentosCatalogoController(DepartamentoCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.DepartamentoDTO> list(@RequestParam(required = false) String q) {
        var all = repo.findAll();
        Stream<DepartamentoCat> s = all.stream();
        if (q != null && !q.isBlank()) {
            final String qq = q.toLowerCase(Locale.ROOT).trim();
            s = s.filter(d ->
                    (d.getNombreDepartamento() != null && d.getNombreDepartamento().toLowerCase(Locale.ROOT).contains(qq)) ||
                            (d.getCodigoDepartamento() != null && d.getCodigoDepartamento().toLowerCase(Locale.ROOT).contains(qq))
            );
        }
        return s.map(d -> new CatalogoDTOs.DepartamentoDTO(
                d.getIdDepartamento(),
                d.getNombreDepartamento()
        )).toList();
    }
}
