package co.assip.erp.shared.financiero.tablaamortizacion;

import co.assip.erp.shared.financiero.tablaamortizacion.dto.TablaAmortizacionRequestDTO;
import co.assip.erp.shared.financiero.tablaamortizacion.dto.TablaAmortizacionResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shared/financiero/tabla-amortizacion")
public class TablaAmortizacionController {

    private final TablaAmortizacionService service;

    public TablaAmortizacionController(
            TablaAmortizacionService service
    ) {
        this.service = service;
    }

    @PostMapping("/calcular")
    public ResponseEntity<TablaAmortizacionResponseDTO> calcular(
            @RequestBody TablaAmortizacionRequestDTO request
    ) {
        return ResponseEntity.ok(
                service.calcular(request)
        );
    }
}