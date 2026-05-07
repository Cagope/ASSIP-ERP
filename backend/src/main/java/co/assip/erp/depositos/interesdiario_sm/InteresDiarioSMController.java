package co.assip.erp.depositos.interesdiario_sm;

import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMEntradaDTO;
import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/depositos/interes-diario-sm")
@RequiredArgsConstructor
public class InteresDiarioSMController {

    private final InteresDiarioSMService service;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    @PostMapping("/liquidar")
    public List<InteresDiarioSMItemDTO> liquidar(
            @RequestBody InteresDiarioSMEntradaDTO input,
            @RequestHeader(name = "usuarioId", required = false) Integer usuarioId
    ) {
        if (usuarioId == null) {
            usuarioId = 1;
        }

        return service.liquidar(input);
    }

    @PostMapping("/aplicar")
    public void aplicar(
            @RequestBody InteresDiarioSMEntradaDTO input,
            @RequestHeader(name = "usuarioId", required = false) Integer usuarioId
    ) {
        if (usuarioId == null) {
            usuarioId = 1;
        }

        service.aplicar(input, usuarioId);
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