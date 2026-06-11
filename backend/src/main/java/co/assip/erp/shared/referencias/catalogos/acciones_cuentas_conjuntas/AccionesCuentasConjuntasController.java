package co.assip.erp.shared.referencias.catalogos.acciones_cuentas_conjuntas;

import co.assip.erp.shared.referencias.catalogos.acciones_cuentas_conjuntas.dto.AccionCuentaConjuntaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shared/acciones-cuentas-conjuntas")
public class AccionesCuentasConjuntasController {

    private final AccionesCuentasConjuntasService service;

    @GetMapping
    public List<AccionCuentaConjuntaDTO> listar() {
        return service.listar();
    }

}