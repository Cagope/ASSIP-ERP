package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.CiudadCat;
import co.assip.erp.catalogos.dto.CatalogoDTOs;
import co.assip.erp.catalogos.repository.CiudadCatalogoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.stream.Stream;

@RestController
@RequestMapping("/catalogos/ciudades")
public class CiudadesCatalogoController {

    private final CiudadCatalogoRepository repo;

    public CiudadesCatalogoController(CiudadCatalogoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public java.util.List<CatalogoDTOs.CiudadDTO> list(
            @RequestParam(name = "departamentoId") Integer departamentoId,
            @RequestParam(required = false) String q
    ) {
        var all = repo.findAll();
        Stream<CiudadCat> s = all.stream()
                .filter(c -> c.getIdDepartamento() != null && c.getIdDepartamento().equals(departamentoId));

        if (q != null && !q.isBlank()) {
            final String qq = q.toLowerCase(Locale.ROOT).trim();
            s = s.filter(c ->
                    (c.getNombreCiudad() != null && c.getNombreCiudad().toLowerCase(Locale.ROOT).contains(qq)) ||
                            (c.getCodigoCiudad() != null && c.getCodigoCiudad().toLowerCase(Locale.ROOT).contains(qq))
            );
        }
        return s.map(c -> new CatalogoDTOs.CiudadDTO(
                c.getIdCiudad(),
                c.getNombreCiudad(),
                c.getIdDepartamento()
        )).toList();
    }
}
