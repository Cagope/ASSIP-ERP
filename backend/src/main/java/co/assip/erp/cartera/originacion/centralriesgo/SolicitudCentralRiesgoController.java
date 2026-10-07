package co.assip.erp.cartera.originacion.centralriesgo;

import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoDTO;
import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoDetalleDTO;
import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoGuardarRequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/central-riesgo")
public class SolicitudCentralRiesgoController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_ORIGINACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudCentralRiesgoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}")
    public ResponseEntity<List<SolicitudCentralRiesgoDTO>> listarPorSolicitud(
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
    // LISTAR POR DEUDOR
    // =========================================================

    @GetMapping("/deudor/{idSolicitudDeudor}")
    public ResponseEntity<List<SolicitudCentralRiesgoDTO>> listarPorDeudor(
            @PathVariable Integer idSolicitudDeudor
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarPorDeudor(
                        idSolicitudDeudor
                )
        );
    }

    // =========================================================
    // CONSULTAR DETALLE
    // =========================================================

    @GetMapping("/{idSolicitudDeudorCentral}")
    public ResponseEntity<SolicitudCentralRiesgoDetalleDTO> buscarPorId(
            @PathVariable Integer idSolicitudDeudorCentral
    ) {

        validarConsulta();

        return service.buscarPorId(
                        idSolicitudDeudorCentral
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
    // GUARDAR / ACTUALIZAR
    // =========================================================

    @PostMapping("/guardar")
    public ResponseEntity<SolicitudCentralRiesgoDetalleDTO> guardar(
            @RequestBody SolicitudCentralRiesgoGuardarRequestDTO request
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