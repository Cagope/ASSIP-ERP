package co.assip.erp.depositos.informes.estadisticos_asociados;

import co.assip.erp.depositos.informes.estadisticos_asociados.dto.EstadisticosAsociadosRequestDTO;
import co.assip.erp.depositos.informes.estadisticos_asociados.dto.EstadisticosAsociadosResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/estadisticos-asociados")
@RequiredArgsConstructor
public class EstadisticosAsociadosController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final EstadisticosAsociadosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public EstadisticosAsociadosResponseDTO consultar(
            @RequestBody EstadisticosAsociadosRequestDTO request
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