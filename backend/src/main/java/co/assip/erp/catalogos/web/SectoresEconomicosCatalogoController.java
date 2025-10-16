package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.SectorEconomicoCatalogoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos/sectores-economicos")
public class SectoresEconomicosCatalogoController {

    private final SectorEconomicoCatalogoRepository repo;

    public SectoresEconomicosCatalogoController(SectorEconomicoCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.CodigoNombreDTO> list() {
        return repo.findAll().stream()
                .map(s -> new CatalogoDTOs.CodigoNombreDTO(s.getCodigoSectorEconomico(), s.getNombreSectorEconomico()))
                .toList();
    }
}
