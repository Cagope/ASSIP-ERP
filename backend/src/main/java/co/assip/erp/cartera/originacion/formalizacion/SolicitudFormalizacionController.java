package co.assip.erp.cartera.originacion.formalizacion;

import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionBandejaDTO;
import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionDetalleDTO;
import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionGuardarRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/formalizacion")
public class SolicitudFormalizacionController {

    private final SolicitudFormalizacionService service;

    private final SolicitudFormalizacionValidacionService validacionService;

    public SolicitudFormalizacionController(
            SolicitudFormalizacionService service,
            SolicitudFormalizacionValidacionService validacionService
    ) {
        this.service = service;
        this.validacionService = validacionService;
    }

    // =========================================================
    // BANDEJA DE FORMALIZACIÓN
    // =========================================================

    @GetMapping("/bandeja")
    public ResponseEntity<List<SolicitudFormalizacionBandejaDTO>> listarBandeja() {

        List<SolicitudFormalizacionBandejaDTO> resultado =
                service.listarBandeja();

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // CONSULTAR SOLICITUD PARA FORMALIZACIÓN
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
    // SIMULAR CONDICIONES FINANCIERAS
    //
    // Calcula TEA y cuota con las condiciones digitadas.
    //
    // No guarda condiciones.
    // No actualiza validaciones de mora ni aportes.
    // No genera pagaré.
    // No cambia de proceso.
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/simular")
    public ResponseEntity<
            SolicitudFormalizacionService.ResultadoSimulacionFinanciera
            > simular(
            @PathVariable Integer idSolicitudCredito,
            @Valid
            @RequestBody SolicitudFormalizacionGuardarRequestDTO request
    ) {

        SolicitudFormalizacionService.ResultadoSimulacionFinanciera resultado =
                service.simular(
                        idSolicitudCredito,
                        request
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // VALIDAR CONDICIONES DE FORMALIZACIÓN
    //
    // Estado de solicitud, personas vinculadas, mora,
    // aportes, reciprocidad, simultaneidad y tasa máxima.
    //
    // No guarda las condiciones financieras.
    // No genera pagaré.
    // No cambia de proceso.
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/validar")
    public ResponseEntity<
            SolicitudFormalizacionValidacionService.ResultadoValidacionFormalizacion
            > validar(
            @PathVariable Integer idSolicitudCredito,
            @Valid
            @RequestBody SolicitudFormalizacionGuardarRequestDTO request
    ) {

        SolicitudFormalizacionValidacionService.ResultadoValidacionFormalizacion resultado =
                validacionService.validar(
                        idSolicitudCredito,
                        request
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // GUARDAR CONDICIONES DEFINITIVAS
    //
    // Calcula TEA y cuota.
    // Guarda las condiciones y el concepto de formalización.
    //
    // No genera pagaré.
    // No constituye crédito.
    // No cambia de proceso.
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
    // CONSULTAR PROPUESTA DEL PAGARÉ
    //
    // Presenta consecutivo provisional del parámetro 605.
    // Sugiere fecha actual y muestra datos de la solicitud.
    //
    // No reserva consecutivo.
    // No constituye crédito.
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/propuesta-pagare")
    public ResponseEntity<
            SolicitudFormalizacionService.PropuestaPagare
            > consultarPropuestaPagare(
            @PathVariable Integer idSolicitudCredito
    ) {

        SolicitudFormalizacionService.PropuestaPagare resultado =
                service.consultarPropuestaPagare(idSolicitudCredito);

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // GENERAR PAGARÉ
    //
    // Revalida condiciones definitivas.
    // Obtiene consecutivo definitivo 605 por agencia.
    // Registra la fecha elegida en fecha_inclusion_sistema.
    // Constituye crédito pendiente de desembolso.
    //
    // No desembolsa ni cambia de proceso.
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/generar-pagare")
    public ResponseEntity<SolicitudFormalizacionDetalleDTO> generarPagare(
            @PathVariable Integer idSolicitudCredito,
            @Valid @RequestBody GenerarPagareRequest request
    ) {

        SolicitudFormalizacionDetalleDTO resultado =
                service.generarPagare(
                        idSolicitudCredito,
                        request.fechaPagare()
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // FINALIZAR FORMALIZACIÓN
    //
    // Proceso 4 - FORMALIZACIÓN
    //         ↓
    // Proceso 5 - DESEMBOLSO
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/finalizar")
    public ResponseEntity<SolicitudFormalizacionDetalleDTO> finalizar(
            @PathVariable Integer idSolicitudCredito
    ) {

        SolicitudFormalizacionDetalleDTO resultado =
                service.finalizar(idSolicitudCredito);

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // REQUEST PARA GRABAR PAGARÉ
    // =========================================================

    public record GenerarPagareRequest(
            @NotNull LocalDate fechaPagare
    ) {
    }
}