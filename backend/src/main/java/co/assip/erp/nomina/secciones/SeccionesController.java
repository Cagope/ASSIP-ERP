package co.assip.erp.nomina.secciones;

import co.assip.erp.nomina.secciones.dto.SeccionNominaDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/secciones")
@RequiredArgsConstructor
public class SeccionesController {

    private final SeccionesService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public List<SeccionNominaDTO> listar() {

        validarAcceso("NOMINA_VIEW");

        return service.listar();
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public SeccionNominaDTO obtener(
            @PathVariable Integer id
    ) {

        validarAcceso("NOMINA_VIEW");

        return service.obtener(id);
    }

    // ============================================================
    // CREAR
    // ============================================================
    @PostMapping
    public void crear(
            @RequestBody SeccionNominaDTO dto
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        service.crear(dto);
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Integer id,
            @RequestBody SeccionNominaDTO dto
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        service.actualizar(
                id,
                dto
        );
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Integer id
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        service.eliminar(id);
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