package co.assip.erp.cartera.originacion.formalizacion;

import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionDetalleDTO;
import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionGuardarRequestDTO;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cartera/originacion/formalizacion")
public class SolicitudFormalizacionController {

    private final SolicitudFormalizacionService service;

    public SolicitudFormalizacionController(
            SolicitudFormalizacionService service
    ) {
        this.service = service;
    }

    // =========================================================
    // CONSULTAR SOLICITUD PARA FORMALIZACIÓN
    //
    // Angular selecciona una solicitud aprobada.
    //
    // Devuelve:
    // - Identificación del asociado.
    // - Condiciones solicitadas.
    // - Condiciones formalizadas, si ya fueron guardadas.
    // - Estado actual del proceso.
    // =========================================================

    @GetMapping("/{idSolicitudCredito}")
    public ResponseEntity<SolicitudFormalizacionDetalleDTO> consultar(
            @PathVariable Integer idSolicitudCredito
    ) {

        SolicitudFormalizacionDetalleDTO resultado =
                service.consultar(idSolicitudCredito);

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // GUARDAR CONDICIONES DEFINITIVAS
    //
    // El asesor puede:
    // - Conservar las condiciones originales.
    // - Modificar las condiciones permitidas.
    //
    // El Service calcula TEA y cuota definitiva.
    //
    // No cambia todavía al proceso DESEMBOLSO.
    // =========================================================

    @PutMapping("/{idSolicitudCredito}")
    public ResponseEntity<SolicitudFormalizacionDetalleDTO> guardar(
            @PathVariable Integer idSolicitudCredito,
            @Valid
            @RequestBody SolicitudFormalizacionGuardarRequestDTO request
    ) {

        SolicitudFormalizacionDetalleDTO resultado =
                service.guardar(
                        idSolicitudCredito,
                        request
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // FINALIZAR FORMALIZACIÓN
    //
    // Proceso 4 - FORMALIZACIÓN
    //         ↓
    // Proceso 5 - DESEMBOLSO
    //
    // El crédito aún no se constituye.
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/finalizar")
    public ResponseEntity<SolicitudFormalizacionDetalleDTO> finalizar(
            @PathVariable Integer idSolicitudCredito
    ) {

        SolicitudFormalizacionDetalleDTO resultado =
                service.finalizar(idSolicitudCredito);

        return ResponseEntity.ok(resultado);
    }
}