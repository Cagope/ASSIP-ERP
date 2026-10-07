package co.assip.erp.cdat.analisis.dashboard_cdat;

import co.assip.erp.cdat.analisis.dashboard_cdat.dto.DashboardCdatResponseDTO;
import co.assip.erp.cdat.analisis.dashboard_cdat.dto.DashboardCdatVencimientoDetalleDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cdat/analisis/dashboard-cdat")
@RequiredArgsConstructor
public class DashboardCdatController {

    private static final String PERMISO_VIEW =
            "CDAT_DASHBOARD_VIEW";

    private final DashboardCdatService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // DASHBOARD ACTUAL
    // =========================================================

    @GetMapping("/consultar")
    public DashboardCdatResponseDTO consultar() {

        validarConsulta();

        return service.consultar();
    }

    // =========================================================
    // DETALLE DE PRÓXIMOS VENCIMIENTOS
    // =========================================================

    @GetMapping("/vencimientos/detalle")
    public List<DashboardCdatVencimientoDetalleDTO>
    consultarVencimientosDetalle(
            @RequestParam String rango
    ) {

        validarConsulta();

        return service.consultarVencimientosDetalle(rango);
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