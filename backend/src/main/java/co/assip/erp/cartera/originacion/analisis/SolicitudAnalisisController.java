package co.assip.erp.cartera.originacion.analisis;

import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisComponenteDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisDeudorDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisPersistenciaDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisResultadoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/analisis")
public class SolicitudAnalisisController {

    private final SolicitudAnalisisService service;

    public SolicitudAnalisisController(
            SolicitudAnalisisService service
    ) {
        this.service = service;
    }


    // =========================================================
    // RESULTADO ACTUAL DE LA SOLICITUD
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/resultado")
    public ResponseEntity<SolicitudAnalisisResultadoDTO> obtenerResultadoSolicitud(
            @PathVariable Integer idSolicitudCredito
    ) {

        return service.obtenerResultadoSolicitud(
                        idSolicitudCredito
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
    // RESULTADOS POR DEUDOR
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/deudores")
    public ResponseEntity<List<SolicitudAnalisisDeudorDTO>> listarDeudores(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                service.listarDeudores(
                        idSolicitudCredito
                )
        );
    }


    // =========================================================
    // COMPONENTES
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/componentes")
    public ResponseEntity<List<SolicitudAnalisisComponenteDTO>> listarComponentes(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                service.listarComponentes(
                        idSolicitudCredito
                )
        );
    }


    // =========================================================
    // GUARDAR ANÁLISIS ACTUAL
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/persistir")
    public ResponseEntity<SolicitudAnalisisPersistenciaDTO> persistirAnalisis(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                service.persistirAnalisis(
                        idSolicitudCredito
                )
        );
    }
}