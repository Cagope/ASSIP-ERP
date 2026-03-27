package co.assip.erp.nomina.contabilizacion.liquidacion;

import co.assip.erp.nomina.contabilizacion.liquidacion.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/contabilizacion/liquidacion")
@RequiredArgsConstructor
public class LiquidacionContabilizacionController {

    private final LiquidacionContabilizacionService service;

    @GetMapping("/preview/{idPeriodoNomina}")
    public List<LiquidacionMovimientoContableDTO> preview(@PathVariable Integer idPeriodoNomina) {
        return service.preview(idPeriodoNomina);
    }

    @GetMapping("/comprobante/{idPeriodoNomina}")
    public String obtenerComprobante(@PathVariable Integer idPeriodoNomina) {
        return service.obtenerComprobanteExistente(idPeriodoNomina);
    }

    @PostMapping("/ejecutar/{idPeriodoNomina}")
    public LiquidacionContabilizacionResultadoDTO ejecutar(@PathVariable Integer idPeriodoNomina) {
        return service.contabilizar(idPeriodoNomina);
    }

    @PostMapping("/reversar/{idPeriodoNomina}")
    public void reversar(@PathVariable Integer idPeriodoNomina) {
        service.reversarContabilizacion(idPeriodoNomina);
    }

}