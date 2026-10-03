package co.assip.erp.depositos.informes.promedios;

import co.assip.erp.depositos.informes.promedios.dto.PromediosRequestDTO;
import co.assip.erp.depositos.informes.promedios.dto.PromediosResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/promedios")
@RequiredArgsConstructor
public class PromediosController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final PromediosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public PromediosResponseDTO consultar(
            @RequestBody PromediosRequestDTO request
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