package co.assip.erp.nomina.vacaciones;

import co.assip.erp.nomina.vacaciones.dto.VacacionPreviewDTO;
import co.assip.erp.nomina.vacaciones.dto.VacacionPreviewRequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nomina/vacaciones")
@RequiredArgsConstructor
public class VacacionesController {

    private final VacacionesService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // ============================================================
    // PREVISUALIZAR VACACIONES
    // ============================================================
    @PostMapping("/preview")
    public VacacionPreviewDTO preview(
            @RequestBody VacacionPreviewRequestDTO request
    ) {

        validarAcceso("NOMINA_VIEW");

        return service.preview(request);
    }

    // ============================================================
    // AUTORIZACIÓN
    // ============================================================
    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}