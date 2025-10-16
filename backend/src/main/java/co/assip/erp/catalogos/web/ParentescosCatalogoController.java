package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.ParentescoCat;
import co.assip.erp.catalogos.dto.CatalogoDTOs.CodigoNombreDTO;
import co.assip.erp.catalogos.repository.ParentescoCatalogoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogos/parentescos")
public class ParentescosCatalogoController {

    private final ParentescoCatalogoRepository repo;

    public ParentescosCatalogoController(ParentescoCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<CodigoNombreDTO> list(@RequestParam(value = "q", required = false) String q) {
        List<ParentescoCat> list = repo.search(q);
        return list.stream()
                .map(p -> new CodigoNombreDTO(p.getCodigoParentesco(), p.getNombreParentesco()))
                .toList();
    }
}
