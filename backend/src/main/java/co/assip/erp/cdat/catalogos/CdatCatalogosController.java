package co.assip.erp.cdat.catalogos;

import co.assip.erp.cdat.catalogos.dto.CdatAmortizacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cdat/catalogos")
@RequiredArgsConstructor
public class CdatCatalogosController {

    private final CdatCatalogosService service;

    @GetMapping("/amortizaciones")
    public List<CdatAmortizacionDTO> listarAmortizaciones() {
        return service.listarAmortizaciones();
    }
}