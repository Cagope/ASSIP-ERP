package co.assip.erp.cdat.causacion_mensual_cdat;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.cdat.causacion_mensual_cdat.dto.CdatCausacionMensualEntradaDTO;
import co.assip.erp.cdat.causacion_mensual_cdat.dto.CdatCausacionMensualPreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cdat/causacion-mensual-cdat")
@RequiredArgsConstructor
public class CdatCausacionMensualController {

    private final CdatCausacionMensualService service;
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
    public CdatCausacionMensualPreviewDTO preview(
            @RequestBody CdatCausacionMensualEntradaDTO dto
    ) {
        return service.preview(dto);
    }

    @PostMapping("/aplicar")
    public CdatCausacionMensualPreviewDTO aplicar(
            @RequestBody CdatCausacionMensualEntradaDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return service.aplicar(dto, idUsuario);
    }

    @GetMapping("/listar")
    public List<CdatCausacionMensualPreviewDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{idCausacion}")
    public CdatCausacionMensualPreviewDTO obtenerPorId(
            @PathVariable Long idCausacion
    ) {
        return service.obtenerPorId(idCausacion);
    }
}