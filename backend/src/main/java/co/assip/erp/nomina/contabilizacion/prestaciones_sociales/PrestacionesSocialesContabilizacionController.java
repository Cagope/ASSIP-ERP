package co.assip.erp.nomina.contabilizacion.prestaciones_sociales;

import co.assip.erp.nomina.contabilizacion.prestaciones_sociales.dto.PrestacionesSocialesContabilizacionResultadoDTO;
import co.assip.erp.nomina.contabilizacion.liquidacion.dto.LiquidacionMovimientoContableDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/contabilizacion/prestaciones-sociales")
@RequiredArgsConstructor
public class PrestacionesSocialesContabilizacionController {

    private final PrestacionesSocialesContabilizacionService service;

    @GetMapping("/preview/{idPeriodoNomina}")
    public List<LiquidacionMovimientoContableDTO> preview(@PathVariable Integer idPeriodoNomina) {
        return service.preview(idPeriodoNomina);
    }

    @GetMapping("/comprobante/{idPeriodoNomina}")
    public String obtenerComprobante(@PathVariable Integer idPeriodoNomina) {
        return service.obtenerComprobanteExistente(idPeriodoNomina);
    }

    @PostMapping("/ejecutar/{idPeriodoNomina}")
    public PrestacionesSocialesContabilizacionResultadoDTO ejecutar(@PathVariable Integer idPeriodoNomina) {
        return service.contabilizar(idPeriodoNomina);
    }

    @PostMapping("/reversar/{idPeriodoNomina}")
    public void reversar(@PathVariable Integer idPeriodoNomina) {
        service.reversarContabilizacion(idPeriodoNomina);
    }
}