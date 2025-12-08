package co.assip.erp.depositos.formasagencias;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/depositos/formasagencias")
@RequiredArgsConstructor
public class FormasAgenciasController {

    private final FormasAgenciasService service;

    @GetMapping("/{idAgencia}")
    public List<FormaAgenciaDTO> listarPorAgencia(@PathVariable Integer idAgencia) {
        return service.listarPorAgencia(idAgencia);
    }
}
