package co.assip.erp.cajas.cierre;

import co.assip.erp.cajas.cierre.dto.CajaCierreConfirmarDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionListDTO;
import java.util.List;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/cajas/cierre")
@RequiredArgsConstructor
public class CajasCierreController {

    private final CajasCierreService service;

    @GetMapping("/preparar")
    public CajaCierreResumenDTO preparar(
            @RequestParam LocalDate fechaContable
    ) {

        return service.prepararCierre(
                fechaContable
        );
    }

    @GetMapping("/disponibles")
    public List<CajasProvisionListDTO> listarDisponibles(
            @RequestParam LocalDate fechaContable
    ) {

        return service.listarCajasDisponibles(
                fechaContable
        );
    }

    @GetMapping("/denominaciones")
    public List<BigDecimal> listarDenominacionesBilletes() {

        return service.listarDenominacionesBilletes();
    }

    @GetMapping("/{idProvision}")
    public CajaCierreResumenDTO obtenerResumen(
            @PathVariable Long idProvision
    ) {

        return service.obtenerResumen(
                idProvision
        );
    }

    @PostMapping("/confirmar")
    public CajaCierreResumenDTO confirmar(
            @RequestBody CajaCierreConfirmarDTO dto
    ) {

        return service.confirmar(dto);
    }
}