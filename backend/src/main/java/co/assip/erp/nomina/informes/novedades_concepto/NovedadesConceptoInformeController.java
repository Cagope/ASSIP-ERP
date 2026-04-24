package co.assip.erp.nomina.informes.novedades_concepto;

import co.assip.erp.nomina.informes.novedades_concepto.dto.NovedadesConceptoInformeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/nomina/informes/novedades-concepto")
@RequiredArgsConstructor
public class NovedadesConceptoInformeController {

    private final NovedadesConceptoInformeService service;

    @GetMapping
    public List<NovedadesConceptoInformeDTO> consultar(
            @RequestParam(required = false) String codigoConcepto,
            @RequestParam(required = false) Integer idEmpleado,
            @RequestParam(required = false) Integer idPeriodo,
            @RequestParam(required = false) LocalDate fechaInicial,
            @RequestParam(required = false) LocalDate fechaFinal
    ) {
        return service.consultar(
                codigoConcepto,
                idEmpleado,
                idPeriodo,
                fechaInicial,
                fechaFinal
        );
    }
}