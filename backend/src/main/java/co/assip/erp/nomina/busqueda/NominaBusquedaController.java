package co.assip.erp.nomina.busqueda;

import co.assip.erp.nomina.busqueda.dto.EmpleadoBusquedaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/busqueda")
@RequiredArgsConstructor
public class NominaBusquedaController {

    private final NominaBusquedaService service;

    // =====================================================
    // 🔍 BUSCAR EMPLEADOS (AUTOCOMPLETE)
    // =====================================================
    @GetMapping("/empleados")
    public List<EmpleadoBusquedaDTO> buscarEmpleados(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "20") Integer limit
    ) {

        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        return service.buscarEmpleados(q.trim(), limit);
    }

    // =====================================================
    // 📄 OBTENER EMPLEADO POR ID (EDICIÓN)
    // =====================================================
    @GetMapping("/empleados/{id}")
    public EmpleadoBusquedaDTO obtenerEmpleadoPorId(
            @PathVariable Integer id
    ) {
        return service.obtener(id);
    }
}
