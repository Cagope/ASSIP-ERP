package co.assip.erp.nomina.eventos_liquidacion;

import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionFormDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionListDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionSaveDTO;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nomina/eventos-liquidacion")
@RequiredArgsConstructor
public class EventosLiquidacionController {

    private final EventosLiquidacionService service;

    @GetMapping
    public List<EventoLiquidacionListDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EventoLiquidacionFormDTO obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public Map<String, Object> guardar(@RequestBody EventoLiquidacionSaveDTO dto) {
        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) idUsuario = 1;

        Long id = service.guardar(dto, idUsuario);

        return Map.of(
                "ok", true,
                "idEventoLiquidacion", id,
                "mensaje", "Evento de liquidación guardado correctamente."
        );
    }

    @PutMapping("/{id}/estado")
    public Map<String, Object> cambiarEstado(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) idUsuario = 1;

        service.cambiarEstado(id, body.get("estado"), idUsuario);

        return Map.of(
                "ok", true,
                "mensaje", "Estado actualizado correctamente."
        );
    }
}