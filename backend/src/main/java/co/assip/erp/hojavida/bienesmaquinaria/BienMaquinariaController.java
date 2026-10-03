package co.assip.erp.hojavida.bienesmaquinaria;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ⚙️ Controlador REST — Bienes Maquinaria del Asociado
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-maquinaria
 */
@RestController
@RequestMapping("/hoja-vida/bienes-maquinaria")
public class BienMaquinariaController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_BIENES_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_BIENES_EDIT";

    private final BienMaquinariaService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public BienMaquinariaController(
            BienMaquinariaService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<List<BienMaquinaria>> listarPorPersona(
            @PathVariable Long idDatosPersonal
    ) {
        validarAcceso(PERMISO_VIEW);

        List<BienMaquinaria> lista =
                service.listarPorPersona(idDatosPersonal);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idBien}")
    public ResponseEntity<BienMaquinaria> buscarPorIdBien(
            @PathVariable Long idBien
    ) {
        validarAcceso(PERMISO_VIEW);

        return service.buscarPorIdBien(idBien)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BienMaquinaria> registrar(
            @RequestBody BienMaquinaria dto
    ) {
        validarAcceso(PERMISO_EDIT);

        BienMaquinaria guardado =
                service.registrarBienMaquinaria(dto);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{idBien}")
    public ResponseEntity<BienMaquinaria> actualizar(
            @PathVariable Long idBien,
            @RequestBody BienMaquinaria dto
    ) {
        validarAcceso(PERMISO_EDIT);

        return service.actualizarBienMaquinaria(idBien, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{idBien}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idBien
    ) {
        validarAcceso(PERMISO_EDIT);

        if (service.eliminarBienMaquinaria(idBien)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
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