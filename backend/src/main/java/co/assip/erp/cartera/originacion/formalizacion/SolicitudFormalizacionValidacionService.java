package co.assip.erp.cartera.originacion.formalizacion;

import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionGuardarRequestDTO;
import co.assip.erp.cartera.originacion.solicitudes.SolicitudCreditoRepository;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.shared.financiero.TasasFinancieras;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SolicitudFormalizacionValidacionService {

    // =========================================================
    // ESTADOS
    // =========================================================

    private static final int PROCESO_FORMALIZACION = 4;

    private static final int RESULTADO_APROBADA = 1;

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudFormalizacionValidacionRepository repository;

    private final SolicitudCreditoRepository solicitudCreditoRepository;

    private final UsuarioSesionService usuarioSesionService;

    public SolicitudFormalizacionValidacionService(
            SolicitudFormalizacionValidacionRepository repository,
            SolicitudCreditoRepository solicitudCreditoRepository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.solicitudCreditoRepository = solicitudCreditoRepository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // VALIDAR FORMALIZACIÓN
    //
    // Recibe las condiciones digitadas en Angular.
    //
    // NO requiere que estén guardadas previamente.
    //
    // NO guarda condiciones financieras.
    // NO genera pagaré.
    // NO constituye crédito.
    // NO cambia de proceso.
    // =========================================================

    @Transactional
    public ResultadoValidacionFormalizacion validar(
            Integer idSolicitudCredito,
            SolicitudFormalizacionGuardarRequestDTO request
    ) {

        validarIdSolicitud(idSolicitudCredito);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Debe indicar las condiciones de formalización."
            );
        }

        // =====================================================
        // 1. BLOQUEAR SOLICITUD
        // =====================================================

        SolicitudFormalizacionValidacionRepository.SolicitudValidacionDatos solicitud =
                repository.bloquearSolicitud(
                        idSolicitudCredito
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la solicitud de crédito "
                                        + idSolicitudCredito
                        )
                );

        List<BloqueoFormalizacion> bloqueos =
                new ArrayList<>();

        List<AlertaFormalizacion> alertas =
                new ArrayList<>();

        // =====================================================
        // 2. ESTADO DE LA SOLICITUD
        // =====================================================

        boolean solicitudHabilitada =
                validarEstadoSolicitud(
                        solicitud,
                        bloqueos
                );

        // =====================================================
        // 3. CONDICIONES FINANCIERAS
        // =====================================================

        ResultadoTasa resultadoTasa =
                validarTasa(
                        solicitud,
                        request,
                        bloqueos
                );

        // =====================================================
        // 4. PERSONAS VINCULADAS
        // =====================================================

        List<SolicitudFormalizacionValidacionRepository.PersonaSolicitudDatos> personas =
                repository.consultarPersonasSolicitud(
                        idSolicitudCredito
                );

        validarPersonasSolicitud(
                solicitud,
                personas,
                bloqueos
        );

        // =====================================================
        // 5. MORA
        // =====================================================

        List<BloqueoMora> bloqueosMora =
                consultarBloqueosMora(
                        idSolicitudCredito
                );

        for (BloqueoMora mora : bloqueosMora) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "MORA",
                            construirMensajeMora(mora)
                    )
            );
        }

        // =====================================================
        // 6. REGISTRAR VALIDACIÓN DE MORA
        // =====================================================

        if (solicitudHabilitada) {

            actualizarValidacionMora(
                    idSolicitudCredito,
                    personas,
                    bloqueosMora,
                    bloqueos
            );
        }

        // =====================================================
        // 7. APORTES Y RECIPROCIDAD
        //
        // Utiliza el valor recibido desde Angular.
        // =====================================================

        ResultadoAportes resultadoAportes =
                validarAportes(
                        idSolicitudCredito,
                        solicitud,
                        request.getValorFormalizado(),
                        solicitudHabilitada,
                        bloqueos
                );

        // =====================================================
        // 8. SIMULTANEIDAD
        // =====================================================

        validarSimultaneidad(
                idSolicitudCredito,
                solicitud,
                bloqueos,
                alertas
        );

        // =====================================================
        // 9. RESULTADO GENERAL
        // =====================================================

        boolean puedeContinuar =
                bloqueos.isEmpty();

        String mensajeGeneral =
                puedeContinuar
                        ? "La solicitud cumple las validaciones "
                        + "de formalización y puede guardar "
                        + "las condiciones definitivas."

                        : "La solicitud presenta condiciones "
                        + "que impiden continuar con la "
                        + "formalización. Revise los bloqueos.";

        return new ResultadoValidacionFormalizacion(
                solicitud.idSolicitudCredito(),
                solicitud.numeroSolicitud(),
                solicitud.idAgencia(),
                solicitud.idDatosPersonal(),
                solicitud.idLineaCredito(),
                puedeContinuar,
                mensajeGeneral,
                bloqueos.size(),
                alertas.size(),
                List.copyOf(bloqueos),
                List.copyOf(alertas),
                List.copyOf(bloqueosMora),
                resultadoAportes,
                resultadoTasa
        );
    }

    // =========================================================
    // VALIDAR ESTADO
    // =========================================================

    private boolean validarEstadoSolicitud(
            SolicitudFormalizacionValidacionRepository.SolicitudValidacionDatos solicitud,
            List<BloqueoFormalizacion> bloqueos
    ) {

        boolean habilitada = true;

        if (solicitud.idSolicitudProceso() == null
                || solicitud.idSolicitudProceso()
                != PROCESO_FORMALIZACION) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "ESTADO_SOLICITUD",
                            "La solicitud no se encuentra "
                                    + "en el proceso de formalización."
                    )
            );

            habilitada = false;
        }

        if (solicitud.idSolicitudResultado() == null
                || solicitud.idSolicitudResultado()
                != RESULTADO_APROBADA) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "APROBACION",
                            "La solicitud no tiene un resultado "
                                    + "de aprobación válido."
                    )
            );

            habilitada = false;
        }

        if (solicitud.idCarteraCredito() != null) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "CREDITO_EXISTENTE",
                            "La solicitud ya tiene un crédito "
                                    + "constituido. Identificador: "
                                    + solicitud.idCarteraCredito()
                    )
            );

            habilitada = false;
        }

        return habilitada;
    }

    // =========================================================
    // VALIDAR TASA EFECTIVA MÁXIMA LEGAL
    //
    // Parámetro 631 por agencia.
    //
    // La tasa nominal se recibe desde Angular.
    // La TEA se calcula exclusivamente en el backend.
    // =========================================================

    private ResultadoTasa validarTasa(
            SolicitudFormalizacionValidacionRepository.SolicitudValidacionDatos solicitud,
            SolicitudFormalizacionGuardarRequestDTO request,
            List<BloqueoFormalizacion> bloqueos
    ) {

        BigDecimal tasaNominal =
                request.getTasaNominalFormalizada();

        BigDecimal tasaMaxima =
                repository.consultarTasaMaximaLegal(
                        solicitud.idAgencia()
                ).orElse(null);

        if (tasaMaxima == null
                || tasaMaxima.compareTo(BigDecimal.ZERO) <= 0) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "PARAMETRO_TASA_MAXIMA",
                            "La agencia no tiene configurado "
                                    + "correctamente el parámetro "
                                    + "631 - Tasa efectiva máxima legal."
                    )
            );
        }

        if (tasaNominal == null
                || tasaNominal.compareTo(BigDecimal.ZERO) < 0) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "TASA_NOMINAL",
                            "La tasa nominal formalizada "
                                    + "no es válida."
                    )
            );

            return new ResultadoTasa(
                    tasaNominal,
                    null,
                    tasaMaxima,
                    false
            );
        }

        String periodo =
                request.getPeriodoCodigoInteresFormalizado();

        String modalidad =
                request.getTipoModalidadInteresFormalizado();

        if (!tieneTexto(periodo)
                || !tieneTexto(modalidad)) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "MODALIDAD_INTERES",
                            "Debe seleccionar un período "
                                    + "y una modalidad de intereses."
                    )
            );

            return new ResultadoTasa(
                    tasaNominal,
                    null,
                    tasaMaxima,
                    false
            );
        }

        Integer periodoMeses =
                solicitudCreditoRepository.buscarPeriodoMeses(
                        periodo.trim(),
                        modalidad.trim()
                ).orElse(null);

        if (periodoMeses == null
                || periodoMeses <= 0) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "PERIODO_INTERES",
                            "La modalidad de intereses "
                                    + "seleccionada no existe "
                                    + "o está inactiva."
                    )
            );

            return new ResultadoTasa(
                    tasaNominal,
                    null,
                    tasaMaxima,
                    false
            );
        }

        // =====================================================
        // REGLA DE CUOTA FIJA
        // =====================================================

        if ("1".equals(
                textoSeguro(
                        request.getCodigoTipoCuotaFormalizada()
                )
        )) {

            if (request.getAmortizacionCapitalFormalizada() == null
                    || !request.getAmortizacionCapitalFormalizada()
                    .equals(periodoMeses)) {

                bloqueos.add(
                        new BloqueoFormalizacion(
                                "CUOTA_FIJA",
                                "Para cuota fija, la periodicidad "
                                        + "del pago de intereses "
                                        + "debe ser igual a la "
                                        + "amortización de capital."
                        )
                );
            }
        }

        // =====================================================
        // CALCULAR TEA
        // =====================================================

        BigDecimal tasaEfectiva =
                TasasFinancieras.tasaEfectivaAnual(
                        tasaNominal,
                        periodoMeses
                );

        if (tasaEfectiva == null) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "CALCULO_TEA",
                            "No fue posible calcular la "
                                    + "tasa efectiva anual."
                    )
            );

            return new ResultadoTasa(
                    tasaNominal,
                    null,
                    tasaMaxima,
                    false
            );
        }

        tasaEfectiva =
                tasaEfectiva.setScale(
                        4,
                        RoundingMode.HALF_UP
                );

        // =====================================================
        // COMPARAR CONTRA PARÁMETRO 631
        // =====================================================

        boolean cumple =
                tasaMaxima != null
                        && tasaMaxima.compareTo(BigDecimal.ZERO) > 0
                        && tasaEfectiva.compareTo(tasaMaxima) <= 0;

        if (tasaMaxima != null
                && tasaMaxima.compareTo(BigDecimal.ZERO) > 0
                && !cumple) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "TASA_MAXIMA_LEGAL",
                            "La tasa efectiva anual formalizada ("
                                    + tasaEfectiva.toPlainString()
                                    + "%) supera la tasa efectiva "
                                    + "máxima configurada para "
                                    + "la agencia ("
                                    + tasaMaxima.toPlainString()
                                    + "%)."
                    )
            );
        }

        return new ResultadoTasa(
                tasaNominal,
                tasaEfectiva,
                tasaMaxima,
                cumple
        );
    }

    // =========================================================
    // VALIDAR PERSONAS
    // =========================================================

    private void validarPersonasSolicitud(
            SolicitudFormalizacionValidacionRepository.SolicitudValidacionDatos solicitud,
            List<SolicitudFormalizacionValidacionRepository.PersonaSolicitudDatos> personas,
            List<BloqueoFormalizacion> bloqueos
    ) {

        if (personas.isEmpty()) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "PERSONAS_SOLICITUD",
                            "La solicitud no tiene deudores registrados."
                    )
            );

            return;
        }

        long principales =
                personas.stream()
                        .filter(persona ->
                                esPrincipal(
                                        persona.tipoDeudor()
                                )
                        )
                        .count();

        if (principales != 1) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "DEUDOR_PRINCIPAL",
                            "La solicitud debe tener exactamente "
                                    + "un deudor principal."
                    )
            );
        }

        boolean principalCoincide =
                personas.stream()
                        .anyMatch(persona ->
                                esPrincipal(
                                        persona.tipoDeudor()
                                )
                                        && solicitud.idDatosPersonal()
                                        .equals(
                                                persona.idDatosPersonal()
                                        )
                        );

        if (!principalCoincide) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "DEUDOR_PRINCIPAL",
                            "El deudor principal registrado "
                                    + "no coincide con el titular "
                                    + "de la solicitud."
                    )
            );
        }
    }

    // =========================================================
    // CONSULTAR MORA
    // =========================================================

    private List<BloqueoMora> consultarBloqueosMora(
            Integer idSolicitudCredito
    ) {

        List<BloqueoMora> resultado =
                new ArrayList<>();

        for (
                SolicitudFormalizacionValidacionRepository.BloqueoMoraDatos obligacion
                : repository.consultarBloqueosMora(idSolicitudCredito)
        ) {

            if (obligacion.diasMora() == null
                    || obligacion.diasMora() <= 0) {

                continue;
            }

            resultado.add(
                    new BloqueoMora(
                            obligacion.idDatosPersonal(),
                            obligacion.cedula(),
                            obligacion.nombreCompleto(),
                            obligacion.calidad(),
                            obligacion.tipoObligacion(),
                            obligacion.idCarteraCredito(),
                            obligacion.idAgencia(),
                            obligacion.idLineaCredito(),
                            obligacion.codigoLineaCredito(),
                            obligacion.nombreLineaCredito(),
                            obligacion.pagareCartera(),
                            valorSeguro(obligacion.saldoActual()),
                            obligacion.diasMora()
                    )
            );
        }

        return resultado;
    }

    // =========================================================
    // ACTUALIZAR VALIDACIÓN DE MORA
    // =========================================================

    private void actualizarValidacionMora(
            Integer idSolicitudCredito,
            List<SolicitudFormalizacionValidacionRepository.PersonaSolicitudDatos> personas,
            List<BloqueoMora> bloqueosMora,
            List<BloqueoFormalizacion> bloqueos
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Map<Integer, Integer> moraMaximaPorPersona =
                new HashMap<>();

        for (BloqueoMora mora : bloqueosMora) {

            moraMaximaPorPersona.merge(
                    mora.idDatosPersonal(),
                    mora.diasMora(),
                    Math::max
            );
        }

        for (
                SolicitudFormalizacionValidacionRepository.PersonaSolicitudDatos persona
                : personas
        ) {

            BigDecimal saldoCartera =
                    repository.consultarSaldoCarteraPersona(
                            persona.idDatosPersonal()
                    );

            int diasMora =
                    moraMaximaPorPersona.getOrDefault(
                            persona.idDatosPersonal(),
                            0
                    );

            boolean cumpleMora =
                    diasMora == 0;

            int actualizados =
                    repository.actualizarValidacionMoraDeudor(
                            idSolicitudCredito,
                            persona.idSolicitudDeudor(),
                            saldoCartera,
                            diasMora,
                            cumpleMora,
                            idUsuario
                    );

            if (actualizados != 1) {

                bloqueos.add(
                        new BloqueoFormalizacion(
                                "ACTUALIZACION_MORA",
                                "No fue posible registrar la "
                                        + "validación de mora de "
                                        + textoSeguro(persona.cedula())
                                        + "."
                        )
                );
            }
        }
    }

    // =========================================================
    // VALIDAR APORTES
    // =========================================================

    private ResultadoAportes validarAportes(
            Integer idSolicitudCredito,
            SolicitudFormalizacionValidacionRepository.SolicitudValidacionDatos solicitud,
            BigDecimal valorFormalizado,
            boolean solicitudHabilitada,
            List<BloqueoFormalizacion> bloqueos
    ) {

        if (valorFormalizado == null
                || valorFormalizado.compareTo(BigDecimal.ZERO) <= 0) {

            String mensaje =
                    "El valor formalizado debe ser mayor que cero.";

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "VALOR_FORMALIZADO",
                            mensaje
                    )
            );

            return new ResultadoAportes(
                    solicitud.idCuentaAportes(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false,
                    mensaje
            );
        }

        SolicitudFormalizacionValidacionRepository.AportesValidacionDatos aportes =
                repository.consultarAportes(
                        idSolicitudCredito,
                        valorFormalizado
                ).orElse(null);

        if (aportes == null) {

            String mensaje =
                    "No fue posible consultar los aportes "
                            + "de la solicitud.";

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "APORTES",
                            mensaje
                    )
            );

            return new ResultadoAportes(
                    solicitud.idCuentaAportes(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false,
                    mensaje
            );
        }

        BigDecimal valorActual =
                valorSeguro(
                        aportes.valorAportesActual()
                );

        BigDecimal valorRequerido =
                valorSeguro(
                        aportes.valorAportesRequerido()
                );

        BigDecimal faltante =
                valorRequerido.subtract(
                        valorActual
                ).max(BigDecimal.ZERO);

        if (aportes.idCuentaAportes() == null) {

            String mensaje =
                    "La solicitud no tiene una cuenta "
                            + "de aportes vinculada.";

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "CUENTA_APORTES",
                            mensaje
                    )
            );

            return new ResultadoAportes(
                    null,
                    valorActual,
                    valorRequerido,
                    faltante,
                    false,
                    mensaje
            );
        }

        if (aportes.valorAportesActual() == null) {

            String mensaje =
                    "No fue posible obtener el saldo vigente "
                            + "de la cuenta de aportes.";

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "SALDO_APORTES",
                            mensaje
                    )
            );

            return new ResultadoAportes(
                    aportes.idCuentaAportes(),
                    BigDecimal.ZERO,
                    valorRequerido,
                    valorRequerido.max(BigDecimal.ZERO),
                    false,
                    mensaje
            );
        }

        if (aportes.valorAportesRequerido() == null) {

            String mensaje =
                    "No fue posible calcular los aportes "
                            + "requeridos. Verifique el factor "
                            + "de reciprocidad.";

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "APORTES_REQUERIDOS",
                            mensaje
                    )
            );

            return new ResultadoAportes(
                    aportes.idCuentaAportes(),
                    valorActual,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false,
                    mensaje
            );
        }

        boolean cumple =
                valorActual.compareTo(
                        valorRequerido
                ) >= 0;

        if (solicitudHabilitada) {

            int actualizados =
                    repository.actualizarValidacionAportes(
                            idSolicitudCredito,
                            valorActual,
                            cumple,
                            usuarioSesionService.idUsuario()
                    );

            if (actualizados != 1) {

                String mensaje =
                        "No fue posible registrar la "
                                + "validación de aportes.";

                bloqueos.add(
                        new BloqueoFormalizacion(
                                "ACTUALIZACION_APORTES",
                                mensaje
                        )
                );

                return new ResultadoAportes(
                        aportes.idCuentaAportes(),
                        valorActual,
                        valorRequerido,
                        faltante,
                        false,
                        mensaje
                );
            }
        }

        if (!cumple) {

            String mensaje =
                    "Los aportes actuales son insuficientes. "
                            + "Requeridos: $"
                            + valorRequerido.toPlainString()
                            + ". Actuales: $"
                            + valorActual.toPlainString()
                            + ". Faltante: $"
                            + faltante.toPlainString()
                            + ".";

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "APORTES_INSUFICIENTES",
                            mensaje
                    )
            );

            return new ResultadoAportes(
                    aportes.idCuentaAportes(),
                    valorActual,
                    valorRequerido,
                    faltante,
                    false,
                    mensaje
            );
        }

        return new ResultadoAportes(
                aportes.idCuentaAportes(),
                valorActual,
                valorRequerido,
                BigDecimal.ZERO,
                true,
                "Los aportes cumplen el valor requerido."
        );
    }

    // =========================================================
    // VALIDAR SIMULTANEIDAD
    //
    // Misma línea:
    // Advertencia de posible novación.
    //
    // Otra línea:
    // Bloqueo cuando la línea solicitada no permite
    // créditos simultáneos.
    // =========================================================

    private void validarSimultaneidad(
            Integer idSolicitudCredito,
            SolicitudFormalizacionValidacionRepository.SolicitudValidacionDatos solicitud,
            List<BloqueoFormalizacion> bloqueos,
            List<AlertaFormalizacion> alertas
    ) {

        SolicitudFormalizacionValidacionRepository.LineaSimultaneidadDatos linea =
                repository.consultarLineaSimultaneidad(
                        idSolicitudCredito
                ).orElse(null);

        if (linea == null) {

            bloqueos.add(
                    new BloqueoFormalizacion(
                            "LINEA_CREDITO",
                            "No fue posible consultar la "
                                    + "línea de crédito solicitada."
                    )
            );

            return;
        }

        if (linea.permiteCreditosSimultaneos()) {
            return;
        }

        List<SolicitudFormalizacionValidacionRepository.CreditoVigenteTitularDatos> creditos =
                repository.consultarCreditosVigentesTitular(
                        idSolicitudCredito
                );

        for (
                SolicitudFormalizacionValidacionRepository.CreditoVigenteTitularDatos credito
                : creditos
        ) {

            String descripcion =
                    "Crédito vigente. Línea: "
                            + textoSeguro(credito.nombreLineaCredito())
                            + ". Pagaré: "
                            + textoSeguro(credito.pagareCartera())
                            + ". Agencia: "
                            + credito.idAgencia()
                            + ". Saldo: $"
                            + valorSeguro(
                            credito.saldoActual()
                    ).toPlainString()
                            + ".";

            if (credito.idLineaCredito()
                    .equals(solicitud.idLineaCredito())) {

                alertas.add(
                        new AlertaFormalizacion(
                                "POSIBLE_NOVACION",
                                "La persona tiene un crédito "
                                        + "vigente en la misma línea. "
                                        + "Revise si corresponde "
                                        + "a una novación. "
                                        + descripcion
                        )
                );

            } else {

                bloqueos.add(
                        new BloqueoFormalizacion(
                                "SIMULTANEIDAD",
                                "La línea solicitada no permite "
                                        + "créditos simultáneos "
                                        + "y la persona tiene una "
                                        + "obligación vigente "
                                        + "en otra línea. "
                                        + descripcion
                        )
                );
            }
        }
    }

    // =========================================================
    // MENSAJE DE MORA
    // =========================================================

    private String construirMensajeMora(
            BloqueoMora mora
    ) {

        String calidad =
                "CODEUDOR".equalsIgnoreCase(mora.calidad())
                        ? "El codeudor"
                        : "El deudor";

        String tipoObligacion =
                "CODEUDA".equalsIgnoreCase(
                        mora.tipoObligacion()
                )
                        ? "Obligación donde figura como codeudor."
                        : "Obligación propia.";

        return calidad
                + " "
                + textoSeguro(mora.nombreCompleto())
                + ", cédula "
                + textoSeguro(mora.cedula())
                + ", presenta mora. Línea: "
                + textoSeguro(mora.nombreLineaCredito())
                + ". Pagaré: "
                + textoSeguro(mora.pagareCartera())
                + ". Agencia: "
                + mora.idAgencia()
                + ". Días de mora: "
                + mora.diasMora()
                + ". "
                + tipoObligacion;
    }

    // =========================================================
    // AUXILIARES
    // =========================================================

    private void validarIdSolicitud(
            Integer idSolicitudCredito
    ) {

        if (idSolicitudCredito == null
                || idSolicitudCredito <= 0) {

            throw new IllegalArgumentException(
                    "Debe indicar una solicitud de crédito válida."
            );
        }
    }

    private boolean esPrincipal(
            String tipoDeudor
    ) {

        return "PRINCIPAL".equalsIgnoreCase(
                tipoDeudor == null
                        ? ""
                        : tipoDeudor.trim()
        );
    }

    private BigDecimal valorSeguro(
            BigDecimal valor
    ) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }

    private boolean tieneTexto(
            String valor
    ) {

        return valor != null
                && !valor.isBlank();
    }

    private String textoSeguro(
            String valor
    ) {

        return tieneTexto(valor)
                ? valor.trim()
                : "No registrado";
    }

    // =========================================================
    // RESULTADO GENERAL
    // =========================================================

    public record ResultadoValidacionFormalizacion(

            Integer idSolicitudCredito,

            String numeroSolicitud,

            Integer idAgencia,

            Integer idDatosPersonal,

            Integer idLineaCredito,

            boolean puedeContinuar,

            String mensajeGeneral,

            int cantidadBloqueos,

            int cantidadAlertas,

            List<BloqueoFormalizacion> bloqueos,

            List<AlertaFormalizacion> alertas,

            List<BloqueoMora> bloqueosMora,

            ResultadoAportes aportes,

            ResultadoTasa tasa

    ) {
    }

    // =========================================================
    // BLOQUEO
    // =========================================================

    public record BloqueoFormalizacion(

            String tipo,

            String mensaje

    ) {
    }

    // =========================================================
    // ADVERTENCIA
    // =========================================================

    public record AlertaFormalizacion(

            String tipo,

            String mensaje

    ) {
    }

    // =========================================================
    // DETALLE DE MORA
    // =========================================================

    public record BloqueoMora(

            Integer idDatosPersonal,

            String cedula,

            String nombreCompleto,

            String calidad,

            String tipoObligacion,

            Integer idCarteraCredito,

            Integer idAgencia,

            Integer idLineaCredito,

            String codigoLineaCredito,

            String nombreLineaCredito,

            String pagareCartera,

            BigDecimal saldoActual,

            Integer diasMora

    ) {
    }

    // =========================================================
    // RESULTADO DE APORTES
    // =========================================================

    public record ResultadoAportes(

            Integer idCuentaAportes,

            BigDecimal valorActual,

            BigDecimal valorRequerido,

            BigDecimal faltante,

            boolean cumple,

            String mensaje

    ) {
    }

    // =========================================================
    // RESULTADO DE TASA
    // =========================================================

    public record ResultadoTasa(

            BigDecimal tasaNominal,

            BigDecimal tasaEfectivaAnual,

            BigDecimal tasaMaximaLegal,

            boolean cumple

    ) {
    }
}