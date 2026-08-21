package co.assip.erp.cartera.cierremensual;

import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/cierre-mensual")
public class CierreMensualController {

    private final CierreMensualService service;

    public CierreMensualController(
            CierreMensualService service
    ) {
        this.service = service;
    }

    // =========================================================
    // LISTAR CIERRES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<CierreMensualDTO>> listar() {

        return ResponseEntity.ok(
                service.listar()
        );
    }

    // =========================================================
    // CONSULTAR CIERRE POR ID
    // =========================================================

    @GetMapping("/{idCierreCartera}")
    public ResponseEntity<CierreMensualDTO> buscarPorId(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.buscarPorId(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // EJECUTAR CIERRE MENSUAL
    //
    // Ejemplo:
    //
    // POST
    // /api/v1/cartera/cierre-mensual/ejecutar
    //      ?fechaCorte=2026-05-31
    //
    // Este proceso:
    //
    // 1. crea / recupera la cabecera
    // 2. genera la foto mensual
    // 3. crea la base de cálculos comunes
    // 4. valida cantidades
    // 5. calcula saldo maestro de la foto
    // 6. actualiza la cabecera
    //
    // NO ejecuta todavía:
    // - edades
    // - aportes
    // - garantías
    // - VEA
    // - Anexo 1
    // - Anexo 2
    // =========================================================

    @PostMapping("/ejecutar")
    public ResponseEntity<CierreMensualDTO> ejecutar(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {

        return ResponseEntity.ok(
                service.ejecutar(
                        fechaCorte
                )
        );
    }

    // =========================================================
    // REGENERAR FOTOGRAFÍA
    //
    // Ejemplo:
    //
    // POST
    // /api/v1/cartera/cierre-mensual/3/regenerar
    //
    // REGLAS:
    //
    // - solamente cierre estado P
    // - conserva la cabecera
    // - conserva id_cierre_cartera
    // - elimina resultados base actuales
    // - elimina fotografía actual
    // - genera nuevamente la fotografía
    // - genera nuevamente resultados base
    // - recalcula cantidad de créditos
    // - recalcula saldo maestro
    //
    // Si el proceso falla, la transacción del Service
    // revierte la regeneración.
    // =========================================================

    @PostMapping("/{idCierreCartera}/regenerar")
    public ResponseEntity<CierreMensualDTO> regenerar(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.regenerar(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // CONSULTAR SI EL CIERRE TIENE FOTO
    // =========================================================

    @GetMapping("/{idCierreCartera}/foto/existe")
    public ResponseEntity<Boolean> existeFoto(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.existeFoto(
                        idCierreCartera
                )
        );
    }

    // =========================================================
    // PREPARAR BASE DE CÁLCULOS DE CIERRE HISTÓRICO
    //
    // Ejemplo:
    //
    // POST
    // /api/v1/cartera/cierre-mensual/1/preparar-base-historica
    //
    // REGLAS:
    //
    // - la cabecera ya debe existir
    // - la fotografía ya debe existir
    // - NO modifica la fotografía
    // - NO modifica la cabecera
    // - crea únicamente la base de cálculos comunes
    //   en cierres_cartera_resultados
    // - se usa para cierres históricos migrados
    // =========================================================

    @PostMapping("/{idCierreCartera}/preparar-base-historica")
    public ResponseEntity<Integer> prepararBaseHistorica(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.prepararBaseCalculosHistorico(
                        idCierreCartera
                )
        );
    }
}