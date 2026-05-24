package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.nomina.periodos_nomina.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/periodos-nomina/generar")
public class PeriodosGeneradorController {

    private final PeriodosGeneradorService service;
    private final UsuarioSesionService usuarioSesionService;

    public PeriodosGeneradorController(
            PeriodosGeneradorService service,
            UsuarioSesionService usuarioSesionService
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
    }

    @PostMapping
    public ResponseEntity<List<PeriodoGeneradoDTO>> generar(
            @RequestBody PeriodosGenerarRequestDTO req
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        List<PeriodoGeneradoDTO> result =
                service.generar(req, idUsuario);

        return ResponseEntity.ok(result);
    }
}
