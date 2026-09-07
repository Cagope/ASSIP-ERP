package co.assip.erp.depositos.analisis.concentracion;

import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosRequestDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/depositos/analisis/concentracion")
@RequiredArgsConstructor
public class ConcentracionDepositosController {

    private final ConcentracionDepositosService service;

    @PostMapping
    public ResponseEntity<ConcentracionDepositosResponseDTO> consultar(
            @RequestBody ConcentracionDepositosRequestDTO request
    ) {
        return ResponseEntity.ok(
                service.consultar(request)
        );
    }
}