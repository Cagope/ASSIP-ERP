package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.EstadoCivilCatalogoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos/estados-civiles")
public class EstadosCivilesCatalogoController {

    private final EstadoCivilCatalogoRepository repo;

    public EstadosCivilesCatalogoController(EstadoCivilCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.CodigoNombreDTO> list() {
        return repo.findAll().stream()
                .map(e -> new CatalogoDTOs.CodigoNombreDTO(e.getCodigoEstadoCivil(), e.getNombreEstadoCivil()))
                .toList();
    }
}
