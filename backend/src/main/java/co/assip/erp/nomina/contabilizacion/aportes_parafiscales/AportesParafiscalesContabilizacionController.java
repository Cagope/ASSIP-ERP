package co.assip.erp.nomina.contabilizacion.aportes_parafiscales;

import co.assip.erp.nomina.contabilizacion.aportes_parafiscales.dto.AportesParafiscalesContabilizacionResultadoDTO;
import co.assip.erp.nomina.contabilizacion.liquidacion.dto.LiquidacionMovimientoContableDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/contabilizacion/aportes-parafiscales")
@RequiredArgsConstructor
public class AportesParafiscalesContabilizacionController {

    private final AportesParafiscalesContabilizacionService service;

    @GetMapping("/preview/{idPeriodoNomina}")
    public List<LiquidacionMovimientoContableDTO> preview(@PathVariable Integer idPeriodoNomina) {
        return service.preview(idPeriodoNomina);
    }

    @GetMapping("/comprobante/{idPeriodoNomina}")
    public String obtenerComprobante(@PathVariable Integer idPeriodoNomina) {
        return service.obtenerComprobanteExistente(idPeriodoNomina);
    }

    @PostMapping("/ejecutar/{idPeriodoNomina}")
    public AportesParafiscalesContabilizacionResultadoDTO ejecutar(@PathVariable Integer idPeriodoNomina) {
        return service.contabilizar(idPeriodoNomina);
    }

    @PostMapping("/reversar/{idPeriodoNomina}")
    public void reversar(@PathVariable Integer idPeriodoNomina) {
        service.reversarContabilizacion(idPeriodoNomina);
    }
}