package co.assip.erp.nomina.eventos_liquidacion;

import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionFormDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionListDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionSaveDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
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
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public List<EventoLiquidacionListDTO> listar() {

        validarAcceso("NOMINA_VIEW");

        return service.listar();
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public EventoLiquidacionFormDTO obtener(
            @PathVariable Long id
    ) {

        validarAcceso("NOMINA_VIEW");

        return service.obtenerPorId(id);
    }

    // ============================================================
    // GUARDAR
    // ============================================================
    @PostMapping
    public Map<String, Object> guardar(
            @RequestBody EventoLiquidacionSaveDTO dto
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Long id = service.guardar(
                dto,
                idUsuario
        );

        return Map.of(
                "ok", true,
                "idEventoLiquidacion", id,
                "mensaje", "Evento de liquidación guardado correctamente."
        );
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    @DeleteMapping("/{id}")
    public Map<String, Object> eliminar(
            @PathVariable Long id
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.eliminar(
                id,
                idUsuario
        );

        return Map.of(
                "ok", true,
                "mensaje", "Evento eliminado correctamente."
        );
    }

    // ============================================================
    // AUTORIZACIÓN
    // ============================================================
    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}