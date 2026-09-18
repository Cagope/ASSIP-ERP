package co.assip.erp.cartera.originacion.solicitudes;

import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCrearRetomarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCrearRetomarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoCrearRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoDetalleDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoResumenDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudFinalizarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudFinalizarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudEnviarAprobacionResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudEnviarAprobacionRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudEnteAprobadorPreviewRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudEnteAprobadorPreviewDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;

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
    // PREVISUALIZAR ENTE APROBADOR
    // =========================================================

    @PostMapping("/preview-ente-aprobador")
    public ResponseEntity<SolicitudEnteAprobadorPreviewDTO> previsualizarEnteAprobador(
            @RequestBody SolicitudEnteAprobadorPreviewRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.previsualizarEnteAprobador(request)
        );
    }

    // =========================================================
// CONSULTAR TASA PARA SIMULACIÓN
// =========================================================

    @GetMapping("/tasa-simulacion")
    public ResponseEntity<BigDecimal> consultarTasaSimulacion(
            @RequestParam Integer idLineaCredito,
            @RequestParam String codigoGarantiaCredito,
            @RequestParam Integer amortizacionCapital,
            @RequestParam Integer plazoSolicitado
    ) {

        return ResponseEntity.ok(
                service.consultarTasaColocacionSimulacion(
                        idLineaCredito,
                        codigoGarantiaCredito,
                        amortizacionCapital,
                        plazoSolicitado
                )
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

    // =========================================================
    // FINALIZAR SOLICITUD
    // =========================================================

    @PutMapping("/finalizar")
    public ResponseEntity<SolicitudFinalizarResponseDTO> finalizarSolicitud(
            @RequestBody SolicitudFinalizarRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.finalizarSolicitud(request)
        );
    }

    // =========================================================
    // ENVIAR A APROBACIÓN
    // =========================================================

    @PutMapping("/{idSolicitudCredito}/enviar-aprobacion")
    public ResponseEntity<SolicitudEnviarAprobacionResponseDTO> enviarAprobacion(
            @PathVariable Integer idSolicitudCredito,
            @RequestBody SolicitudEnviarAprobacionRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.enviarAprobacion(
                        idSolicitudCredito,
                        request
                )
        );
    }
}