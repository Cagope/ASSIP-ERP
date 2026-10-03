package co.assip.erp.depositos.informes.movimientos_por_meses;

import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesRequestDTO;
import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/movimientos-por-meses")
@RequiredArgsConstructor
public class MovimientosPorMesesController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final MovimientosPorMesesService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public MovimientosPorMesesResponseDTO consultar(
            @RequestBody MovimientosPorMesesRequestDTO request
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