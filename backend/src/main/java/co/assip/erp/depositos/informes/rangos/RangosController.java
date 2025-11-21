package co.assip.erp.depositos.informes.rangos;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 RangosController
 * ------------------------------------------------------------
 * Endpoint único para generar informes estadísticos de cuentas
 * por rangos:
 *
 *   - EDAD        (años)
 *   - SALDO       (pesos)
 *   - ANTIGUEDAD  (años)
 *
 * Body:
 *  {
 *      "tipo": "EDAD" | "SALDO" | "ANTIGUEDAD",
 *      "rangos": [
 *          { "desde": 0, "hasta": 20 },
 *          { "desde": 21, "hasta": 40 },
 *          { "desde": 41, "hasta": 60 },
 *          { "desde": 61, "hasta": 120 }
 *      ]
 *  }
 *
 * Retorna:
 *  List<RangosItemDTO>   (resultado plano para tabla o Excel)
 */
@RestController
@RequestMapping("/depositos/informes/rangos")
public class RangosController {

    private final RangosService service;

    public RangosController(RangosService service) {
        this.service = service;
    }

    /**
     * 📌 POST /depositos/informes/rangos
     * Genera el informe según el tipo y los rangos enviados.
     */
    @PostMapping
    public List<RangosItemDTO> consultar(@RequestBody RangosRequest request) {
        return service.consultar(request);
    }
}
