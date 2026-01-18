package co.assip.erp.activosfijos.catalogos;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activos-fijos/tipos-adquisicion")
@RequiredArgsConstructor
public class TipoAdquisicionController {

    private final TipoAdquisicionRepository repository;

    @GetMapping
    public List<TipoAdquisicionDTO> listar() {
        return repository.listar();
    }
}
