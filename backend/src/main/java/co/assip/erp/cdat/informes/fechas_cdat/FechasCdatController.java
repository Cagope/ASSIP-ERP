package co.assip.erp.cdat.informes.fechas_cdat;

import co.assip.erp.cdat.informes.fechas_cdat.dto.FechasCdatRequestDTO;
import co.assip.erp.cdat.informes.fechas_cdat.dto.FechasCdatResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cdat/informes/fechas")
@RequiredArgsConstructor
public class FechasCdatController {

    private static final String PERMISO =
            "CDAT_INFORMES_VIEW";

    private final FechasCdatService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping("/consultar")
    public FechasCdatResponseDTO consultar(
            @RequestBody FechasCdatRequestDTO request
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
                PERMISO
        );
    }
}