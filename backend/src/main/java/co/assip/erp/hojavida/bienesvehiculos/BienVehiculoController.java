package co.assip.erp.hojavida.bienesvehiculos;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🚗 Controlador REST — Bienes Vehículos del Asociado
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-vehiculos
 */
@RestController
@RequestMapping("/hoja-vida/bienes-vehiculos")
public class BienVehiculoController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_BIENES_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_BIENES_EDIT";

    private final BienVehiculoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public BienVehiculoController(
            BienVehiculoService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    /** 🔹 Listar bienes vehículos por asociado */
    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<List<BienVehiculo>> listarPorPersona(
            @PathVariable Long idDatosPersonal
    ) {
        validarAcceso(PERMISO_VIEW);

        List<BienVehiculo> lista =
                service.listarPorPersona(idDatosPersonal);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    /** 🔹 Buscar un bien vehículo por ID del bien */
    @GetMapping("/{idBien}")
    public ResponseEntity<BienVehiculo> buscarPorIdBien(
            @PathVariable Long idBien
    ) {
        validarAcceso(PERMISO_VIEW);

        return service.buscarPorIdBien(idBien)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Registrar nuevo bien vehículo */
    @PostMapping
    public ResponseEntity<BienVehiculo> registrar(
            @RequestBody BienVehiculo dto
    ) {
        validarAcceso(PERMISO_EDIT);

        BienVehiculo guardado =
                service.registrarBienVehiculo(dto);

        return ResponseEntity.ok(guardado);
    }

    /** 🔹 Actualizar bien vehículo existente */
    @PutMapping("/{idBien}")
    public ResponseEntity<BienVehiculo> actualizar(
            @PathVariable Long idBien,
            @RequestBody BienVehiculo dto
    ) {
        validarAcceso(PERMISO_EDIT);

        return service.actualizarBienVehiculo(idBien, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Eliminar bien vehículo */
    @DeleteMapping("/{idBien}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idBien
    ) {
        validarAcceso(PERMISO_EDIT);

        if (service.eliminarBienVehiculo(idBien)) {
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