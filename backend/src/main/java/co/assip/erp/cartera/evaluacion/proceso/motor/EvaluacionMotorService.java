package co.assip.erp.cartera.evaluacion.proceso.motor;

import co.assip.erp.cartera.evaluacion.proceso.EvaluacionCarteraRepository;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio102.Criterio102ActualizacionDatosRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio102.Criterio102ActualizacionDatosService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio102.dto.Criterio102DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio201.Criterio201EdadCronologicaRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio201.Criterio201EdadCronologicaService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio201.dto.Criterio201DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio401.Criterio401ServicioDeudaRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio401.Criterio401ServicioDeudaService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio401.dto.Criterio401DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio402.Criterio402HistorialCentralRiesgoRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio402.Criterio402HistorialCentralRiesgoService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio402.dto.Criterio402DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio403.Criterio403CapacidadPagoRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio403.Criterio403CapacidadPagoService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio403.dto.Criterio403DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio502.Criterio502SolvenciaDeudorRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio502.Criterio502SolvenciaDeudorService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio502.dto.Criterio502DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio601.Criterio601GarantiasRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio601.Criterio601GarantiasService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio601.dto.Criterio601DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio701.Criterio701ReestructuracionesRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio701.Criterio701ReestructuracionesService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio701.dto.Criterio701DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio710.Criterio710SectorEconomicoRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio710.Criterio710SectorEconomicoService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio710.dto.Criterio710DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio711.Criterio711AlertasCentralRiesgosRepository;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio711.Criterio711AlertasCentralRiesgosService;
import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio711.dto.Criterio711DatoDTO;

import co.assip.erp.cartera.evaluacion.proceso.dto.ContextoEvaluacionDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCarteraDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCreditoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;

import co.assip.erp.cartera.evaluacion.proceso.motor.dto.EvaluacionCreditoResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.motor.dto.EvaluacionEdadRiesgoResultadoDTO;

