package co.assip.erp.activosfijos.catalogos;

import co.assip.erp.activosfijos.catalogos.dto.EstadoActivoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activos-fijos/estados-activo")
@RequiredArgsConstructor
public class EstadosActivoController {

    private final EstadosActivoRepository repository;

    @GetMapping
    public List<EstadoActivoDTO> listar() {
        return repository.listar();
    }
}
