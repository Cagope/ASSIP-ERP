package co.assip.erp.cartera.analisis.dashboard_cartera;

import co.assip.erp.cartera.analisis.dashboard_cartera.dto.DashboardCarteraRequestDTO;
import co.assip.erp.cartera.analisis.dashboard_cartera.dto.DashboardCarteraResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cartera/analisis/dashboard-cartera")
@RequiredArgsConstructor
public class DashboardCarteraController {

    private static final String PERMISO_VIEW =
            "CARTERA_DASHBOARD_VIEW";

    private final DashboardCarteraService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // Consulta principal
    // =========================================================

    @PostMapping("/consultar")
    public ResponseEntity<DashboardCarteraResponseDTO> consultar(
            @RequestBody(required = false)
            DashboardCarteraRequestDTO request
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.consultar(request)
        );
    }

    // =========================================================
    // Seguridad
    // =========================================================

    private void validarConsulta() {
        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_VIEW
        );
    }

    private Usuario usuarioActual() {
        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        return usuarioActual;
    }
}