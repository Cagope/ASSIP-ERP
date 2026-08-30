package co.assip.erp.cartera.consultacreditos;

import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoAlivioDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoDetalleDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoEvaluacionDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoExtractoDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoIntegralDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoInteresDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoResumenDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoSeguroDTO;
import co.assip.erp.cartera.consultacreditos.pdf.ConsultaCreditoExtractoPdfService;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoProrrogaDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoResultadoMensualDTO;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * Controlador de consulta especializada de créditos.
 *
 * Permite consultar:
 *
 * - Todos los créditos de un asociado.
 * - Créditos vigentes y cancelados.
 * - Detalle general del crédito.
 * - Extracto de pagos.
 * - Extracto de pagos en PDF.
 * - Seguros.
 * - Alivios.
 * - Intereses causados.
 * - Evaluaciones de cartera.
 * - Última evaluación.
 * - Prórrogas.
 * - Consulta integral.
 *
 * Este controlador es exclusivamente de lectura.
 */
@RestController
@RequestMapping("/cartera/consulta-creditos")
public class ConsultaCreditosController {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final ConsultaCreditosService service;

    private final ConsultaCreditoExtractoPdfService
            extractoPdfService;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ConsultaCreditosController(
            ConsultaCreditosService service,
            ConsultaCreditoExtractoPdfService extractoPdfService
    ) {

        this.service =
                service;

        this.extractoPdfService =
                extractoPdfService;
    }


    // =========================================================
    // CRÉDITOS DEL ASOCIADO
    // =========================================================

    /**
     * Lista todos los créditos asociados a una persona.
     *
     * Incluye créditos:
     *
     * - Con saldo.
     * - Saldados.
     * - Vigentes.
     * - Cancelados.
     * - En mora.
     * - En cobro jurídico.
     */
    @GetMapping("/persona/{idDatosPersonal}/creditos")
    public ResponseEntity<List<ConsultaCreditoResumenDTO>>
    listarPorPersona(
            @PathVariable
            Integer idDatosPersonal
    ) {

        return ResponseEntity.ok(
                service.listarPorPersona(
                        idDatosPersonal
                )
        );
    }


    // =========================================================
    // DETALLE DEL CRÉDITO
    // =========================================================

    /**
     * Consulta el detalle funcional de un crédito.
     */
    @GetMapping("/{idCarteraCredito}")
    public ResponseEntity<ConsultaCreditoDetalleDTO>
    buscarPorId(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.buscarPorId(
                        idCarteraCredito
                )
        );
    }


    // =========================================================
    // EXTRACTO
    // =========================================================

    /**
     * Consulta todos los movimientos del extracto.
     */
    @GetMapping("/{idCarteraCredito}/extracto")
    public ResponseEntity<List<ConsultaCreditoExtractoDTO>>
    listarExtracto(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.listarExtracto(
                        idCarteraCredito
                )
        );
    }


    // =========================================================
    // PDF DEL EXTRACTO
    // =========================================================

    /**
     * Genera el extracto del crédito en formato PDF.
     *
     * El documento se entrega en modo inline para permitir:
     *
     * - Visualización.
     * - Impresión.
     * - Descarga desde el visor PDF.
     */
    @GetMapping(
            value = "/{idCarteraCredito}/extracto/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]>
    generarExtractoPdf(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return extractoPdfService
                .generarPdfResponse(
                        idCarteraCredito
                );
    }


    // =========================================================
    // SEGUROS
    // =========================================================

    /**
     * Consulta las configuraciones vigentes e históricas
     * de seguro del crédito.
     */
    @GetMapping("/{idCarteraCredito}/seguros")
    public ResponseEntity<List<ConsultaCreditoSeguroDTO>>
    listarSeguros(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.listarSeguros(
                        idCarteraCredito
                )
        );
    }


    // =========================================================
    // ALIVIOS
    // =========================================================

    /**
     * Consulta los alivios relacionados con el crédito.
     */
    @GetMapping("/{idCarteraCredito}/alivios")
    public ResponseEntity<List<ConsultaCreditoAlivioDTO>>
    listarAlivios(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.listarAlivios(
                        idCarteraCredito
                )
        );
    }


    // =========================================================
    // INTERESES CAUSADOS
    // =========================================================

    /**
     * Consulta las causaciones de intereses del crédito.
     */
    @GetMapping("/{idCarteraCredito}/intereses-causados")
    public ResponseEntity<List<ConsultaCreditoInteresDTO>>
    listarInteresesCausados(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.listarInteresesCausados(
                        idCarteraCredito
                )
        );
    }


    // =========================================================
    // EVALUACIONES
    // =========================================================

    /**
     * Consulta el historial completo de evaluaciones
     * realizadas al crédito.
     */
    @GetMapping("/{idCarteraCredito}/evaluaciones")
    public ResponseEntity<List<ConsultaCreditoEvaluacionDTO>>
    listarEvaluaciones(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.listarEvaluaciones(
                        idCarteraCredito
                )
        );
    }

    // =========================================================
    // PRÓRROGAS
    // =========================================================

    /**
     * Consulta el historial completo de prórrogas
     * registradas para el crédito.
     */
    @GetMapping("/{idCarteraCredito}/prorrogas")
    public ResponseEntity<List<ConsultaCreditoProrrogaDTO>>
    listarProrrogas(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.listarProrrogas(
                        idCarteraCredito
                )
        );
    }

    // =========================================================
// RESULTADOS MENSUALES
// =========================================================

    /**
     * Consulta el historial de resultados mensuales
     * calculados para el crédito.
     *
     * Cada registro corresponde a un cierre de cartera
     * en el cual el crédito tuvo resultado de cálculo.
     */
    @GetMapping("/{idCarteraCredito}/resultados-mensuales")
    public ResponseEntity<List<ConsultaCreditoResultadoMensualDTO>>
    listarResultadosMensuales(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.listarResultadosMensuales(
                        idCarteraCredito
                )
        );
    }


    /**
     * Consulta la última evaluación registrada.
     *
     * Si el crédito existe pero no tiene evaluaciones,
     * responde con HTTP 204.
     */
    @GetMapping("/{idCarteraCredito}/ultima-evaluacion")
    public ResponseEntity<ConsultaCreditoEvaluacionDTO>
    buscarUltimaEvaluacion(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return service.buscarUltimaEvaluacion(
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
    // CONSULTA INTEGRAL
    // =========================================================

    /**
     * Consulta toda la información disponible del crédito.
     */
    @GetMapping("/{idCarteraCredito}/integral")
    public ResponseEntity<ConsultaCreditoIntegralDTO>
    consultarIntegral(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.consultarIntegral(
                        idCarteraCredito
                )
        );
    }


    // =========================================================
    // EXISTENCIA
    // =========================================================

    /**
     * Verifica si existe un crédito.
     */
    @GetMapping("/{idCarteraCredito}/existe")
    public ResponseEntity<Boolean>
    existeCredito(
            @PathVariable
            Integer idCarteraCredito
    ) {

        return ResponseEntity.ok(
                service.existeCredito(
                        idCarteraCredito
                )
        );
    }
}