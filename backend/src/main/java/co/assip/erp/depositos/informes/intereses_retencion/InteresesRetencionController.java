package co.assip.erp.depositos.informes.intereses_retencion;

import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionRequestDTO;
import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/intereses-retencion")
@RequiredArgsConstructor
public class InteresesRetencionController {

    private final InteresesRetencionService service;

    @PostMapping
    public InteresesRetencionResponseDTO consultar(
            @RequestBody InteresesRetencionRequestDTO request
    ) {
        return service.consultar(request);
    }

}