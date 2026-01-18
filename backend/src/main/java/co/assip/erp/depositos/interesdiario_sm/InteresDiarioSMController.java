package co.assip.erp.depositos.interesdiario_sm;

import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMEntradaDTO;
import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 InteresDiarioSMController
 * ----------------------------------------------------
 * Expone el proceso técnico de Interés Diario SM.
 *
 * Endpoint:
 *   POST /depositos/interes-diario-sm/ejecutar
 *
 * Recibe parámetros desde el frontend:
 *  - agencia
 *  - fecha de proceso
 *  - fecha de liquidación
 *  - forma seleccionada
 *
 * Devuelve:
 *  - listado de cuentas liquidadas (solo calculado, sin impacto en BD)
 */
@RestController
@RequestMapping("/depositos/interes-diario-sm")
@RequiredArgsConstructor
public class InteresDiarioSMController {

    private final InteresDiarioSMService service;

    @PostMapping("/ejecutar")
    public List<InteresDiarioSMItemDTO> ejecutar(
            @RequestBody InteresDiarioSMEntradaDTO input,
            @RequestHeader(name = "usuarioId", required = false) Integer usuarioId
    ) {
        if (usuarioId == null) {
            usuarioId = 1; // Ajustar según tu seguridad real
        }

        return service.ejecutar(input);
    }
}
