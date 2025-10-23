package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.RolPermiso;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.service.RolPermisoService;
import co.assip.erp.seguridad.service.UsuarioService;
import co.assip.erp.seguridad.service.AccessValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seguridad/rol-permisos")
public class RolPermisoController {

    private final RolPermisoService service;
    private final UsuarioService usuarioService;
    private final AccessValidator accessValidator;

    public RolPermisoController(RolPermisoService service, UsuarioService usuarioService, AccessValidator accessValidator) {
        this.service = service;
        this.usuarioService = usuarioService;
        this.accessValidator = accessValidator;
    }

    @PostMapping("/{idRol}")
    public void asignarPermisos(@PathVariable Integer idRol, @RequestBody List<Integer> idPermisos, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_EDIT");
        service.asignarPermisos(idRol, idPermisos);
    }

    @GetMapping("/{idRol}")
    public List<RolPermiso> listar(@PathVariable Integer idRol, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_VIEW");
        return service.listarPorRol(idRol);
    }
}
