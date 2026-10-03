package co.assip.erp.depositos.informes.inconsistencias;

import co.assip.erp.depositos.informes.inconsistencias.dto.InconsistenciasRequest;
import co.assip.erp.depositos.informes.inconsistencias.dto.InconsistenciaSaldoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/informes/inconsistencias")
public class InconsistenciasController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final InconsistenciasService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public InconsistenciasController(
            InconsistenciasService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    @PostMapping
    public List<InconsistenciaSaldoDTO> consultar(
            @RequestBody InconsistenciasRequest request
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