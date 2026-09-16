package co.assip.erp.cartera.originacion.financiero;

import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroDTO;
import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroDetalleDTO;
import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroGuardarRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/financiero")
public class SolicitudFinancieroController {

    private final SolicitudFinancieroService service;

    public SolicitudFinancieroController(
            SolicitudFinancieroService service
    ) {
        this.service = service;
    }


    // =========================================================
    // LISTAR FINANCIEROS DE LA SOLICITUD
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}")
    public ResponseEntity<List<SolicitudFinancieroDTO>> listarPorSolicitud(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                service.listarPorSolicitud(
                        idSolicitudCredito
                )
        );
    }


    // =========================================================
    // CONSULTAR FINANCIERO DE UN DEUDOR
    // =========================================================

    @GetMapping("/deudor/{idSolicitudDeudor}")
    public ResponseEntity<SolicitudFinancieroDetalleDTO> buscarPorDeudor(
            @PathVariable Integer idSolicitudDeudor
    ) {

        return service.buscarPorDeudor(
                        idSolicitudDeudor
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
    // GUARDAR / REFRESCAR FOTOGRAFÍA FINANCIERA
    // =========================================================

    @PostMapping("/guardar")
    public ResponseEntity<SolicitudFinancieroDetalleDTO> guardar(
            @RequestBody SolicitudFinancieroGuardarRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.guardar(
                        request
                )
        );
    }
}