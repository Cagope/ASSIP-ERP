package co.assip.erp.cartera.analisis.moratemprana;

import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaCosechaDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaDetalleDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaPrimeraMoraDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaResumenDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaSegmentoDTO;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cartera/analisis/mora-temprana")
public class AnalisisMoraTempranaController {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final AnalisisMoraTempranaService service;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AnalisisMoraTempranaController(
            AnalisisMoraTempranaService service
    ) {
        this.service = service;
    }

    // =========================================================
    // INFORMACIÓN DE CONTROL
    // =========================================================

    @GetMapping("/control")
    public ResponseEntity<Map<String, Object>> obtenerControl() {

        LocalDate ultimoCorte =
                service.obtenerUltimoCorteDisponible();

        LocalDate ultimaCosechaMadura =
                ultimoCorte
                        .minusMonths(6)
                        .withDayOfMonth(1);

        return ResponseEntity.ok(
                Map.of(
                        "ultimoCorteDisponible",
                        ultimoCorte,
                        "ultimaCosechaMadura",
                        ultimaCosechaMadura,
                        "mobMinimoEvaluacion",
                        6
                )
        );
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    @GetMapping("/resumen")
    public ResponseEntity<MoraTempranaResumenDTO> consultarResumen(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        MoraTempranaResumenDTO resultado =
                service.consultarResumen(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // COSECHAS
    // =========================================================

    @GetMapping("/cosechas")
    public ResponseEntity<List<MoraTempranaCosechaDTO>> consultarCosechas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        List<MoraTempranaCosechaDTO> resultado =
                service.consultarCosechas(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // ANÁLISIS POR AGENCIA
    // =========================================================

    @GetMapping("/agencias")
    public ResponseEntity<List<MoraTempranaSegmentoDTO>> consultarPorAgencia(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        List<MoraTempranaSegmentoDTO> resultado =
                service.consultarPorAgencia(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // ANÁLISIS POR LÍNEA
    // =========================================================

    @GetMapping("/lineas")
    public ResponseEntity<List<MoraTempranaSegmentoDTO>> consultarPorLinea(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        List<MoraTempranaSegmentoDTO> resultado =
                service.consultarPorLinea(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // PRIMERA MORA
    // =========================================================

    @GetMapping("/primera-mora")
    public ResponseEntity<List<MoraTempranaPrimeraMoraDTO>> consultarPrimeraMora(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        List<MoraTempranaPrimeraMoraDTO> resultado =
                service.consultarPrimeraMora(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                );

        return ResponseEntity.ok(resultado);
    }

    // =========================================================
    // DETALLE AUDITABLE
    // =========================================================

    @GetMapping("/detalle")
    public ResponseEntity<List<MoraTempranaDetalleDTO>> consultarDetalle(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate cosechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito,

            @RequestParam(
                    required = false,
                    defaultValue = "TODOS"
            )
            String indicador
    ) {

        List<MoraTempranaDetalleDTO> resultado =
                service.consultarDetalle(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito,
                        indicador
                );

        return ResponseEntity.ok(resultado);
    }
}