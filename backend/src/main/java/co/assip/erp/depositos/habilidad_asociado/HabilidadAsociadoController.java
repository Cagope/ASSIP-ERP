package co.assip.erp.depositos.habilidad_asociado;

import co.assip.erp.depositos.habilidad_asociado.dto.HabilidadAsociadoEntradaDTO;
import co.assip.erp.depositos.habilidad_asociado.dto.HabilidadAsociadoItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 HabilidadAsociadoController
 * ----------------------------------------------------
 * Expone el proceso técnico para determinar la
 * HABILIDAD / INHABILIDAD de los asociados según:
 *   - Tipo de persona
 *   - Edad
 *   - Aportes en el rango
 *   - Estado de la cuenta
 *   - Saldo actual
 *
 * Endpoint principal:
 *   POST /depositos/habilidad-asociado/ejecutar
 *
 * Procesos adicionales:
 *   POST /depositos/habilidad-asociado/actualizar-estado
 */
@RestController
@RequestMapping("/depositos/habilidad-asociado")
@RequiredArgsConstructor
public class HabilidadAsociadoController {

    private final HabilidadAsociadoService service;

    // ===============================================================
    // 1️⃣ EJECUTAR HABILIDAD / INHABILIDAD
    // ===============================================================
    @PostMapping("/ejecutar")
    public List<HabilidadAsociadoItemDTO> ejecutar(
            @RequestBody HabilidadAsociadoEntradaDTO input,
            @RequestHeader(name = "usuarioId", required = false) Integer usuarioId
    ) {

        if (usuarioId == null) {
            usuarioId = 1;
        }

        return service.ejecutar(input);
    }

    // ===============================================================
    // 2️⃣ ACTUALIZAR ESTADO DE LA CUENTA (A <-> I)
    // ===============================================================
    @PostMapping("/actualizar-estado")
    public String actualizarEstado(
            @RequestParam Integer idCuentaAhorro,
            @RequestParam String nuevoEstado,
            @RequestHeader(name = "usuarioId", required = false) Integer usuarioId
    ) {

        if (usuarioId == null) {
            usuarioId = 1;
        }

        service.actualizarEstado(idCuentaAhorro, nuevoEstado, usuarioId);

        return "OK";
    }
}
