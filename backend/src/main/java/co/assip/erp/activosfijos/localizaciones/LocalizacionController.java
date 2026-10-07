package co.assip.erp.activosfijos.localizaciones;

import co.assip.erp.activosfijos.localizaciones.dto.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activos-fijos/localizaciones")
public class LocalizacionController {

    private static final String PERMISO_VIEW =
            "ACTIVOS_FIJOS_VIEW";

    private static final String PERMISO_EDIT =
            "ACTIVOS_FIJOS_EDIT";

    private final LocalizacionService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTADO
    // =========================================================
    @GetMapping
    public List<LocalizacionListDTO> listar() {

        validarAcceso(PERMISO_VIEW);

        return service.listar();
    }

    // =========================================================
    // FORMULARIO
    // =========================================================
    @GetMapping("/{id}")
    public LocalizacionFormDTO obtener(
            @PathVariable Long id
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.obtener(id);
    }

    // =========================================================
    // CREAR
    // =========================================================
    @PostMapping
    public void crear(
            @RequestBody LocalizacionSaveDTO dto
    ) {

        validarAcceso(PERMISO_EDIT);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.crear(dto, idUsuario);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Long id,
            @RequestBody LocalizacionSaveDTO dto
    ) {

        validarAcceso(PERMISO_EDIT);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(id, dto, idUsuario);
    }

    // =========================================================
    // ELIMINAR
    // =========================================================
    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id
    ) {

        validarAcceso(PERMISO_EDIT);

        service.eliminar(id);
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================
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