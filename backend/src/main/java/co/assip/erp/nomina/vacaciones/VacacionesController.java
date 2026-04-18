package co.assip.erp.nomina.vacaciones;

import co.assip.erp.nomina.vacaciones.dto.VacacionPreviewDTO;
import co.assip.erp.nomina.vacaciones.dto.VacacionPreviewRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/vacaciones")
@RequiredArgsConstructor
public class VacacionesController {

    private final VacacionesService service;

    @PostMapping("/preview")
    public VacacionPreviewDTO preview(
            @RequestBody VacacionPreviewRequestDTO request
    ) {
        return service.preview(request);
    }
}