package co.assip.erp.depositos.informes.rangos;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 RangosController
 * ------------------------------------------------------------
 * Endpoint único para generar informes estadísticos de cuentas
 * por rangos:
 *
 * - EDAD
 * - SALDO
 * - ANTIGUEDAD
 */
@RestController
@RequestMapping("/depositos/informes/rangos")
public class RangosController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final RangosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public RangosController(
            RangosService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    @PostMapping
    public List<RangosItemDTO> consultar(
            @RequestBody RangosRequestDTO request
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