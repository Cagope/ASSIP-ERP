package co.assip.erp.cartera.analisis.cosechas;

import co.assip.erp.cartera.analisis.cosechas.dto.CosechaCatalogoDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaCorteDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaDetalleDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaResumenDTO;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(
        "/cartera/analisis/cosechas"
)
public class AnalisisCosechasController {

    private final AnalisisCosechasService service;


    public AnalisisCosechasController(
            AnalisisCosechasService service
    ) {
        this.service = service;
    }


    // =========================================================
    // CORTES
    // =========================================================

    @GetMapping("/cortes")
    public ResponseEntity<List<CosechaCorteDTO>>
    listarCortes() {

        return ResponseEntity.ok(
                service.listarCortes()
        );
    }


    // =========================================================
    // AGENCIAS
    // =========================================================

    @GetMapping("/agencias")
    public ResponseEntity<List<CosechaCatalogoDTO>>
    listarAgencias() {

        return ResponseEntity.ok(
                service.listarAgencias()
        );
    }


    // =========================================================
    // LÍNEAS
    // =========================================================

    @GetMapping("/lineas")
    public ResponseEntity<List<CosechaCatalogoDTO>>
    listarLineas() {

        return ResponseEntity.ok(
                service.listarLineas()
        );
    }


    // =========================================================
    // ANALIZAR
    // =========================================================

    @GetMapping("/analizar")
    public ResponseEntity<CosechaResumenDTO>
    analizar(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate cosechaDesde,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate cosechaHasta,

            @RequestParam(
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate hastaCorte,

            @RequestParam(
                    required = false
            )
            Integer idAgencia,

            @RequestParam(
                    required = false
            )
            Integer idLineaCredito
    ) {

        return ResponseEntity.ok(
                service.analizar(
                        cosechaDesde,
                        cosechaHasta,
                        hastaCorte,
                        idAgencia,
                        idLineaCredito
                )
        );
    }


    // =========================================================
    // DETALLE / INFORME DETALLADO
    // =========================================================

    @GetMapping("/detalle")
    public ResponseEntity<List<CosechaDetalleDTO>>
    listarDetalle(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate cosecha,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte,

            @RequestParam
            String indicador,

            @RequestParam(
                    required = false
            )
            Integer idAgencia,

            @RequestParam(
                    required = false
            )
            Integer idLineaCredito
    ) {

        return ResponseEntity.ok(
                service.listarDetalle(
                        cosecha,
                        fechaCorte,
                        indicador,
                        idAgencia,
                        idLineaCredito
                )
        );
    }
}