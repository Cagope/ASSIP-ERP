package co.assip.erp.general.web;

import co.assip.erp.general.dto.ZonaDTOs;
import co.assip.erp.general.service.ZonaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/general/zonas")
public class ZonasController {

    private final ZonaService service;

    public ZonasController(ZonaService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ZonaDTOs.ZonaListDTO> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombreZona,asc") String sort
    ) {
        Sort s = Sort.by(sort.split(",")[0]).ascending();
        if (sort.endsWith(",desc")) s = s.descending();
        return service.list(q, PageRequest.of(page, size, s));
    }

    @GetMapping("/{id}")
    public ZonaDTOs.ZonaDetailDTO get(@PathVariable Integer id) {
        return service.get(id);
    }
}
