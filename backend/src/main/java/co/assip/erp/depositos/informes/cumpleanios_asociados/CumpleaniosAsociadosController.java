package co.assip.erp.depositos.informes.cumpleanios_asociados;

import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosRequestDTO;
import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/cumpleanios-asociados")
@RequiredArgsConstructor
public class CumpleaniosAsociadosController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final CumpleaniosAsociadosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public CumpleaniosAsociadosResponseDTO consultar(
            @RequestBody CumpleaniosAsociadosRequestDTO request
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