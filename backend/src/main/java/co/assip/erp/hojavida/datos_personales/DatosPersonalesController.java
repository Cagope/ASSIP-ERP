package co.assip.erp.hojavida.datos_personales;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de Datos Personales.
 *
 * Endpoint base: /api/v1/hoja-vida/datos-personales
 *
 * Seguridad:
 * - Consulta: HOJAVIDA_PERSONA_VIEW
 * - Creación, actualización y eliminación: HOJAVIDA_PERSONA_EDIT
 */
@RestController
@RequestMapping("/hoja-vida/datos-personales")
public class DatosPersonalesController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_PERSONA_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_PERSONA_EDIT";

    private final DatosPersonalesService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public DatosPersonalesController(
            DatosPersonalesService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    /**
     * Lista todos los registros de datos personales.
     */
    @GetMapping
    public List<DatosPersonales> listar() {

        validarAcceso(PERMISO_VIEW);

        return service.listar();
    }

    /**
     * Busca un registro de datos personales por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DatosPersonales> buscarPorId(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_VIEW);

        Optional<DatosPersonales> dato =
                service.buscarPorId(id);

        return dato
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo registro de datos personales.
     */
    @PostMapping
    public ResponseEntity<DatosPersonales> crear(
            @RequestBody DatosPersonales nuevo
    ) {

        validarAcceso(PERMISO_EDIT);

        DatosPersonales guardado =
                service.crear(nuevo);

        return ResponseEntity.ok(guardado);
    }

    /**
     * Actualiza un registro existente por su ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DatosPersonales> actualizar(
            @PathVariable Integer id,
            @RequestBody DatosPersonales actualizado
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.actualizar(id, actualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un registro por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_EDIT);

        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Obtiene el usuario autenticado y valida el permiso
     * requerido para ejecutar la operación.
     */
    private void validarAcceso(
            String permiso
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Usuario usuarioActual =
                new Usuario();

        usuarioActual.setIdUsuario(
                idUsuario
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}