import co.assip.erp.seguridad.service.UsuarioSesionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluacionMotorService {

    private static final int CANTIDAD_CRITERIOS = 10;

    private static final String ACCION_RECALIFICAR = "R";
    private static final String ACCION_HABILITAR = "H";
    private static final String ACCION_MANTENER = "M";

    // =========================================================
    // CONTEXTO Y PERSISTENCIA
    // =========================================================

    private final EvaluacionCarteraRepository evaluacionCarteraRepository;

    private final EvaluacionMotorRepository motorRepository;

    private final EvaluacionMotorParametrosRepository
            motorParametrosRepository;

    private final EvaluacionEdadRiesgoService
            edadRiesgoService;

    private final UsuarioSesionService
            usuarioSesionService;

    // =========================================================
    // CRITERIO 102
    // =========================================================

    private final Criterio102ActualizacionDatosRepository
            criterio102Repository;

    private final Criterio102ActualizacionDatosService
            criterio102Service;

    // =========================================================
    // CRITERIO 201
    // =========================================================

    private final Criterio201EdadCronologicaRepository
            criterio201Repository;

    private final Criterio201EdadCronologicaService
            criterio201Service;

    // =========================================================
    // CRITERIO 401
    // =========================================================

    private final Criterio401ServicioDeudaRepository
            criterio401Repository;

    private final Criterio401ServicioDeudaService
            criterio401Service;

    // =========================================================
    // CRITERIO 402
    // =========================================================

    private final Criterio402HistorialCentralRiesgoRepository
            criterio402Repository;

    private final Criterio402HistorialCentralRiesgoService
            criterio402Service;

    // =========================================================
    // CRITERIO 403
    // =========================================================

    private final Criterio403CapacidadPagoRepository
            criterio403Repository;

    private final Criterio403CapacidadPagoService
            criterio403Service;

    // =========================================================
    // CRITERIO 502
    // =========================================================

    private final Criterio502SolvenciaDeudorRepository
            criterio502Repository;

    private final Criterio502SolvenciaDeudorService
            criterio502Service;

    // =========================================================
    // CRITERIO 601
    // =========================================================

    private final Criterio601GarantiasRepository
            criterio601Repository;

    private final Criterio601GarantiasService
            criterio601Service;

    // =========================================================
    // CRITERIO 701
    // =========================================================

    private final Criterio701ReestructuracionesRepository
            criterio701Repository;

    private final Criterio701ReestructuracionesService
            criterio701Service;

    // =========================================================
    // CRITERIO 710
    // =========================================================

    private final Criterio710SectorEconomicoRepository
            criterio710Repository;

    private final Criterio710SectorEconomicoService
            criterio710Service;

    // =========================================================
    // CRITERIO 711
    // =========================================================

    private final Criterio711AlertasCentralRiesgosRepository
            criterio711Repository;

    private final Criterio711AlertasCentralRiesgosService
            criterio711Service;

    // =========================================================
    // EJECUTAR MOTOR
    // =========================================================

    public List<EvaluacionCreditoResultadoDTO> ejecutar(
            Integer idEvaluacionCartera
    ) {

        // =====================================================
        // PREPARAR CONTEXTO
        // =====================================================

        ContextoEvaluacionDTO contexto =
                prepararContextoEvaluacion(
                        idEvaluacionCartera
                );

        LocalDate fechaCorte =
                contexto.getFechaCorte();

        List<EvaluacionCreditoDTO> creditos =
                contexto.getCreditos();

        // =====================================================
        // PARÁMETROS
        // =====================================================

        BigDecimal puntajeMinimoFavorable =
                motorParametrosRepository
                        .obtenerPuntajeMinimoResultadoFavorable();

        validarPuntajeMinimo(
                puntajeMinimoFavorable
        );

        BigDecimal diasMaximoActualizacion =
                criterio102Repository
                        .obtenerDiasMaximoActualizacion();

        validarDiasMaximoActualizacion(
                diasMaximoActualizacion
        );

        // =====================================================
        // CREAR RESULTADOS BASE
        // =====================================================

        Map<Integer, EvaluacionCreditoResultadoDTO> resultadosPorCredito =
                crearResultadosBase(
                        idEvaluacionCartera,
                        creditos
                );

        // =====================================================
        // EJECUTAR CRITERIOS
        // =====================================================

        ejecutarCriterio102(
                fechaCorte,
                diasMaximoActualizacion,
                resultadosPorCredito
        );

        ejecutarCriterio201(
                fechaCorte,
                resultadosPorCredito
        );

        ejecutarCriterio401(
                fechaCorte,
                resultadosPorCredito
        );

        ejecutarCriterio402(
                fechaCorte,
                creditos,
                resultadosPorCredito
        );

        ejecutarCriterio403(
                fechaCorte,
                resultadosPorCredito
        );

        ejecutarCriterio502(
                fechaCorte,
                resultadosPorCredito
        );

        ejecutarCriterio601(
                fechaCorte,
                resultadosPorCredito
        );

        ejecutarCriterio701(
                fechaCorte,
                resultadosPorCredito
        );

        ejecutarCriterio710(
                fechaCorte,
                resultadosPorCredito
        );

        ejecutarCriterio711(
                fechaCorte,
                creditos,
                resultadosPorCredito
        );

        // =====================================================
        // VALIDAR Y SUMAR PUNTAJES
        // =====================================================

        List<EvaluacionCreditoResultadoDTO> resultados =
                new ArrayList<>(
                        resultadosPorCredito.values()
                );

        validarCriteriosCompletos(
                resultados
        );

        calcularPuntajes(
                resultados
        );

        // =====================================================
        // CALCULAR EDAD INDIVIDUAL
        // =====================================================

        calcularEdadesIndividuales(
                resultados,
                puntajeMinimoFavorable
        );

        // =====================================================
        // ALINEAR PERSONA + CLASIFICACIÓN
        // =====================================================

        alinearPorPersonaClasificacion(
                resultados
        );

        // =====================================================
        // DETERMINAR R / H / M
        // =====================================================

        determinarAccionesFinales(
                resultados
        );

        // =====================================================
        // PERSISTIR
        //
        // Solo llegamos aquí si TODOS los cálculos terminaron
        // correctamente.
        // =====================================================

        persistirResultados(
                idEvaluacionCartera,
                creditos,
                resultados
        );

        return resultados;
    }

    // =========================================================
    // PREPARAR CONTEXTO
    // =========================================================

    private ContextoEvaluacionDTO prepararContextoEvaluacion(
            Integer idEvaluacionCartera
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        EvaluacionCarteraDTO evaluacion =
                evaluacionCarteraRepository
                        .buscarPorId(
                                idEvaluacionCartera
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No existe la evaluación de cartera solicitada."
                                )
                        );

        if (
                !"P".equals(
                        evaluacion.getEstado()
                )
        ) {
            throw new IllegalArgumentException(
                    "La evaluación es definitiva y no admite ejecución."
            );
        }

        LocalDate fechaCorte =
                evaluacion.getFechaCorte();

        if (fechaCorte == null) {
            throw new IllegalArgumentException(
                    "La evaluación no tiene fecha de corte."
            );
        }

        if (
                !evaluacionCarteraRepository
                        .existenCierresPorFecha(
                                fechaCorte
                        )
        ) {
            throw new IllegalArgumentException(
                    "No existen cierres de cartera para la fecha "
                            + fechaCorte
                            + "."
            );
        }

        if (
                !evaluacionCarteraRepository
                        .existenCreditosPorFecha(
                                fechaCorte
                        )
        ) {
            throw new IllegalArgumentException(
                    "Los cierres de cartera de la fecha "
                            + fechaCorte
                            + " no contienen créditos."
            );
        }

        if (
                !evaluacionCarteraRepository
                        .existeCierreHojaVidaDefinitivo(
                                fechaCorte
                        )
        ) {
            throw new IllegalArgumentException(
                    "No existe un cierre definitivo de Hoja de Vida "
                            + "para la fecha "
                            + fechaCorte
                            + "."
            );
        }

        int faltantes =
                evaluacionCarteraRepository
                        .contarCreditosSinCierreHojaVida(
                                fechaCorte
                        );

        if (faltantes > 0) {
            throw new IllegalArgumentException(
                    "Existen "
                            + faltantes
                            + " créditos cuyos titulares no aparecen "
                            + "en la fotografía histórica de Hoja de Vida."
            );
        }

        List<EvaluacionCreditoDTO> creditos =
                evaluacionCarteraRepository
                        .obtenerCreditos(
                                fechaCorte
                        );

        if (creditos.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existen créditos disponibles para evaluar."
            );
        }

        return new ContextoEvaluacionDTO(
                evaluacion,
                fechaCorte,
                creditos
        );
    }

    // =========================================================
    // CREAR RESULTADOS BASE
    // =========================================================

    private Map<Integer, EvaluacionCreditoResultadoDTO> crearResultadosBase(
            Integer idEvaluacionCartera,
            List<EvaluacionCreditoDTO> creditos
    ) {

        Map<Integer, EvaluacionCreditoResultadoDTO> resultados =
                new LinkedHashMap<>();

        for (EvaluacionCreditoDTO credito : creditos) {

            if (credito.getIdCierreCarteraCredito() == null) {
                throw new IllegalArgumentException(
                        "Existe un crédito sin identificador histórico."
                );
            }

            EvaluacionCreditoResultadoDTO resultado =
                    new EvaluacionCreditoResultadoDTO();

            resultado.setIdEvaluacionCartera(
                    idEvaluacionCartera
            );

            resultado.setIdCierreCarteraCredito(
                    credito.getIdCierreCarteraCredito()
            );

            resultado.setIdCarteraCredito(
                    credito.getIdCarteraCredito()
            );

            resultado.setIdDatosPersonal(
                    credito.getIdDatosPersonal()
            );

            resultado.setDocumento(
                    credito.getDocumento()
            );

            resultado.setPagareCartera(
                    credito.getPagareCartera()
            );

            resultado.setCodigoClasificacionCredito(
                    credito.getCodigoClasificacionCredito()
            );

            resultado.setCreditoEvaluado(
                    credito.getCreditoEvaluado()
            );

            resultado.setEdadMora(
                    credito.getEdadMora()
            );

            resultado.setEdadRiesgoAnterior(
                    credito.getEdadRiesgoAnterior()
            );

            resultado.setEdadRiesgoInicial(
                    credito.getEdadRiesgoInicial()
            );

            if (
                    resultados.put(
                            credito.getIdCierreCarteraCredito(),
                            resultado
                    ) != null
            ) {
                throw new IllegalArgumentException(
                        "El crédito histórico "
                                + credito.getIdCierreCarteraCredito()
                                + " aparece duplicado en el contexto."
                );
            }
        }

        return resultados;
    }

    // =========================================================
    // CRITERIO 102
    // =========================================================

    private void ejecutarCriterio102(
            LocalDate fechaCorte,
            BigDecimal diasMaximoActualizacion,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        List<Criterio102DatoDTO> datos =
                criterio102Repository.obtenerDatos(
                        fechaCorte
                );

        validarCantidadDatos(
                "102",
                datos.size(),
                resultados.size()
        );

        for (Criterio102DatoDTO dato : datos) {

            agregarResultado(
                    resultados,
                    criterio102Service.evaluar(
                            dato,
                            fechaCorte,
                            diasMaximoActualizacion
                    )
            );
        }
    }

    // =========================================================
    // CRITERIO 201
    // =========================================================

    private void ejecutarCriterio201(
            LocalDate fechaCorte,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        List<Criterio201DatoDTO> datos =
                criterio201Repository.obtenerDatos(
                        fechaCorte
                );

        validarCantidadDatos(
                "201",
                datos.size(),
                resultados.size()
        );

        for (Criterio201DatoDTO dato : datos) {

            agregarResultado(
                    resultados,
                    criterio201Service.evaluar(
                            dato,
                            fechaCorte
                    )
            );
        }
    }

    // =========================================================
    // CRITERIO 401
    // =========================================================

    private void ejecutarCriterio401(
            LocalDate fechaCorte,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        List<Criterio401DatoDTO> datos =
                criterio401Repository.obtenerDatos(
                        fechaCorte
                );

        validarCantidadDatos(
                "401",
                datos.size(),
                resultados.size()
        );

        for (Criterio401DatoDTO dato : datos) {

            agregarResultado(
                    resultados,
                    criterio401Service.evaluar(
                            dato,
                            fechaCorte
                    )
            );
        }
    }

    // =========================================================
