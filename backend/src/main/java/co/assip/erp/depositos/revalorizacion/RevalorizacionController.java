package co.assip.erp.depositos.revalorizacion;

import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionEntradaDTO;
import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 RevalorizacionController
 * ----------------------------------------------------
 * Exposición del proceso técnico de revalorización.
 *
 * Endpoint:
 *   POST /api/v1/depositos/revalorizacion/ejecutar
 *
 * Recibe parámetros desde el frontend:
 *  - agencia
 *  - fechas de evaluación
 *  - fecha de contabilización
 *  - tasa de revalorización
 *
 * Devuelve:
 *  - listado de cuentas con saldo, promedio y valor revalorización
 */
@RestController
@RequestMapping("/depositos/revalorizacion")
@RequiredArgsConstructor
public class RevalorizacionController {

    private final RevalorizacionService service;

    @PostMapping("/ejecutar")
    public List<RevalorizacionItemDTO> ejecutar(
            @RequestBody RevalorizacionEntradaDTO input,
            @RequestHeader(name = "usuarioId", required = false) Integer usuarioId
    ) {
        // Si no llega usuario, usamos uno por defecto (según tus reglas)
        if (usuarioId == null) {
            usuarioId = 1; // Este valor lo cambias según tu seguridad
        }

        return service.ejecutar(input);
    }
}
