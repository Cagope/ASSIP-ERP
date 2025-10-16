package co.assip.erp.seguridad.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seguridad/health")
public class SeguridadHealthController {

    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SEGURIDAD_OK");
    }
}
