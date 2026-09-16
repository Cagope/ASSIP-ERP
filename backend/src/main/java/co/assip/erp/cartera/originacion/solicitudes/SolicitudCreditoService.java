package co.assip.erp.cartera.originacion.solicitudes;

import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCrearRetomarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCrearRetomarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoCrearRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoDetalleDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoGuardarResponseDTO;
import co.assip.erp.cartera.originacion.solicitudes.dto.SolicitudCreditoResumenDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.shared.financiero.CuotasFinancieras;
import co.assip.erp.shared.financiero.TasasFinancieras;
import co.assip.erp.shared.financiero.dto.CuotaVariableResultado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class SolicitudCreditoService {

    private final SolicitudCreditoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public SolicitudCreditoService(
            SolicitudCreditoRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }


    // =========================================================
    // CREAR SOLICITUD + GUARDAR DATOS DEL CRÉDITO
    // =========================================================

    public SolicitudCreditoGuardarResponseDTO crearSolicitud(
            SolicitudCreditoCrearRequestDTO request
    ) {

        validarCrearSolicitud(request);

        usuarioSesionService.validarAgencia(
                request.getIdAgencia()
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        SolicitudCrearRetomarResponseDTO solicitudCreada =
                crearRetomarInterno(
                        null,
                        request.getIdAgencia(),
                        request.getIdDatosPersonal(),
                        idUsuario
                );

        SolicitudCreditoGuardarRequestDTO guardarRequest =
                SolicitudCreditoGuardarRequestDTO.builder()

                        .idSolicitudCredito(
                                solicitudCreada.getIdSolicitudCredito()
                        )

                        .idLineaCredito(
                                request.getIdLineaCredito()
                        )

                        .codigoClasificacionCredito(
                                request.getCodigoClasificacionCredito()
                        )

                        .codigoDestinoEconomico(
                                request.getCodigoDestinoEconomico()
                        )

                        .codigoGarantiaCredito(
                                request.getCodigoGarantiaCredito()
                        )

                        .codigoSubgarantia(
                                request.getCodigoSubgarantia()
                        )

                        .idFondoGarantia(
                                request.getIdFondoGarantia()
                        )

                        .codigoFormaPago(
                                request.getCodigoFormaPago()
                        )

                        .periodoCodigoInteres(
                                request.getPeriodoCodigoInteres()
                        )

                        .tipoModalidadInteres(
                                request.getTipoModalidadInteres()
                        )

                        .amortizacionCapital(
                                request.getAmortizacionCapital()
                        )

                        .codigoTipoCuota(
                                request.getCodigoTipoCuota()
                        )

                        .plazoSolicitado(
                                request.getPlazoSolicitado()
                        )

                        .mesesGraciaCapital(
                                request.getMesesGraciaCapital()
                        )

                        .mesesGraciaInteres(
                                request.getMesesGraciaInteres()
                        )

                        .valorSolicitado(
                                request.getValorSolicitado()
                        )

                        .idEmpresaLibranza(
                                request.getIdEmpresaLibranza()
                        )

                        .observacionAsesor(
                                request.getObservacionAsesor()
                        )

                        .build();

        validarGuardarCredito(
                guardarRequest
        );

        return guardarCreditoInterno(
                guardarRequest,
                idUsuario
        );
    }


    // =========================================================
    // CREAR / RETOMAR SOLICITUD
    // =========================================================

    public SolicitudCrearRetomarResponseDTO crearRetomar(
            SolicitudCrearRetomarRequestDTO request
    ) {

        validarCrearRetomar(request);

        usuarioSesionService.validarAgencia(
                request.getIdAgencia()
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return crearRetomarInterno(
                request.getIdSolicitudCredito(),
                request.getIdAgencia(),
                request.getIdDatosPersonal(),
                idUsuario
        );
    }


    private SolicitudCrearRetomarResponseDTO crearRetomarInterno(
            Integer idSolicitudCredito,
            Integer idAgencia,
            Integer idDatosPersonal,
            Integer idUsuario
    ) {

        // ---------------------------------------------------------
        // PERSONA
        // ---------------------------------------------------------

        if (!repository.existePersona(
                idDatosPersonal
        )) {

            throw new IllegalArgumentException(
                    "La persona "
                            + idDatosPersonal
                            + " no existe en hoja de vida."
            );
        }


        // ---------------------------------------------------------
        // CATÁLOGOS
        // ---------------------------------------------------------

        SolicitudCreditoRepository.CatalogoProceso
                procesoIniciada =
                repository.obtenerProcesoIniciada();

        SolicitudCreditoRepository.CatalogoResultado
                resultadoEnCurso =
                repository.obtenerResultadoEnCurso();


        // ---------------------------------------------------------
        // APORTES
        // ---------------------------------------------------------

        SolicitudCreditoRepository.CuentaAportesResumen
                aportes =
                repository.buscarCuentaAportes(
                        idDatosPersonal
                );


        // =========================================================
        // RETOMAR
        // =========================================================

        if (idSolicitudCredito != null) {

            SolicitudCreditoRepository.SolicitudRetomarDatos
                    solicitud =
                    repository.buscarParaRetomar(
                                    idSolicitudCredito
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "La solicitud "
                                                            + idSolicitudCredito
                                                            + " no existe o se encuentra inactiva."
                                            )
                            );

            if (!solicitud.idAgencia().equals(
                    idAgencia
            )
                    || !solicitud.idDatosPersonal().equals(
                    idDatosPersonal
            )) {

                throw new IllegalArgumentException(
                        "La solicitud "
                                + idSolicitudCredito
                                + " no corresponde a la agencia y asociado indicados."
                );
            }

            if (Boolean.TRUE.equals(
                    solicitud.resultadoFinal()
            )) {

                throw new IllegalStateException(
                        "La solicitud "
                                + solicitud.numeroSolicitud()
                                + " tiene resultado final "
                                + solicitud.nombreResultado()
                                + " y no puede retomarse."
                );
            }


            // -----------------------------------------------------
            // DEUDOR PRINCIPAL
            // -----------------------------------------------------

            boolean principalCorrecto =
                    repository.existeDeudorPrincipalCorrecto(
                            idSolicitudCredito,
                            idDatosPersonal
                    );

            if (!principalCorrecto) {

                boolean inconsistencia =
                        repository.existeInconsistenciaDeudorPrincipal(
                                idSolicitudCredito,
                                idDatosPersonal
                        );

                if (inconsistencia) {

                    throw new IllegalStateException(
                            "La solicitud "
                                    + solicitud.numeroSolicitud()
                                    + " presenta inconsistencia en la definición del deudor principal."
                    );
                }

                repository.insertarDeudorPrincipal(
                        idSolicitudCredito,
                        idDatosPersonal,
                        idUsuario
                );
            }


            // -----------------------------------------------------
            // CUENTA DE APORTES
            // -----------------------------------------------------

            Integer idCuentaAportes =
                    solicitud.idCuentaAportes();

            if (idCuentaAportes == null
                    && aportes.cantidad() != null
                    && aportes.cantidad() == 1) {

                idCuentaAportes =
                        aportes.idCuentaAportes();
            }


            // -----------------------------------------------------
            // ACTUALIZAR GESTIÓN
            // -----------------------------------------------------

            var fechaGestion =
                    repository.actualizarRetoma(
                            idSolicitudCredito,
                            idCuentaAportes,
                            idUsuario
                    );

            return SolicitudCrearRetomarResponseDTO.builder()

                    .idSolicitudCredito(
                            solicitud.idSolicitudCredito()
                    )

                    .numeroSolicitud(
                            solicitud.numeroSolicitud()
                    )

                    .accion(
                            "RETOMADA"
                    )

                    .idSolicitudProceso(
                            solicitud.idSolicitudProceso()
                    )

                    .nombreProceso(
                            solicitud.nombreProceso()
                    )

                    .idSolicitudResultado(
                            solicitud.idSolicitudResultado()
                    )

                    .nombreResultado(
                            solicitud.nombreResultado()
                    )

                    .fechaUltimaGestion(
                            fechaGestion
                    )

                    .build();
        }


        // =========================================================
        // CREAR NUEVA SOLICITUD
        // =========================================================

        if (aportes.cantidad() == null
                || aportes.cantidad() == 0) {

            throw new IllegalStateException(
                    "La persona "
                            + idDatosPersonal
                            + " no tiene una cuenta operativa de APORTES SOCIALES."
            );
        }

        if (aportes.cantidad() > 1) {

            throw new IllegalStateException(
                    "La persona "
                            + idDatosPersonal
                            + " tiene más de una cuenta operativa de APORTES SOCIALES."
            );
        }


        // ---------------------------------------------------------
        // CONSECUTIVO
        // ---------------------------------------------------------

        BigDecimal consecutivo =
                repository.siguienteConsecutivo(
                                idAgencia,
                                idUsuario
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No existe el parámetro 600 de consecutivo de solicitudes para la agencia "
                                                        + idAgencia
                                                        + "."
                                        )
                        );

        long consecutivoEntero;

        try {

            consecutivoEntero =
                    consecutivo.longValueExact();

        } catch (ArithmeticException ex) {

            throw new IllegalStateException(
                    "El parámetro 600 de la agencia "
                            + idAgencia
                            + " contiene un valor no entero: "
                            + consecutivo
                            + "."
            );
        }

        if (consecutivoEntero < 1L
                || consecutivoEntero > 9_999_999_999L) {

            throw new IllegalStateException(
                    "El consecutivo de solicitudes "
                            + consecutivo
                            + " está fuera del rango permitido de 10 dígitos."
            );
        }

        String numeroSolicitud =
                String.format(
                        "%010d",
                        consecutivoEntero
                );


        // ---------------------------------------------------------
        // CABECERA
        // ---------------------------------------------------------

        Integer idCuentaAportes =
                aportes.cantidad() == 1
                        ? aportes.idCuentaAportes()
                        : null;

        SolicitudCreditoRepository.SolicitudCreadaDatos
                solicitudCreada =
                repository.crearCabecera(
                        numeroSolicitud,
                        idAgencia,
                        idDatosPersonal,
                        idCuentaAportes,
                        procesoIniciada.idSolicitudProceso(),
                        resultadoEnCurso.idSolicitudResultado(),
                        idUsuario
                );


        // ---------------------------------------------------------
        // DEUDOR PRINCIPAL
        // ---------------------------------------------------------

        repository.insertarDeudorPrincipal(
                solicitudCreada.idSolicitudCredito(),
                idDatosPersonal,
                idUsuario
        );


        // ---------------------------------------------------------
        // RESPUESTA
        // ---------------------------------------------------------

        return SolicitudCrearRetomarResponseDTO.builder()

                .idSolicitudCredito(
                        solicitudCreada.idSolicitudCredito()
                )

                .numeroSolicitud(
                        numeroSolicitud
                )

                .accion(
                        "CREADA"
                )

                .idSolicitudProceso(
                        procesoIniciada.idSolicitudProceso()
                )

                .nombreProceso(
                        procesoIniciada.nombreProceso()
                )

                .idSolicitudResultado(
                        resultadoEnCurso.idSolicitudResultado()
                )

                .nombreResultado(
                        resultadoEnCurso.nombreResultado()
                )

                .fechaUltimaGestion(
                        solicitudCreada.fechaUltimaGestion()
                )

                .build();
    }


    // =========================================================
    // GUARDAR DATOS DEL CRÉDITO
    // =========================================================

    public SolicitudCreditoGuardarResponseDTO guardarCredito(
            SolicitudCreditoGuardarRequestDTO request
    ) {

        validarGuardarCredito(request);
        validarCatalogosActivos(request);


        SolicitudCreditoDetalleDTO solicitud =
                repository.buscarPorId(
                                request.getIdSolicitudCredito()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No existe la solicitud de crédito con id "
                                                        + request.getIdSolicitudCredito()
                                                        + "."
                                        )
                        );

        usuarioSesionService.validarAgencia(
                solicitud.getIdAgencia()
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return guardarCreditoInterno(
                request,
                idUsuario
        );
    }


    private SolicitudCreditoGuardarResponseDTO guardarCreditoInterno(
            SolicitudCreditoGuardarRequestDTO request,
            Integer idUsuario
    ) {

        // ---------------------------------------------------------
        // SOLICITUD
        // ---------------------------------------------------------

        SolicitudCreditoRepository.SolicitudGuardarContexto
                solicitud =
                repository.buscarContextoGuardar(
                                request.getIdSolicitudCredito()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La solicitud "
                                                        + request.getIdSolicitudCredito()
                                                        + " no existe o se encuentra inactiva."
                                        )
                        );

        if (Boolean.TRUE.equals(
                solicitud.resultadoFinal()
        )) {

            throw new IllegalStateException(
                    "La solicitud "
                            + solicitud.numeroSolicitud()
                            + " tiene un resultado final y no puede modificarse."
            );
        }


        // ---------------------------------------------------------
        // TIPO DE GARANTÍA
        // ---------------------------------------------------------

        String tipoGarantia =
                repository.buscarTipoGarantia(
                                request.getCodigoGarantiaCredito()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La garantía "
                                                        + request.getCodigoGarantiaCredito()
                                                        + " no existe o se encuentra inactiva."
                                        )
                        );

        // ---------------------------------------------------------
