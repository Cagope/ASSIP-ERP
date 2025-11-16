package co.assip.erp.sarlaft.informes;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sarlaft/informes")
public class InformeSarlaftController {

    private final InformeSarlaftService service;

    public InformeSarlaftController(InformeSarlaftService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> generarInforme(@RequestBody InformeSarlaftRequest request) {
        return ResponseEntity.ok(service.resolverInforme(request));
    }
}
