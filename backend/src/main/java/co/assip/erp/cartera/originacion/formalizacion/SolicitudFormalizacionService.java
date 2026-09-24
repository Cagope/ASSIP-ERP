package co.assip.erp.cartera.originacion.formalizacion;

import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionDetalleDTO;
import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionGuardarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.SolicitudCreditoRepository;

import co.assip.erp.seguridad.service.UsuarioSesionService;

import co.assip.erp.shared.financiero.CuotasFinancieras;
import co.assip.erp.shared.financiero.TasasFinancieras;
import co.assip.erp.shared.financiero.dto.CuotaVariableResultado;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
public class SolicitudFormalizacionService {

    private static final int PROCESO_FORMALIZACION = 4;
    private static final int RESULTADO_APROBADA = 1;

    private final SolicitudFormalizacionRepository repository;

    private final SolicitudFormalizacionValidacionService validacionService;

    private final SolicitudCreditoRepository solicitudCreditoRepository;

    private final UsuarioSesionService usuarioSesionService;

    public SolicitudFormalizacionService(
            SolicitudFormalizacionRepository repository,
            SolicitudFormalizacionValidacionService validacionService,
            SolicitudCreditoRepository solicitudCreditoRepository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.validacionService = validacionService;
        this.solicitudCreditoRepository = solicitudCreditoRepository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // CONSULTAR SOLICITUD PARA FORMALIZACIÓN
    //
    // No modifica información.
    // Recupera las condiciones solicitadas y las formalizadas.
    // =========================================================

    @Transactional(readOnly = true)
    public SolicitudFormalizacionDetalleDTO consultar(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(idSolicitudCredito);

        return repository.buscarPorId(idSolicitudCredito)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe la solicitud de crédito "
                                        + idSolicitudCredito
                        )
                );
    }

    // =========================================================
    // GUARDAR FORMALIZACIÓN
    //
    // 1. Consulta la solicitud.
    // 2. Valida que esté en FORMALIZACIÓN.
    // 3. Valida las condiciones recibidas.
    // 4. Calcula TEA y cuota.
    // 5. Identifica modificaciones.
    // 6. Guarda las condiciones definitivas.
    //
    // Guardar NO envía todavía a DESEMBOLSO.
    // =========================================================

    @Transactional
    public SolicitudFormalizacionDetalleDTO guardar(
            Integer idSolicitudCredito,
            SolicitudFormalizacionGuardarRequestDTO request
    ) {

        validarIdSolicitud(idSolicitudCredito);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Las condiciones de formalización son obligatorias."
            );
        }

        SolicitudFormalizacionDetalleDTO solicitud =
                consultar(idSolicitudCredito);

        validarProcesoFormalizacion(solicitud);

        validarCondiciones(request);

        var validacion = validacionService.validar(
                idSolicitudCredito,
                request
        );

        if (!validacion.puedeContinuar()) {

            String detalle = validacion.bloqueos()
                    .stream()
                    .map(
                            SolicitudFormalizacionValidacionService
                                    .BloqueoFormalizacion::mensaje
                    )
                    .collect(java.util.stream.Collectors.joining("; "));

            throw new IllegalStateException(
                    "No es posible guardar la formalización. "
                            + detalle
            );
        }

        // =====================================================
        // PERIODO DE INTERESES
        //
        // Se reutiliza el catálogo y la consulta existente
        // del módulo de solicitudes.
        // =====================================================

        Integer periodoMeses =
                solicitudCreditoRepository.buscarPeriodoMeses(
                                request.getPeriodoCodigoInteresFormalizado(),
                                request.getTipoModalidadInteresFormalizado()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "La modalidad de intereses seleccionada "
                                                + "no existe o está inactiva."
                                )
                        );

        if (periodoMeses <= 0) {
            throw new IllegalArgumentException(
                    "La modalidad de intereses no tiene "
                            + "un período válido."
            );
        }

        // =====================================================
        // VALIDACIÓN DE CUOTA FIJA
        //
        // Misma regla utilizada en SolicitudCreditoService.
        // =====================================================

        String codigoTipoCuota =
                request.getCodigoTipoCuotaFormalizada().trim();

        if ("1".equals(codigoTipoCuota)
                && !Objects.equals(
                request.getAmortizacionCapitalFormalizada(),
                periodoMeses
        )) {

            throw new IllegalArgumentException(
                    "Para cuota fija, la periodicidad del pago "
                            + "de intereses debe ser igual a la "
                            + "amortización de capital."
            );
        }

        // =====================================================
        // TASA EFECTIVA ANUAL DEFINITIVA
        // =====================================================

        BigDecimal tasaEfectivaAnual =
                TasasFinancieras.tasaEfectivaAnual(
                        request.getTasaNominalFormalizada(),
                        periodoMeses
                );

        if (tasaEfectivaAnual == null) {
            throw new IllegalStateException(
                    "No fue posible calcular la tasa efectiva anual."
            );
        }

        tasaEfectivaAnual =
                tasaEfectivaAnual.setScale(
                        4,
                        RoundingMode.HALF_UP
                );

        // =====================================================
        // CUOTA DEFINITIVA
        //
        // Se conserva la misma metodología de originación.
        // =====================================================

        BigDecimal valorCuota =
                calcularCuota(
                        request,
                        periodoMeses
                );

        // =====================================================
        // IDENTIFICAR CAMBIOS
        //
        // Se compara contra las condiciones originales
        // de la solicitud, no contra el último guardado.
        // =====================================================

        boolean condicionesModificadas =
                identificarCondicionesModificadas(
                        solicitud,
                        request
                );

        // =====================================================
        // AUDITORÍA
        // =====================================================

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // PERSISTENCIA
        // =====================================================

        int actualizados =
                repository.guardarCondiciones(
                        idSolicitudCredito,
                        request,
                        tasaEfectivaAnual,
                        valorCuota,
                        condicionesModificadas,
                        idUsuario
                );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No fue posible guardar la formalización. "
                            + "Verifique que la solicitud continúe "
                            + "en Formalización y que usted sea "
                            + "el asesor responsable."
            );
        }

        // =====================================================
        // DEVOLVER CONDICIONES GUARDADAS
        // =====================================================

        return consultar(idSolicitudCredito);
    }

    // =========================================================
    // FINALIZAR FORMALIZACIÓN
    //
    // Proceso 4 -> Proceso 5
    //
    // El crédito ya debe estar constituido en estado P.
    // Finalizar únicamente traslada la solicitud
    // del proceso 4 - FORMALIZACIÓN
    // al proceso 5 - DESEMBOLSO.
    // =========================================================

    @Transactional
    public SolicitudFormalizacionDetalleDTO finalizar(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(idSolicitudCredito);

        SolicitudFormalizacionDetalleDTO solicitud =
                consultar(idSolicitudCredito);

        validarProcesoFormalizacion(solicitud);

        validarFormalizacionCompleta(solicitud);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int actualizados =
                repository.finalizarFormalizacion(
                        idSolicitudCredito,
                        idUsuario
                );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No fue posible finalizar la formalización. "
                            + "Verifique las condiciones definitivas "
                            + "y el estado de la solicitud."
            );
        }

        return consultar(idSolicitudCredito);
    }

    // =========================================================
    // CÁLCULO DE CUOTA
    // =========================================================

    private BigDecimal calcularCuota(
            SolicitudFormalizacionGuardarRequestDTO request,
            Integer periodoMeses
    ) {

        String codigoTipoCuota =
                request.getCodigoTipoCuotaFormalizada().trim();

        // -----------------------------------------------------
        // CUOTA FIJA
        // -----------------------------------------------------

        if ("1".equals(codigoTipoCuota)) {

            return CuotasFinancieras.cuotaFija(
                    periodoMeses,
                    request.getPlazoFormalizado(),
                    request.getTasaNominalFormalizada(),
                    request.getValorFormalizado()
            );
        }

        // -----------------------------------------------------
        // CUOTA VARIABLE / OTRA
        //
        // Se conserva la metodología utilizada actualmente
        // por SolicitudCreditoService.
        // -----------------------------------------------------

        if ("2".equals(codigoTipoCuota)
                || "3".equals(codigoTipoCuota)) {

            int numeroCuotasCapital =
                    request.getPlazoFormalizado()
                            / request.getAmortizacionCapitalFormalizada();

            if (numeroCuotasCapital <= 0) {

                throw new IllegalArgumentException(
                        "No es posible calcular las cuotas de capital "
                                + "con el plazo y la amortización "
                                + "seleccionados."
                );
            }

            CuotaVariableResultado resultado =
                    CuotasFinancieras.cuotaVariable(
                            request.getValorFormalizado(),
                            numeroCuotasCapital
                    );

            if (resultado == null
                    || resultado.valorCuotaRegular() == null) {

                throw new IllegalStateException(
                        "No fue posible calcular la cuota variable."
                );
            }

            return resultado.valorCuotaRegular();
        }

        throw new IllegalArgumentException(
                "El tipo de cuota seleccionado no es válido."
        );
    }

    // =========================================================
    // IDENTIFICAR CONDICIONES MODIFICADAS
    // =========================================================

    private boolean identificarCondicionesModificadas(
            SolicitudFormalizacionDetalleDTO solicitud,
            SolicitudFormalizacionGuardarRequestDTO request
    ) {

        return diferente(
                solicitud.getValorSolicitado(),
                request.getValorFormalizado()
        )

                || !Objects.equals(
                solicitud.getPlazoSolicitado(),
                request.getPlazoFormalizado()
        )

                || !Objects.equals(
                normalizar(solicitud.getCodigoFormaPago()),
                normalizar(request.getCodigoFormaPagoFormalizada())
        )

                || !Objects.equals(
                normalizar(solicitud.getPeriodoCodigoInteres()),
                normalizar(request.getPeriodoCodigoInteresFormalizado())
        )

                || !Objects.equals(
                normalizar(solicitud.getTipoModalidadInteres()),
                normalizar(request.getTipoModalidadInteresFormalizado())
        )

                || !Objects.equals(
                solicitud.getAmortizacionCapital(),
                request.getAmortizacionCapitalFormalizada()
        )

                || !Objects.equals(
                normalizar(solicitud.getCodigoTipoCuota()),
                normalizar(request.getCodigoTipoCuotaFormalizada())
        )

                || !Objects.equals(
                solicitud.getMesesGraciaCapital(),
                request.getMesesGraciaCapitalFormalizados()
        )

                || !Objects.equals(
                solicitud.getMesesGraciaInteres(),
                request.getMesesGraciaInteresFormalizados()
        )

                || diferente(
                solicitud.getTasaColocacionAplicada(),
                request.getTasaNominalFormalizada()
        );
    }

    // =========================================================
    // VALIDAR CONDICIONES
    // =========================================================

    private void validarCondiciones(
            SolicitudFormalizacionGuardarRequestDTO request
    ) {

        if (request.getValorFormalizado() == null
                || request.getValorFormalizado()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El valor formalizado debe ser mayor que cero."
            );
        }

        if (request.getPlazoFormalizado() == null
                || request.getPlazoFormalizado() <= 0) {

            throw new IllegalArgumentException(
                    "El plazo formalizado debe ser mayor que cero."
            );
        }

        if (request.getTasaNominalFormalizada() == null
                || request.getTasaNominalFormalizada()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "La tasa nominal no puede ser negativa."
            );
        }

        if (request.getAmortizacionCapitalFormalizada() == null
                || request.getAmortizacionCapitalFormalizada() <= 0) {

            throw new IllegalArgumentException(
                    "La amortización de capital debe ser mayor que cero."
            );
        }

        if (request.getAmortizacionCapitalFormalizada()
                > request.getPlazoFormalizado()) {

            throw new IllegalArgumentException(
                    "La amortización de capital no puede superar "
                            + "el plazo del crédito."
            );
        }

        if (request.getMesesGraciaCapitalFormalizados() == null
                || request.getMesesGraciaCapitalFormalizados() < 0) {

            throw new IllegalArgumentException(
                    "Los meses de gracia de capital no son válidos."
            );
        }

        if (request.getMesesGraciaInteresFormalizados() == null
                || request.getMesesGraciaInteresFormalizados() < 0) {

            throw new IllegalArgumentException(
                    "Los meses de gracia de intereses no son válidos."
            );
        }

        if (request.getMesesGraciaCapitalFormalizados()
                >= request.getPlazoFormalizado()) {

            throw new IllegalArgumentException(
                    "Los meses de gracia de capital deben ser "
                            + "menores que el plazo."
            );
        }

        if (request.getMesesGraciaInteresFormalizados()
                >= request.getPlazoFormalizado()) {

            throw new IllegalArgumentException(
                    "Los meses de gracia de intereses deben ser "
                            + "menores que el plazo."
            );
        }

        if (vacio(request.getCodigoFormaPagoFormalizada())
                || vacio(request.getPeriodoCodigoInteresFormalizado())
                || vacio(request.getTipoModalidadInteresFormalizado())
                || vacio(request.getCodigoTipoCuotaFormalizada())) {

            throw new IllegalArgumentException(
                    "Debe completar la forma de pago, modalidad "
                            + "de intereses y tipo de cuota."
            );
        }
    }

    // =========================================================
    // VALIDAR ESTADO
    // =========================================================

    private void validarProcesoFormalizacion(
            SolicitudFormalizacionDetalleDTO solicitud
    ) {

        if (!Objects.equals(
                solicitud.getIdSolicitudProceso(),
                PROCESO_FORMALIZACION
        )) {

            throw new IllegalStateException(
                    "La solicitud no se encuentra en Formalización."
            );
        }

        if (!Objects.equals(
                solicitud.getIdSolicitudResultado(),
                RESULTADO_APROBADA
        )) {

            throw new IllegalStateException(
                    "La solicitud no tiene resultado aprobado."
            );
        }

        if (solicitud.getFechaFinFormalizacion() != null) {

            throw new IllegalStateException(
                    "La formalización ya fue finalizada."
            );
        }
    }

    // =========================================================
    // VALIDAR FORMALIZACIÓN COMPLETA
    // =========================================================

    private void validarFormalizacionCompleta(
            SolicitudFormalizacionDetalleDTO solicitud
    ) {

        if (solicitud.getValorFormalizado() == null
                || solicitud.getPlazoFormalizado() == null
                || solicitud.getCodigoFormaPagoFormalizada() == null
                || solicitud.getPeriodoCodigoInteresFormalizado() == null
                || solicitud.getTipoModalidadInteresFormalizado() == null
                || solicitud.getAmortizacionCapitalFormalizada() == null
                || solicitud.getCodigoTipoCuotaFormalizada() == null
                || solicitud.getMesesGraciaCapitalFormalizados() == null
                || solicitud.getMesesGraciaInteresFormalizados() == null
                || solicitud.getTasaNominalFormalizada() == null
                || solicitud.getTasaEfectivaAnualFormalizada() == null
                || solicitud.getValorCuotaFormalizada() == null
                || solicitud.getCondicionesModificadas() == null) {

            throw new IllegalStateException(
                    "Debe guardar las condiciones definitivas "
                            + "antes de finalizar la formalización."
            );
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private void validarIdSolicitud(
            Integer idSolicitudCredito
    ) {

        if (idSolicitudCredito == null
                || idSolicitudCredito <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de la solicitud no es válido."
            );
        }
    }

    private boolean diferente(
            BigDecimal original,
            BigDecimal definitivo
    ) {

        if (original == null || definitivo == null) {
            return !Objects.equals(original, definitivo);
        }

        return original.compareTo(definitivo) != 0;
    }

    private String normalizar(
            String valor
    ) {

        return valor == null
                ? null
                : valor.trim();
    }

    private boolean vacio(
            String valor
    ) {

        return valor == null
                || valor.isBlank();
    }

    // =========================================================
// GENERAR PAGARÉ
// =========================================================

    @Transactional
    public SolicitudFormalizacionDetalleDTO generarPagare(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(idSolicitudCredito);

        Integer idUsuario = usuarioSesionService.idUsuario();

        // 1. Bloquear solicitud para impedir generación simultánea.

        var contexto = repository.bloquearParaGenerarPagare(
                idSolicitudCredito
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "No existe la solicitud de crédito "
                                + idSolicitudCredito
                )
        );

        // 2. Verificar estado y responsable.

        if (!Objects.equals(
                contexto.idSolicitudProceso(),
                PROCESO_FORMALIZACION
        )) {
            throw new IllegalStateException(
                    "La solicitud no se encuentra en Formalización."
            );
        }

        if (!Objects.equals(
                contexto.idSolicitudResultado(),
                RESULTADO_APROBADA
        )) {
            throw new IllegalStateException(
                    "La solicitud no tiene resultado aprobado."
            );
        }

        if (!Objects.equals(
                contexto.idAsesor(),
                idUsuario
        )) {
            throw new IllegalStateException(
                    "La solicitud pertenece a otro asesor."
            );
        }

        if (contexto.formalizacionFinalizada()) {
            throw new IllegalStateException(
                    "La formalización ya fue finalizada."
            );
        }

        if (contexto.idCarteraCredito() != null) {
            throw new IllegalStateException(
                    "La solicitud ya tiene un crédito generado. "
                            + "Utilice la opción Reimprimir pagaré."
            );
        }

        // 3. Consultar condiciones definitivas guardadas.

        SolicitudFormalizacionDetalleDTO solicitud =
                consultar(idSolicitudCredito);

        validarFormalizacionCompleta(solicitud);

        // =====================================================
        // 4. REVALIDAR CONDICIONES DEFINITIVAS GUARDADAS
        // =====================================================

        SolicitudFormalizacionGuardarRequestDTO condicionesGuardadas =
                SolicitudFormalizacionGuardarRequestDTO.builder()

                        .valorFormalizado(
                                solicitud.getValorFormalizado()
                        )

                        .plazoFormalizado(
                                solicitud.getPlazoFormalizado()
                        )

                        .codigoFormaPagoFormalizada(
                                solicitud.getCodigoFormaPagoFormalizada()
                        )

                        .periodoCodigoInteresFormalizado(
                                solicitud.getPeriodoCodigoInteresFormalizado()
                        )

                        .tipoModalidadInteresFormalizado(
                                solicitud.getTipoModalidadInteresFormalizado()
                        )

                        .amortizacionCapitalFormalizada(
                                solicitud.getAmortizacionCapitalFormalizada()
                        )

                        .codigoTipoCuotaFormalizada(
                                solicitud.getCodigoTipoCuotaFormalizada()
                        )

                        .mesesGraciaCapitalFormalizados(
                                solicitud.getMesesGraciaCapitalFormalizados()
                        )

                        .mesesGraciaInteresFormalizados(
                                solicitud.getMesesGraciaInteresFormalizados()
                        )

                        .tasaNominalFormalizada(
                                solicitud.getTasaNominalFormalizada()
                        )

                        .build();

        var validacion = validacionService.validar(
                idSolicitudCredito,
                condicionesGuardadas
        );

        if (!validacion.puedeContinuar()) {

            String detalle = validacion.bloqueos()
                    .stream()
                    .map(
                            SolicitudFormalizacionValidacionService
                                    .BloqueoFormalizacion::mensaje
                    )
                    .collect(
                            java.util.stream.Collectors.joining("; ")
                    );

            throw new IllegalStateException(
                    "No es posible generar el pagaré. "
                            + detalle
            );
        }

        // =====================================================
        // VERIFICAR CONSISTENCIA DE LA TEA GUARDADA
        // =====================================================

        if (validacion.tasa() == null
                || validacion.tasa().tasaEfectivaAnual() == null
                || solicitud.getTasaEfectivaAnualFormalizada()
                .compareTo(
                        validacion.tasa().tasaEfectivaAnual()
                ) != 0) {

            throw new IllegalStateException(
                    "La tasa efectiva anual guardada no coincide "
                            + "con las condiciones definitivas. "
                            + "Revise y guarde nuevamente la formalización."
            );
        }

        // 5. Obtener consecutivo 605.

        Long consecutivo = repository.siguienteConsecutivoPagare(
                contexto.idAgencia(),
                idUsuario
        );

        String pagareCartera = consecutivo.toString();

        // 6. Bloquear si el pagaré ya existe en la agencia.

        if (repository.existePagareEnAgencia(
                contexto.idAgencia(),
                pagareCartera
        )) {

            throw new IllegalStateException(
                    "El pagaré "
                            + pagareCartera
                            + " ya existe en la agencia "
                            + contexto.idAgencia()
                            + ". Revise el parámetro 605. "
                            + "No se generó el crédito."
            );
        }

        // 7. Constituir crédito pendiente de desembolso.

        Integer idCarteraCredito =
                repository.insertarCreditoDesdeFormalizacion(
                        idSolicitudCredito,
                        pagareCartera,
                        idUsuario
                );

        // 8. Vincular crédito con solicitud.

        int vinculados = repository.vincularCreditoGenerado(
                idSolicitudCredito,
                idCarteraCredito,
                idUsuario
        );

        if (vinculados != 1) {
            throw new IllegalStateException(
                    "No fue posible vincular el crédito generado "
                            + "con la solicitud."
            );
        }

        return consultar(idSolicitudCredito);
    }
}