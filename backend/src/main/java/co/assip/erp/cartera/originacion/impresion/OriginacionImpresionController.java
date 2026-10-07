package co.assip.erp.cartera.originacion.impresion;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/originacion/impresion")
public class OriginacionImpresionController {

    private static final String PERMISO_VIEW =
            "CARTERA_ORIGINACION_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final OriginacionExpedientePdfService pdfService;
    private final OriginacionPagareService pagareService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // EXPEDIENTE DE LA SOLICITUD
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/expediente/pdf")
    public ResponseEntity<byte[]> generarExpediente(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        return pdfService.generarPdfResponse(
                idSolicitudCredito
        );
    }

    // =========================================================
    // EXPEDIENTE HISTÓRICO DE UNA ACTUACIÓN
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/actuaciones/{idSolicitudAprobacion}/expediente/pdf")
    public ResponseEntity<byte[]> generarExpedienteActuacion(
            @PathVariable Integer idSolicitudCredito,
            @PathVariable Integer idSolicitudAprobacion
    ) {

        validarConsulta();

        return pdfService.generarPdfActuacionResponse(
                idSolicitudCredito,
                idSolicitudAprobacion
        );
    }

    // =========================================================
    // DATOS DEL PAGARÉ DILIGENCIADO PARA ANGULAR
    //
    // Devuelve:
    // - Identificación del crédito y pagaré.
    // - Condiciones financieras definitivas.
    // - Fechas del crédito constituido.
    // - Deudor principal y codeudores.
    //
    // No genera PDF ni modifica información del crédito.
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/pagare/diligenciado/datos")
    public ResponseEntity<OriginacionPagareService.PagareImpresion>
    consultarDatosPagareDiligenciado(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                pagareService.obtenerPagareDiligenciado(
                        idSolicitudCredito
                )
        );
    }

    // =========================================================
    // DATOS DEL PAGARÉ EN BLANCO Y CARTA DE INSTRUCCIONES
    //
    // Ambos documentos se generan desde Angular.
    //
    // Conserva:
    // - Número del pagaré.
    // - Identificación del crédito.
    // - Deudor principal.
    // - Codeudores.
    // - Datos de contacto de los firmantes.
    //
    // No genera PDF ni modifica información del crédito.
    // =========================================================

    @GetMapping("/{idSolicitudCredito}/pagare/en-blanco/datos")
    public ResponseEntity<OriginacionPagareService.PagareImpresion>
    consultarDatosPagareEnBlanco(
            @PathVariable Integer idSolicitudCredito
    ) {

        validarConsulta();

        return ResponseEntity.ok(
                pagareService.obtenerPagareEnBlanco(
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

    private Usuario usuarioActual() {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        return usuarioActual;
    }
}