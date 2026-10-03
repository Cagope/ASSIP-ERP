package co.assip.erp.hojavida.bienesvehiculos;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🛡️ Controlador REST — Seguros de Bienes Vehículos
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-vehiculos-seguros
 */
@RestController
@RequestMapping("/hoja-vida/bienes-vehiculos-seguros")
public class BienVehiculoSeguroController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_BIENES_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_BIENES_EDIT";

    private final BienVehiculoSeguroService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public BienVehiculoSeguroController(
            BienVehiculoSeguroService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    @GetMapping("/bien/{idBien}")
    public ResponseEntity<List<BienVehiculoSeguro>> listarPorBien(
            @PathVariable Long idBien
    ) {
        validarAcceso(PERMISO_VIEW);

        List<BienVehiculoSeguro> lista =
                service.listarPorBien(idBien);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idSeguro}")
    public ResponseEntity<BienVehiculoSeguro> buscarPorId(
            @PathVariable Long idSeguro
    ) {
        validarAcceso(PERMISO_VIEW);

        return service.buscarPorId(idSeguro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BienVehiculoSeguro> crear(
            @RequestBody BienVehiculoSeguro dto
    ) {
        validarAcceso(PERMISO_EDIT);

        BienVehiculoSeguro guardado =
                service.crear(dto);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{idSeguro}")
    public ResponseEntity<BienVehiculoSeguro> actualizar(
            @PathVariable Long idSeguro,
            @RequestBody BienVehiculoSeguro dto
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