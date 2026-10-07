package co.assip.erp.nomina.cargos;

import co.assip.erp.nomina.cargos.dto.CargoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public List<CargoDTO> listar() {

        validarAcceso("NOMINA_VIEW");

        return service.listar();
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{idCargo}")
    public CargoDTO obtener(
            @PathVariable Integer idCargo
    ) {

        validarAcceso("NOMINA_VIEW");

        return service.obtener(idCargo);
    }

    // ============================================================
    // CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody CargoDTO dto
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        Integer id = service.crear(dto);

        return ResponseEntity.ok(id);
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{idCargo}")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer idCargo,
            @RequestBody CargoDTO dto
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        service.actualizar(
                idCargo,
                dto
        );

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // DESACTIVAR
    // ============================================================
    @DeleteMapping("/{idCargo}")
    public ResponseEntity<?> desactivar(
            @PathVariable Integer idCargo
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        service.desactivar(idCargo);

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // AUTORIZACIÓN
    // ============================================================
    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}