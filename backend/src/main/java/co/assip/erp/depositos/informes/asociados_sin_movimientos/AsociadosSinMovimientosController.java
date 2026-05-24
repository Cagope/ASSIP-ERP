package co.assip.erp.depositos.informes.asociados_sin_movimientos;

import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosRequestDTO;
import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/asociados_sin_movimientos")
@RequiredArgsConstructor
public class AsociadosSinMovimientosController {

    private final AsociadosSinMovimientosService service;

    @PostMapping
    public AsociadosSinMovimientosResponseDTO consultar(
            @RequestBody AsociadosSinMovimientosRequestDTO request
    ) {
        return service.consultar(request);
    }

}