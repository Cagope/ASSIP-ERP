package co.assip.erp.cartera.originacion.solicitudes.list;

import co.assip.erp.cartera.originacion.solicitudes.list.dto.SolicitudListadoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/solicitudes/list")
public class SolicitudListadoController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudListadoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTAR SOLICITUDES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<SolicitudListadoDTO>> listar(
            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            String numeroSolicitud,

            @RequestParam(required = false)
            String documento,

            @RequestParam(required = false)
            String nombreSolicitante,

            @RequestParam(required = false)
            Integer idAsesor,

            @RequestParam(required = false)
            Integer idSolicitudProceso,

            @RequestParam(required = false)
            Integer idSolicitudResultado
    ) {

        validarConsulta();

        List<SolicitudListadoDTO> solicitudes =
                service.listar(
                        idAgencia,
                        numeroSolicitud,
                        documento,
                        nombreSolicitante,
                        idAsesor,
                        idSolicitudProceso,
                        idSolicitudResultado
                );

        return ResponseEntity.ok(solicitudes);
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