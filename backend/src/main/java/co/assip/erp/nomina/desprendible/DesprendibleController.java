package co.assip.erp.nomina.desprendible;

import co.assip.erp.nomina.desprendible.dto.DesprendibleEmpleadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/nomina/desprendible")
public class DesprendibleController {

    private final DesprendibleService service;
    private final DesprendiblePdfService pdfService;

    @GetMapping("/{idPeriodo}/empleados")
    public ResponseEntity<List<DesprendibleRepository.EmpleadoPeriodoRow>>
    listarEmpleados(@PathVariable Integer idPeriodo) {

        return ResponseEntity.ok(
                service.listarEmpleadosPeriodo(idPeriodo)
        );
    }

    @GetMapping
    public ResponseEntity<DesprendibleEmpleadoDTO> obtener(
            @RequestParam Integer idPeriodo,
            @RequestParam Integer idContrato
    ) {
        return ResponseEntity.ok(
                service.generar(idPeriodo, idContrato)
        );
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam Integer idPeriodo,
            @RequestParam Integer idContrato
    ) {
        return pdfService.generarPdfResponse(
                service.generar(idPeriodo, idContrato)
        );
    }

    @GetMapping("/pdf/todos")
    public ResponseEntity<byte[]> pdfTodos(
            @RequestParam Integer idPeriodo
    ) {
        return pdfService.generarZipPeriodo(idPeriodo);
    }

}