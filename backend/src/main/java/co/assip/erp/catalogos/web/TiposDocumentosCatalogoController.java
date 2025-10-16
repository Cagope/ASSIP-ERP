package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.TipoDocumentoCat;
import co.assip.erp.catalogos.repository.TipoDocumentoCatalogoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalogos/tipos-documentos")
public class TiposDocumentosCatalogoController {

    private final TipoDocumentoCatalogoRepository repo;

    public TiposDocumentosCatalogoController(TipoDocumentoCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<ItemDTO> list() {
        return repo.findAll().stream()
                .map(t -> new ItemDTO(
                        t.getTipoDocumento(),        // codigo: "C","E","I","N","O","P","R","U"
                        t.getNombreTipoDocumento(),  // nombre: "Cédula de Ciudadanía", etc.
                        t.getTipoDv(),               // Boolean: true/false
                        t.getTipoDocumentoDian()     // "13","22","12","31","41","41","11","41"
                ))
                .collect(Collectors.toList());
    }

    public record ItemDTO(
            String codigo,
            String nombre,
            Boolean tipoDv,
            String tipoDocumentoDian
    ) {}
}
