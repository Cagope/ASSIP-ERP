package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.nomina.periodos_nomina.dto.PeriodoAccionDTO;
import co.assip.erp.nomina.periodos_nomina.dto.PeriodoNominaListDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/periodos-nomina")
public class PeriodosNominaController {

    private final PeriodosNominaService service;

    public PeriodosNominaController(PeriodosNominaService service) {
        this.service = service;
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
            @RequestBody PeriodoAccionDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.ejecutarAccion(dto, usr);
        return ResponseEntity.ok().build();
    }
}