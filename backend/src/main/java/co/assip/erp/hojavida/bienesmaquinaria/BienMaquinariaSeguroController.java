package co.assip.erp.hojavida.bienesmaquinaria;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🛡️ Controlador REST — Seguros de Bienes Maquinaria
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-maquinaria-seguros
 */
@RestController
@RequestMapping("/hoja-vida/bienes-maquinaria-seguros")
public class BienMaquinariaSeguroController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_BIENES_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_BIENES_EDIT";

    private final BienMaquinariaSeguroService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public BienMaquinariaSeguroController(
            BienMaquinariaSeguroService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    @GetMapping("/bien/{idBien}")
    public ResponseEntity<List<BienMaquinariaSeguro>> listarPorBien(
            @PathVariable Long idBien
    ) {
        validarAcceso(PERMISO_VIEW);

        List<BienMaquinariaSeguro> lista =
                service.listarPorBien(idBien);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idSeguro}")
    public ResponseEntity<BienMaquinariaSeguro> buscarPorId(
            @PathVariable Long idSeguro
    ) {
        validarAcceso(PERMISO_VIEW);

        return service.buscarPorId(idSeguro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BienMaquinariaSeguro> crear(
            @RequestBody BienMaquinariaSeguro dto
    ) {
        validarAcceso(PERMISO_EDIT);

        BienMaquinariaSeguro guardado =
                service.crear(dto);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{idSeguro}")
    public ResponseEntity<BienMaquinariaSeguro> actualizar(
            @PathVariable Long idSeguro,
            @RequestBody BienMaquinariaSeguro dto
    ) {
        validarAcceso(PERMISO_EDIT);

        return service.actualizar(idSeguro, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{idSeguro}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idSeguro
    ) {
        validarAcceso(PERMISO_EDIT);

        if (service.eliminar(idSeguro)) {
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