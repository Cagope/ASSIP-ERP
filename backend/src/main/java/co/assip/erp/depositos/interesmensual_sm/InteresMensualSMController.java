package co.assip.erp.depositos.interesmensual_sm;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMEntradaDTO;
import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/depositos/interes-mensual-sm")
@RequiredArgsConstructor
public class InteresMensualSMController {

    private final InteresMensualSMService service;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final UsuarioSesionService usuarioSesionService;

    @PostMapping("/liquidar")
    public List<InteresMensualSMItemDTO> liquidar(
            @RequestBody InteresMensualSMEntradaDTO input
    ) {

        usuarioSesionService.idUsuario();

        return service.liquidar(input);
    }

    @PostMapping("/aplicar")
    public void aplicar(
            @RequestBody InteresMensualSMEntradaDTO input
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