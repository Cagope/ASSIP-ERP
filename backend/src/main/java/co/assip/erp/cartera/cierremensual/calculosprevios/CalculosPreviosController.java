package co.assip.erp.cartera.calculosprevios;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartera/calculos-previos")
public class CalculosPreviosController {

    private final CalculosPreviosService service;

    public CalculosPreviosController(
            CalculosPreviosService service
    ) {
        this.service = service;
    }

    // =========================================================
    // EJECUTAR CÁLCULOS PREVIOS
    //
    // Ejemplo:
    //
    // POST
    // /api/v1/cartera/calculos-previos/4/ejecutar
    //
    // PROCESO ACTUAL:
    //
    // 1. valida el cierre
    // 2. valida estado P
    // 3. valida la base de cálculos
    // 4. calcula días de mora
    // 5. determina edad de mora
    // 6. determina crédito de una sola cuota
    // 7. determina crédito reestructurado
    // 8. valida cantidades
    //
    // Todavía NO calcula:
    // - edades de riesgo
    // - aportes
    // - garantías
    // - VEA
    // - deterioros
    // =========================================================

    @PostMapping("/{idCierreCartera}/ejecutar")
    public ResponseEntity<Integer> ejecutar(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.ejecutar(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // CALCULAR SOLAMENTE MORA
    //
    // Endpoint específico para pruebas y validaciones.
    //
    // Ejemplo:
    //
    // POST
    // /api/v1/cartera/calculos-previos/4/mora
    //
    // REGLA:
    //
    // dias_mora =
    // MAX(
    //     fecha_corte - proxima_fecha_capital,
    //     0
    // )
    //
    // edad_de_mora:
    // cartera.clasificaciones_mora
    // =========================================================

    @PostMapping("/{idCierreCartera}/mora")
    public ResponseEntity<Integer> calcularMora(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.calcularMora(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // CALCULAR SOLO PRORRATEO DE APORTES
    //
    // POST
    // /api/v1/cartera/calculos-previos/{idCierreCartera}/aportes
    //
    // Permite ejecutar el prorrateo independientemente,
    // incluso para cierres históricos migrados.
    // =========================================================

    @PostMapping("/{idCierreCartera}/aportes")
    public ResponseEntity<Integer> calcularProrrateoAportes(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.calcularProrrateoAportes(
                        idCierreCartera
                )
        );
    }
}