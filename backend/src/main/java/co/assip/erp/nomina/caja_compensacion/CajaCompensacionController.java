package co.assip.erp.nomina.caja_compensacion;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.nomina.caja_compensacion.dto.CajaCompensacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/caja-compensacion")
@RequiredArgsConstructor
public class CajaCompensacionController {

    private final CajaCompensacionService service;
    private final UsuarioSesionService usuarioSesionService;

    @GetMapping
    public ResponseEntity<List<CajaCompensacionDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CajaCompensacionDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody CajaCompensacionDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.crear(dto, idUsuario);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Integer id,
            @RequestBody CajaCompensacionDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(id, dto, idUsuario);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