// CRITERIO 402 - HISTORIAL CENTRAL DE RIESGO
//
// Con archivo:
// utiliza la calificación reportada por la Central.
//
// Sin archivo:
// utiliza la edad de mora del cierre.
// =========================================================

    private void ejecutarCriterio402(
            LocalDate fechaCorte,
            List<EvaluacionCreditoDTO> creditos,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        boolean existeCentral =
                criterio402Repository
                        .existeArchivoPorFechaCorte(
                                fechaCorte
                        );

        // =====================================================
        // CON CENTRAL DE RIESGO
        // =====================================================

        if (existeCentral) {

            List<Criterio402DatoDTO> datos =
                    criterio402Repository.obtenerDatos(
                            fechaCorte
                    );

            validarCantidadDatos(
                    "402",
                    datos.size(),
                    resultados.size()
            );

            for (Criterio402DatoDTO dato : datos) {

                agregarResultado(
                        resultados,
                        criterio402Service.evaluar(
                                dato,
                                fechaCorte
                        )
                );
            }

            return;
        }

        // =====================================================
        // SIN CENTRAL DE RIESGO
        //
        // Se utiliza edad_de_mora del cierre.
        // =====================================================

        validarCantidadDatos(
                "402",
                creditos.size(),
                resultados.size()
        );

        for (EvaluacionCreditoDTO credito : creditos) {

            agregarResultado(
                    resultados,
                    criterio402Service.evaluarSinCentral(
                            credito,
                            fechaCorte
                    )
            );
        }
    }

    // =========================================================
    // CRITERIO 403
    // =========================================================

    private void ejecutarCriterio403(
            LocalDate fechaCorte,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        List<Criterio403DatoDTO> datos =
                criterio403Repository.obtenerDatos(
                        fechaCorte
                );

        validarCantidadDatos(
                "403",
                datos.size(),
                resultados.size()
        );

        for (Criterio403DatoDTO dato : datos) {

            agregarResultado(
                    resultados,
                    criterio403Service.evaluar(
                            dato,
                            fechaCorte
                    )
            );
        }
    }

    // =========================================================
    // CRITERIO 502
    // =========================================================

    private void ejecutarCriterio502(
            LocalDate fechaCorte,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        List<Criterio502DatoDTO> datos =
                criterio502Repository.obtenerDatos(
                        fechaCorte
                );

        validarCantidadDatos(
                "502",
                datos.size(),
                resultados.size()
        );

        for (Criterio502DatoDTO dato : datos) {

            agregarResultado(
                    resultados,
                    criterio502Service.evaluar(
                            dato,
                            fechaCorte
                    )
            );
        }
    }

    // =========================================================
    // CRITERIO 601
    // =========================================================

    private void ejecutarCriterio601(
            LocalDate fechaCorte,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        List<Criterio601DatoDTO> datos =
                criterio601Repository.obtenerDatos(
                        fechaCorte
                );

        validarCantidadDatos(
                "601",
                datos.size(),
                resultados.size()
        );

        for (Criterio601DatoDTO dato : datos) {

            agregarResultado(
                    resultados,
                    criterio601Service.evaluar(
                            dato,
                            fechaCorte
                    )
            );
        }
    }

    // =========================================================
    // CRITERIO 701
    // =========================================================

    private void ejecutarCriterio701(
            LocalDate fechaCorte,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        List<Criterio701DatoDTO> datos =
                criterio701Repository.obtenerDatos(
                        fechaCorte
                );

        validarCantidadDatos(
                "701",
                datos.size(),
                resultados.size()
        );

        for (Criterio701DatoDTO dato : datos) {

            agregarResultado(
                    resultados,
                    criterio701Service.evaluar(
                            dato,
                            fechaCorte
                    )
            );
        }
    }

    // =========================================================
    // CRITERIO 710
    // =========================================================

    private void ejecutarCriterio710(
            LocalDate fechaCorte,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        List<Criterio710DatoDTO> datos =
                criterio710Repository.obtenerDatos(
                        fechaCorte
                );

        validarCantidadDatos(
                "710",
                datos.size(),
                resultados.size()
        );

        for (Criterio710DatoDTO dato : datos) {

            agregarResultado(
                    resultados,
                    criterio710Service.evaluar(
                            dato,
                            fechaCorte
                    )
            );
        }
    }

    // =========================================================
