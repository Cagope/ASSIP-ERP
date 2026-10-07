package co.assip.erp.nomina.afp;

import co.assip.erp.nomina.afp.dto.AfpDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/afp")
@RequiredArgsConstructor
public class AfpController {

    private final AfpService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public List<AfpDTO> listar() {

        validarAcceso("NOMINA_VIEW");

        return service.listar();
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public AfpDTO obtener(
            @PathVariable Integer id
    ) {

        validarAcceso("NOMINA_VIEW");

        return service.obtener(id);
    }

    // ============================================================
    // CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody AfpDTO dto
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.crear(
                dto,
                idUsuario
        );

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Integer id,
            @RequestBody AfpDTO dto
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(
                id,
                dto,
                idUsuario
        );

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        service.eliminar(id);

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