// FONDO DE GARANTÍAS
// ---------------------------------------------------------

        SolicitudCreditoRepository.FondoGarantiaAplicable fondoGarantia =
                repository.buscarFondoGarantia(
                                request.getIdFondoGarantia()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "El fondo de garantías seleccionado no existe "
                                                        + "o se encuentra inactivo."
                                        )
                        );

        BigDecimal porcentajeFondoAplicado =
                fondoGarantia.porcentajeFondo();

        if (porcentajeFondoAplicado == null
                || porcentajeFondoAplicado.signum() < 0) {

            throw new IllegalStateException(
                    "El fondo de garantías "
                            + fondoGarantia.nombreFondo()
                            + " no tiene un porcentaje válido."
            );
        }

        BigDecimal valorFondoGarantia =
                request.getValorSolicitado()
                        .multiply(porcentajeFondoAplicado)
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        // ---------------------------------------------------------
        // SMMLV
        // ---------------------------------------------------------

        BigDecimal valorSmmlv =
                obtenerSmmlvValido(
                        solicitud.idAgencia()
                );

        BigDecimal cantidadSmmlv =
                request.getValorSolicitado()
                        .divide(
                                valorSmmlv,
                                4,
                                RoundingMode.HALF_UP
                        );


        // ---------------------------------------------------------
        // APORTES
        // ---------------------------------------------------------

        BigDecimal valorAportesActual =
                repository.buscarSaldoAportes(
                                solicitud.idCuentaAportes()
                        )
                        .orElse(null);

        BigDecimal valorAportesInicio =
                solicitud.valorAportesInicio() != null
                        ? solicitud.valorAportesInicio()
                        : valorAportesActual;


        // ---------------------------------------------------------
        // CONDICIÓN INICIAL
        // ---------------------------------------------------------

        List<SolicitudCreditoRepository.CondicionInicialAplicable>
                condiciones =
                repository.buscarCondicionesAplicables(
                        request.getIdLineaCredito(),
                        tipoGarantia,
                        request.getCodigoFormaPago(),
                        request.getPlazoSolicitado(),
                        cantidadSmmlv
                );

        if (condiciones.size() > 1) {

            throw new IllegalStateException(
                    "Existe más de una condición inicial aplicable para línea "
                            + request.getIdLineaCredito()
                            + ", garantía "
                            + tipoGarantia
                            + ", forma de pago "
                            + request.getCodigoFormaPago()
                            + ", plazo "
                            + request.getPlazoSolicitado()
                            + " y "
                            + cantidadSmmlv
                            + " SMMLV."
            );
        }

        SolicitudCreditoRepository.CondicionInicialAplicable
                condicion =
                condiciones.isEmpty()
                        ? null
                        : condiciones.get(0);


        // ---------------------------------------------------------
        // RECIPROCIDAD
        // ---------------------------------------------------------

        BigDecimal factorAportes =
                condicion != null
                        ? condicion.factorReciprocidadAportes()
                        : null;

        BigDecimal cupoMaximo =
                null;

        BigDecimal aportesRequerido =
                null;

        Boolean cumpleAportes =
                null;

        if (factorAportes != null
                && factorAportes.signum() > 0
                && valorAportesInicio != null) {

            cupoMaximo =
                    valorAportesInicio
                            .multiply(
                                    factorAportes
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            aportesRequerido =
                    request.getValorSolicitado()
                            .divide(
                                    factorAportes,
                                    2,
                                    RoundingMode.HALF_UP
                            );

            cumpleAportes =
                    valorAportesInicio.compareTo(
                            aportesRequerido
                    ) >= 0;
        }


        // ---------------------------------------------------------
        // TASA
        // ---------------------------------------------------------

        SolicitudCreditoRepository.TasaAplicable tasa =
                obtenerTasaColocacionAplicable(
                        request.getIdLineaCredito(),
                        tipoGarantia,
                        request.getAmortizacionCapital(),
                        request.getPlazoSolicitado()
                );

        // ---------------------------------------------------------
        // MODALIDAD DE INTERÉS
        // ---------------------------------------------------------

        Integer periodoMeses =
                repository.buscarPeriodoMeses(
                                request.getPeriodoCodigoInteres(),
                                request.getTipoModalidadInteres()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La modalidad de intereses "
                                                        + request.getPeriodoCodigoInteres()
                                                        + "/"
                                                        + request.getTipoModalidadInteres()
                                                        + " no existe o se encuentra inactiva."
                                        )
                        );

        if (periodoMeses <= 0) {

            throw new IllegalStateException(
                    "La modalidad de intereses "
                            + request.getPeriodoCodigoInteres()
                            + "/"
                            + request.getTipoModalidadInteres()
                            + " no tiene un período en meses válido."
            );
        }


        // ---------------------------------------------------------
        // TASA EFECTIVA ANUAL
        // ---------------------------------------------------------

        BigDecimal tasaEfectivaAnual =
                TasasFinancieras.tasaEfectivaAnual(
                        tasa.tasaColocacion(),
                        periodoMeses
                );

        // ---------------------------------------------------------
        // CUOTA PROYECTADA
        // ---------------------------------------------------------

        BigDecimal valorCuotaProyectada =
                null;

        String codigoTipoCuota =
                request.getCodigoTipoCuota().trim();

        if ("1".equals(
                codigoTipoCuota
        )) {

            valorCuotaProyectada =
                    CuotasFinancieras.cuotaFija(
                            periodoMeses,
                            request.getPlazoSolicitado(),
                            tasa.tasaColocacion(),
                            request.getValorSolicitado()
                    );

        } else if ("2".equals(codigoTipoCuota)
                || "3".equals(codigoTipoCuota)) {

            int numeroCuotasCapital =
                    request.getPlazoSolicitado()
                            / request.getAmortizacionCapital();

            if (numeroCuotasCapital <= 0) {

                throw new IllegalStateException(
                        "No es posible calcular el número de cuotas de capital para plazo "
                                + request.getPlazoSolicitado()
                                + " y amortización "
                                + request.getAmortizacionCapital()
                                + "."
                );
            }

            CuotaVariableResultado cuotaVariable =
                    CuotasFinancieras.cuotaVariable(
                            request.getValorSolicitado(),
                            numeroCuotasCapital
                    );

            valorCuotaProyectada =
                    cuotaVariable.valorCuotaRegular();
        }


        // ---------------------------------------------------------
        // FOTOGRAFÍA A PERSISTIR
        // ---------------------------------------------------------

        SolicitudCreditoRepository.PersistenciaCredito
                persistencia =
                new SolicitudCreditoRepository.PersistenciaCredito(

                        condicion != null
                                ? condicion.idCondicionInicial()
                                : null,

                        condicion != null
                                ? condicion.idCondicionInicialDetalle()
                                : null,

                        condicion != null
                                ? condicion.plazoMinimo()
                                : null,

                        condicion != null
                                ? condicion.plazoMaximo()
                                : null,

                        condicion != null
                                ? condicion.cantidadSmmlvMinimo()
                                : null,

                        condicion != null
                                ? condicion.cantidadSmmlvMaximo()
                                : null,

                        factorAportes,

                        valorSmmlv,

                        cantidadSmmlv,

                        valorAportesInicio,

                        cumpleAportes,

                        cupoMaximo,

                        aportesRequerido,

                        tasa.idTasaColocacionDetalle(),

                        tasa.tasaColocacion(),

                        tasaEfectivaAnual,

                        valorCuotaProyectada,

                        fondoGarantia.idFondoGarantia(),

                        porcentajeFondoAplicado,

                        "D",

                        valorFondoGarantia
                );


        // ---------------------------------------------------------
        // PERSISTIR
        // ---------------------------------------------------------

        SolicitudCreditoGuardarResponseDTO respuesta =
                repository.guardarCredito(
                        request,
                        persistencia,
                        idUsuario
                );


        // ---------------------------------------------------------
        // FOTOGRAFIAR ENTE APROBADOR
        // ---------------------------------------------------------

        repository.fotografiarEnteAprobador(
                request.getIdSolicitudCredito(),
                idUsuario
        );

        return respuesta;
    }


    // =========================================================
    // LISTAR SOLICITUDES
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudCreditoResumenDTO> listar() {

        List<SolicitudCreditoResumenDTO> solicitudes =
                repository.listar();

        if (usuarioSesionService.tieneAccesoTotal()) {
            return solicitudes;
        }

        List<Integer> agenciasUsuario =
                usuarioSesionService.agencias();

        if (agenciasUsuario == null
                || agenciasUsuario.isEmpty()) {

            return List.of();
        }

        Set<Integer> agenciasPermitidas =
                Set.copyOf(
                        agenciasUsuario
                );

        return solicitudes.stream()

                .filter(
                        solicitud ->
                                solicitud.getIdAgencia() != null
                                        && agenciasPermitidas.contains(
                                        solicitud.getIdAgencia()
                                )
                )

                .toList();
    }


    // =========================================================
    // BUSCAR SOLICITUD POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public Optional<SolicitudCreditoDetalleDTO> buscarPorId(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitudCredito(
                idSolicitudCredito
        );

        Optional<SolicitudCreditoDetalleDTO> solicitud =
                repository.buscarPorId(
                        idSolicitudCredito
                );

        solicitud.ifPresent(
                detalle ->
                        usuarioSesionService.validarAgencia(
                                detalle.getIdAgencia()
                        )
        );

        return solicitud;
    }


    // =========================================================
    // VALIDACIONES - CREAR SOLICITUD
    // =========================================================

    private void validarCrearSolicitud(
            SolicitudCreditoCrearRequestDTO request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La información para crear la solicitud es obligatoria."
            );
        }

        if (request.getIdAgencia() == null
                || request.getIdAgencia() <= 0) {

            throw new IllegalArgumentException(
                    "La agencia no es válida."
            );
        }

        if (request.getIdDatosPersonal() == null
                || request.getIdDatosPersonal() <= 0) {

            throw new IllegalArgumentException(
                    "La persona solicitante no es válida."
            );
        }

        SolicitudCreditoGuardarRequestDTO guardarRequest =
                SolicitudCreditoGuardarRequestDTO.builder()

                        .idSolicitudCredito(
                                1
                        )

                        .idLineaCredito(
                                request.getIdLineaCredito()
                        )

                        .codigoClasificacionCredito(
                                request.getCodigoClasificacionCredito()
                        )

                        .codigoDestinoEconomico(
                                request.getCodigoDestinoEconomico()
                        )

                        .codigoGarantiaCredito(
                                request.getCodigoGarantiaCredito()
                        )

                        .codigoSubgarantia(
                                request.getCodigoSubgarantia()
                        )

                        .idFondoGarantia(
                                request.getIdFondoGarantia()
                        )

                        .codigoFormaPago(
                                request.getCodigoFormaPago()
                        )

                        .periodoCodigoInteres(
                                request.getPeriodoCodigoInteres()
                        )

                        .tipoModalidadInteres(
                                request.getTipoModalidadInteres()
                        )

                        .amortizacionCapital(
                                request.getAmortizacionCapital()
                        )

                        .codigoTipoCuota(
                                request.getCodigoTipoCuota()
                        )

                        .plazoSolicitado(
                                request.getPlazoSolicitado()
                        )

                        .mesesGraciaCapital(
                                request.getMesesGraciaCapital()
                        )

                        .mesesGraciaInteres(
                                request.getMesesGraciaInteres()
                        )

                        .valorSolicitado(
                                request.getValorSolicitado()
                        )

                        .idEmpresaLibranza(
                                request.getIdEmpresaLibranza()
                        )

                        .observacionAsesor(
                                request.getObservacionAsesor()
                        )

                        .build();

        // =========================================================
        // 1. VALIDACIONES ESTRUCTURALES
        // =========================================================

        validarGuardarCredito(
                guardarRequest
        );

        // =========================================================
        // 2. CATÁLOGOS ACTIVOS
        // =========================================================

        validarCatalogosActivos(
                guardarRequest
        );

        // =========================================================
        // 3. TIPO DE GARANTÍA
        // =========================================================

        String tipoGarantia =
                repository.buscarTipoGarantia(
                                request.getCodigoGarantiaCredito()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La garantía "
                                                        + request.getCodigoGarantiaCredito()
                                                        + " no existe o se encuentra inactiva."
                                        )
                        );

        // =========================================================
        // 4. SMMLV DE LA AGENCIA
        // =========================================================

        BigDecimal valorSmmlv =
                obtenerSmmlvValido(
                        request.getIdAgencia()
                );

        BigDecimal cantidadSmmlv =
                request.getValorSolicitado()
                        .divide(
                                valorSmmlv,
                                4,
                                RoundingMode.HALF_UP
                        );

        // =========================================================
        // 5. CONDICIÓN INICIAL APLICABLE
        // =========================================================

        obtenerCondicionInicialAplicable(
                request.getIdLineaCredito(),
                tipoGarantia,
                request.getCodigoFormaPago(),
                request.getPlazoSolicitado(),
                cantidadSmmlv
        );

        // =========================================================
        // 6. TASA DE COLOCACIÓN SUGERIDA
        // =========================================================

        obtenerTasaColocacionAplicable(
                request.getIdLineaCredito(),
                tipoGarantia,
                request.getAmortizacionCapital(),
                request.getPlazoSolicitado()
        );

        // =========================================================
        // 7. MODALIDAD DE INTERÉS / PERÍODO
        // =========================================================

        Integer periodoMeses =
                repository.buscarPeriodoMeses(
                                request.getPeriodoCodigoInteres(),
                                request.getTipoModalidadInteres()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La modalidad de intereses "
                                                        + request.getPeriodoCodigoInteres()
                                                        + "/"
                                                        + request.getTipoModalidadInteres()
                                                        + " no existe o se encuentra inactiva."
                                        )
                        );

        if (periodoMeses <= 0) {

            throw new IllegalStateException(
                    "La modalidad de intereses "
                            + request.getPeriodoCodigoInteres()
                            + "/"
                            + request.getTipoModalidadInteres()
                            + " no tiene un período en meses válido."
            );
        }

        if ("1".equals(request.getCodigoTipoCuota().trim())
                && !request.getAmortizacionCapital().equals(periodoMeses)) {

            throw new IllegalArgumentException(
                    "Para cuota fija, la periodicidad del pago de intereses "
                            + "debe ser igual a la amortización de capital."
            );
        }

        // =========================================================
        // 8. TEA
        // =========================================================

        SolicitudCreditoRepository.TasaAplicable tasa =
                obtenerTasaColocacionAplicable(
                        request.getIdLineaCredito(),
                        tipoGarantia,
                        request.getAmortizacionCapital(),
                        request.getPlazoSolicitado()
                );

        TasasFinancieras.tasaEfectivaAnual(
                tasa.tasaColocacion(),
                periodoMeses
        );

    }

    // =========================================================
    // VALIDACIONES - CREAR / RETOMAR
    // =========================================================

    private void validarCrearRetomar(
            SolicitudCrearRetomarRequestDTO request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La información para crear o retomar la solicitud es obligatoria."
            );
        }

        if (request.getIdAgencia() == null
                || request.getIdAgencia() <= 0) {

            throw new IllegalArgumentException(
                    "La agencia no es válida."
            );
        }

        if (request.getIdDatosPersonal() == null
                || request.getIdDatosPersonal() <= 0) {

            throw new IllegalArgumentException(
                    "La persona solicitante no es válida."
            );
        }

        if (request.getIdSolicitudCredito() != null
                && request.getIdSolicitudCredito() <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de la solicitud no es válido."
            );
        }
    }


    // =========================================================
    // VALIDACIONES - GUARDAR CRÉDITO
    // =========================================================

    private void validarGuardarCredito(
            SolicitudCreditoGuardarRequestDTO request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La información del crédito es obligatoria."
            );
        }

        if (request.getIdSolicitudCredito() == null
                || request.getIdSolicitudCredito() <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de la solicitud es obligatorio."
            );
        }

        if (request.getIdLineaCredito() == null
                || request.getIdLineaCredito() <= 0) {

            throw new IllegalArgumentException(
                    "La línea de crédito es obligatoria."
            );
        }

        if (esVacio(
                request.getCodigoClasificacionCredito()
        )) {

            throw new IllegalArgumentException(
                    "La clasificación del crédito es obligatoria."
            );
        }

        if (esVacio(
                request.getCodigoDestinoEconomico()
        )) {

            throw new IllegalArgumentException(
                    "El destino económico es obligatorio."
            );
        }

        if (esVacio(
                request.getCodigoGarantiaCredito()
        )) {

            throw new IllegalArgumentException(
                    "La garantía del crédito es obligatoria."
            );
        }

        if (request.getIdFondoGarantia() == null
                || request.getIdFondoGarantia() <= 0) {

            throw new IllegalArgumentException(
                    "El fondo de garantías es obligatorio."
            );
        }

        if (esVacio(
                request.getCodigoFormaPago()
        )) {

            throw new IllegalArgumentException(
                    "La forma de pago es obligatoria."
            );
        }

        if (esVacio(
                request.getPeriodoCodigoInteres()
        )) {

            throw new IllegalArgumentException(
                    "El período de intereses es obligatorio."
            );
        }

        if (esVacio(
                request.getTipoModalidadInteres()
        )) {

            throw new IllegalArgumentException(
                    "La modalidad de intereses es obligatoria."
            );
        }

        if (request.getAmortizacionCapital() == null
                || request.getAmortizacionCapital() <= 0) {

            throw new IllegalArgumentException(
                    "La amortización de capital debe ser mayor que cero."
            );
        }

        if (esVacio(
                request.getCodigoTipoCuota()
        )) {

            throw new IllegalArgumentException(
                    "El tipo de cuota es obligatorio."
            );
        }

        if (request.getPlazoSolicitado() == null
                || request.getPlazoSolicitado() <= 0) {

            throw new IllegalArgumentException(
                    "El plazo solicitado debe ser mayor que cero."
            );
        }

        if (request.getMesesGraciaCapital() != null
                && request.getMesesGraciaCapital() < 0) {

            throw new IllegalArgumentException(
                    "Los meses de gracia de capital no pueden ser negativos."
            );
        }

        if (request.getMesesGraciaInteres() != null
                && request.getMesesGraciaInteres() < 0) {

            throw new IllegalArgumentException(
                    "Los meses de gracia de intereses no pueden ser negativos."
            );
        }

        if (request.getValorSolicitado() == null
                || request.getValorSolicitado()
                .compareTo(java.math.BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El valor solicitado debe ser mayor que cero."
            );
        }

        if (request.getAmortizacionCapital()
                > request.getPlazoSolicitado()) {

            throw new IllegalArgumentException(
                    "La amortización de capital no puede ser mayor que el plazo solicitado."
            );
        }

    }


    // =========================================================
    // VALIDACIÓN ID SOLICITUD
    // =========================================================

    private void validarIdSolicitudCredito(
            Integer idSolicitudCredito
    ) {

        if (idSolicitudCredito == null) {

            throw new IllegalArgumentException(
                    "El identificador de la solicitud es obligatorio."
            );
        }

        if (idSolicitudCredito <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de la solicitud no es válido."
            );
        }
    }

    private BigDecimal obtenerSmmlvValido(
            Integer idAgencia
    ) {

        BigDecimal valorSmmlv =
                repository.buscarSmmlv(
                                idAgencia
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No existe un SALARIO MINIMO válido, parámetro 50, para la agencia "
                                                        + idAgencia
                                                        + "."
                                        )
                        );

        if (valorSmmlv.signum() <= 0) {

            throw new IllegalStateException(
                    "El parámetro 50 SALARIO MINIMO de la agencia "
                            + idAgencia
                            + " debe ser mayor que cero."
            );
        }

        return valorSmmlv;
    }


    // =========================================================
    // SOPORTE
    // =========================================================

    private boolean esVacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }

    // =========================================================
