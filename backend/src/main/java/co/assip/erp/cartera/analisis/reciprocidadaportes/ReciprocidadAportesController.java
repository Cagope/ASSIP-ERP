package co.assip.erp.cartera.analisis.reciprocidadaportes;

import co.assip.erp.cartera.analisis.reciprocidadaportes.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cartera/analisis/reciprocidad-aportes")
@RequiredArgsConstructor
public class ReciprocidadAportesController {

    private final ReciprocidadAportesService service;

    @GetMapping("/control")
    public Map<String, Object> control() {
        return Map.of(
                "proceso", "Reciprocidad de Aportes",
                "estado", "OK",
                "soloLectura", true
        );
    }

    @GetMapping("/cortes")
    public List<LocalDate> cortes() {
        return service.listarCortes();
    }

    @GetMapping("/resumen")
    public ReciprocidadAportesResumenDTO resumen(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {
        return service.obtenerResumen(fechaCorte);
    }

    @GetMapping("/personas")
    public List<ReciprocidadAportesPersonaDTO> personas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {
        return service.listarPersonas(fechaCorte);
    }

    @GetMapping("/detalle")
    public List<ReciprocidadAportesDetalleDTO> detalle(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte
    ) {
        return service.listarDetalle(fechaCorte);
    }

    @GetMapping("/detalle/persona/{idDatosPersonal}")
    public List<ReciprocidadAportesDetalleDTO> detallePersona(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @PathVariable Long idDatosPersonal
    ) {
        return service.listarDetallePorPersona(fechaCorte, idDatosPersonal);
    }
}
