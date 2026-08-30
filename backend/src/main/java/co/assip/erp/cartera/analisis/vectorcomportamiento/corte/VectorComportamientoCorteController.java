package co.assip.erp.cartera.analisis.vectorcomportamiento.corte;

import co.assip.erp.cartera.analisis.vectorcomportamiento.corte.dto.VectorComportamientoCorteDetalleDTO;
import co.assip.erp.cartera.analisis.vectorcomportamiento.corte.dto.VectorComportamientoCorteResumenDTO;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;


/**
 * Controlador del proceso de análisis
 * Vector de Comportamiento por Corte.
 *
 * Permite reconstruir el Vector de Comportamiento
 * tomando una fecha histórica de cierre
 * como fecha de referencia actual.
 *
 * Reglas fundamentales:
 *
 * 1. La población está compuesta exclusivamente
 *    por créditos cuyo saldo en la fecha de corte
 *    seleccionada es mayor que cero.
 *
 * 2. No se utiliza el saldo actual del maestro
 *    de cartera para determinar la población.
 *
 * 3. La fecha de corte seleccionada constituye
 *    la posición 1 del Vector y se trata
 *    conceptualmente como el ACTUAL.
 *
 * 4. Después de la posición de referencia
 *    se consultan máximo 12 cierres anteriores.
 *
 * 5. El Vector puede contener:
 *
 *      posición 1:
 *          corte seleccionado.
 *
 *      posiciones 2..13:
 *          máximo 12 cierres anteriores,
 *          ordenados del más reciente
 *          al más antiguo.
 *
 * 6. Toda la información financiera,
 *    de comportamiento y riesgo proviene
 *    exclusivamente de los cierres históricos.
 *
 * Este controlador es exclusivamente de lectura.
 */
@RestController
@RequestMapping(
        "/cartera/analisis/vector-comportamiento/corte"
)
public class VectorComportamientoCorteController {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final VectorComportamientoCorteService service;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public VectorComportamientoCorteController(
            VectorComportamientoCorteService service
    ) {

        this.service =
                service;
    }


    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    /**
     * Consulta las fechas históricas disponibles
     * para construir el Vector de Comportamiento.
     *
     * Solo devuelve fechas que contienen
     * por lo menos un crédito
     * con saldo mayor que cero.
     */
    @GetMapping("/fechas")
    public ResponseEntity<List<LocalDate>>
    listarFechasCorteDisponibles() {

        return ResponseEntity.ok(
                service.listarFechasCorteDisponibles()
        );
    }


    // =========================================================
    // CARTERA DEL CORTE - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen del Vector de Comportamiento
     * para todos los créditos pertenecientes
     * a la población del corte seleccionado.
     *
     * La fecha seleccionada actúa
     * como referencia actual.
     *
     * Los indicadores históricos consideran
     * máximo los 12 cierres anteriores.
     *
     * Devuelve una fila por crédito.
     *
     * Ejemplo:
     *
     * /resumen?fechaCorte=2026-05-31
     */
    @GetMapping("/resumen")
    public ResponseEntity<List<VectorComportamientoCorteResumenDTO>>
    listarResumenPorCorte(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte
    ) {

        return ResponseEntity.ok(
                service.listarResumenPorCorte(
                        fechaCorte
                )
        );
    }


    // =========================================================
    // CARTERA DEL CORTE - DETALLE
    // =========================================================

    /**
     * Consulta el Vector detallado
     * de los créditos pertenecientes
     * a la población del corte seleccionado.
     *
     * Por cada crédito devuelve:
     *
     * posición 1:
     *     corte seleccionado tratado como ACTUAL.
     *
     * posiciones 2..13:
     *     máximo 12 cierres anteriores,
     *     ordenados del más reciente
     *     al más antiguo.
     *
     * Ejemplo:
     *
     * /detalle?fechaCorte=2026-05-31
     */
    @GetMapping("/detalle")
    public ResponseEntity<List<VectorComportamientoCorteDetalleDTO>>
    listarDetallePorCorte(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte
    ) {

        return ResponseEntity.ok(
                service.listarDetallePorCorte(
                        fechaCorte
                )
        );
    }


