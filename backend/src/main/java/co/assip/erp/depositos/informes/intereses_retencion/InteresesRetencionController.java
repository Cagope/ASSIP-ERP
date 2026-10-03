package co.assip.erp.depositos.informes.intereses_retencion;

import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionRequestDTO;
import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/intereses-retencion")
@RequiredArgsConstructor
public class InteresesRetencionController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final InteresesRetencionService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public InteresesRetencionResponseDTO consultar(
            @RequestBody InteresesRetencionRequestDTO request
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