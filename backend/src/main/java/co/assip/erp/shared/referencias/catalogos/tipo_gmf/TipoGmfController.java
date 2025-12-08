package co.assip.erp.shared.referencias.catalogos.tipo_gmf;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shared/tipo-gmf")
public class TipoGmfController {

    private final TipoGmfService service;

    @GetMapping
    public List<TipoGmf> listar() {
        return service.listar();
    }
}
