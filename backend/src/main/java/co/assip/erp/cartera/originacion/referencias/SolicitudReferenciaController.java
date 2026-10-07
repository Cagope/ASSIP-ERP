package co.assip.erp.cartera.referencias;

import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaListadoDTO;
import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaParticipanteDTO;
import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaPersonalDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/referencias-personales")
public class SolicitudReferenciaController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_ORIGINACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudReferenciaService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // 1. LISTAR SOLICITUDES
    // =========================================================

    @GetMapping
    public List<SolicitudReferenciaListadoDTO> listar(
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
            String estadoReferencias
    ) {

        validarConsulta();

        return service.listar(
                idAgencia,
                numeroSolicitud,
                documento,
                nombreSolicitante,
                idAsesor,
                idSolicitudProceso,
                estadoReferencias
        );
    }

    // =========================================================
    // 2. CONSULTAR SOLICITUD
    // =========================================================

    @GetMapping("/{idSolicitudCredito}")
    public SolicitudReferenciaListadoDTO buscarPorSolicitud(
            @PathVariable
            Integer idSolicitudCredito
    ) {

        validarConsulta();

        return service.buscarPorSolicitud(
                idSolicitudCredito
        );
    }

    // =========================================================
    // 3. LISTAR PARTICIPANTES
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/participantes")
    public List<SolicitudReferenciaParticipanteDTO>
    listarParticipantes(
            @PathVariable
            Integer idSolicitudCredito
    ) {

        validarConsulta();

        return service.listarParticipantes(
                idSolicitudCredito
        );
    }

    // =========================================================
    // 4. CONSULTAR PARTICIPANTE
    // =========================================================

    @GetMapping(
            "/{idSolicitudCredito}/participantes/{idSolicitudDeudor}"
    )
    public SolicitudReferenciaParticipanteDTO buscarParticipante(
            @PathVariable
            Integer idSolicitudCredito,

            @PathVariable
            Integer idSolicitudDeudor
    ) {

        validarConsulta();

        return service.buscarParticipante(
                idSolicitudCredito,
                idSolicitudDeudor
        );
    }

    // =========================================================
    // 5. LISTAR REFERENCIAS DE UNA SOLICITUD
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/referencias")
    public List<SolicitudReferenciaPersonalDTO>
    listarReferenciasPorSolicitud(
            @PathVariable
            Integer idSolicitudCredito
    ) {

        validarConsulta();

        return service.listarReferenciasPorSolicitud(
                idSolicitudCredito
        );
    }

    // =========================================================
    // 6. LISTAR REFERENCIAS DE UN PARTICIPANTE
    // =========================================================

    @GetMapping(
            "/{idSolicitudCredito}/participantes/{idSolicitudDeudor}/referencias"
    )
    public List<SolicitudReferenciaPersonalDTO>
    listarReferenciasPorParticipante(
            @PathVariable
            Integer idSolicitudCredito,

            @PathVariable
            Integer idSolicitudDeudor
    ) {

        validarConsulta();

        return service.listarReferenciasPorParticipante(
                idSolicitudCredito,
                idSolicitudDeudor
        );
    }

    // =========================================================
    // 7. CONSULTAR UNA REFERENCIA PERSONAL
    // =========================================================

    @GetMapping("/referencias/{idSolicitudReferenciaPersonal}")
    public SolicitudReferenciaPersonalDTO buscarReferenciaPorId(
            @PathVariable
            Long idSolicitudReferenciaPersonal
    ) {

        validarConsulta();

        return service.buscarReferenciaPorId(
                idSolicitudReferenciaPersonal
        );
    }

    // =========================================================
    // 8. INICIAR GESTIÓN DE REFERENCIAS
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/iniciar")
    public Long iniciarGestion(
            @PathVariable
            Integer idSolicitudCredito
    ) {

        validarEdicion();

        return service.iniciarGestion(
                idSolicitudCredito
        );
    }

    // =========================================================
    // 9. REGISTRAR REFERENCIA PERSONAL
    // =========================================================

    @PostMapping(
            "/{idSolicitudCredito}/participantes/{idSolicitudDeudor}/referencias"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudReferenciaPersonalDTO crearReferencia(
            @PathVariable
            Integer idSolicitudCredito,

            @PathVariable
            Integer idSolicitudDeudor,

            @RequestBody
            SolicitudReferenciaPersonalDTO dto
    ) {

        validarEdicion();

        dto.setIdSolicitudCredito(
                idSolicitudCredito
        );

        dto.setIdSolicitudDeudor(
                idSolicitudDeudor
        );

        return service.crearReferencia(
                dto
        );
    }

    // =========================================================
    // 10. ACTUALIZAR DATOS DE CONTACTO
    // =========================================================

    @PutMapping(
            "/referencias/{idSolicitudReferenciaPersonal}"
    )
    public SolicitudReferenciaPersonalDTO actualizarReferencia(
            @PathVariable
            Long idSolicitudReferenciaPersonal,

            @RequestBody
            SolicitudReferenciaPersonalDTO dto
    ) {

        validarEdicion();

        return service.actualizarReferencia(
                idSolicitudReferenciaPersonal,
                dto
        );
    }

    // =========================================================
    // 11. REGISTRAR ENTREVISTA
    // =========================================================

    @PutMapping(
            "/referencias/{idSolicitudReferenciaPersonal}/entrevista"
    )
    public SolicitudReferenciaPersonalDTO registrarEntrevista(
            @PathVariable
            Long idSolicitudReferenciaPersonal,

            @RequestBody
            EntrevistaReferenciaRequest request
    ) {

        validarEdicion();

        return service.registrarEntrevista(
                idSolicitudReferenciaPersonal,
                request.medioEntrevista(),
                request.contactoEstablecido(),
                request.conceptoReferencia()
        );
    }

    // =========================================================
    // 12. DESACTIVAR REFERENCIA PERSONAL
    // =========================================================

    @DeleteMapping(
            "/referencias/{idSolicitudReferenciaPersonal}"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivarReferencia(
            @PathVariable
            Long idSolicitudReferenciaPersonal
    ) {

        validarEdicion();

        service.desactivarReferencia(
                idSolicitudReferenciaPersonal
        );
    }

    // =========================================================
    // 13. CERRAR GESTIÓN DE REFERENCIAS
    // =========================================================

    @PostMapping("/{idSolicitudCredito}/cerrar")
    public SolicitudReferenciaListadoDTO cerrarGestion(
            @PathVariable
            Integer idSolicitudCredito,

            @RequestBody
            CierreReferenciaRequest request
    ) {

        validarEdicion();

        return service.cerrarGestion(
                idSolicitudCredito,
                request.tipoCierre(),
                request.observacionCierre()
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

    // =========================================================
    // REQUEST - ENTREVISTA
    // =========================================================

    public record EntrevistaReferenciaRequest(
            String medioEntrevista,
            Boolean contactoEstablecido,
            String conceptoReferencia
    ) {
    }

    // =========================================================
    // REQUEST - CIERRE
    // =========================================================

    public record CierreReferenciaRequest(
            String tipoCierre,
            String observacionCierre
    ) {
    }
}