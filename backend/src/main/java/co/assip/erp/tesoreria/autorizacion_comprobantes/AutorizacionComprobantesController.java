
package co.assip.erp.tesoreria.autorizacion_comprobantes;

import co.assip.erp.tesoreria.autorizacion_comprobantes.dto.ComprobanteDecisionRequestDTO;
import co.assip.erp.tesoreria.autorizacion_comprobantes.dto.ComprobanteDecisionResponseDTO;
import co.assip.erp.tesoreria.autorizacion_comprobantes.dto.ComprobanteDetalleDTO;
import co.assip.erp.tesoreria.autorizacion_comprobantes.dto.ComprobanteResumenDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para la autorización de comprobantes de Tesorería.
 *
 * Funcionalidades:
 *
 * 1. Consultar comprobantes pendientes por agencia.
 * 2. Consultar el detalle completo de un comprobante.
 * 3. Autorizar comprobantes pendientes.
 * 4. Rechazar comprobantes pendientes.
 * 5. Anular comprobantes pendientes.
 *
 * Seguridad:
 *
 * - La identidad del usuario se obtiene de la sesión.
 * - El Service valida los permisos funcionales.
 * - El elaborador no puede autorizar ni rechazar
 *   su propio comprobante.
 * - El elaborador o un usuario autorizado pueden
 *   anular un comprobante pendiente.
 *
 * Contabilización:
 *
 * - No se genera consecutivo al elaborar.
 * - La autorización genera el consecutivo definitivo.
 * - La fecha contable corresponde al día de autorización.
 * - Los registros contables y físicos se realizan
 *   dentro de una única transacción.
 *
 * Context path de ASSIP ERP: /api/v1
 */
@RestController
@RequestMapping("/tesoreria/autorizacion-comprobantes")
@RequiredArgsConstructor
@Validated
public class AutorizacionComprobantesController {

    private final AutorizacionComprobantesService service;

    // =========================================================
    // LISTAR COMPROBANTES PENDIENTES
    // =========================================================

    /**
     * Lista los comprobantes pendientes de una agencia.
     *
     * GET
     * /api/v1/tesoreria/autorizacion-comprobantes/pendientes?idAgencia=2
     *
     * Requiere permiso de autorización.
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<ComprobanteResumenDTO>> listarPendientes(

            @RequestParam
            @NotNull(message = "Debe indicar la agencia.")
            @Positive(message = "La agencia debe ser válida.")
            Integer idAgencia

    ) {

        List<ComprobanteResumenDTO> resultado =
                service.listarPendientes(idAgencia);

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // CONSULTAR DETALLE
    // =========================================================

    /**
     * Consulta la cabecera, partidas contables,
     * movimientos de caja y auditoría.
     *
     * GET
     * /api/v1/tesoreria/autorizacion-comprobantes/125
     *
     * El Service valida el acceso del usuario.
     */
    @GetMapping("/{idComprobante}")
    public ResponseEntity<ComprobanteDetalleDTO> consultar(

            @PathVariable
            @NotNull
            @Positive(message = "El comprobante debe ser válido.")
            Long idComprobante

    ) {

        ComprobanteDetalleDTO resultado =
                service.consultar(idComprobante);

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // PROCESAR DECISIÓN
    // =========================================================

    /**
     * Procesa una decisión sobre un comprobante pendiente.
     *
     * POST
     * /api/v1/tesoreria/autorizacion-comprobantes/125/decision
     *
     * Ejemplo AUTORIZAR:
     *
     * {
     *   "decision": "AUTORIZAR",
     *   "motivo": null
     * }
     *
     * Ejemplo RECHAZAR:
     *
     * {
     *   "decision": "RECHAZAR",
     *   "motivo": "La cuenta contable es incorrecta."
     * }
     *
     * Ejemplo ANULAR:
     *
     * {
     *   "decision": "ANULAR",
     *   "motivo": "La operación ya no se requiere."
     * }
     *
     * La operación se procesa de manera transaccional
     * en AutorizacionComprobantesService.
     */
    @PostMapping("/{idComprobante}/decision")
    public ResponseEntity<ComprobanteDecisionResponseDTO> procesarDecision(

            @PathVariable
            @NotNull
            @Positive(message = "El comprobante debe ser válido.")
            Long idComprobante,

            @Valid
            @RequestBody
            @NotNull(message = "Debe enviar la decisión.")
            ComprobanteDecisionRequestDTO solicitud

    ) {

        ComprobanteDecisionResponseDTO resultado =
                service.procesarDecision(
                        idComprobante,
                        solicitud
                );

        return ResponseEntity.ok(resultado);
    }
}
