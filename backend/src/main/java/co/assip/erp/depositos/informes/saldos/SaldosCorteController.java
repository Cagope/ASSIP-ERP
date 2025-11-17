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

    public SaldosCorteController(SaldosCorteService service) {
        this.service = service;
    }

    // ============================================================
    // 🔍 CONSULTA DETALLADA — YA EXISTENTE (NO TOCAR)
    // ============================================================
    @PostMapping("/corte")
    public Map<String, Object> consultar(@RequestBody Map<String, Object> filtros) {

        // agencia llega como número → convertir a String
        String agencia = filtros.get("agencia").toString();
        String fechaCorte = filtros.get("fechaCorte").toString();

        List<SaldosCorteItemDTO> datos = service.consultar(fechaCorte, agencia);
        SaldosCorteResumenDTO resumen = service.resumen(datos);

        return Map.of(
                "resumen", resumen,
                "items", datos
        );
    }

    // ============================================================
    // 🆕 RESUMEN POR AGENCIA Y FORMA DE AHORRO
    // ============================================================
    @GetMapping("/resumen-agencia")
    public List<Map<String, Object>> resumenPorAgencia(
            @RequestParam String fechaCorte
    ) {
        return service.resumenPorAgencia(fechaCorte);
    }
}
