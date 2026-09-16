package co.assip.erp.cdat.analisis.tasascondiciones;

import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatCondicionDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatCorteDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatDetalleDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatRangoSaldoDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatResumenDTO;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cdat/analisis/tasas-condiciones")
public class AnalisisTasasCdatController {

    private final AnalisisTasasCdatService service;

    public AnalisisTasasCdatController(
            AnalisisTasasCdatService service
    ) {
        this.service = service;
    }


    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    @GetMapping("/cortes")
    public ResponseEntity<List<AnalisisTasasCdatCorteDTO>> listarCortes() {

        return ResponseEntity.ok(
                service.listarCortes()
        );
    }


    // =========================================================
    // RESUMEN
    // =========================================================

    @GetMapping("/resumen")
    public ResponseEntity<AnalisisTasasCdatResumenDTO> obtenerResumen(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer plazoMeses,

            @RequestParam(required = false)
            String amortizacion,

            @RequestParam(required = false)
            String rangoSaldo
    ) {

        return ResponseEntity.ok(
                service.obtenerResumen(
                        fechaCorte,
                        idAgencia,
                        plazoMeses,
                        amortizacion,
                        rangoSaldo
                )
        );
    }


    // =========================================================
    // CONDICIONES DE CAPTACIÓN
    // =========================================================

    @GetMapping("/condiciones")
    public ResponseEntity<List<AnalisisTasasCdatCondicionDTO>> obtenerCondiciones(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer plazoMeses,

            @RequestParam(required = false)
            String amortizacion,

            @RequestParam(required = false)
            String rangoSaldo
    ) {

        return ResponseEntity.ok(
                service.obtenerCondiciones(
                        fechaCorte,
                        idAgencia,
                        plazoMeses,
                        amortizacion,
                        rangoSaldo
                )
        );
    }


    // =========================================================
    // RANGOS DE SALDO
    // =========================================================

    @GetMapping("/rangos-saldo")
    public ResponseEntity<List<AnalisisTasasCdatRangoSaldoDTO>> obtenerRangosSaldo(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer plazoMeses,

            @RequestParam(required = false)
            String amortizacion,

            @RequestParam(required = false)
            String rangoSaldo
    ) {

        return ResponseEntity.ok(
                service.obtenerRangosSaldo(
                        fechaCorte,
                        idAgencia,
                        plazoMeses,
                        amortizacion,
                        rangoSaldo
                )
        );
    }


    // =========================================================
    // DETALLE AUDITABLE
    // =========================================================

    @GetMapping("/detalle")
    public ResponseEntity<List<AnalisisTasasCdatDetalleDTO>> obtenerDetalle(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer plazoMeses,

            @RequestParam(required = false)
            String amortizacion,

            @RequestParam(required = false)
            String rangoSaldo
    ) {

        return ResponseEntity.ok(
                service.obtenerDetalle(
                        fechaCorte,
                        idAgencia,
                        plazoMeses,
                        amortizacion,
                        rangoSaldo
                )
        );
    }
}