package co.assip.erp.depositos.informes.cuentasnr;

import co.assip.erp.depositos.informes.cuentasnr.dto.CuentasNRRequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/cuentas-nr")
public class CuentasNRController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final CuentasNRService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public CuentasNRController(
            CuentasNRService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    @PostMapping
    public Object resolver(
            @RequestBody CuentasNRRequestDTO request
    ) {

        validarAcceso();

        return service.resolver(request);
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