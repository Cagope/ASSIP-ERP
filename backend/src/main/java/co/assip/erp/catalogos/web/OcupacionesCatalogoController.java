package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.OcupacionCatalogoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos/ocupaciones")
public class OcupacionesCatalogoController {

    private final OcupacionCatalogoRepository repo;

    public OcupacionesCatalogoController(OcupacionCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.CodigoNombreDTO> list() {
        return repo.findAll().stream()
                .map(o -> new CatalogoDTOs.CodigoNombreDTO(o.getCodigoOcupacion(), o.getNombreOcupacion()))
                .toList();
    }
}
