package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.PaisCat;
import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.PaisCatalogoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.stream.Stream;

@RestController
@RequestMapping("/catalogos/paises")
public class PaisesCatalogoController {

    private final PaisCatalogoRepository repo;

    public PaisesCatalogoController(PaisCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.IdNombreDTO> list(@RequestParam(required = false) String q) {
        var all = repo.findAll();
        Stream<PaisCat> s = all.stream();
        if (q != null && !q.isBlank()) {
            String qq = q.toLowerCase(Locale.ROOT).trim();
            s = s.filter(p ->
                    (p.getNombrePais() != null && p.getNombrePais().toLowerCase(Locale.ROOT).contains(qq)) ||
                            (p.getCodigoPais() != null && p.getCodigoPais().toLowerCase(Locale.ROOT).contains(qq)) ||
                            (p.getCodigoDos() != null && p.getCodigoDos().toLowerCase(Locale.ROOT).contains(qq))
            );
        }
        return s.map(p -> new CatalogoDTOs.IdNombreDTO(
                p.getIdPais(),
                p.getNombrePais(),
                p.getCodigoPais()
        )).toList();
    }
}
