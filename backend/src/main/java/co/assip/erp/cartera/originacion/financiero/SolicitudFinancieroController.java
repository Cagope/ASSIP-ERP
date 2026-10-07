package co.assip.erp.cartera.originacion.financiero;

import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroDTO;
import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroDetalleDTO;
import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroGuardarRequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/financiero")
public class SolicitudFinancieroController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_ORIGINACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudFinancieroService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTAR FINANCIEROS DE LA SOLICITUD
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}")
    public ResponseEntity<List<SolicitudFinancieroDTO>> listarPorSolicitud(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarPorSolicitud(
                        idSolicitudCredito
                )
        );
    }

    // =========================================================
    // CONSULTAR FINANCIERO DE UN DEUDOR
    // =========================================================

    @GetMapping("/deudor/{idSolicitudDeudor}")
    public ResponseEntity<SolicitudFinancieroDetalleDTO> buscarPorDeudor(
            @PathVariable Integer idSolicitudDeudor
    ) {

        validarConsulta();

        return service.buscarPorDeudor(
                        idSolicitudDeudor
                )
                .map(ResponseEntity::ok)
                .orElseGet(
                        () ->
                                ResponseEntity
                                        .notFound()
                                        .build()
                );
    }

    // =========================================================
    // GUARDAR / REFRESCAR FOTOGRAFÍA FINANCIERA
    // =========================================================

    @PostMapping("/guardar")
    public ResponseEntity<SolicitudFinancieroDetalleDTO> guardar(
            @RequestBody SolicitudFinancieroGuardarRequestDTO request
    ) {

        validarEdicion();

        return ResponseEntity.ok(
                service.guardar(
                        request
                )
        );
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

    private void validarEdicion() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_EDIT
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