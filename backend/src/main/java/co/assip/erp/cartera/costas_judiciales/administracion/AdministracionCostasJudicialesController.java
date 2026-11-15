
package co.assip.erp.cartera.costas_judiciales.administracion;

import co.assip.erp.cartera.costas_judiciales.administracion.dto.CostasJudicialesRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Expone la previsualización y confirmación de costas judiciales.
 * Las validaciones financieras se realizan en el servicio.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/costas-judiciales")
public class AdministracionCostasJudicialesController {

    private final AdministracionCostasJudicialesService service;

    /** Consulta el comprobante sin registrar movimientos. */
    @PostMapping("/comprobante/preview")
    public ResponseEntity<AdministracionCostasJudicialesService.VistaPrevia> previsualizar(
            @RequestBody CostasJudicialesRequestDTO solicitud) {
        return ResponseEntity.ok(service.previsualizar(solicitud));
    }

    /** Confirma el cargo y genera el comprobante contable. */
    @PostMapping("/confirmar")
    public ResponseEntity<AdministracionCostasJudicialesService.Resultado> confirmar(
            @RequestBody CostasJudicialesRequestDTO solicitud) {
        return ResponseEntity.ok(service.confirmar(solicitud));
    }
}
