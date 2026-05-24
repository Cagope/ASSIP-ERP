package co.assip.erp.cdat.liquidacion_diaria;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.cdat.liquidacion_diaria.dto.CdatLiquidacionDiariaEntradaDTO;
import co.assip.erp.cdat.liquidacion_diaria.dto.CdatLiquidacionDiariaPreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cdat/liquidacion-diaria")
@RequiredArgsConstructor
public class CdatLiquidacionDiariaController {

    private final CdatLiquidacionDiariaService service;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;

    @GetMapping("/proximo-comprobante/{idAgencia}/{tipoComprobante}")
    public Map<String, String> obtenerProximoComprobante(
            @PathVariable Integer idAgencia,
            @PathVariable String tipoComprobante
    ) {
        String numero = consecutivosComprobantesService.obtenerNumeroSugerido(
                tipoComprobante,
                idAgencia
        );

        return Map.of("numeroComprobante", numero);
    }

    @PostMapping("/preview")
    public CdatLiquidacionDiariaPreviewDTO preview(
            @RequestBody CdatLiquidacionDiariaEntradaDTO input
    ) {
        return service.preview(input);
    }

    @PostMapping("/aplicar")
    public void aplicar(
            @RequestBody CdatLiquidacionDiariaEntradaDTO input
    ) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.aplicar(input, idUsuario);
    }
}