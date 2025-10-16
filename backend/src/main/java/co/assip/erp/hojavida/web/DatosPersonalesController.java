package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonalesListItem;
import co.assip.erp.hojavida.dto.DatosPersonalesDTOs.DatosPersonalesDetail;
import co.assip.erp.hojavida.service.DatosPersonalesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hoja-vida/datos-personales")
public class DatosPersonalesController {

    private final DatosPersonalesService service;

    public DatosPersonalesController(DatosPersonalesService service) {
        this.service = service;
    }

    @GetMapping
    public Page<DatosPersonalesListItem> list(
            @RequestParam(value = "q", required = false) String q,
            Pageable pageable
    ) {
        return service.list(q, pageable);
    }

    @GetMapping("/{id}")
    public DatosPersonalesDetail get(@PathVariable Integer id) {
        return service.get(id);
    }

    // 🔎 NUEVO: endpoint de existencia (útil para advertir en front)
    @GetMapping("/existe")
    public Map<String, Boolean> exists(
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("documento") String documento
    ) {
        boolean ok = service.exists(tipoDocumento, documento);
        return Map.of("exists", ok);
    }
}
