package co.assip.erp.depositos.informes.movimientos_por_meses;

import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesRequestDTO;
import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/movimientos-por-meses")
@RequiredArgsConstructor
public class MovimientosPorMesesController {

    private final MovimientosPorMesesService service;

    @PostMapping
    public MovimientosPorMesesResponseDTO consultar(
            @RequestBody MovimientosPorMesesRequestDTO request
    ) {
        return service.consultar(request);
    }

}