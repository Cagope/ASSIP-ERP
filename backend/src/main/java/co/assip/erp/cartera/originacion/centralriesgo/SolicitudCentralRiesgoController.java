package co.assip.erp.cartera.originacion.centralriesgo;

import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoDTO;
import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoDetalleDTO;
import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoGuardarRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/central-riesgo")
public class SolicitudCentralRiesgoController {

    private final SolicitudCentralRiesgoService service;

    public SolicitudCentralRiesgoController(
            SolicitudCentralRiesgoService service
    ) {
        this.service = service;
    }


    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}")
    public ResponseEntity<List<SolicitudCentralRiesgoDTO>> listarPorSolicitud(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                service.listarPorSolicitud(
                        idSolicitudCredito
                )
        );
    }


    // =========================================================
    // LISTAR POR DEUDOR
    // =========================================================

    @GetMapping("/deudor/{idSolicitudDeudor}")
    public ResponseEntity<List<SolicitudCentralRiesgoDTO>> listarPorDeudor(
            @PathVariable Integer idSolicitudDeudor
    ) {

        return ResponseEntity.ok(
                service.listarPorDeudor(
                        idSolicitudDeudor
                )
        );
    }


    // =========================================================
    // CONSULTAR DETALLE
    // =========================================================

    @GetMapping("/{idSolicitudDeudorCentral}")
    public ResponseEntity<SolicitudCentralRiesgoDetalleDTO> buscarPorId(
            @PathVariable Integer idSolicitudDeudorCentral
    ) {

        return service.buscarPorId(
                        idSolicitudDeudorCentral
                )
                .map(ResponseEntity::ok)
                .orElseGet(
                        () ->
                                ResponseEntity
                                        .notFound()
                                        .build()
                );
    }


    // =========================================================
    // GUARDAR / ACTUALIZAR
    // =========================================================

    @PostMapping("/guardar")
    public ResponseEntity<SolicitudCentralRiesgoDetalleDTO> guardar(
            @RequestBody SolicitudCentralRiesgoGuardarRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.guardar(
                        request
                )
        );
    }
}