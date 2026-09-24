package co.assip.erp.cartera.referencias;

import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaListadoDTO;
import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaParticipanteDTO;
import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaPersonalDTO;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cartera/referencias-personales")
public class SolicitudReferenciaController {

    private final SolicitudReferenciaService service;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SolicitudReferenciaController(
            SolicitudReferenciaService service
    ) {
        this.service = service;
    }

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

        return service.cerrarGestion(
                idSolicitudCredito,
                request.tipoCierre(),
                request.observacionCierre()
        );
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