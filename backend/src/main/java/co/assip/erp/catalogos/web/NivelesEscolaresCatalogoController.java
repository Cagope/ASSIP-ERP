package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.NivelEscolarCatalogoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos/niveles-escolares")
public class NivelesEscolaresCatalogoController {

    private final NivelEscolarCatalogoRepository repo;

    public NivelesEscolaresCatalogoController(NivelEscolarCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.CodigoNombreDTO> list() {
        return repo.findAll().stream()
                .map(n -> new CatalogoDTOs.CodigoNombreDTO(n.getCodigoEscolaridad(), n.getNombreEscolaridad()))
                .toList();
    }
}
