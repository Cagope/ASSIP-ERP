package co.assip.erp.cartera.originacion.bienes;

import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienCreditoRespaldadoDTO;
import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienDTO;
import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienSeleccionRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cartera/originacion/bienes")
public class SolicitudBienController {

    private final SolicitudBienService service;

    public SolicitudBienController(
            SolicitudBienService service
    ) {
        this.service = service;
    }


    // =========================================================
    // LISTAR BIENES DE LA SOLICITUD
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}")
    public ResponseEntity<List<SolicitudBienDTO>> listarPorSolicitud(
            @PathVariable Integer idSolicitudCredito
    ) {

        return ResponseEntity.ok(
                service.listarPorSolicitud(
                        idSolicitudCredito
                )
        );
    }


    // =========================================================
    // LISTAR BIENES DE UN DEUDOR
    // =========================================================

    @GetMapping("/deudor/{idSolicitudDeudor}")
    public ResponseEntity<List<SolicitudBienDTO>> listarPorDeudor(
            @PathVariable Integer idSolicitudDeudor
    ) {

        return ResponseEntity.ok(
                service.listarPorDeudor(
                        idSolicitudDeudor
                )
        );
    }


    // =========================================================
    // CRÉDITOS ACTUALES RESPALDADOS POR UN BIEN
    // =========================================================

    @GetMapping("/{idBien}/creditos-respaldados")
    public ResponseEntity<List<SolicitudBienCreditoRespaldadoDTO>>
    listarCreditosRespaldados(
            @PathVariable Long idBien
    ) {

        return ResponseEntity.ok(
                service.listarCreditosRespaldados(
                        idBien
                )
        );
    }


    // =========================================================
    // SELECCIONAR / RETIRAR BIEN
    // =========================================================

    @PutMapping("/seleccion")
    public ResponseEntity<SolicitudBienDTO> seleccionar(
            @RequestBody SolicitudBienSeleccionRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.seleccionar(
                        request
                )
        );
    }


    // =========================================================
    // VALIDAR PASO BIENES
    // =========================================================
    //
    // Para garantía REAL exige al menos un bien seleccionado
    // entre los deudores activos.
    //
    // Para garantía PERSONAL permite continuar sin bienes.
    //
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}/validar")
    public ResponseEntity<Map<String, Boolean>> validarParaContinuar(
            @PathVariable Integer idSolicitudCredito
    ) {

        service.validarParaContinuar(
                idSolicitudCredito
        );

        return ResponseEntity.ok(
                Map.of(
                        "puedeContinuar",
                        true
                )
        );
    }
}