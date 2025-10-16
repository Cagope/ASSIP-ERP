package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.GeneroCatalogoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos/generos")
public class GenerosCatalogoController {

    private final GeneroCatalogoRepository repo;

    public GenerosCatalogoController(GeneroCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.CodigoNombreDTO> list() {
        return repo.findAll().stream()
                .map(g -> new CatalogoDTOs.CodigoNombreDTO(g.getCodigoGenero(), g.getNombreGenero()))
                .toList();
    }
}
