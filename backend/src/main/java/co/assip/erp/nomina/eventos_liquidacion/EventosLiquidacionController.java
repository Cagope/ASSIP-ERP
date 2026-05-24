package co.assip.erp.nomina.eventos_liquidacion;


import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionFormDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionListDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionSaveDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nomina/eventos-liquidacion")
@RequiredArgsConstructor
public class EventosLiquidacionController {

    private final EventosLiquidacionService service;
    private final UsuarioSesionService usuarioSesionService;

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
        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Long id = service.guardar(dto, idUsuario);

        return Map.of(
                "ok", true,
                "idEventoLiquidacion", id,
                "mensaje", "Evento de liquidación guardado correctamente."
        );
    }

    // =========================
    // NUEVO: ELIMINAR
    // =========================
    @DeleteMapping("/{id}")
    public Map<String, Object> eliminar(@PathVariable Long id) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.eliminar(id, idUsuario);

        return Map.of(
                "ok", true,
                "mensaje", "Evento eliminado correctamente."
        );
    }
}