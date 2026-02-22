package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.nomina.periodos_nomina.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/periodos-nomina/generar")
public class PeriodosGeneradorController {

    private final PeriodosGeneradorService service;

    public PeriodosGeneradorController(PeriodosGeneradorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<List<PeriodoGeneradoDTO>> generar(
            @RequestBody PeriodosGenerarRequestDTO req,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {

        List<PeriodoGeneradoDTO> result =
                service.generar(req, idUsuario);

        return ResponseEntity.ok(result);
    }
}