    // =========================================================
    // ASOCIADO - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen de los créditos
     * de un asociado pertenecientes
     * a la población del corte seleccionado.
     *
     * Cada crédito se analiza tomando
     * el corte seleccionado como referencia actual
     * y máximo 12 cierres anteriores.
     *
     * Ejemplo:
     *
     * /persona/1947/resumen?fechaCorte=2026-05-31
     */
    @GetMapping("/persona/{idDatosPersonal}/resumen")
    public ResponseEntity<List<VectorComportamientoCorteResumenDTO>>
    listarResumenPorPersona(
            @PathVariable
            Integer idDatosPersonal,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte
    ) {

        return ResponseEntity.ok(
                service.listarResumenPorPersona(
                        fechaCorte,
                        idDatosPersonal
                )
        );
    }


    // =========================================================
    // ASOCIADO - DETALLE
    // =========================================================

    /**
     * Consulta el Vector detallado
     * de los créditos de un asociado
     * pertenecientes a la población
     * del corte seleccionado.
     *
     * Por cada crédito devuelve:
     *
     * posición 1:
     *     corte seleccionado.
     *
     * posiciones 2..13:
     *     máximo 12 cierres anteriores.
     *
     * Ejemplo:
     *
     * /persona/1947/detalle?fechaCorte=2026-05-31
     */
    @GetMapping("/persona/{idDatosPersonal}/detalle")
    public ResponseEntity<List<VectorComportamientoCorteDetalleDTO>>
    listarDetallePorPersona(
            @PathVariable
            Integer idDatosPersonal,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte
    ) {

        return ResponseEntity.ok(
                service.listarDetallePorPersona(
                        fechaCorte,
                        idDatosPersonal
                )
        );
    }


    // =========================================================
    // CRÉDITO - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen del Vector de Comportamiento
     * de un crédito específico
     * en la fecha seleccionada.
     *
     * El crédito debe pertenecer
     * a la población del corte.
     *
     * Si el crédito no tenía saldo mayor que cero
     * en esa fecha, responde HTTP 204.
     *
     * Ejemplo:
     *
     * /credito/28810/resumen?fechaCorte=2026-05-31
     */
    @GetMapping("/credito/{idCarteraCredito}/resumen")
    public ResponseEntity<VectorComportamientoCorteResumenDTO>
    buscarResumenPorCredito(
            @PathVariable
            Integer idCarteraCredito,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte
    ) {

        return service
                .buscarResumenPorCredito(
                        fechaCorte,
                        idCarteraCredito
                )
                .map(
                        ResponseEntity::ok
                )
                .orElseGet(
                        () ->
                                ResponseEntity
                                        .noContent()
                                        .build()
                );
    }


    // =========================================================
    // CRÉDITO - DETALLE
    // =========================================================

    /**
     * Consulta el Vector detallado
     * de un crédito perteneciente
     * a la población del corte seleccionado.
     *
     * El resultado contiene:
     *
     * posición 1:
     *     corte seleccionado tratado como ACTUAL.
     *
     * posiciones 2..13:
     *     máximo 12 cierres anteriores,
     *     ordenados del más reciente
     *     al más antiguo.
     *
     * Ejemplo:
     *
     * /credito/28810/detalle?fechaCorte=2026-05-31
     */
    @GetMapping("/credito/{idCarteraCredito}/detalle")
    public ResponseEntity<List<VectorComportamientoCorteDetalleDTO>>
    listarDetallePorCredito(
            @PathVariable
            Integer idCarteraCredito,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte
    ) {

        return ResponseEntity.ok(
                service.listarDetallePorCredito(
                        fechaCorte,
                        idCarteraCredito
                )
        );
    }


    // =========================================================
    // EXISTENCIA DEL CRÉDITO EN EL CORTE
    // =========================================================

    /**
     * Verifica si un crédito pertenecía
     * a la población del Vector
     * en la fecha seleccionada.
     *
     * Condición poblacional:
     *
     * saldo_credito_fecha_corte > 0.
     *
     * Ejemplo:
     *
     * /credito/28810/existe?fechaCorte=2026-05-31
     */
    @GetMapping("/credito/{idCarteraCredito}/existe")
    public ResponseEntity<Boolean>
    existeCreditoEnCorte(
            @PathVariable
            Integer idCarteraCredito,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte
    ) {

        return ResponseEntity.ok(
                service.existeCreditoEnCorte(
                        fechaCorte,
                        idCarteraCredito
                )
        );
    }
}