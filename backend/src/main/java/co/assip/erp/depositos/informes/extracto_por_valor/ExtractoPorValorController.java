package co.assip.erp.depositos.informes.extracto_por_valor;

import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorRequestDTO;
import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/extracto-por-valor")
@RequiredArgsConstructor
public class ExtractoPorValorController {

    private final ExtractoPorValorService service;

    @PostMapping
    public ExtractoPorValorResponseDTO consultar(
            @RequestBody ExtractoPorValorRequestDTO request
    ) {
        return service.consultar(request);
    }

}