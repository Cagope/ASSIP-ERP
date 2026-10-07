package co.assip.erp.cartera.originacion.solicitudes;

import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCrearRetomarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCrearRetomarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoCrearRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoDetalleDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoResumenDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudFinalizarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudFinalizarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudEnviarAprobacionResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudEnviarAprobacionRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudEnteAprobadorPreviewRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudEnteAprobadorPreviewDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudValidacionAprobacionDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/solicitudes")
public class SolicitudCreditoController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_ORIGINACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudCreditoService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CREAR SOLICITUD + GUARDAR DATOS DEL CRÉDITO
    // =========================================================

    @PostMapping("/crear")
    public ResponseEntity<SolicitudCreditoGuardarResponseDTO> crear(
            @RequestBody SolicitudCreditoCrearRequestDTO request
    ) {

        validarEdicion();

        return ResponseEntity.ok(
                service.crearSolicitud(request)
        );
    }

    // =========================================================
    // CREAR / RETOMAR SOLICITUD
    // =========================================================

    @PostMapping("/crear-retomar")
    public ResponseEntity<SolicitudCrearRetomarResponseDTO> crearRetomar(
            @RequestBody SolicitudCrearRetomarRequestDTO request
    ) {

        validarEdicion();

        return ResponseEntity.ok(
                service.crearRetomar(request)
        );
    }

    // =========================================================
    // GUARDAR DATOS DEL CRÉDITO
    // =========================================================

    @PostMapping("/guardar-credito")
    public ResponseEntity<SolicitudCreditoGuardarResponseDTO> guardarCredito(
            @RequestBody SolicitudCreditoGuardarRequestDTO request
    ) {

        validarEdicion();

        return ResponseEntity.ok(
                service.guardarCredito(request)
        );
    }

    // =========================================================
    // PREVISUALIZAR ENTE APROBADOR
    // =========================================================

    @PostMapping("/preview-ente-aprobador")
    public ResponseEntity<SolicitudEnteAprobadorPreviewDTO> previsualizarEnteAprobador(
            @RequestBody SolicitudEnteAprobadorPreviewRequestDTO request
    ) {

        validarEdicion();

        return ResponseEntity.ok(
                service.previsualizarEnteAprobador(request)
        );
    }

    // =========================================================
    // CONSULTAR TASA PARA SIMULACIÓN
    // =========================================================

    @GetMapping("/tasa-simulacion")
    public ResponseEntity<BigDecimal> consultarTasaSimulacion(
            @RequestParam Integer idLineaCredito,
            @RequestParam String codigoGarantiaCredito,
            @RequestParam Integer amortizacionCapital,
            @RequestParam Integer plazoSolicitado
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.consultarTasaColocacionSimulacion(
                        idLineaCredito,
                        codigoGarantiaCredito,
                        amortizacionCapital,
                        plazoSolicitado
                )
        );
    }

    // =========================================================
    // LISTAR SOLICITUDES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<SolicitudCreditoResumenDTO>> listar() {

        validarConsulta();

        return ResponseEntity.ok(
                service.listar()
        );
    }

    // =========================================================
    // CONSULTAR VIGENCIA DE HOJA DE VIDA
    // =========================================================

    @GetMapping("/vigencia-hoja-vida")
    public ResponseEntity<SolicitudCreditoRepository.VigenciaHojaVida>
    consultarVigenciaHojaVida(
            @RequestParam Integer idDatosPersonal,
            @RequestParam Integer idAgencia
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.consultarVigenciaHojaVida(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }

    // =========================================================
    // CONSULTAR SOLICITUD POR ID
    // =========================================================

    @GetMapping("/{idSolicitudCredito}")
    public ResponseEntity<SolicitudCreditoDetalleDTO> buscarPorId(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        return service.buscarPorId(idSolicitudCredito)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    // =========================================================
    // FINALIZAR SOLICITUD
    // =========================================================

    @PutMapping("/finalizar")
    public ResponseEntity<SolicitudFinalizarResponseDTO> finalizarSolicitud(
            @RequestBody SolicitudFinalizarRequestDTO request
    ) {

        validarEdicion();

        return ResponseEntity.ok(
                service.finalizarSolicitud(request)
        );
    }

    // =========================================================
    // VALIDAR PARA APROBACIÓN
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/validar-aprobacion")
    public ResponseEntity<SolicitudValidacionAprobacionDTO> validarParaAprobacion(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                service.validarParaAprobacion(
                        idSolicitudCredito
                )
        );
    }

    // =========================================================
    // ENVIAR A APROBACIÓN
    // =========================================================

    @PutMapping("/{idSolicitudCredito}/enviar-aprobacion")
    public ResponseEntity<SolicitudEnviarAprobacionResponseDTO> enviarAprobacion(
            @PathVariable Integer idSolicitudCredito,
            @RequestBody SolicitudEnviarAprobacionRequestDTO request
    ) {

        validarEdicion();

        return ResponseEntity.ok(
                service.enviarAprobacion(
                        idSolicitudCredito,
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