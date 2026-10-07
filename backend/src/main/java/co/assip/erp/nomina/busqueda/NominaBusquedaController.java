package co.assip.erp.nomina.busqueda;

import co.assip.erp.nomina.busqueda.dto.EmpleadoBusquedaDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/busqueda")
@RequiredArgsConstructor
public class NominaBusquedaController {

    private final NominaBusquedaService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =====================================================
    // BUSCAR EMPLEADOS (AUTOCOMPLETE)
    // =====================================================
    @GetMapping("/empleados")
    public List<EmpleadoBusquedaDTO> buscarEmpleados(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "20") Integer limit
    ) {

        validarAcceso("NOMINA_VIEW");

        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        return service.buscarEmpleados(
                q.trim(),
                limit
        );
    }

    // =====================================================
    // OBTENER EMPLEADO POR ID
    // =====================================================
    @GetMapping("/empleados/{id}")
    public EmpleadoBusquedaDTO obtenerEmpleadoPorId(
            @PathVariable Integer id
    ) {

        validarAcceso("NOMINA_VIEW");

        return service.obtener(id);
    }

    // =====================================================
    // AUTORIZACIÓN
    // =====================================================
    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}