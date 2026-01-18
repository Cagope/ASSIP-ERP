package co.assip.erp.activosfijos.catalogos;

import co.assip.erp.activosfijos.catalogos.dto.FormaDepreciacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activos-fijos/formas-depreciacion")
@RequiredArgsConstructor
public class FormasDepreciacionController {

    private final FormasDepreciacionRepository repository;

    @GetMapping
    public List<FormaDepreciacionDTO> listar() {
        return repository.listar();
    }
}
