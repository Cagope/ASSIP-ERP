package co.assip.erp.depositos.informes.saldos;

import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteItemDTO;
import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteResumenDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/depositos/informes/saldos")
public class SaldosCorteController {

    private final SaldosCorteService service;

    public SaldosCorteController(
            SaldosCorteService service
    ) {
        this.service = service;
    }

    @PostMapping("/corte")
    public Map<String, Object> consultar(
            @RequestBody Map<String, Object> filtros
    ) {

        String agencia =
                filtros.getOrDefault("agencia", "0").toString();

        String fechaCorte =
                filtros.get("fechaCorte").toString();

        List<SaldosCorteItemDTO> datos =
                service.consultar(
                        fechaCorte,
                        agencia
                );

        SaldosCorteResumenDTO resumen =
                service.resumen(datos);

        return Map.of(
                "resumen", resumen,
                "items", datos
        );
    }

    @GetMapping("/resumen-agencia")
    public List<Map<String, Object>> resumenPorAgencia(
            @RequestParam String fechaCorte,
            @RequestParam(defaultValue = "0") String agencia
    ) {
        return service.resumenPorAgencia(
                fechaCorte,
                agencia
        );
    }

}