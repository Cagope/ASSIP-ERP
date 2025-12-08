package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.domain.Permiso;
import co.assip.erp.seguridad.repository.PermisoRepository;
import co.assip.erp.seguridad.service.UsuarioService;
import co.assip.erp.seguridad.service.AccessValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seguridad/rol-permisos")
public class RolPermisoController {

    private final PermisoRepository permisoRepository;
    private final UsuarioService usuarioService;
    private final AccessValidator accessValidator;

    public RolPermisoController(
            PermisoRepository permisoRepository,
            UsuarioService usuarioService,
            AccessValidator accessValidator
    ) {
        this.permisoRepository = permisoRepository;
        this.usuarioService = usuarioService;
        this.accessValidator = accessValidator;
    }

    /**
     * Asignar permisos = actualizar permisos con idRol en la tabla permisos
     */
    @PostMapping("/{idRol}")
    public void asignarPermisos(
            @PathVariable Integer idRol,
            @RequestBody List<Integer> idPermisos,
            HttpServletRequest req
    ) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_EDIT");

        // 1. Borrar permisos existentes de ese rol
        List<Permiso> actuales = permisoRepository.findByIdRol(idRol);
        permisoRepository.deleteAll(actuales);

        // 2. Crear nuevos
        for (Integer idPermiso : idPermisos) {
            permisoRepository.findById(idPermiso).ifPresent(p -> {
                p.setIdRol(idRol);
                permisoRepository.save(p);
            });
        }
    }

    /**
     * Listar permisos de un rol
     */
    @GetMapping("/{idRol}")
    public List<Permiso> listar(@PathVariable Integer idRol, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_VIEW");

        return permisoRepository.findByIdRol(idRol);
    }
}
