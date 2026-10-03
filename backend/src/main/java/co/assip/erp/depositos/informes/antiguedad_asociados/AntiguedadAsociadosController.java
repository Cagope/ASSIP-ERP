package co.assip.erp.depositos.informes.antiguedad_asociados;

import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosRequestDTO;
import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/antiguedad-asociados")
@RequiredArgsConstructor
public class AntiguedadAsociadosController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final AntiguedadAsociadosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public AntiguedadAsociadosResponseDTO consultar(
            @RequestBody AntiguedadAsociadosRequestDTO request
    ) {

        validarAcceso();

        return service.consultar(request);
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