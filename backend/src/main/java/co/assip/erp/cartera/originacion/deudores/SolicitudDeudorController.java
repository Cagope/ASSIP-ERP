package co.assip.erp.cartera.originacion.deudores;

import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorAgregarRequestDTO;
import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorDTO;
import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorDetalleDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/deudores")
public class SolicitudDeudorController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_ORIGINACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudDeudorService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTAR DEUDORES DE UNA SOLICITUD
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}")
    public ResponseEntity<List<SolicitudDeudorDTO>> listarPorSolicitud(
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
    // CONSULTAR DEUDOR
    // =========================================================

    @GetMapping("/{idSolicitudDeudor}")
    public ResponseEntity<SolicitudDeudorDetalleDTO> buscarPorId(
            @PathVariable Integer idSolicitudDeudor
    ) {

        validarConsulta();

        return service.buscarPorId(
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
    // AGREGAR CODEUDOR
    // =========================================================

    @PostMapping("/agregar")
    public ResponseEntity<SolicitudDeudorDetalleDTO> agregarCodeudor(
            @RequestBody SolicitudDeudorAgregarRequestDTO request
    ) {

        validarEdicion();

        return ResponseEntity.ok(
                service.agregarCodeudor(
                        request
                )
        );
    }

    // =========================================================
    // RETIRAR CODEUDOR
    // =========================================================

    @DeleteMapping("/{idSolicitudDeudor}")
    public ResponseEntity<Void> retirarCodeudor(
            @PathVariable Integer idSolicitudDeudor
    ) {

        validarEdicion();

        service.retirarCodeudor(
                idSolicitudDeudor
        );

        return ResponseEntity.noContent().build();
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