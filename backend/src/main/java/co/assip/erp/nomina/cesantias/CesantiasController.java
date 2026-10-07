package co.assip.erp.nomina.cesantias;

import co.assip.erp.nomina.cesantias.dto.CesantiasDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/cesantias")
@RequiredArgsConstructor
public class CesantiasController {

    private final CesantiasService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public ResponseEntity<List<CesantiasDTO>> listar() {

        validarAcceso("NOMINA_VIEW");

        return ResponseEntity.ok(
                service.listar()
        );
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<CesantiasDTO> obtener(
            @PathVariable Integer id
    ) {

        validarAcceso("NOMINA_VIEW");

        return ResponseEntity.ok(
                service.obtener(id)
        );
    }

    // ============================================================
    // CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody CesantiasDTO dto
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
            @RequestBody CesantiasDTO dto
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