package co.assip.erp.depositos.informes.asociados_sin_movimientos;

import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosRequestDTO;
import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/asociados_sin_movimientos")
@RequiredArgsConstructor
public class AsociadosSinMovimientosController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final AsociadosSinMovimientosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public AsociadosSinMovimientosResponseDTO consultar(
            @RequestBody AsociadosSinMovimientosRequestDTO request
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