package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajasProvisionCerrarDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionFormDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionListDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cajas/provisiones")
@RequiredArgsConstructor
public class CajasProvisionController {

    private final CajasProvisionService service;

    @GetMapping
    public List<CajasProvisionListDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{idProvision}")
    public CajasProvisionFormDTO obtenerPorId(@PathVariable Long idProvision) {
        return service.obtenerPorId(idProvision);
    }

    @PostMapping
    public Long guardar(@RequestBody CajasProvisionSaveDTO dto) {
        return service.guardar(dto);
    }

    @PostMapping("/cerrar")
    public void cerrar(@RequestBody CajasProvisionCerrarDTO dto) {
        service.cerrar(dto);
    }

    @GetMapping("/abiertas")
    public List<CajasProvisionListDTO> listarCajasAbiertas(
            @RequestParam Integer idAgencia,
            @RequestParam String fecha
    ) {
        return service.listarCajasAbiertas(
                idAgencia,
                java.time.LocalDate.parse(fecha)
        );
    }
}