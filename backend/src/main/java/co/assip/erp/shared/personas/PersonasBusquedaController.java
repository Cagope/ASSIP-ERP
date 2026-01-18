package co.assip.erp.shared.personas;

import co.assip.erp.shared.personas.dto.PersonaBusquedaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shared/personas")
public class PersonasBusquedaController {

    private final PersonasBusquedaService service;

    @GetMapping("/buscar")
    public List<PersonaBusquedaDTO> buscar(@RequestParam("q") String q) {
        return service.buscar(q);
    }

    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }

    // =========================================================
    // 2) OBTENER PERSONA POR ID (EDICIÓN / CARGA FORM)
    // =========================================================
    @GetMapping("/{id}")
    public PersonaBusquedaDTO obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

}
