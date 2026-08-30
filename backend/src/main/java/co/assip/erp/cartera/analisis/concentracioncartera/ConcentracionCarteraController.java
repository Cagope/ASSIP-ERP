package co.assip.erp.cartera.analisis.concentracioncartera;

import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionCarteraControlDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionCarteraResumenDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionDetalleCreditoDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionDetalleDeudorDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionRankingDeudorDTO;
import co.assip.erp.cartera.analisis.concentracioncartera.dto.ConcentracionSegmentoDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/analisis/concentracion-cartera")
public class ConcentracionCarteraController {

    private final ConcentracionCarteraService service;

    public ConcentracionCarteraController(ConcentracionCarteraService service) {
        this.service = service;
    }

    @GetMapping("/control")
    public ConcentracionCarteraControlDTO control() {
        return service.control();
    }

    @GetMapping("/cortes")
    public List<LocalDate> cortes() {
        return service.cortes();
    }

    @GetMapping("/resumen")
    public ConcentracionCarteraResumenDTO resumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
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

    @GetMapping("/deudores-exposicion")
    public List<ConcentracionRankingDeudorDTO> deudoresExposicion(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(defaultValue = "50") Integer limite,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
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

    @GetMapping("/deudores-deterioro")
    public List<ConcentracionRankingDeudorDTO> deudoresDeterioro(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(defaultValue = "50") Integer limite,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
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

    @GetMapping("/lineas")
    public List<ConcentracionSegmentoDTO> lineas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
        return service.lineas(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino);
    }

    @GetMapping("/agencias")
    public List<ConcentracionSegmentoDTO> agencias(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
        return service.agencias(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino);
    }

    @GetMapping("/garantias")
    public List<ConcentracionSegmentoDTO> garantias(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
        return service.garantias(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino);
    }

    @GetMapping("/clasificaciones")
    public List<ConcentracionSegmentoDTO> clasificaciones(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
        return service.clasificaciones(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino);
    }

    @GetMapping("/edades-contables")
    public List<ConcentracionSegmentoDTO> edadesContables(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
        return service.edadesContables(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino);
    }

    @GetMapping("/destinos")
    public List<ConcentracionSegmentoDTO> destinos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
        return service.destinos(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino);
    }

    @GetMapping("/detalle-deudores")
    public List<ConcentracionDetalleDeudorDTO> detalleDeudores(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
        return service.detalleDeudores(fechaCorte, idAgencia, idLineaCredito, codigoGarantia,
                codigoClasificacion, edadContable, codigoDestino);
    }

    @GetMapping("/detalle-creditos")
    public List<ConcentracionDetalleCreditoDTO> detalleCreditos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) Long idDatosPersonal,
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String codigoGarantia,
            @RequestParam(required = false) String codigoClasificacion,
            @RequestParam(required = false) String edadContable,
            @RequestParam(required = false) String codigoDestino
    ) {
        return service.detalleCreditos(fechaCorte, idDatosPersonal, idAgencia, idLineaCredito,
                codigoGarantia, codigoClasificacion, edadContable, codigoDestino);
    }
}
