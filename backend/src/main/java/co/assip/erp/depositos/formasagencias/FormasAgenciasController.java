package co.assip.erp.depositos.formasagencias;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/formasagencias")
@RequiredArgsConstructor
public class FormasAgenciasController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_FORMAS_AHORRO_VIEW";

    private final FormasAgenciasService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @GetMapping("/{idAgencia}")
    public List<FormaAgenciaDTO> listarPorAgencia(
            @PathVariable Integer idAgencia
    ) {

        validarAcceso();

        return service.listarPorAgencia(idAgencia);
    }

    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_VIEW
        );
    }
}