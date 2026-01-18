package co.assip.erp.activosfijos.ingreso;

import co.assip.erp.activosfijos.ingreso.dto.IngresoActivosRequestDTO;
import co.assip.erp.activosfijos.ingreso.dto.IngresoActivosResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activos-fijos/ingreso")
@RequiredArgsConstructor
public class IngresoActivosController {

    private final IngresoActivosService service;

    @PostMapping
    public ResponseEntity<IngresoActivosResponseDTO> ingresar(
            @Valid @RequestBody IngresoActivosRequestDTO request
    ) {
        IngresoActivosResponseDTO response = service.ingresarActivos(request);
        return ResponseEntity.ok(response);
    }
}