// CRITERIO 711 - ALERTAS CENTRAL DE RIESGOS
//
// Con archivo:
// utiliza las alertas reportadas por la Central.
//
// Sin archivo:
// se consideran 0 alertas.
// =========================================================

    private void ejecutarCriterio711(
            LocalDate fechaCorte,
            List<EvaluacionCreditoDTO> creditos,
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados
    ) {

        boolean existeCentral =
                criterio711Repository
                        .existeArchivoPorFechaCorte(
                                fechaCorte
                        );

        // =====================================================
        // CON CENTRAL DE RIESGO
        // =====================================================

        if (existeCentral) {

            List<Criterio711DatoDTO> datos =
                    criterio711Repository.obtenerDatos(
                            fechaCorte
                    );

            validarCantidadDatos(
                    "711",
                    datos.size(),
                    resultados.size()
            );

            for (Criterio711DatoDTO dato : datos) {

                agregarResultado(
                        resultados,
                        criterio711Service.evaluar(
                                dato,
                                fechaCorte
                        )
                );
            }

            return;
        }

        // =====================================================
        // SIN CENTRAL DE RIESGO
        //
        // Se consideran 0 alertas.
        // =====================================================

        validarCantidadDatos(
                "711",
                creditos.size(),
                resultados.size()
        );

        for (EvaluacionCreditoDTO credito : creditos) {

            agregarResultado(
                    resultados,
                    criterio711Service.evaluarSinCentral(
                            credito,
                            fechaCorte
                    )
            );
        }
    }

    // =========================================================
    // AGREGAR RESULTADO DE CRITERIO
    // =========================================================

    private void agregarResultado(
            Map<Integer, EvaluacionCreditoResultadoDTO> resultados,
            EvaluacionCriterioResultadoDTO criterio
    ) {

        if (criterio == null) {
            throw new IllegalArgumentException(
                    "Un criterio devolvió un resultado nulo."
            );
        }

        Integer idCierreCarteraCredito =
                criterio.getIdCierreCarteraCredito();

        EvaluacionCreditoResultadoDTO credito =
                resultados.get(
                        idCierreCarteraCredito
                );

        if (credito == null) {
            throw new IllegalArgumentException(
                    "El criterio "
                            + criterio.getCodigoCriterio()
                            + " devolvió un crédito que no pertenece "
                            + "al contexto de evaluación: "
                            + idCierreCarteraCredito
                            + "."
            );
        }

        boolean duplicado =
                credito.getCriterios()
                        .stream()
                        .anyMatch(
                                item ->
                                        item.getCodigoCriterio()
                                                .equals(
                                                        criterio.getCodigoCriterio()
                                                )
                        );

        if (duplicado) {
            throw new IllegalArgumentException(
                    "El crédito "
                            + credito.getIdCarteraCredito()
                            + " tiene duplicado el criterio "
                            + criterio.getCodigoCriterio()
                            + "."
            );
        }

        credito.getCriterios()
                .add(
                        criterio
                );
    }

    // =========================================================
    // VALIDAR LOS 10 CRITERIOS
    // =========================================================

    private void validarCriteriosCompletos(
            List<EvaluacionCreditoResultadoDTO> resultados
    ) {

        Set<String> criteriosEsperados =
                Set.of(
                        "102",
                        "201",
                        "401",
                        "402",
                        "403",
                        "502",
                        "601",
                        "701",
                        "710",
                        "711"
                );

        for (EvaluacionCreditoResultadoDTO resultado : resultados) {

            if (
                    resultado.getCriterios() == null
                            || resultado.getCriterios().size()
                            != CANTIDAD_CRITERIOS
            ) {
                throw new IllegalArgumentException(
                        "El crédito "
                                + resultado.getIdCarteraCredito()
                                + " no tiene los "
                                + CANTIDAD_CRITERIOS
                                + " criterios completos."
                );
            }

            Set<String> encontrados =
                    new HashSet<>();

            for (
                    EvaluacionCriterioResultadoDTO criterio
                    : resultado.getCriterios()
            ) {
                encontrados.add(
                        criterio.getCodigoCriterio()
                );
            }

            if (!encontrados.equals(criteriosEsperados)) {
                throw new IllegalArgumentException(
                        "El crédito "
                                + resultado.getIdCarteraCredito()
                                + " no contiene exactamente "
                                + "los criterios configurados para el motor."
                );
            }
        }
    }

    // =========================================================
    // CALCULAR PUNTAJE
    // =========================================================

    private void calcularPuntajes(
            List<EvaluacionCreditoResultadoDTO> resultados
    ) {

        for (EvaluacionCreditoResultadoDTO resultado : resultados) {

            BigDecimal total =
                    BigDecimal.ZERO;

            for (
                    EvaluacionCriterioResultadoDTO criterio
                    : resultado.getCriterios()
            ) {

                BigDecimal puntaje =
                        criterio.getPuntajeObtenido() == null
                                ? BigDecimal.ZERO
                                : criterio.getPuntajeObtenido();

                total =
                        total.add(
                                puntaje
                        );
            }

            resultado.setPuntajeTotal(
                    total
            );
        }
    }

    // =========================================================
    // CALCULAR EDADES INDIVIDUALES
    // =========================================================

    private void calcularEdadesIndividuales(
            List<EvaluacionCreditoResultadoDTO> resultados,
            BigDecimal puntajeMinimoFavorable
    ) {

        for (EvaluacionCreditoResultadoDTO resultado : resultados) {

            EvaluacionEdadRiesgoResultadoDTO edad =
                    edadRiesgoService.calcular(
                            resultado.getPuntajeTotal(),
                            puntajeMinimoFavorable,
                            resultado.getEdadRiesgoAnterior(),
                            resultado.getEdadMora(),
                            resultado.getCreditoEvaluado()
                    );

            resultado.setEdadRiesgoCalculada(
                    edad.getEdadRiesgoCalculada()
            );

            resultado.setEdadRiesgoArrastre(
                    edad.getEdadRiesgoArrastre()
            );

            resultado.setComentarioEvaluacion(
                    edad.getComentarioEvaluacion()
            );
        }
    }

    // =========================================================
    // ALINEAR POR PERSONA + CLASIFICACIÓN
    // =========================================================

    private void alinearPorPersonaClasificacion(
            List<EvaluacionCreditoResultadoDTO> resultados
    ) {

        Map<String, String> peorCategoriaPorGrupo =
                new HashMap<>();

        // =====================================================
        // PRIMERA PASADA: ENCONTRAR PEOR CATEGORÍA DEL GRUPO
        // =====================================================

        for (EvaluacionCreditoResultadoDTO resultado : resultados) {

            String llave =
                    construirLlaveGrupo(
                            resultado
                    );

            String actual =
                    peorCategoriaPorGrupo.get(
                            llave
                    );

            if (actual == null) {

                peorCategoriaPorGrupo.put(
                        llave,
                        resultado.getEdadRiesgoArrastre()
                );

            } else {

                peorCategoriaPorGrupo.put(
                        llave,
                        edadRiesgoService.peorCategoria(
                                actual,
                                resultado.getEdadRiesgoArrastre()
                        )
                );
            }
        }

        // =====================================================
        // SEGUNDA PASADA: ALINEAR TODOS LOS CRÉDITOS
        // =====================================================

        for (EvaluacionCreditoResultadoDTO resultado : resultados) {

            String llave =
                    construirLlaveGrupo(
                            resultado
                    );

            String edadIndividual =
                    resultado.getEdadRiesgoArrastre();

            String edadFinal =
                    peorCategoriaPorGrupo.get(
                            llave
                    );

            resultado.setEdadRiesgoFinal(
                    edadFinal
            );

            if (!edadFinal.equals(edadIndividual)) {

                resultado.setComentarioEvaluacion(
                        resultado.getComentarioEvaluacion()
                                + " Por alineación de riesgo del asociado "
                                + "dentro de la clasificación "
                                + resultado.getCodigoClasificacionCredito()
                                + ", la categoría individual "
                                + edadIndividual
                                + " se ajusta a "
                                + edadFinal
                                + "."
                );

            } else {

                resultado.setComentarioEvaluacion(
                        resultado.getComentarioEvaluacion()
                                + " La categoría final del grupo "
                                + "persona + clasificación permanece en "
                                + edadFinal
                                + "."
                );
            }
        }
    }

    // =========================================================
