package co.assip.erp.depositos.interesmensual_sm;

import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMEntradaDTO;
import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMItemDTO;   // ⬅️ ESTE ES EL DTO CORRECTO
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 InteresMensualSMController
 * ----------------------------------------------------
 * Expone el proceso técnico de liquidación mensual
 * del interés sobre saldo mínimo del mes.
 *
 * Endpoint:
 *   POST /depositos/interes-mensual-sm/liquidar
 */
@RestController
@RequestMapping("/depositos/interes-mensual-sm")
@RequiredArgsConstructor
public class InteresMensualSMController {

    private final InteresMensualSMService service;

    @PostMapping("/liquidar")
    public List<InteresMensualSMItemDTO> liquidar(   // ⬅️ AQUI EL CAMBIO
                                                     @RequestBody InteresMensualSMEntradaDTO input,
                                                     @RequestHeader(name = "usuarioId", required = false) Integer usuarioId
    ) {

        if (usuarioId == null) {
            usuarioId = 1;
        }

        return service.liquidar(input);   // ⬅️ YA COINCIDE CON EL SERVICE
    }
}
