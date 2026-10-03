package co.assip.erp.hojavida.bienesinmueblesavaluos;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de avalúos
 * asociados a bienes inmuebles.
 *
 * Endpoint base:
 * /api/v1/hoja-vida/bienes-inmuebles-avaluos
 */
@RestController
@RequestMapping("/hoja-vida/bienes-inmuebles-avaluos")
public class BienInmuebleAvaluoController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_BIENES_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_BIENES_EDIT";

    private final BienInmuebleAvaluoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public BienInmuebleAvaluoController(
            BienInmuebleAvaluoService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    /**
     * Lista todos los avalúos asociados a un bien.
     *
     * Devuelve siempre 200 OK, incluso cuando la lista esté vacía.
     */
    @GetMapping("/bien/{idBien}")
    public ResponseEntity<List<BienInmuebleAvaluo>> listarPorBien(
            @PathVariable Long idBien
    ) {
        validarAcceso(PERMISO_VIEW);

        List<BienInmuebleAvaluo> lista =
                service.listarPorBien(idBien);

        return ResponseEntity.ok(lista);
    }

    /**
     * Consulta un avalúo por su identificador.
     */
    @GetMapping("/{idAvaluo}")
    public ResponseEntity<BienInmuebleAvaluo> buscarPorId(
            @PathVariable Long idAvaluo
    ) {
        validarAcceso(PERMISO_VIEW);

        return service.buscarPorId(idAvaluo)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    /**
     * Crea un nuevo avalúo.
     */
    @PostMapping
    public ResponseEntity<BienInmuebleAvaluo> crear(
            @RequestBody BienInmuebleAvaluo dto
    ) {
        validarAcceso(PERMISO_EDIT);

        BienInmuebleAvaluo guardado =
                service.crear(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardado);
    }

    /**
     * Actualiza un avalúo existente.
     */
    @PutMapping("/{idAvaluo}")
    public ResponseEntity<BienInmuebleAvaluo> actualizar(
            @PathVariable Long idAvaluo,
            @RequestBody BienInmuebleAvaluo dto
    ) {
        validarAcceso(PERMISO_EDIT);

        return service.actualizar(idAvaluo, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    /**
     * Elimina un avalúo existente.
     */
    @DeleteMapping("/{idAvaluo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idAvaluo
    ) {
        validarAcceso(PERMISO_EDIT);

        boolean eliminado =
                service.eliminar(idAvaluo);

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