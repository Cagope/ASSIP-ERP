package co.assip.erp.nomina.concepto_cuentas_contables;

import co.assip.erp.nomina.concepto_cuentas_contables.dto.ConceptoCuentaContableFormDTO;
import co.assip.erp.nomina.concepto_cuentas_contables.dto.ConceptoCuentaContableListDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/concepto-cuentas-contables")
@RequiredArgsConstructor
public class ConceptoCuentasContablesController {

    private final ConceptoCuentasContablesService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public ResponseEntity<List<ConceptoCuentaContableListDTO>> listar() {

        validarAcceso("NOMINA_VIEW");

        return ResponseEntity.ok(
                service.listar()
        );
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{idMapeo}")
    public ResponseEntity<ConceptoCuentaContableFormDTO> obtener(
            @PathVariable Integer idMapeo
    ) {

        validarAcceso("NOMINA_VIEW");

        return ResponseEntity.ok(
                service.obtener(idMapeo)
        );
    }

    // ============================================================
    // CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody ConceptoCuentaContableFormDTO dto
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
    @PutMapping("/{idMapeo}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Integer idMapeo,
            @RequestBody ConceptoCuentaContableFormDTO dto
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(
                idMapeo,
                dto,
                idUsuario
        );

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ELIMINAR (SOFT DELETE)
    // ============================================================
    @DeleteMapping("/{idMapeo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer idMapeo
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.eliminar(
                idMapeo,
                idUsuario
        );

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // AUTORIZACIÓN
    // ============================================================
    private void validarAcceso(
            String permiso
    ) {

        Usuario usuarioActual =
                new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}