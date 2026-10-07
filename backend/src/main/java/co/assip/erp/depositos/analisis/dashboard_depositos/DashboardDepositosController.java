package co.assip.erp.depositos.analisis.dashboard_depositos;

import co.assip.erp.depositos.analisis.dashboard_depositos.dto.DashboardDepositosRequestDTO;
import co.assip.erp.depositos.analisis.dashboard_depositos.dto.DashboardDepositosResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/depositos/analisis/dashboard-depositos")
@RequiredArgsConstructor
public class DashboardDepositosController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_DASHBOARD_VIEW";

    private final DashboardDepositosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    @PostMapping
    public DashboardDepositosResponseDTO consultar(
            @RequestBody DashboardDepositosRequestDTO request
    ) {

        validarConsulta();

        return service.consultar(request);
    }

    // =========================================================
    // SEGURIDAD
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