package co.assip.erp.nomina.empleado_contratos;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoListViewDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/nomina/empleado-contratos")
public class EmpleadoContratoController {

    private final EmpleadoContratoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public List<EmpleadoContratoListViewDTO> listar() {

        validarAcceso("NOMINA_VIEW");

        return service.listar();
    }

    // ============================================================
    // LISTAR POR EMPLEADO
    // ============================================================
    @GetMapping("/por-empleado/{idEmpleado}")
    public List<EmpleadoContratoListViewDTO> listarPorEmpleado(
            @PathVariable Integer idEmpleado
    ) {

        validarAcceso("NOMINA_VIEW");

        return service.listarPorEmpleado(idEmpleado);
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public EmpleadoContratoDTO obtener(
            @PathVariable Integer id
    ) {

        validarAcceso("NOMINA_VIEW");

        return service.obtener(id);
    }

    // ============================================================
    // CREAR
    // ============================================================
    @PostMapping
    public Integer crear(
            @RequestBody EmpleadoContratoDTO dto
    ) {

        validarAcceso("NOMINA_EMPLEADOS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return service.crear(
                dto,
                idUsuario
        );
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Integer id,
            @RequestBody EmpleadoContratoDTO dto
    ) {

        validarAcceso("NOMINA_EMPLEADOS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(
                id,
                dto,
                idUsuario
        );
    }

    // ============================================================
    // ELIMINAR / DESACTIVAR
    // ============================================================
    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Integer id
    ) {

        validarAcceso("NOMINA_EMPLEADOS_EDIT");

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.eliminar(
                id,
                idUsuario
        );
    }

    // ============================================================
    // AUTORIZACIÓN
    // ============================================================
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