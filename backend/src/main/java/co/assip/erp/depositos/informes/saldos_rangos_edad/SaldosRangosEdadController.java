package co.assip.erp.depositos.informes.saldos_rangos_edad;

import co.assip.erp.depositos.informes.saldos_rangos_edad.dto.SaldosRangosEdadRequestDTO;
import co.assip.erp.depositos.informes.saldos_rangos_edad.dto.SaldosRangosEdadResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/saldos-rangos-edad")
@RequiredArgsConstructor
public class SaldosRangosEdadController {

    private final SaldosRangosEdadService service;

    @PostMapping
    public SaldosRangosEdadResponseDTO consultar(
            @RequestBody SaldosRangosEdadRequestDTO request
    ) {
        return service.consultar(request);
    }

}