package co.assip.erp.cartera.originacion.solicitudes;

import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCrearRetomarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCrearRetomarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoCrearRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoDetalleDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoResumenDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/solicitudes")
public class SolicitudCreditoController {

    private final SolicitudCreditoService service;

    public SolicitudCreditoController(
            SolicitudCreditoService service
    ) {
        this.service = service;
    }


    // =========================================================
    // CREAR SOLICITUD + GUARDAR DATOS DEL CRÉDITO
    // =========================================================

    @PostMapping("/crear")
    public ResponseEntity<SolicitudCreditoGuardarResponseDTO> crear(
            @RequestBody SolicitudCreditoCrearRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.crearSolicitud(request)
        );
    }


    // =========================================================
    // CREAR / RETOMAR SOLICITUD
    // =========================================================

    @PostMapping("/crear-retomar")
    public ResponseEntity<SolicitudCrearRetomarResponseDTO> crearRetomar(
            @RequestBody SolicitudCrearRetomarRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.crearRetomar(request)
        );
    }


    // =========================================================
    // GUARDAR DATOS DEL CRÉDITO
    // =========================================================

    @PostMapping("/guardar-credito")
    public ResponseEntity<SolicitudCreditoGuardarResponseDTO> guardarCredito(
            @RequestBody SolicitudCreditoGuardarRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.guardarCredito(request)
        );
    }


    // =========================================================
    // LISTAR SOLICITUDES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<SolicitudCreditoResumenDTO>> listar() {

        return ResponseEntity.ok(
                service.listar()
        );
    }


    // =========================================================
    // CONSULTAR SOLICITUD POR ID
    // =========================================================

    @GetMapping("/{idSolicitudCredito}")
    public ResponseEntity<SolicitudCreditoDetalleDTO> buscarPorId(
            @PathVariable Integer idSolicitudCredito
    ) {

        return service.buscarPorId(idSolicitudCredito)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }
}