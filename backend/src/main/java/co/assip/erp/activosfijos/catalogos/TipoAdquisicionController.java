package co.assip.erp.activosfijos.catalogos;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activos-fijos/tipos-adquisicion")
@RequiredArgsConstructor
public class TipoAdquisicionController {

    private static final String PERMISO =
            "ACTIVOS_FIJOS_VIEW";

    private final TipoAdquisicionRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping
    public List<TipoAdquisicionDTO> listar() {

        validarAcceso();

        return repository.listar();
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================
    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO
        );
    }
}