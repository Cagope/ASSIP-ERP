package co.assip.erp.nomina.informes.novedades_empleado;

import co.assip.erp.nomina.informes.novedades_empleado.dto.NovedadesEmpleadoInformeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/nomina/informes/novedades-empleado")
@RequiredArgsConstructor
public class NovedadesEmpleadoInformeController {

    private final NovedadesEmpleadoInformeService service;

    @GetMapping
    public List<NovedadesEmpleadoInformeDTO> consultar(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) Integer idEmpleado,
            @RequestParam(required = false) Integer idPeriodo,
            @RequestParam(required = false) String codigoConcepto,
            @RequestParam(required = false) LocalDate fechaInicial,
            @RequestParam(required = false) LocalDate fechaFinal
    ) {
        return service.consultar(
                documento,
                idEmpleado,
                idPeriodo,
                codigoConcepto,
                fechaInicial,
                fechaFinal
        );
    }
}