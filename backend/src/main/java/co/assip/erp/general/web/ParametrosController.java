package co.assip.erp.general.web;

import co.assip.erp.general.dto.ParametroDTOs;
import co.assip.erp.general.service.ParametroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/general/parametros")
public class ParametrosController {

    private final ParametroService service;

    public ParametrosController(ParametroService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ParametroDTOs.ParametroListDTO> list(
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer codigo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombreParametro,asc") String sort
    ) {
        Sort s = sanitizeSort(sort,
                new String[] { "idParametro", "codigoParametro", "nombreParametro", "valorParametro", "tipoValor" },
                "nombreParametro"
        );
        return service.list(idAgencia, q, codigo, PageRequest.of(page, size, s));
    }

    @GetMapping("/{id}")
    public ParametroDTOs.ParametroDetailDTO get(@PathVariable Integer id) {
        return service.get(id);
    }

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
