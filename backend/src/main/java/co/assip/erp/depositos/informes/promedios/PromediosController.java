package co.assip.erp.depositos.informes.promedios;

import co.assip.erp.depositos.informes.promedios.dto.PromediosRequestDTO;
import co.assip.erp.depositos.informes.promedios.dto.PromediosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/promedios")
@RequiredArgsConstructor
public class PromediosController {

    private final PromediosService service;

    @PostMapping
    public PromediosResponseDTO consultar(
            @RequestBody PromediosRequestDTO request
    ) {
        return service.consultar(request);
    }

}