package co.assip.erp.cartera.originacion.analisis;

import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisComponenteDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisDeudorDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisPersistenciaDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisResultadoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/analisis")
public class SolicitudAnalisisController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    private static final String PERMISO_ANALISIS =
            "CARTERA_ORIGINACION_ANALISIS";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudAnalisisService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // RESULTADO ACTUAL DE LA SOLICITUD
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/resultado")
    public ResponseEntity<SolicitudAnalisisResultadoDTO> obtenerResultadoSolicitud(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        return service.obtenerResultadoSolicitud(
                        idSolicitudCredito
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
    // RESULTADOS POR DEUDOR
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/deudores")
    public ResponseEntity<List<SolicitudAnalisisDeudorDTO>> listarDeudores(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarDeudores(
                        idSolicitudCredito
                )
        );
    }

    // =========================================================
    // COMPONENTES
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/componentes")
    public ResponseEntity<List<SolicitudAnalisisComponenteDTO>> listarComponentes(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.listarComponentes(
                        idSolicitudCredito
                )
        );
    }

    // =========================================================
    // GUARDAR ANÁLISIS ACTUAL
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/persistir")
    public ResponseEntity<SolicitudAnalisisPersistenciaDTO> persistirAnalisis(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarAnalisis();

        return ResponseEntity.ok(
                service.persistirAnalisis(
                        idSolicitudCredito
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

    private void validarAnalisis() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_ANALISIS
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