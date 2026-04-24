package co.assip.erp.nomina.informes.consolidado_conceptos;

import co.assip.erp.nomina.informes.consolidado_conceptos.dto.ConsolidadoConceptosInformeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/nomina/informes/consolidado-conceptos")
@RequiredArgsConstructor
public class ConsolidadoConceptosInformeController {

    private final ConsolidadoConceptosInformeService service;

    @GetMapping
    public List<ConsolidadoConceptosInformeDTO> consultar(
            @RequestParam(required = false) String codigoConcepto,
            @RequestParam(required = false) String tipoConcepto,
            @RequestParam(required = false) Integer idPeriodo,
            @RequestParam(required = false) LocalDate fechaInicial,
            @RequestParam(required = false) LocalDate fechaFinal
    ) {
        return service.consultar(
                codigoConcepto,
                tipoConcepto,
                idPeriodo,
                fechaInicial,
                fechaFinal
        );
    }
}