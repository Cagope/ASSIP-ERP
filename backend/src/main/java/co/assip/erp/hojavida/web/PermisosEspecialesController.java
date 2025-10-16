package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.service.PermisoEspecialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoja-vida/datos-personales/{idDatosPersonal}/permiso-especial")
public class PermisosEspecialesController {

    private final PermisoEspecialService service;

    public PermisosEspecialesController(PermisoEspecialService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getByPersona(@PathVariable Integer idDatosPersonal) {
        return service.getByPersona(idDatosPersonal)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
