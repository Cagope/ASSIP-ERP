package co.assip.erp.cartera.originacion.solicitudes.list;

import co.assip.erp.cartera.originacion.solicitudes.list.dto.SolicitudListadoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/solicitudes/list")
public class SolicitudListadoController {

    private final SolicitudListadoService service;


    public SolicitudListadoController(
            SolicitudListadoService service
    ) {
        this.service = service;
    }


    // =========================================================
    // LISTAR SOLICITUDES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<SolicitudListadoDTO>> listar(
            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            String numeroSolicitud,

            @RequestParam(required = false)
            String documento,

            @RequestParam(required = false)
            String nombreSolicitante,

            @RequestParam(required = false)
            Integer idAsesor,

            @RequestParam(required = false)
            Integer idSolicitudProceso,

            @RequestParam(required = false)
            Integer idSolicitudResultado
    ) {

        List<SolicitudListadoDTO> solicitudes =
                service.listar(
                        idAgencia,
                        numeroSolicitud,
                        documento,
                        nombreSolicitante,
                        idAsesor,
                        idSolicitudProceso,
                        idSolicitudResultado
                );

        return ResponseEntity.ok(solicitudes);
    }
}