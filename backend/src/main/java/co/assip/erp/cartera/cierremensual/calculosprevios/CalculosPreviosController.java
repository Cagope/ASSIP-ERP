package co.assip.erp.cartera.calculosprevios;

import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.cartera.calculosprevios.dto.ResumenEdadMoraDTO;
import co.assip.erp.cartera.calculosprevios.dto.ResumenControlesCalculosDTO;
import co.assip.erp.cartera.calculosprevios.dto.ResumenAportesGarantiasDTO;
import co.assip.erp.cartera.calculosprevios.dto.DetalleCalculosCierreDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    // POST
    // /api/v1/cartera/calculos-previos/{idCierreCartera}/ejecutar
    //
    // REQUISITOS:
    // - fotografía cerrada en firme
    // - cálculos no cerrados en firme
    //
    // RESULTADO:
    // - estado_calculos = E
    // - fecha_calculos_inicio registrada
    //
    // PROCESO:
    // - mora
    // - edad de mora
    // - banderas comunes
    // - edades de riesgo
    // - edades de reestructuración
    // - prorrateo de aportes
    // - prorrateo de garantías
    // - consolidación de garantías
    // - costas judiciales
    //
    // Todavía NO calcula:
    // - VEA
    // - deterioros
    // - edad de PE
    // - edad de homologación
    // - edad contable
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
    // CERRAR CÁLCULOS EN FIRME
    //
    // POST
    // /api/v1/cartera/calculos-previos/
    // {idCierreCartera}/cerrar
    //
    // REQUISITOS:
    // - fotografía cerrada en firme
    // - cálculos en estado E
    // - resultados existentes
    // - controles de integridad correctos
    //
    // RESULTADO:
    // - estado_calculos = C
    // - fecha_calculos_firme registrada
    //
    // Una vez cerrado:
    // - no permite volver a ejecutar cálculos
    // - habilita la etapa de Anexo 1
    // =========================================================

    @PostMapping("/{idCierreCartera}/cerrar")
    public ResponseEntity<CierreMensualDTO> cerrarCalculos(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.cerrarCalculos(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // CALCULAR SOLAMENTE MORA
    //
    // Endpoint específico para pruebas y validaciones.
    //
    // POST
    // /api/v1/cartera/calculos-previos/{idCierreCartera}/mora
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
    // Método de soporte.
    // Puede utilizarse para cierres históricos migrados.
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

    // =========================================================
    // RESUMEN POR CLASIFICACIÓN Y EDAD DE MORA
    // =========================================================

    @GetMapping("/{idCierreCartera}/resumen-edad-mora")
    public ResponseEntity<List<ResumenEdadMoraDTO>>
    obtenerResumenEdadMora(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.obtenerResumenEdadMora(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // RESUMEN DE CONTROLES DE CÁLCULOS
    //
    // GET
    // /api/v1/cartera/calculos-previos/
    // {idCierreCartera}/resumen-controles
    //
    // NO ejecuta cálculos.
    // NO modifica información.
    // =========================================================

    @GetMapping("/{idCierreCartera}/resumen-controles")
    public ResponseEntity<ResumenControlesCalculosDTO>
    obtenerResumenControles(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.obtenerResumenControles(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // RESUMEN DE APORTES Y GARANTÍAS
    //
    // GET
    // /api/v1/cartera/calculos-previos/
    // {idCierreCartera}/resumen-aportes-garantias
    //
    // NO ejecuta cálculos.
    // NO modifica información.
    // =========================================================

    @GetMapping("/{idCierreCartera}/resumen-aportes-garantias")
    public ResponseEntity<ResumenAportesGarantiasDTO>
    obtenerResumenAportesGarantias(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.obtenerResumenAportesGarantias(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // DETALLE COMPLETO DE CÁLCULOS DEL CIERRE
    //
    // GET
    // /api/v1/cartera/calculos-previos/
    // {idCierreCartera}/detalle-calculos
    //
    // Fuente para:
    // - revisión
    // - auditoría
    // - Excel
    //
    // NO ejecuta cálculos.
    // NO modifica información.
    // =========================================================

    @GetMapping("/{idCierreCartera}/detalle-calculos")
    public ResponseEntity<List<DetalleCalculosCierreDTO>>
    obtenerDetalleCalculosCierre(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.obtenerDetalleCalculosCierre(
                        idCierreCartera
                )
        );
    }
}