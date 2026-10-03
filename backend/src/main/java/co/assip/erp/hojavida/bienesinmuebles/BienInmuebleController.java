package co.assip.erp.hojavida.bienesinmuebles;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * Bienes Inmuebles
 * ============================================================
 */
@RestController
@RequestMapping("/hoja-vida/bienes-inmuebles")
public class BienInmuebleController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_BIENES_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_BIENES_EDIT";

    private final BienInmuebleService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public BienInmuebleController(
            BienInmuebleService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<List<BienInmueble>> listarPorPersona(
            @PathVariable Long idDatosPersonal
    ) {

        validarAcceso(PERMISO_VIEW);

        List<BienInmueble> lista =
                service.listarPorPersona(idDatosPersonal);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idBien}")
    public ResponseEntity<BienInmueble> buscarPorIdBien(
            @PathVariable Long idBien
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.buscarPorIdBien(idBien)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // PROCESOS
    // ============================================================

    @PostMapping
    public ResponseEntity<BienInmueble> registrar(
            @RequestBody BienInmueble dto
    ) {

        validarAcceso(PERMISO_EDIT);

        BienInmueble respuesta =
                service.registrarBienInmueble(dto);

        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idBien}")
    public ResponseEntity<BienInmueble> actualizar(
            @PathVariable Long idBien,
            @RequestBody BienInmueble dto
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.actualizarBienInmueble(idBien, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{idBien}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idBien
    ) {

        validarAcceso(PERMISO_EDIT);

        boolean eliminado =
                service.eliminarBienInmueble(idBien);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // SEGURIDAD
    // ============================================================

    private void validarAcceso(String permiso) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(idUsuario);

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}