package co.assip.erp.depositos.revalorizacion;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionEntradaDTO;
import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/depositos/revalorizacion")
@RequiredArgsConstructor
public class RevalorizacionController {

    private final RevalorizacionService service;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;

    @PostMapping("/liquidar")
    public List<RevalorizacionItemDTO> liquidar(
            @RequestBody RevalorizacionEntradaDTO input
    ) {

        usuarioSesionService.idUsuario();

        return service.liquidar(input);
    }

    @PostMapping("/aplicar")
    public void aplicar(
            @RequestBody RevalorizacionEntradaDTO input
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.aplicar(input, idUsuario);
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

        return Map.of(
                "numeroComprobante",
                numero
        );
    }

    @GetMapping("/ultima-liquidacion/{idAgencia}/{idFormaAhorro}")
    public Map<String, Object> obtenerUltimaLiquidacion(
            @PathVariable Integer idAgencia,
            @PathVariable Integer idFormaAhorro
    ) {

        return service.obtenerUltimaLiquidacion(
                idAgencia,
                idFormaAhorro
        );
    }

}