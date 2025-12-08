package co.assip.erp.depositos.interesmensual_tac;

import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACEntradaDTO;
import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 InteresMensualTACController
 * ----------------------------------------------------
 * Liquidación mensual del TAC (forma 14).
 *
 * Endpoint:
 *   POST /depositos/interes-mensual-tac/liquidar
 */
@RestController
@RequestMapping("/depositos/interes-mensual-tac")
@RequiredArgsConstructor
public class InteresMensualTACController {

    private final InteresMensualTACService service;

    @PostMapping("/liquidar")
    public List<InteresMensualTACItemDTO> liquidar(
            @RequestBody InteresMensualTACEntradaDTO input,
            @RequestHeader(name = "usuarioId", required = false) Integer usuarioId
    ) {

        if (usuarioId == null) {
            usuarioId = 1;
        }

        return service.liquidar(input);
    }
}
