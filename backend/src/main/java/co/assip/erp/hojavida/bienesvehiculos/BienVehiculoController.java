package co.assip.erp.hojavida.bienesvehiculos;

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

    private final BienVehiculoService service;

    public BienVehiculoController(BienVehiculoService service) {
        this.service = service;
    }

    /** 🔹 Listar bienes vehículos por asociado */
    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<List<BienVehiculo>> listarPorPersona(
            @PathVariable Long idDatosPersonal
    ) {
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
        return service.buscarPorIdBien(idBien)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Registrar nuevo bien vehículo */
    @PostMapping
    public ResponseEntity<BienVehiculo> registrar(
            @RequestBody BienVehiculo dto
    ) {
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
        return service.actualizarBienVehiculo(idBien, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Eliminar bien vehículo */
    @DeleteMapping("/{idBien}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idBien
    ) {
        if (service.eliminarBienVehiculo(idBien)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}