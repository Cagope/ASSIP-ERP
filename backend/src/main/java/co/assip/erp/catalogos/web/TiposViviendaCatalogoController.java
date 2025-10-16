package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.TipoViviendaCatalogoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos/tipos-vivienda")
public class TiposViviendaCatalogoController {

    private final TipoViviendaCatalogoRepository repo;

    public TiposViviendaCatalogoController(TipoViviendaCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.CodigoNombreDTO> list() {
        return repo.findAll().stream()
                .map(t -> new CatalogoDTOs.CodigoNombreDTO(t.getCodigoTipoVivienda(), t.getNombreTipoVivienda()))
                .toList();
    }
}
