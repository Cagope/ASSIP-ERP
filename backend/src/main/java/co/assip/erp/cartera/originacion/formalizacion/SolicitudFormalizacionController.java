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

    private final SolicitudFormalizacionValidacionService validacionService;

    public SolicitudFormalizacionController(
            SolicitudFormalizacionService service,
            SolicitudFormalizacionValidacionService validacionService
    ) {
        this.service = service;
        this.validacionService = validacionService;
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
    // VALIDAR CONDICIONES DE FORMALIZACIÓN
    //
    // Recibe las condiciones digitadas en Angular.
    //
    // Valida:
    // - Estado de la solicitud.
    // - Deudor principal y codeudores.
    // - Mora.
    // - Aportes y reciprocidad.
    // - Simultaneidad.
    // - Tasa efectiva máxima legal (parámetro 631).
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
    // El asesor puede:
    // - Conservar las condiciones originales.
    // - Modificar las condiciones permitidas.
    //
    // El Service calcula TEA y cuota definitiva.
    //
    // No genera pagaré.
    // No constituye crédito.
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
    // GENERAR PAGARÉ
    //
    // Revalida las condiciones definitivas guardadas.
    // Obtiene el consecutivo del parámetro 605 por agencia.
    // Verifica que el pagaré no exista en la agencia.
    // Constituye el crédito en estado P (pendiente).
    // Vincula el crédito con la solicitud.
    //
    // No desembolsa.
    // No cambia de proceso.
    //
    // La impresión y reimpresión serán operaciones separadas.
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/generar-pagare")
    public ResponseEntity<SolicitudFormalizacionDetalleDTO> generarPagare(
            @PathVariable Integer idSolicitudCredito
    ) {

        SolicitudFormalizacionDetalleDTO resultado =
                service.generarPagare(idSolicitudCredito);

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // FINALIZAR FORMALIZACIÓN
    //
    // Proceso 4 - FORMALIZACIÓN
    //         ↓
    // Proceso 5 - DESEMBOLSO
    //
    // Antes de finalizar deberá existir un pagaré generado
    // y un crédito vinculado en estado P (pendiente).
    //
    // El Service y el Repository verifican que exista
    // el crédito vinculado en estado P.
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