package co.assip.erp.general.web;

import co.assip.erp.general.dto.SubZonaDTOs;
import co.assip.erp.general.service.SubZonaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/general/sub-zonas")
public class SubZonasController {

    private final SubZonaService service;

    public SubZonasController(SubZonaService service) {
        this.service = service;
    }

    @GetMapping
    public Page<SubZonaDTOs.SubZonaListDTO> list(
            @RequestParam(required = false) Integer idZona,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombreSubZona,asc") String sort
    ) {
        // Whitelist: EXACTAMENTE nombres de atributos de la ENTIDAD SubZona
        String[] allowed = { "idSubZona", "codigoSubZona", "nombreSubZona", "comentarioSubZona" };

        Sort s = sanitizeSort(sort, allowed, "nombreSubZona");
        return service.list(idZona, q, PageRequest.of(page, size, s));
    }

    @GetMapping("/{id}")
    public SubZonaDTOs.SubZonaDetailDTO get(@PathVariable Integer id) {
        return service.get(id);
    }

    // -------- util local para no romper por sort inválidos ----------
    private Sort sanitizeSort(String sort, String[] allowedProps, String fallbackProp) {
        String raw = (sort == null) ? "" : sort.trim();
        String prop = fallbackProp;
        boolean desc = false;

        if (!raw.isEmpty()) {
            String[] parts = raw.split(",", 2);
            String candidate = parts[0].trim();
            for (String ok : allowedProps) {
                if (ok.equals(candidate)) { prop = candidate; break; }
            }
            if (parts.length > 1) {
                String dir = parts[1].trim();
                desc = "desc".equalsIgnoreCase(dir);
            }
        }
        return desc ? Sort.by(prop).descending() : Sort.by(prop).ascending();
    }
}
