package co.assip.erp.depositos.informes.antiguedad_asociados;

import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosRequestDTO;
import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/antiguedad-asociados")
@RequiredArgsConstructor
public class AntiguedadAsociadosController {

    private final AntiguedadAsociadosService service;

    @PostMapping
    public AntiguedadAsociadosResponseDTO consultar(
            @RequestBody AntiguedadAsociadosRequestDTO request
    ) {
        return service.consultar(request);
    }

}