package co.assip.erp.cdat.cancelacion;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionEntradaDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionItemDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionPreviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionFiltroDTO;
import java.util.List;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import java.util.Map;

@RestController
@RequestMapping("/cdat/cancelacion")
@RequiredArgsConstructor
public class CdatCancelacionController {

    private final CdatCancelacionService service;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;

    @GetMapping("/{idCuentaCdat}")
    public CdatCancelacionItemDTO obtenerPorId(
            @PathVariable Long idCuentaCdat
    ) {
        return service.obtenerPorId(idCuentaCdat);
    }

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
    public CdatCancelacionPreviewDTO preview(
            @RequestBody CdatCancelacionEntradaDTO input
    ) {
        return service.preview(input);
    }

    @PostMapping("/aplicar")
    public void aplicar(
            @RequestBody CdatCancelacionEntradaDTO input
    ) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.aplicar(input, idUsuario);
    }

    @PostMapping("/buscar")
    public List<CdatCancelacionItemDTO> buscar(
            @RequestBody CdatCancelacionFiltroDTO filtro
    ) {
        return service.buscar(filtro);
    }




}