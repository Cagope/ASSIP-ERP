package co.assip.erp.cartera.analisis.vectorcomportamiento.actual;

import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto.VectorComportamientoDetalleDTO;
import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto.VectorComportamientoResumenDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * Controlador del proceso de análisis
 * Vector de Comportamiento Actual.
 *
 * Permite consultar:
 *
 * - Resumen de toda la cartera actualmente con saldo.
 * - Detalle del Vector de Comportamiento de toda la cartera actualmente con saldo.
 * - Resumen de los créditos actualmente con saldo de un asociado.
 * - Detalle del Vector de Comportamiento de los créditos actualmente con saldo de un asociado.
 * - Resumen de un crédito actualmente con saldo.
 * - Detalle del Vector de Comportamiento de un crédito actualmente con saldo.
 *
 * La población del Vector de Comportamiento Actual
 * está compuesta exclusivamente por créditos cuyo
 * saldo actual en cartera.carteras_creditos
 * es mayor que cero.
 *
 * Para cada crédito el vector contiene:
 *
 * - Una posición ACTUAL.
 * - Hasta 12 cierres históricos mensuales.
 *
 * Por lo tanto, cada crédito puede tener como máximo
 * 13 posiciones dentro del vector.
 *
 * Este controlador es exclusivamente de lectura.
 */
@RestController
@RequestMapping(
        "/cartera/analisis/vector-comportamiento/actual"
)
public class VectorComportamientoActualController {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final VectorComportamientoActualService service;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public VectorComportamientoActualController(
            VectorComportamientoActualService service
    ) {

        this.service =
                service;
    }


    // =========================================================
    // CARTERA ACTUAL - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen del Vector de Comportamiento
     * para toda la cartera actualmente con saldo.
     *
     * Devuelve una fila por crédito.
     *
     * Los indicadores históricos del resumen se calculan
     * sobre un máximo de 12 cierres históricos.
     */
    @GetMapping("/resumen")
    public ResponseEntity<List<VectorComportamientoResumenDTO>>
    listarResumenCarteraActiva() {

        return ResponseEntity.ok(
                service.listarResumenCarteraActiva()
        );
    }


    // =========================================================
    // CARTERA ACTUAL - DETALLE
    // =========================================================

    /**
     * Consulta el Vector de Comportamiento detallado
     * de todos los créditos actualmente con saldo.
     *
     * Cada crédito contiene:
     *
     * - Posición 1: ACTUAL.
     * - Posiciones 2 a 13: hasta 12 cierres históricos,
     *   ordenados desde el más reciente hacia el más antiguo.
     *
     * Devuelve una fila por crédito y posición del vector.
     */
    @GetMapping("/detalle")
    public ResponseEntity<List<VectorComportamientoDetalleDTO>>
    listarDetalleCarteraActiva() {

        return ResponseEntity.ok(
                service.listarDetalleCarteraActiva()
        );
    }


    // =========================================================
    // ASOCIADO - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen de todos los créditos
     * actualmente con saldo de un asociado.
     *
     * Devuelve una fila por crédito.
     */
    @GetMapping("/persona/{idDatosPersonal}/resumen")
    public ResponseEntity<List<VectorComportamientoResumenDTO>>
    listarResumenPorPersona(
            @PathVariable
            Integer idDatosPersonal
    ) {

        return ResponseEntity.ok(
                service.listarResumenPorPersona(
                        idDatosPersonal
                )
        );
    }


    // =========================================================
    // ASOCIADO - DETALLE
    // =========================================================

    /**
     * Consulta el Vector de Comportamiento detallado
     * de todos los créditos actualmente con saldo
     * de un asociado.
     *
     * Para cada crédito:
     *
     * - ACTUAL aparece en posición 1.
     * - Después aparecen hasta 12 cierres históricos.
     */
    @GetMapping("/persona/{idDatosPersonal}/detalle")
    public ResponseEntity<List<VectorComportamientoDetalleDTO>>
    listarDetallePorPersona(
            @PathVariable
            Integer idDatosPersonal
    ) {

        return ResponseEntity.ok(
                service.listarDetallePorPersona(
                        idDatosPersonal
                )
        );
    }


    // =========================================================
    // CRÉDITO - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen del Vector de Comportamiento
     * para un crédito específico.
     *
     * Si el crédito no pertenece actualmente
     * a la población del Vector,
     * responde HTTP 204.
     */
    @GetMapping("/credito/{idCarteraCredito}/resumen")
    public ResponseEntity<VectorComportamientoResumenDTO>
    buscarResumenPorCredito(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return service
                .buscarResumenPorCredito(
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
     * Consulta el Vector de Comportamiento detallado
     * de un crédito actualmente con saldo.
     *
     * El resultado contiene:
     *
     * - Posición 1: estado ACTUAL.
     * - Posiciones 2 a 13: hasta 12 cierres históricos,
     *   desde el más reciente hacia el más antiguo.
     */
    @GetMapping("/credito/{idCarteraCredito}/detalle")
    public ResponseEntity<List<VectorComportamientoDetalleDTO>>
    listarDetallePorCredito(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.listarDetallePorCredito(
                        idCarteraCredito
                )
        );
    }


    // =========================================================
    // EXISTENCIA EN VECTOR ACTUAL
    // =========================================================

    /**
     * Verifica si un crédito pertenece actualmente
     * a la población del Vector de Comportamiento.
     *
     * La población está compuesta únicamente por créditos
     * cuyo saldo actual en el maestro es mayor que cero.
     */
    @GetMapping("/credito/{idCarteraCredito}/existe")
    public ResponseEntity<Boolean>
    existeCreditoEnVector(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.existeCreditoEnVector(
                        idCarteraCredito
                )
        );
    }
}