package co.assip.erp.cartera.originacion.aprobacion;

import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionActuacionDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionBandejaDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionDecisionDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionDecisionRequestDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionFotosDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cartera/originacion/aprobacion")
public class SolicitudAprobacionController {

    private final SolicitudAprobacionService service;

    public SolicitudAprobacionController(
            SolicitudAprobacionService service
    ) {
        this.service = service;
    }


    // =========================================================
    // BANDEJA
    // =========================================================

    @GetMapping("/bandeja")
    public ResponseEntity<List<SolicitudAprobacionBandejaDTO>>
    listarBandeja() {

        return ResponseEntity.ok(
                service.listarBandeja()
        );
    }


    // =========================================================
    // CATÁLOGO DE DECISIONES
    // =========================================================

    @GetMapping("/decisiones")
    public ResponseEntity<List<SolicitudAprobacionDecisionDTO>>
    listarDecisiones() {

        return ResponseEntity.ok(
                service.listarDecisiones()
        );
    }


    // =========================================================
    // HISTORIAL DE APROBACIÓN
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/historial")
    public ResponseEntity<List<SolicitudAprobacionActuacionDTO>>
    listarHistorial(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                service.listarActuaciones(
                        idSolicitudCredito
                )
        );
    }


    // =========================================================
    // FOTOGRAFÍAS PARA APROBACIÓN
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/fotos")
    public ResponseEntity<SolicitudAprobacionFotosDTO>
    obtenerFotos(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                service.obtenerFotos(
                        idSolicitudCredito
                )
        );
    }


    // =========================================================
    // FOTOGRAFÍAS HISTÓRICAS DE UNA ACTUACIÓN
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/actuaciones/{idSolicitudAprobacion}/fotos")
    public ResponseEntity<SolicitudAprobacionFotosDTO>
    obtenerFotosActuacion(
            @PathVariable Integer idSolicitudCredito,
            @PathVariable Integer idSolicitudAprobacion
    ) {

        return ResponseEntity.ok(
                service.obtenerFotosActuacion(
                        idSolicitudCredito,
                        idSolicitudAprobacion
                )
        );
    }


    // =========================================================
    // REGISTRAR DECISIÓN DEL ENTE APROBADOR
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/decision")
    public ResponseEntity<Integer> registrarDecision(
            @PathVariable Integer idSolicitudCredito,
            @RequestBody SolicitudAprobacionDecisionRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.registrarDecision(
                        idSolicitudCredito,
                        request
                )
        );
    }


    // =========================================================
    // GESTIONAR CONCEPTO VIGENTE - ASESOR
    // RETOMAR / FORMALIZAR / CERRAR
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/gestionar-concepto")
    public ResponseEntity<Void> gestionarConcepto(
            @PathVariable Integer idSolicitudCredito,
            @RequestBody Map<String, String> request
    ) {

        service.gestionarConcepto(
                idSolicitudCredito,
                request.get("accion")
        );

        return ResponseEntity.noContent().build();
    }
}