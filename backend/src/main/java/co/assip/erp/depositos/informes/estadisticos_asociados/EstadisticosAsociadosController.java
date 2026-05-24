package co.assip.erp.depositos.informes.estadisticos_asociados;

import co.assip.erp.depositos.informes.estadisticos_asociados.dto.EstadisticosAsociadosRequestDTO;
import co.assip.erp.depositos.informes.estadisticos_asociados.dto.EstadisticosAsociadosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/estadisticos-asociados")
@RequiredArgsConstructor
public class EstadisticosAsociadosController {

    private final EstadisticosAsociadosService service;

    @PostMapping
    public EstadisticosAsociadosResponseDTO consultar(
            @RequestBody EstadisticosAsociadosRequestDTO request
    ) {
        return service.consultar(request);
    }

}