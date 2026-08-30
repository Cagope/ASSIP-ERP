package co.assip.erp.cartera.analisis.matrizrodamiento;

import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoCorteDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoDetalleDTO;

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
        "/cartera/analisis/matriz-rodamiento"
)
public class MatrizRodamientoController {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final MatrizRodamientoService service;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MatrizRodamientoController(
            MatrizRodamientoService service
    ) {

        this.service =
                service;
    }

    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    @GetMapping("/cortes")
    public ResponseEntity<List<MatrizRodamientoCorteDTO>>
    listarCortesDisponibles() {

        return ResponseEntity.ok(
                service.listarCortesDisponibles()
        );
    }

    // =========================================================
    // CALCULAR MATRIZ
    // =========================================================

    /**
     * ACTUAL CONTRA CORTE:
     *
     * /calcular
     * ?tipoPartida=ACTUAL
     * &fechaComparacion=2026-05-31
     *
     *
     * CORTE CONTRA CORTE:
     *
     * /calcular
     * ?tipoPartida=CORTE
     * &fechaPartida=2026-05-31
     * &fechaComparacion=2026-04-30
     */
    @GetMapping("/calcular")
    public ResponseEntity<MatrizRodamientoDTO>
    calcular(
            @RequestParam
            String tipoPartida,

            @RequestParam(
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaPartida,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaComparacion
    ) {

        return ResponseEntity.ok(
                service.calcular(
                        tipoPartida,
                        fechaPartida,
                        fechaComparacion
                )
        );
    }

    // =========================================================
    // DETALLE DE CELDA
    // =========================================================

    /**
     * Ejemplo:
     *
     * /detalle
     * ?tipoPartida=CORTE
     * &fechaPartida=2026-05-31
     * &fechaComparacion=2026-04-30
     * &categoriaAnterior=B
     * &categoriaPartida=D
     *
     *
     * Devuelve exclusivamente los créditos
     * que forman la celda B -> D.
     */
    @GetMapping("/detalle")
    public ResponseEntity<List<MatrizRodamientoDetalleDTO>>
    listarDetalleCelda(
            @RequestParam
            String tipoPartida,

            @RequestParam(
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaPartida,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaComparacion,

            @RequestParam
            String categoriaAnterior,

            @RequestParam
            String categoriaPartida
    ) {

        return ResponseEntity.ok(
                service.listarDetalleCelda(
                        tipoPartida,
                        fechaPartida,
                        fechaComparacion,
                        categoriaAnterior,
                        categoriaPartida
                )
        );
    }
}