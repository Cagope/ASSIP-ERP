package co.assip.erp.nomina.secciones;

import co.assip.erp.nomina.secciones.dto.SeccionNominaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/secciones")
@RequiredArgsConstructor
public class SeccionesController {

    private final SeccionesService service;

    @GetMapping
    public List<SeccionNominaDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SeccionNominaDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    public void crear(@RequestBody SeccionNominaDTO dto) {
        service.crear(dto);
    }

    @PutMapping("/{id}")
    public void actualizar(@PathVariable Integer id, @RequestBody SeccionNominaDTO dto) {
        service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
