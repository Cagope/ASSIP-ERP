
package co.assip.erp.contabilidad.cierres.mensualcontable;

import co.assip.erp.contabilidad.cierres.mensualcontable.dto.CierreMensualContableDetalleCuentaDTO;
import co.assip.erp.contabilidad.cierres.mensualcontable.dto.CierreMensualContableResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expone las consultas y la confirmación del cierre mensual contable.
 * La autorización, el alcance de agencia y las validaciones
 * financieras se realizan en el servicio.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/cierres/mensualcontable")
public class CierreMensualContableController {

    private final CierreMensualContableService service;

    /**
     * Consulta el estado del período, las validaciones,
     * los días pendientes y los totales de la consolidación.
     */
    @GetMapping("/resumen")
    public ResponseEntity<CierreMensualContableResumenDTO> consultarResumen(
            @RequestParam Integer idAgencia,
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        return ResponseEntity.ok(
                service.consultar(idAgencia, ano, mes)
        );
    }

    /**
     * Consulta los saldos anteriores, movimientos del mes
     * y saldos acumulados por cuenta contable.
     */
    @GetMapping("/detalle")
    public ResponseEntity<List<CierreMensualContableDetalleCuentaDTO>> consultarDetalle(
            @RequestParam Integer idAgencia,
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        return ResponseEntity.ok(
                service.consultarDetalle(idAgencia, ano, mes)
        );
    }

    /**
     * Confirma el cierre mensual contable.
     * El servicio valida el período, consolida los saldos,
     * registra el cierre y actualiza su estado en una transacción.
     */
    @PostMapping("/cerrar")
    public ResponseEntity<CierreMensualContableResumenDTO> cerrar(
            @RequestParam Integer idAgencia,
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        return ResponseEntity.ok(
                service.cerrar(idAgencia, ano, mes)
        );
    }
    /** Reabre un período cerrado con justificación obligatoria y auditoría. */
    @PostMapping("/reabrir")
    public ResponseEntity<CierreMensualContableResumenDTO> reabrir(
            @RequestParam Integer idAgencia,
            @RequestParam int ano,
            @RequestParam int mes,
            @RequestParam String justificacion
    ) {
        return ResponseEntity.ok(service.reabrir(idAgencia, ano, mes, justificacion));
    }
}
