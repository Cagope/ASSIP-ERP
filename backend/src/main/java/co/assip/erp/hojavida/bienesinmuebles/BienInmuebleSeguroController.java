package co.assip.erp.hojavida.bienesinmueblesseguros;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de seguros
 * asociados a bienes inmuebles.
 *
 * Endpoint base:
 * /api/v1/hoja-vida/bienes-inmuebles-seguros
 */
@RestController
@RequestMapping("/hoja-vida/bienes-inmuebles-seguros")
public class BienInmuebleSeguroController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_BIENES_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_BIENES_EDIT";

    private final BienInmuebleSeguroService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public BienInmuebleSeguroController(
            BienInmuebleSeguroService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    /**
     * Lista todos los seguros asociados a un bien.
     *
     * Devuelve siempre 200 OK, incluso cuando la lista esté vacía.
     */
    @GetMapping("/bien/{idBien}")
    public ResponseEntity<List<BienInmuebleSeguro>> listarPorBien(
            @PathVariable Long idBien
    ) {
        validarAcceso(PERMISO_VIEW);

        List<BienInmuebleSeguro> lista =
                service.listarPorBien(idBien);

        return ResponseEntity.ok(lista);
    }

    /**
     * Consulta un seguro por su identificador.
     */
    @GetMapping("/{idSeguro}")
    public ResponseEntity<BienInmuebleSeguro> buscarPorId(
            @PathVariable Long idSeguro
    ) {
        validarAcceso(PERMISO_VIEW);

        return service.buscarPorId(idSeguro)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    /**
     * Crea un nuevo seguro.
     */
    @PostMapping
    public ResponseEntity<BienInmuebleSeguro> crear(
            @RequestBody BienInmuebleSeguro dto
    ) {
        validarAcceso(PERMISO_EDIT);

        BienInmuebleSeguro guardado =
                service.crear(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardado);
    }

    /**
     * Actualiza un seguro existente.
     */
    @PutMapping("/{idSeguro}")
    public ResponseEntity<BienInmuebleSeguro> actualizar(
            @PathVariable Long idSeguro,
            @RequestBody BienInmuebleSeguro dto
    ) {
        validarAcceso(PERMISO_EDIT);

        return service.actualizar(idSeguro, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    /**
     * Elimina un seguro existente.
     */
    @DeleteMapping("/{idSeguro}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idSeguro
    ) {
        validarAcceso(PERMISO_EDIT);

        boolean eliminado =
                service.eliminar(idSeguro);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private void validarAcceso(String permiso) {
        Integer idUsuario = usuarioSesionService.idUsuario();

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(idUsuario);

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}