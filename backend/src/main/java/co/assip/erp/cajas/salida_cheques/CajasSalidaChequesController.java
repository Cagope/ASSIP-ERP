
package co.assip.erp.cajas.salida_cheques;

import co.assip.erp.cajas.salida_cheques.dto.CajasSalidaChequesChequeDTO;
import co.assip.erp.cajas.salida_cheques.dto.CajasSalidaChequesPreviewDTO;
import co.assip.erp.cajas.salida_cheques.dto.CajasSalidaChequesRequestDTO;
import co.assip.erp.cajas.salida_cheques.dto.CajasSalidaChequesResponseDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cajas/salida-cheques")
@RequiredArgsConstructor
public class CajasSalidaChequesController {

    private final CajasSalidaChequesService service;

    // =========================================================
    // CHEQUES DISPONIBLES DEL USUARIO AUTENTICADO
    // =========================================================

    @GetMapping("/disponibles")
    public ResponseEntity<List<CajasSalidaChequesChequeDTO>>
    listarDisponibles() {

        return ResponseEntity.ok(
                service.listarDisponibles()
        );
    }

    // =========================================================
    // VISTA PREVIA DEL COMPROBANTE CONTABLE RC
    //
    // No genera consecutivo definitivo.
    // No registra movimientos contables ni de caja.
    // No modifica la custodia de los cheques.
    // =========================================================

    @PostMapping("/preview-contable")
    public ResponseEntity<CajasSalidaChequesPreviewDTO> previewContable(
            @RequestBody CajasSalidaChequesRequestDTO request
    ) {

        CajasSalidaChequesPreviewDTO resultado =
                service.preview(request);

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // EJECUTAR SALIDA DE CHEQUES
    // =========================================================

    @PostMapping("/ejecutar")
    public ResponseEntity<CajasSalidaChequesResponseDTO> ejecutar(
            @RequestBody CajasSalidaChequesRequestDTO request
    ) {

        CajasSalidaChequesResponseDTO resultado =
                service.ejecutar(request);

        return ResponseEntity.ok(resultado);
    }
}
