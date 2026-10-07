package co.assip.erp.nomina.eps;

import co.assip.erp.nomina.eps.dto.EpsDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/eps")
@RequiredArgsConstructor
public class EpsController {

    private final EpsService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public List<EpsDTO> listar() {

        validarAcceso("NOMINA_VIEW");

        return service.listar();
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public EpsDTO obtener(
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
            @RequestBody EpsDTO dto
    ) {

        validarAcceso("NOMINA_PARAMETROS_EDIT");

        return service.crear(dto);
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Integer id,
            @RequestBody EpsDTO dto
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