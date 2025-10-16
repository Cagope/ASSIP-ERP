package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.TiposDirectivosCatalogoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos/tipos-directivos")
public class TiposDirectivosCatalogoController {

    private final TiposDirectivosCatalogoRepository repo;

    public TiposDirectivosCatalogoController(TiposDirectivosCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.CodigoNombreDTO> list() {
        return repo.findAll().stream()
                .map(t -> new CatalogoDTOs.CodigoNombreDTO(
                        t.getCodigoTipoDirectivo(),
                        t.getNombreTipoDirectivo()))
                .toList();
    }
}
