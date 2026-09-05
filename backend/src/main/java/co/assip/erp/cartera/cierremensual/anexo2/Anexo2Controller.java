package co.assip.erp.cartera.cierremensual.anexo2;

import co.assip.erp.cartera.cierremensual.anexo2.dto.DetalleAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.MoraAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.ResumenAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.TrabajoAnexo2DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cartera/anexo2")
@RequiredArgsConstructor
public class Anexo2Controller {

    private final Anexo2Service service;


    // =========================================================
    // PREPARAR / EJECUTAR ANEXO 2
    // =========================================================

    @PostMapping("/{idCierreCartera}/preparar")
    public ResponseEntity<Anexo2Service.ResultadoPreparacionAnexo2> preparar(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.preparar(
                        idCierreCartera
                )
        );
    }


    // =========================================================
    // CERRAR ANEXO 2 EN FIRME
    // =========================================================

    @PostMapping("/{idCierreCartera}/cerrar")
    public ResponseEntity<Void> cerrar(
            @PathVariable Integer idCierreCartera
    ) {

        service.cerrar(
                idCierreCartera
        );

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // MODELOS PE DEL CIERRE
    // =========================================================

    @GetMapping("/{idCierreCartera}/modelos")
    public ResponseEntity<List<Integer>> obtenerModelosDelCierre(
            @PathVariable Integer idCierreCartera
    ) {

        return ResponseEntity.ok(
                service.obtenerModelosDelCierre(
                        idCierreCartera
                )
        );
    }


    // =========================================================
    // HOJA: RESULTADO
    // =========================================================

    @GetMapping("/{idCierreCartera}/modelos/{idModeloPe}/detalle")
    public ResponseEntity<List<DetalleAnexo2DTO>> obtenerDetalle(
            @PathVariable Integer idCierreCartera,
            @PathVariable Integer idModeloPe
    ) {

        return ResponseEntity.ok(
                service.obtenerDetalle(
                        idCierreCartera,
                        idModeloPe
                )
        );
    }


    // =========================================================
    // HOJA: HOJA_TRABAJO
    // =========================================================

    @GetMapping("/{idCierreCartera}/modelos/{idModeloPe}/trabajo")
    public ResponseEntity<List<TrabajoAnexo2DTO>> obtenerTrabajo(
            @PathVariable Integer idCierreCartera,
            @PathVariable Integer idModeloPe
    ) {

        return ResponseEntity.ok(
                service.obtenerTrabajo(
                        idCierreCartera,
                        idModeloPe
                )
        );
    }


    // =========================================================
    // HOJA: MORA
    // =========================================================

    @GetMapping("/{idCierreCartera}/modelos/{idModeloPe}/mora")
    public ResponseEntity<List<MoraAnexo2DTO>> obtenerMora(
            @PathVariable Integer idCierreCartera,
            @PathVariable Integer idModeloPe
    ) {

        return ResponseEntity.ok(
                service.obtenerMora(
                        idCierreCartera,
                        idModeloPe
                )
        );
    }


    // =========================================================
    // HOJA: RESUMEN
    // =========================================================

    @GetMapping("/{idCierreCartera}/modelos/{idModeloPe}/resumen")
    public ResponseEntity<List<ResumenAnexo2DTO>> obtenerResumen(
            @PathVariable Integer idCierreCartera,
            @PathVariable Integer idModeloPe
    ) {

        return ResponseEntity.ok(
                service.obtenerResumen(
                        idCierreCartera,
                        idModeloPe
                )
        );
    }
}