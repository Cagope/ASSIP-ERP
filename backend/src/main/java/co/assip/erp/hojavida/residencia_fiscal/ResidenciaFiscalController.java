package co.assip.erp.hojavida.residencia_fiscal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🌐 Controlador REST — Residencia Fiscal
 * ------------------------------------------------------------
 * Esquema: hoja_vida
 *
 * Rutas:
 *  - GET    /api/v1/hoja-vida/residencia-fiscal
 *  - GET    /api/v1/hoja-vida/residencia-fiscal/{id}
 *  - GET    /api/v1/hoja-vida/residencia-fiscal/persona/{idDatosPersonal}
 *  - POST   /api/v1/hoja-vida/residencia-fiscal
 *  - PUT    /api/v1/hoja-vida/residencia-fiscal/{id}
 *  - DELETE /api/v1/hoja-vida/residencia-fiscal/{id}
 *
 * Cada persona puede registrar una única información
 * de residencia fiscal (FATCA / CRS).
 */
@RestController
@RequestMapping("/hoja-vida/residencia-fiscal")
public class ResidenciaFiscalController {

    private final ResidenciaFiscalService service;

    public ResidenciaFiscalController(
            ResidenciaFiscalService service
    ) {
        this.service = service;
    }

    // ==========================================================
    // 🔹 LISTAR TODOS
    // ==========================================================

    @GetMapping
    public ResponseEntity<List<ResidenciaFiscal>> listar() {

        return ResponseEntity.ok(
                service.listar()
        );
    }

    // ==========================================================
    // 🔹 OBTENER POR ID
    // ==========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ResidenciaFiscal> obtener(
            @PathVariable Long id
    ) {

        return service
                .buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 OBTENER POR PERSONA
    // ==========================================================

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<ResidenciaFiscal> obtenerPorPersona(
            @PathVariable Integer idDatosPersonal
    ) {

        return service
                .buscarPorPersona(idDatosPersonal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 CREAR
    // ==========================================================

    @PostMapping
    public ResponseEntity<ResidenciaFiscal> crear(
            @RequestBody ResidenciaFiscal residenciaFiscal
    ) {

        return ResponseEntity.ok(
                service.crear(residenciaFiscal)
        );
    }

    // ==========================================================
    // 🔹 ACTUALIZAR
    // ==========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ResidenciaFiscal> actualizar(
            @PathVariable Long id,
            @RequestBody ResidenciaFiscal residenciaFiscal
    ) {

        return service
                .actualizar(id, residenciaFiscal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 ELIMINAR
    // ==========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        if (!service.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

}