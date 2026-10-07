package co.assip.erp.activosfijos.bloques;

import co.assip.erp.activosfijos.bloques.dto.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activos-fijos/bloques")
public class BloqueController {

    private static final String PERMISO_VIEW =
            "ACTIVOS_FIJOS_VIEW";

    private static final String PERMISO_EDIT =
            "ACTIVOS_FIJOS_EDIT";

    private final BloqueService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping
    public List<BloqueListDTO> listar() {

        validarAcceso(PERMISO_VIEW);

        return service.listar();
    }

    @GetMapping("/{id}")
    public BloqueFormDTO obtener(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.obtener(id);
    }

    @PostMapping
    public void crear(
            @RequestBody BloqueSaveDTO dto
    ) {

        validarAcceso(PERMISO_EDIT);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.crear(dto, idUsuario);
    }

    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Integer id,
            @RequestBody BloqueSaveDTO dto
    ) {

        validarAcceso(PERMISO_EDIT);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(id, dto, idUsuario);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Integer id
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