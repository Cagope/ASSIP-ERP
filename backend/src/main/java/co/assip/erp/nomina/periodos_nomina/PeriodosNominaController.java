package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.nomina.periodos_nomina.dto.PeriodoAccionDTO;
import co.assip.erp.nomina.periodos_nomina.dto.PeriodoNominaListDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/periodos-nomina")
public class PeriodosNominaController {

    private final PeriodosNominaService service;
    private final UsuarioSesionService usuarioSesionService;

    public PeriodosNominaController(
            PeriodosNominaService service,
            UsuarioSesionService usuarioSesionService
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
    }

    @GetMapping
    public ResponseEntity<List<PeriodoNominaListDTO>> listar(
            @RequestParam(value = "agencia", required = false) Integer idAgencia,
            @RequestParam(value = "anio", required = false) Integer anio
    ) {
        return ResponseEntity.ok(service.listar(idAgencia, anio));
    }

    // 🔎 NUEVO
    @GetMapping("/para-contabilizacion")
    public ResponseEntity<List<PeriodoNominaListDTO>> listarParaContabilizacion() {
        return ResponseEntity.ok(service.listarParaContabilizacion());
    }

    @PostMapping("/accion")
    public ResponseEntity<Void> accion(
            @RequestBody PeriodoAccionDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.ejecutarAccion(dto, idUsuario);

        return ResponseEntity.ok().build();
    }
}