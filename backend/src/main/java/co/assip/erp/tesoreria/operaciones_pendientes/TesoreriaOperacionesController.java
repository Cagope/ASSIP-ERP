
package co.assip.erp.tesoreria.operaciones_pendientes;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import co.assip.erp.tesoreria.operaciones_pendientes.dto.OperacionPagoRequestDTO;
import co.assip.erp.tesoreria.operaciones_pendientes.dto.OperacionPagoResponseDTO;
import co.assip.erp.tesoreria.operaciones_pendientes.dto.OperacionComprobantePreviewDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tesoreria/operaciones-pendientes")
@RequiredArgsConstructor
public class TesoreriaOperacionesController {

    private static final String PERMISO_VIEW =
            "TESORERIA_MOVIMIENTOS_FONDOS_VIEW";

    private static final String PERMISO_EXECUTE =
            "TESORERIA_MOVIMIENTOS_FONDOS_EXECUTE";

    private final TesoreriaOperacionesPendientesService
            operacionesPendientesService;

    private final TesoreriaOperacionesEjecucionesService
            operacionesEjecucionesService;

    private final UsuarioSesionService usuarioSesionService;

    private final AccessValidator accessValidator;

    // =========================================================
    // OBLIGACIONES PENDIENTES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>>
    listarPendientes(
            @RequestParam Integer idAgencia
    ) {
        validarAcceso(PERMISO_VIEW);

        usuarioSesionService.validarAgencia(idAgencia);

        return ResponseEntity.ok(
                operacionesPendientesService.listarDisponibles(
                        idAgencia
                )
        );
    }

    // =========================================================
    // CUENTAS BANCARIAS PARA PAGOS
    // =========================================================

    /** Lista cuentas bancarias para recaudos, sin exigir habilitación para egresos. */
    @GetMapping("/cuentas-para-recaudo")
    public ResponseEntity<List<Map<String, Object>>> listarCuentasParaRecaudo(
            @RequestParam Integer idAgencia) {
        validarAcceso(PERMISO_VIEW);
        usuarioSesionService.validarAgencia(idAgencia);
        return ResponseEntity.ok(operacionesPendientesService.listarCuentasParaRecaudo(idAgencia));
    }

    @GetMapping("/cuentas-para-pago")
    public ResponseEntity<List<Map<String, Object>>>
    listarCuentasParaPago(
            @RequestParam Integer idAgencia
    ) {
        validarAcceso(PERMISO_VIEW);

        usuarioSesionService.validarAgencia(idAgencia);

        return ResponseEntity.ok(
                operacionesPendientesService.listarCuentasParaPago(
                        idAgencia
                )
        );
    }

    // =========================================================
    // EJECUCIONES DE UNA OBLIGACIÓN
    // =========================================================

    @GetMapping("/{idOperacion}/ejecuciones")
    public ResponseEntity<List<Map<String, Object>>>
    listarEjecuciones(
            @PathVariable Long idOperacion
    ) {
        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                operacionesEjecucionesService.listarEjecuciones(
                        idOperacion
                )
        );
    }

    // =========================================================
    // SALDO DISPONIBLE
    // =========================================================

    @GetMapping("/{idOperacion}/saldo")
    public ResponseEntity<Map<String, Object>>
    consultarSaldo(
            @PathVariable Long idOperacion
    ) {
        validarAcceso(PERMISO_VIEW);

        BigDecimal saldoDisponible =
                operacionesEjecucionesService
                        .consultarSaldoDisponible(idOperacion);

        return ResponseEntity.ok(
                Map.of(
                        "idOperacionPendiente", idOperacion,
                        "saldoDisponible", saldoDisponible
                )
        );
    }

    // =========================================================
    // VISTA PREVIA DEL COMPROBANTE
    // NO PERSISTE
    // =========================================================

    @PostMapping("/preview")
    public ResponseEntity<OperacionComprobantePreviewDTO>
    generarVistaPrevia(
            @RequestBody OperacionPagoRequestDTO request
    ) {
        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                operacionesEjecucionesService.generarVistaPrevia(
                        request
                )
        );
    }

    // =========================================================
    // CONFIRMACIÓN DEFINITIVA DEL PAGO
    // GENERA COMPROBANTE Y ACTUALIZA SALDOS
    // =========================================================

    @PostMapping("/confirmar-pago")
    public ResponseEntity<OperacionPagoResponseDTO>
    confirmarPago(
            @RequestBody OperacionPagoRequestDTO request
    ) {
        validarAcceso(PERMISO_EXECUTE);

        return ResponseEntity.ok(
                operacionesEjecucionesService.confirmarPago(
                        request
                )
        );
    }

    /** Previsualiza el comprobante de entrada bancaria sin registrar movimientos. */
    @PostMapping("/preview-recaudo")
    public ResponseEntity<OperacionComprobantePreviewDTO> generarVistaPreviaRecaudo(
            @RequestBody OperacionPagoRequestDTO request
    ) {
        validarAcceso(PERMISO_VIEW);
        return ResponseEntity.ok(
                operacionesEjecucionesService.generarVistaPreviaRecaudo(request)
        );
    }

    /** Confirma el recaudo bancario de Cartera y registra su ejecución en Tesorería. */
    @PostMapping("/confirmar-recaudo")
    public ResponseEntity<OperacionPagoResponseDTO> confirmarRecaudo(
            @RequestBody OperacionPagoRequestDTO request
    ) {
        validarAcceso(PERMISO_EXECUTE);
        return ResponseEntity.ok(
                operacionesEjecucionesService.confirmarRecaudo(request)
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}
