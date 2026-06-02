package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajasProvisionCerrarDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionFormDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionListDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.cajas.provisiones.dto.CajaDisponibleDTO;
import co.assip.erp.cajas.provisiones.dto.CajaEstadoDTO;

import java.util.List;
import co.assip.erp.cajas.provisiones.dto.CajaProvisionActivaDTO;

@RestController
@RequestMapping("/cajas/provisiones")
@RequiredArgsConstructor
public class CajasProvisionController {

    private final CajasProvisionService service;

    @GetMapping("/estado-cajas")
    public List<CajaEstadoDTO> listarEstadoCajas(
            @RequestParam Integer idAgencia,
            @RequestParam String fechaContable
    ) {
        return service.listarEstadoCajas(
                idAgencia,
                java.time.LocalDate.parse(fechaContable)
        );
    }

    @GetMapping("/activa-usuario")
    public CajaProvisionActivaDTO obtenerProvisionActivaUsuario() {
        return service.obtenerProvisionActivaUsuario();
    }

    @PostMapping
    public Long guardar(@RequestBody CajasProvisionSaveDTO dto) {
        return service.guardar(dto);
    }

    @PostMapping("/vincular")
    public Long vincularUsuarioCaja(
            @RequestBody CajasProvisionSaveDTO dto
    ) {
        return service.vincularUsuarioCaja(dto);
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

    @GetMapping("/disponibles")
    public List<CajaDisponibleDTO> listarCajasDisponibles(
            @RequestParam Integer idAgencia
    ) {
        return service.listarCajasDisponibles(idAgencia);
    }
}