package co.assip.erp.activosfijos.bloques;

import co.assip.erp.activosfijos.bloques.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activos-fijos/bloques")
public class BloqueController {

    private final BloqueService service;

    @GetMapping
    public List<BloqueListDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public BloqueFormDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    public void crear(
            @RequestBody BloqueSaveDTO dto,
            @RequestHeader("X-Usuario-Id") Integer idUsuario
    ) {
        service.crear(dto, idUsuario);
    }

    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Integer id,
            @RequestBody BloqueSaveDTO dto,
            @RequestHeader("X-Usuario-Id") Integer idUsuario
    ) {
        service.actualizar(id, dto, idUsuario);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
