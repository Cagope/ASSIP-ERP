package co.assip.erp.activosfijos.activos;

import co.assip.erp.activosfijos.activos.dto.ActivoFijoFormDTO;
import co.assip.erp.activosfijos.activos.dto.ActivoFijoListDTO;
import co.assip.erp.activosfijos.activos.dto.ActivoFijoSaveDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activos-fijos/activos")
public class ActivoFijoController {

    private static final String PERMISO_VIEW =
            "ACTIVOS_FIJOS_VIEW";

    private static final String PERMISO_EDIT =
            "ACTIVOS_FIJOS_EDIT";

    private final ActivoFijoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // 1. LISTADO
    // =========================================================
    @GetMapping
    public List<ActivoFijoListDTO> listar() {

        validarAcceso(PERMISO_VIEW);

        return service.listar();
    }

    // =========================================================
    // 2. FORMULARIO (detalle completo)
    // =========================================================
    @GetMapping("/{id}/form")
    public ActivoFijoFormDTO obtenerFormulario(
            @PathVariable Long id
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.obtenerFormulario(id);
    }

    // =========================================================
    // 3. CREAR
    // =========================================================
    @PostMapping
    public void crear(
            @RequestBody ActivoFijoSaveDTO dto
    ) {

        validarAcceso(PERMISO_EDIT);

        service.crear(dto);
    }

    // =========================================================
    // 4. ACTUALIZAR
    // =========================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Long id,
            @RequestBody ActivoFijoSaveDTO dto
    ) {

        validarAcceso(PERMISO_EDIT);

        service.actualizar(id, dto);
    }

    // =========================================================
    // 5. ELIMINAR
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