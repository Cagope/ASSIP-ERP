package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.TipoRegimenCatalogoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos/tipos-regimen")
public class TiposRegimenCatalogoController {

    private final TipoRegimenCatalogoRepository repo;

    public TiposRegimenCatalogoController(TipoRegimenCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.TipoRegimenDTO> list() {
        return repo.findAll().stream()
                .map(t -> new CatalogoDTOs.TipoRegimenDTO(
                        t.getCodigoRegimen(),
                        t.getNombreRegimen(),
                        t.getPorcentajeRetencion(),
                        t.getBaseRetencion()
                ))
                .toList();
    }
}