// VALIDAR CATÁLOGOS ACTIVOS
// =========================================================

    private void validarCatalogosActivos(
            SolicitudCreditoGuardarRequestDTO request
    ) {

        if (!repository.existeLineaCreditoActiva(
                request.getIdLineaCredito()
        )) {

            throw new IllegalArgumentException(
                    "La línea de crédito no existe o se encuentra inactiva."
            );
        }


        if (!repository.existeClasificacionCreditoActiva(
                request.getCodigoClasificacionCredito()
        )) {

            throw new IllegalArgumentException(
                    "La clasificación del crédito no existe o se encuentra inactiva."
            );
        }


        if (!repository.existeDestinoEconomicoActivo(
                request.getCodigoDestinoEconomico()
        )) {

            throw new IllegalArgumentException(
                    "El destino económico no existe o se encuentra inactivo."
            );
        }


        if (!repository.existeGarantiaCreditoActiva(
                request.getCodigoGarantiaCredito()
        )) {

            throw new IllegalArgumentException(
                    "La garantía del crédito no existe o se encuentra inactiva."
            );
        }


        if (!esVacio(request.getCodigoSubgarantia())
                && !repository.existeSubgarantiaCreditoActiva(
                request.getCodigoSubgarantia()
        )) {

            throw new IllegalArgumentException(
                    "La subgarantía del crédito no existe o se encuentra inactiva."
            );
        }


        if (!repository.existeFormaPagoActiva(
                request.getCodigoFormaPago()
        )) {

            throw new IllegalArgumentException(
                    "La forma de pago no existe o se encuentra inactiva."
            );
        }


        if (!repository.existeModalidadInteresActiva(
                request.getPeriodoCodigoInteres(),
                request.getTipoModalidadInteres()
        )) {

            throw new IllegalArgumentException(
                    "La modalidad de intereses no existe o se encuentra inactiva."
            );
        }


        if (!repository.existeTipoCuotaActivo(
                request.getCodigoTipoCuota()
        )) {

            throw new IllegalArgumentException(
                    "El tipo de cuota no existe o se encuentra inactivo."
            );
        }
    }

    private SolicitudCreditoRepository.CondicionInicialAplicable
    obtenerCondicionInicialAplicable(
            Integer idLineaCredito,
            String tipoGarantia,
            String codigoFormaPago,
            Integer plazoSolicitado,
            BigDecimal cantidadSmmlv
    ) {

        List<SolicitudCreditoRepository.CondicionInicialAplicable> condiciones =
                repository.buscarCondicionesAplicables(
                        idLineaCredito,
                        tipoGarantia,
                        codigoFormaPago,
                        plazoSolicitado,
                        cantidadSmmlv
                );

        if (condiciones.isEmpty()) {

            throw new IllegalStateException(
                    "No existe una condición inicial aplicable para línea "
                            + idLineaCredito
                            + ", garantía "
                            + tipoGarantia
                            + ", forma de pago "
                            + codigoFormaPago
                            + ", plazo "
                            + plazoSolicitado
                            + " y "
                            + cantidadSmmlv
                            + " SMMLV."
            );
        }

        if (condiciones.size() > 1) {

            throw new IllegalStateException(
                    "Existe más de una condición inicial aplicable para línea "
                            + idLineaCredito
                            + ", garantía "
                            + tipoGarantia
                            + ", forma de pago "
                            + codigoFormaPago
                            + ", plazo "
                            + plazoSolicitado
                            + " y "
                            + cantidadSmmlv
                            + " SMMLV."
            );
        }

        return condiciones.get(0);
    }

    private SolicitudCreditoRepository.TasaAplicable
    obtenerTasaColocacionAplicable(
            Integer idLineaCredito,
            String tipoGarantia,
            Integer amortizacionCapital,
            Integer plazoSolicitado
    ) {

        List<SolicitudCreditoRepository.TasaAplicable> tasas =
                repository.buscarTasasAplicables(
                        idLineaCredito,
                        tipoGarantia,
                        amortizacionCapital,
                        plazoSolicitado
                );

        if (tasas.isEmpty()) {

            throw new IllegalStateException(
                    "No existe una tasa de colocación aplicable para línea "
                            + idLineaCredito
                            + ", garantía "
                            + tipoGarantia
                            + ", amortización "
                            + amortizacionCapital
                            + " y plazo "
                            + plazoSolicitado
                            + "."
            );
        }

        if (tasas.size() > 1) {

            throw new IllegalStateException(
                    "Existe más de una tasa de colocación aplicable para línea "
                            + idLineaCredito
                            + ", garantía "
                            + tipoGarantia
                            + ", amortización "
                            + amortizacionCapital
                            + " y plazo "
                            + plazoSolicitado
                            + "."
            );
        }

        SolicitudCreditoRepository.TasaAplicable tasa =
                tasas.get(0);

        if (tasa.tasaColocacion() == null) {

            throw new IllegalStateException(
                    "La tasa de colocación parametrizada no tiene un valor definido."
            );
        }

        return tasa;
    }

}