// DETERMINAR RECOMENDACIÓN FINAL
//
// R = Sugerir recalificación
// H = Sugerir habilitación
// M = Sugerir mantener
//
// La comparación se realiza contra la edad de riesgo
// vigente que traía el cierre.
// =========================================================

    private void determinarAccionesFinales(
            List<EvaluacionCreditoResultadoDTO> resultados
    ) {

        for (EvaluacionCreditoResultadoDTO resultado : resultados) {

            int comparacion =
                    edadRiesgoService.compararCategorias(
                            resultado.getEdadRiesgoFinal(),
                            resultado.getEdadRiesgoAnterior()
                    );

            String accion;

            if (comparacion > 0) {

                accion =
                        ACCION_RECALIFICAR;

            } else if (comparacion < 0) {

                accion =
                        ACCION_HABILITAR;

            } else {

                accion =
                        ACCION_MANTENER;
            }

            resultado.setAccionEvaluacion(
                    accion
            );

            resultado.setComentarioEvaluacion(
                    resultado.getComentarioEvaluacion()
                            + " Edad de riesgo vigente: "
                            + edadRiesgoService.normalizarCategoria(
                            resultado.getEdadRiesgoAnterior()
                    )
                            + ". Edad de riesgo sugerida: "
                            + resultado.getEdadRiesgoFinal()
                            + ". Recomendación: "
                            + accion
                            + "."
            );
        }
    }

    // =========================================================
    // PERSISTIR RESULTADOS
    // =========================================================

    private void persistirResultados(
            Integer idEvaluacionCartera,
            List<EvaluacionCreditoDTO> creditos,
            List<EvaluacionCreditoResultadoDTO> resultados
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // REEMPLAZAR EJECUCIÓN ANTERIOR
        // =====================================================

        motorRepository.eliminarDetallesPorEvaluacion(
                idEvaluacionCartera
        );

        motorRepository.eliminarCreditosPorEvaluacion(
                idEvaluacionCartera
        );

        motorRepository.reiniciarTotalesEvaluacion(
                idEvaluacionCartera,
                idUsuario
        );

        // =====================================================
        // GUARDAR RESULTADOS
        // =====================================================

        for (EvaluacionCreditoResultadoDTO resultado : resultados) {

            Integer idEvaluacionCarteraCredito =
                    motorRepository.guardarCredito(
                            resultado,
                            idUsuario
                    );

            motorRepository.guardarDetalles(
                    idEvaluacionCarteraCredito,
                    resultado.getCriterios(),
                    idUsuario
            );
        }

        // =====================================================
        // RESUMEN
        // =====================================================

        int recalificados =
                contarAccion(
                        resultados,
                        ACCION_RECALIFICAR
                );

        int habilitados =
                contarAccion(
                        resultados,
                        ACCION_HABILITAR
                );

        int mantenidos =
                contarAccion(
                        resultados,
                        ACCION_MANTENER
                );

        int cantidadAsociados =
                (int) creditos.stream()
                        .map(
                                EvaluacionCreditoDTO::getIdDatosPersonal
                        )
                        .filter(
                                id -> id != null
                        )
                        .distinct()
                        .count();

        BigDecimal saldoTotal =
                creditos.stream()
                        .map(
                                EvaluacionCreditoDTO::getSaldoActual
                        )
                        .filter(
                                valor -> valor != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        int filas =
                motorRepository.actualizarResumenEvaluacion(
                        idEvaluacionCartera,
                        resultados.size(),
                        cantidadAsociados,
                        saldoTotal,
                        recalificados,
                        habilitados,
                        mantenidos,
                        idUsuario
                );

        if (filas == 0) {
            throw new IllegalStateException(
                    "No fue posible actualizar el resumen "
                            + "de la evaluación."
            );
        }
    }

    // =========================================================
    // CONTAR ACCIÓN
    // =========================================================

    private int contarAccion(
            List<EvaluacionCreditoResultadoDTO> resultados,
            String accion
    ) {

        return (int) resultados.stream()
                .filter(
                        resultado ->
                                accion.equals(
                                        resultado.getAccionEvaluacion()
                                )
                )
                .count();
    }

    // =========================================================
    // LLAVE DE ALINEACIÓN
    // =========================================================

    private String construirLlaveGrupo(
            EvaluacionCreditoResultadoDTO resultado
    ) {

        if (resultado.getIdDatosPersonal() == null) {
            throw new IllegalArgumentException(
                    "El crédito "
                            + resultado.getIdCarteraCredito()
                            + " no tiene persona asociada."
            );
        }

        if (
                resultado.getCodigoClasificacionCredito() == null
                        || resultado.getCodigoClasificacionCredito().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El crédito "
                            + resultado.getIdCarteraCredito()
                            + " no tiene clasificación."
            );
        }

        return resultado.getIdDatosPersonal()
                + "|"
                + resultado.getCodigoClasificacionCredito()
                .trim()
                .toUpperCase();
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarIdEvaluacion(
            Integer idEvaluacionCartera
    ) {

        if (
                idEvaluacionCartera == null
                        || idEvaluacionCartera <= 0
        ) {
            throw new IllegalArgumentException(
                    "El identificador de la evaluación no es válido."
            );
        }
    }

    private void validarCantidadDatos(
            String codigoCriterio,
            int cantidadDatos,
            int cantidadCreditos
    ) {

        if (cantidadDatos != cantidadCreditos) {
            throw new IllegalArgumentException(
                    "El criterio "
                            + codigoCriterio
                            + " devolvió "
                            + cantidadDatos
                            + " registros para "
                            + cantidadCreditos
                            + " créditos del contexto."
            );
        }
    }

    private void validarPuntajeMinimo(
            BigDecimal valor
    ) {

        if (
                valor == null
                        || valor.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "No está configurado correctamente el parámetro "
                            + "210 — Puntaje mínimo resultado favorable."
            );
        }
    }

    private void validarDiasMaximoActualizacion(
            BigDecimal valor
    ) {

        if (
                valor == null
                        || valor.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "No está configurado correctamente el parámetro "
                            + "121 — Días máximo de actualización."
            );
        }
    }
}