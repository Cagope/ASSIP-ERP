package co.assip.erp.nomina.tipos_contratos;

import co.assip.erp.nomina.tipos_contratos.dto.TipoContratoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/tipos_contratos")
@RequiredArgsConstructor
public class TiposContratosController {

    private final TiposContratosService service;

    // =========================
    // LISTAR
    // =========================
    @GetMapping
    public List<TipoContratoDTO> listar() {
        return service.listar();
    }
}