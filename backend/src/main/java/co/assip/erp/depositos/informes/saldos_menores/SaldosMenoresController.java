package co.assip.erp.depositos.informes.saldos_menores;

import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresRequestDTO;
import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/saldos-menores")
@RequiredArgsConstructor
public class SaldosMenoresController {

    private final SaldosMenoresService service;

    @PostMapping
    public SaldosMenoresResponseDTO consultar(
            @RequestBody SaldosMenoresRequestDTO request
    ) {
        return service.consultar(request);
    }

}