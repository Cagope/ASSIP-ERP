package co.assip.erp.cartera.originacion.deudores;

import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorAgregarRequestDTO;
import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorDTO;
import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorDetalleDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/deudores")
public class SolicitudDeudorController {

    private final SolicitudDeudorService service;

    public SolicitudDeudorController(
            SolicitudDeudorService service
    ) {
        this.service = service;
    }


    // =========================================================
    // LISTAR DEUDORES DE UNA SOLICITUD
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}")
    public ResponseEntity<List<SolicitudDeudorDTO>> listarPorSolicitud(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                service.listarPorSolicitud(
                        idSolicitudCredito
                )
        );
    }


    // =========================================================
    // CONSULTAR DEUDOR
    // =========================================================

    @GetMapping("/{idSolicitudDeudor}")
    public ResponseEntity<SolicitudDeudorDetalleDTO> buscarPorId(
            @PathVariable Integer idSolicitudDeudor
    ) {

        return service.buscarPorId(
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
    // AGREGAR CODEUDOR
    // =========================================================

    @PostMapping("/agregar")
    public ResponseEntity<SolicitudDeudorDetalleDTO> agregarCodeudor(
            @RequestBody SolicitudDeudorAgregarRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.agregarCodeudor(
                        request
                )
        );
    }


    // =========================================================
    // RETIRAR CODEUDOR
    // =========================================================

    @DeleteMapping("/{idSolicitudDeudor}")
    public ResponseEntity<Void> retirarCodeudor(
            @PathVariable Integer idSolicitudDeudor
    ) {

        service.retirarCodeudor(
                idSolicitudDeudor
        );

        return ResponseEntity.noContent().build();
    }
}