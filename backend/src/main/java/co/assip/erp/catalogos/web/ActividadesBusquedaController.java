package co.assip.erp.catalogos.web;

import co.assip.erp.catalogos.domain.ActividadEconomicaDian;
import co.assip.erp.catalogos.domain.ActividadEconomicaSes;
import co.assip.erp.catalogos.repository.ActividadDianRepository;
import co.assip.erp.catalogos.repository.ActividadSesRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogos/actividades")
public class ActividadesBusquedaController {

    private final ActividadSesRepository sesRepo;
    private final ActividadDianRepository dianRepo;

    public ActividadesBusquedaController(ActividadSesRepository sesRepo, ActividadDianRepository dianRepo) {
        this.sesRepo = sesRepo;
        this.dianRepo = dianRepo;
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscar(
            @RequestParam String fuente,          // "SES" o "DIAN"
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        String f = (fuente == null ? "" : fuente.trim().toUpperCase());
        String query = (q == null ? "" : q.trim());
        int top = (limit == null ? 20 : Math.max(1, Math.min(limit, 100)));
        Pageable pageable = PageRequest.of(0, top);

        if (query.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        switch (f) {
            case "SES": {
                List<ActividadEconomicaSes> list =
                        sesRepo.findByCodigoActividadSesContainingIgnoreCaseOrNombreActividadSesContainingIgnoreCase(
                                query, query, pageable
                        );
                return ResponseEntity.ok(
                        list.stream().map(a -> new SimpleItem(a.getCodigoActividadSes(), a.getNombreActividadSes())).toList()
                );
            }
            case "DIAN": {
                List<ActividadEconomicaDian> list =
                        dianRepo.findByCodigoActividadDianContainingIgnoreCaseOrNombreActividadDianContainingIgnoreCase(
                                query, query, pageable
                        );
                return ResponseEntity.ok(
                        list.stream().map(a -> new SimpleItem(a.getCodigoActividadDian(), a.getNombreActividadDian())).toList()
                );
            }
            default:
                return ResponseEntity.badRequest().body("Fuente inválida. Use SES o DIAN.");
        }
    }

    public record SimpleItem(String codigo, String nombre) {}
}
