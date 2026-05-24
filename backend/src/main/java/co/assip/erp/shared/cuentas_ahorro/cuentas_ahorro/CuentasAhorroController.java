package co.assip.erp.shared.cuentas_ahorro.cuentas_ahorro;

import co.assip.erp.shared.cuentas_ahorro.cuentas_ahorro.dto.CuentaAhorroSelectDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/shared/cuentas_ahorro")
public class CuentasAhorroController {

    private final CuentasAhorroService service;

    public CuentasAhorroController(CuentasAhorroService service) {
        this.service = service;
    }

    @GetMapping("/{idDatosPersonal}")
    public List<CuentaAhorroSelectDTO> listar(
            @PathVariable Integer idDatosPersonal,
            @RequestHeader(value = "X-Agencias", required = false)
            String agenciasHeader
    ) {

        List<Integer> agencias = null;

        if (agenciasHeader != null && !agenciasHeader.isBlank()) {
            agencias = Arrays.stream(agenciasHeader.split(","))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .toList();
        }

        return service.listarPorDatosPersonal(idDatosPersonal, agencias);
    }
}
