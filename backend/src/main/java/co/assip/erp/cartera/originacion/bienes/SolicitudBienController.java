package co.assip.erp.cartera.originacion.bienes;

import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienCreditoRespaldadoDTO;
import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienDTO;
import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienSeleccionRequestDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/bienes")
public class SolicitudBienController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_ORIGINACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudBienService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTAR BIENES DE LA SOLICITUD
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}")
    public ResponseEntity<List<SolicitudBienDTO>> listarPorSolicitud(
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
    // LISTAR BIENES DE UN DEUDOR
    // =========================================================

    @GetMapping("/deudor/{idSolicitudDeudor}")
    public ResponseEntity<List<SolicitudBienDTO>> listarPorDeudor(
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
    // CRÉDITOS ACTUALES RESPALDADOS POR UN BIEN
    // =========================================================

    @GetMapping("/{idBien}/creditos-respaldados")
    public ResponseEntity<List<SolicitudBienCreditoRespaldadoDTO>>
    listarCreditosRespaldados(
            @PathVariable Long idBien
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarCreditosRespaldados(
                        idBien
                )
        );
    }

    // =========================================================
    // SELECCIONAR / RETIRAR BIEN
    // =========================================================

    @PutMapping("/seleccion")
    public ResponseEntity<SolicitudBienDTO> seleccionar(
            @RequestBody SolicitudBienSeleccionRequestDTO request
    ) {

        validarEdicion();

        return ResponseEntity.ok(
                service.seleccionar(
                        request
                )
        );
    }

    // =========================================================
    // VALIDAR PASO BIENES
    // =========================================================
    //
    // Para garantía REAL exige al menos un bien seleccionado
    // entre los deudores activos.
    //
    // Para garantía PERSONAL permite continuar sin bienes.
    //
    // =========================================================

    @GetMapping("/solicitud/{idSolicitudCredito}/validar")
    public ResponseEntity<Map<String, Boolean>> validarParaContinuar(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        service.validarParaContinuar(
                idSolicitudCredito
        );

        return ResponseEntity.ok(
                Map.of(
                        "puedeContinuar",
                        true
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