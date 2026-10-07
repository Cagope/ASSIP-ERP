package co.assip.erp.cartera.analisis.concentracioncartera;

import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionCarteraControlDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionCarteraResumenDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionDetalleCreditoDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionDetalleDeudorDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionRankingDeudorDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionSegmentoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/analisis/concentracion-cartera")
@RequiredArgsConstructor
public class ConcentracionCarteraController {

    private static final String PERMISO_VIEW = "CARTERA_ANALISIS_VIEW";

    private final ConcentracionCarteraService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // CONTROL
    // =========================================================

    @GetMapping("/control")
    public ConcentracionCarteraControlDTO control() {

        validarAcceso();

        return service.control();
    }

    // =========================================================
    // CORTES
    // =========================================================

    @GetMapping("/cortes")
    public List<LocalDate> cortes() {

        validarAcceso();

        return service.cortes();
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    @GetMapping("/resumen")
    public ConcentracionCarteraResumenDTO resumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.resumen(
                fechaCorte,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // DEUDORES POR EXPOSICIÓN
    // =========================================================

    @GetMapping("/deudores-exposicion")
    public List<ConcentracionRankingDeudorDTO> deudoresExposicion(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(defaultValue = "50")
            Integer limite,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.deudoresExposicion(
                fechaCorte,
                limite,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // DEUDORES POR DETERIORO
    // =========================================================

    @GetMapping("/deudores-deterioro")
    public List<ConcentracionRankingDeudorDTO> deudoresDeterioro(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(defaultValue = "50")
            Integer limite,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.deudoresDeterioro(
                fechaCorte,
                limite,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // LÍNEAS
    // =========================================================

    @GetMapping("/lineas")
    public List<ConcentracionSegmentoDTO> lineas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.lineas(
                fechaCorte,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // AGENCIAS
    // =========================================================

    @GetMapping("/agencias")
    public List<ConcentracionSegmentoDTO> agencias(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.agencias(
                fechaCorte,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // GARANTÍAS
    // =========================================================

    @GetMapping("/garantias")
    public List<ConcentracionSegmentoDTO> garantias(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.garantias(
                fechaCorte,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // CLASIFICACIONES
    // =========================================================

    @GetMapping("/clasificaciones")
    public List<ConcentracionSegmentoDTO> clasificaciones(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.clasificaciones(
                fechaCorte,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // EDADES CONTABLES
    // =========================================================

    @GetMapping("/edades-contables")
    public List<ConcentracionSegmentoDTO> edadesContables(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.edadesContables(
                fechaCorte,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // DESTINOS
    // =========================================================

    @GetMapping("/destinos")
    public List<ConcentracionSegmentoDTO> destinos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.destinos(
                fechaCorte,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // DETALLE DEUDORES
    // =========================================================

    @GetMapping("/detalle-deudores")
    public List<ConcentracionDetalleDeudorDTO> detalleDeudores(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.detalleDeudores(
                fechaCorte,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // DETALLE CRÉDITOS
    // =========================================================

    @GetMapping("/detalle-creditos")
    public List<ConcentracionDetalleCreditoDTO> detalleCreditos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Long idDatosPersonal,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(required = false)
            String codigoGarantia,

            @RequestParam(required = false)
            String codigoClasificacion,

            @RequestParam(required = false)
            String edadContable,

            @RequestParam(required = false)
            String codigoDestino
    ) {

        validarAcceso();

        return service.detalleCreditos(
                fechaCorte,
                idDatosPersonal,
                idAgencia,
                idLineaCredito,
                codigoGarantia,
                codigoClasificacion,
                edadContable,
                codigoDestino
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_VIEW
        );
    }
}