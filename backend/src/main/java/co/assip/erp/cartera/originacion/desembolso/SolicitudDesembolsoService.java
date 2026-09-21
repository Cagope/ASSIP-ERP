package co.assip.erp.cartera.originacion.desembolso;

import co.assip.erp.seguridad.service.UsuarioSesionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SolicitudDesembolsoService {

    // =========================================================
    // ESTADOS DE ORIGINACIÓN
    // =========================================================

    private static final int PROCESO_DESEMBOLSO = 5;
    private static final int RESULTADO_APROBADA = 1;

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudDesembolsoRepository repository;

    private final UsuarioSesionService usuarioSesionService;

    public SolicitudDesembolsoService(
            SolicitudDesembolsoRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // VALIDACIONES FINALES DEL DESEMBOLSO
    //
    // 1. Bloquea la solicitud durante la validación.
    // 2. Verifica el estado de aprobación.
    // 3. Consulta al deudor y los codeudores.
    // 4. Consulta todas las obligaciones en mora.
    // 5. Actualiza la validación de mora por persona.
    // 6. Consulta y actualiza los aportes vigentes.
    // 7. Devuelve todos los bloqueos encontrados.
    //
    // NO asigna pagaré.
    // NO constituye crédito.
    // NO modifica el resultado de aprobación.
    // =========================================================

    @Transactional
    public ResultadoValidacionDesembolso validar(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(
                idSolicitudCredito
        );

        // =====================================================
        // 1. CONSULTAR Y BLOQUEAR SOLICITUD
        // =====================================================

        SolicitudDesembolsoRepository.SolicitudDesembolsoDatos solicitud =
                repository.bloquearSolicitud(
                        idSolicitudCredito
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la solicitud de crédito "
                                        + idSolicitudCredito
                        )
                );

        List<BloqueoDesembolso> bloqueos =
                new ArrayList<>();

        // =====================================================
        // 2. VALIDAR ESTADO
        // =====================================================

        boolean solicitudHabilitada =
                validarEstadoSolicitud(
                        solicitud,
                        bloqueos
                );

        // =====================================================
        // 3. CONSULTAR PERSONAS DE LA SOLICITUD
        // =====================================================

        List<SolicitudDesembolsoRepository.PersonaSolicitudDatos> personas =
                repository.consultarPersonasSolicitud(
                        idSolicitudCredito
                );

        validarPersonasSolicitud(
                solicitud,
                personas,
                bloqueos
        );

        // =====================================================
        // 4. CONSULTAR OBLIGACIONES EN MORA
        // =====================================================

        List<BloqueoMora> bloqueosMora =
                consultarBloqueosMora(
                        idSolicitudCredito
                );

        for (BloqueoMora mora : bloqueosMora) {

            bloqueos.add(
                    new BloqueoDesembolso(
                            "MORA",
                            construirMensajeMora(
                                    mora
                            )
                    )
            );
        }

        // =====================================================
        // 5. ACTUALIZAR VALIDACIÓN DE MORA
        //
        // Se actualizan las personas incluso cuando alguna
        // presenta mora, para conservar el resultado vigente.
        //
        // Si la solicitud ya no está habilitada, no se
        // modifican sus datos.
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
        // 6. VALIDAR APORTES
        // =====================================================

        ResultadoAportes resultadoAportes =
                validarAportes(
                        idSolicitudCredito,
                        solicitud,
                        solicitudHabilitada,
                        bloqueos
                );

        // =====================================================
        // 7. RESULTADO GENERAL
        // =====================================================

        boolean puedeContinuar =
                bloqueos.isEmpty();

        String mensajeGeneral =
                puedeContinuar
                        ? "La solicitud cumple las validaciones "
                        + "finales y puede continuar con la "
                        + "constitución del crédito."

                        : "La solicitud presenta condiciones "
                        + "que impiden continuar con el "
                        + "desembolso. Revise los bloqueos "
                        + "identificados.";

        return new ResultadoValidacionDesembolso(
                solicitud.idSolicitudCredito(),
                solicitud.numeroSolicitud(),
                solicitud.idAgencia(),
                solicitud.idDatosPersonal(),
                solicitud.idLineaCredito(),
                puedeContinuar,
                mensajeGeneral,
                bloqueos.size(),
                List.copyOf(bloqueos),
                List.copyOf(bloqueosMora),
                resultadoAportes
        );
    }

    // =========================================================
    // VALIDAR ESTADO DE LA SOLICITUD
    // =========================================================

    private boolean validarEstadoSolicitud(
            SolicitudDesembolsoRepository.SolicitudDesembolsoDatos solicitud,
            List<BloqueoDesembolso> bloqueos
    ) {

        boolean habilitada = true;

        // =====================================================
        // PROCESO DE DESEMBOLSO
        // =====================================================

        if (solicitud.idSolicitudProceso() == null
                || solicitud.idSolicitudProceso()
                != PROCESO_DESEMBOLSO) {

            bloqueos.add(
                    new BloqueoDesembolso(
                            "ESTADO_SOLICITUD",
                            "La solicitud no se encuentra "
                                    + "en el proceso de desembolso."
                    )
            );

            habilitada = false;
        }

        // =====================================================
        // RESULTADO DE APROBACIÓN
        // =====================================================

        if (solicitud.idSolicitudResultado() == null
                || solicitud.idSolicitudResultado()
                != RESULTADO_APROBADA) {

            bloqueos.add(
                    new BloqueoDesembolso(
                            "APROBACION",
                            "La solicitud no tiene un resultado "
                                    + "de aprobación válido para "
                                    + "continuar con el desembolso."
                    )
            );

            habilitada = false;
        }

        // =====================================================
        // CRÉDITO YA CONSTITUIDO
        // =====================================================

        if (solicitud.idCarteraCredito() != null) {

            bloqueos.add(
                    new BloqueoDesembolso(
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
    // VALIDAR PERSONAS VINCULADAS
    //
    // El deudor principal debe estar registrado.
    //
    // No se exige que existan codeudores porque algunas
    // solicitudes pueden no requerirlos.
    // =========================================================

    private void validarPersonasSolicitud(
            SolicitudDesembolsoRepository.SolicitudDesembolsoDatos solicitud,
            List<SolicitudDesembolsoRepository.PersonaSolicitudDatos> personas,
            List<BloqueoDesembolso> bloqueos
    ) {

        if (personas.isEmpty()) {

            bloqueos.add(
                    new BloqueoDesembolso(
                            "PERSONAS_SOLICITUD",
                            "La solicitud no tiene deudores "
                                    + "registrados para efectuar "
                                    + "las validaciones finales."
                    )
            );

            return;
        }

        long principales = personas.stream()
                .filter(persona ->
                        esPrincipal(
                                persona.tipoDeudor()
                        )
                )
                .count();

        if (principales == 0) {

            bloqueos.add(
                    new BloqueoDesembolso(
                            "DEUDOR_PRINCIPAL",
                            "La solicitud no tiene registrado "
                                    + "el deudor principal."
                    )
            );
        }

        if (principales > 1) {

            bloqueos.add(
                    new BloqueoDesembolso(
                            "DEUDOR_PRINCIPAL",
                            "La solicitud tiene más de un "
                                    + "registro de deudor principal."
                    )
            );
        }

        boolean principalCoincide = personas.stream()
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
                    new BloqueoDesembolso(
                            "DEUDOR_PRINCIPAL",
                            "El deudor principal registrado "
                                    + "no coincide con el titular "
                                    + "de la solicitud."
                    )
            );
        }
    }

    // =========================================================
    // CONSULTAR BLOQUEOS POR MORA
    //
    // Cada obligación se devuelve individualmente.
    //
    // Una persona puede tener:
    //
    // - Mora en un crédito propio.
    // - Mora en un crédito donde es codeudora.
    // - Mora en obligaciones de diferentes agencias.
    // =========================================================

    private List<BloqueoMora> consultarBloqueosMora(
            Integer idSolicitudCredito
    ) {

        List<SolicitudDesembolsoRepository.BloqueoMoraDatos> obligaciones =
                repository.consultarBloqueosMora(
                        idSolicitudCredito
                );

        List<BloqueoMora> resultado =
                new ArrayList<>();

        for (
                SolicitudDesembolsoRepository.BloqueoMoraDatos obligacion
                : obligaciones
        ) {

            Integer diasMora =
                    obligacion.diasMora();

            if (diasMora == null
                    || diasMora <= 0) {

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
                            valorSeguro(
                                    obligacion.saldoActual()
                            ),
                            diasMora
                    )
            );
        }

        return resultado;
    }

    // =========================================================
    // ACTUALIZAR VALIDACIÓN DE MORA POR PERSONA
    //
    // Se registra:
    //
    // saldo_cartera_validacion
    // dias_mora_validacion
    // cumple_mora_validacion
    //
    // El saldo corresponde a las obligaciones identificadas
    // en la consulta de mora.
    //
    // Si una obligación aparece repetida para la misma
    // persona, no se duplica su saldo.
    // =========================================================

    private void actualizarValidacionMora(
            Integer idSolicitudCredito,
            List<SolicitudDesembolsoRepository.PersonaSolicitudDatos> personas,
            List<BloqueoMora> bloqueosMora,
            List<BloqueoDesembolso> bloqueos
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Map<Integer, List<BloqueoMora>> moraPorPersona =
                new HashMap<>();

        for (BloqueoMora mora : bloqueosMora) {

            moraPorPersona
                    .computeIfAbsent(
                            mora.idDatosPersonal(),
                            id -> new ArrayList<>()
                    )
                    .add(mora);
        }

        for (
                SolicitudDesembolsoRepository.PersonaSolicitudDatos persona
                : personas
        ) {

            List<BloqueoMora> obligaciones =
                    moraPorPersona.getOrDefault(
                            persona.idDatosPersonal(),
                            List.of()
                    );

            // =================================================
            // SALDO TOTAL DE CARTERA PROPIA
            //
            // Se consulta independientemente de las obligaciones
            // en mora. Incluye créditos al día y vencidos.
            // =================================================

            BigDecimal saldoCarteraValidacion =
                    repository.consultarSaldoCarteraPersona(
                            persona.idDatosPersonal()
                    );

            // =================================================
            // MORA MÁXIMA DE LA PERSONA
            //
            // Incluye obligaciones propias y aquellas en las
            // que figura como codeudor.
            // =================================================

            int diasMoraValidacion = 0;

            for (BloqueoMora obligacion : obligaciones) {

                diasMoraValidacion =
                        Math.max(
                                diasMoraValidacion,
                                obligacion.diasMora()
                        );
            }

            // =================================================
            // CUMPLIMIENTO
            //
            // Cualquier obligación en mora impide continuar.
            // =================================================

            boolean cumpleMoraValidacion =
                    diasMoraValidacion == 0;

            // =================================================
            // PERSISTIR VALIDACIÓN
            // =================================================

            int actualizados =
                    repository.actualizarValidacionMoraDeudor(
                            idSolicitudCredito,
                            persona.idSolicitudDeudor(),
                            saldoCarteraValidacion,
                            diasMoraValidacion,
                            cumpleMoraValidacion,
                            idUsuario
                    );

            if (actualizados != 1) {

                bloqueos.add(
                        new BloqueoDesembolso(
                                "ACTUALIZACION_MORA",
                                "No fue posible actualizar "
                                        + "la validación de mora "
                                        + "de la persona con cédula "
                                        + textoSeguro(
                                        persona.cedula()
                                )
                                        + "."
                        )
                );
            }
        }
    }

    // =========================================================
    // CONSTRUIR MENSAJE DE MORA
    //
    // Identifica:
    //
    // - Deudor o codeudor.
    // - Nombre.
    // - Cédula.
    // - Línea.
    // - Pagaré.
    // - Agencia.
    // - Días de mora.
    // - Calidad en la obligación.
    // =========================================================

    private String construirMensajeMora(
            BloqueoMora mora
    ) {

        String calidad =
                "CODEUDOR".equalsIgnoreCase(
                        mora.calidad()
                )
                        ? "El codeudor"
                        : "El deudor";

        String linea =
                tieneTexto(
                        mora.nombreLineaCredito()
                )
                        ? mora.nombreLineaCredito()
                        : mora.codigoLineaCredito();

        String tipoObligacion =
                "CODEUDA".equalsIgnoreCase(
                        mora.tipoObligacion()
                )
                        ? "Obligación donde figura "
                        + "como codeudor."

                        : "Obligación propia.";

        return calidad
                + " "
                + textoSeguro(
                mora.nombreCompleto()
        )
                + ", cédula "
                + textoSeguro(
                mora.cedula()
        )
                + ", presenta una obligación en mora. "
                + "Línea: "
                + textoSeguro(
                linea
        )
                + ". Pagaré: "
                + textoSeguro(
                mora.pagareCartera()
        )
                + ". Agencia: "
                + mora.idAgencia()
                + ". Días de mora: "
                + mora.diasMora()
                + ". "
                + tipoObligacion
                + " Debe ponerse al día antes "
                + "de continuar.";
    }

    // =========================================================
    // VALIDAR APORTES
    //
    // Consulta el saldo vigente de la cuenta vinculada.
    //
    // Actualiza:
    //
    // valor_aportes_validacion
    // cumple_aportes_validacion
    //
    // No modifica:
    //
    // valor_aportes_inicio
    // cumple_aportes_inicio
    // valor_aportes_requerido
    // valor_solicitado
    // =========================================================

    private ResultadoAportes validarAportes(
            Integer idSolicitudCredito,
            SolicitudDesembolsoRepository.SolicitudDesembolsoDatos solicitud,
            boolean solicitudHabilitada,
            List<BloqueoDesembolso> bloqueos
    ) {

        SolicitudDesembolsoRepository.AportesDesembolsoDatos aportes =
                repository.consultarAportes(
                        idSolicitudCredito
                ).orElse(null);

        // =====================================================
        // NO SE ENCONTRÓ LA SOLICITUD
        // =====================================================

        if (aportes == null) {

            String mensaje =
                    "No fue posible consultar los aportes "
                            + "de la solicitud.";

            bloqueos.add(
                    new BloqueoDesembolso(
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
                ).max(
                        BigDecimal.ZERO
                );

        // =====================================================
        // CUENTA DE APORTES NO VINCULADA
        // =====================================================

        if (aportes.idCuentaAportes() == null) {

            String mensaje =
                    "La solicitud no tiene una cuenta "
                            + "de aportes vinculada.";

            bloqueos.add(
                    new BloqueoDesembolso(
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

        // =====================================================
        // CUENTA NO ENCONTRADA O SIN SALDO DISPONIBLE
        //
        // SQL_APORTES devuelve NULL cuando no encuentra
        // la cuenta vinculada o su saldo no está definido.
        //
        // No se interpreta una cuenta inexistente como
        // una cuenta válida con saldo cero.
        // =====================================================

        if (aportes.valorAportesActual() == null) {

            String mensaje =
                    "No fue posible obtener el saldo vigente "
                            + "de la cuenta de aportes "
                            + aportes.idCuentaAportes()
                            + ".";

            bloqueos.add(
                    new BloqueoDesembolso(
                            "SALDO_APORTES",
                            mensaje
                    )
            );

            return new ResultadoAportes(
                    aportes.idCuentaAportes(),
                    BigDecimal.ZERO,
                    valorRequerido,
                    valorRequerido.max(
                            BigDecimal.ZERO
                    ),
                    false,
                    mensaje
            );
        }

        // =====================================================
        // VALOR REQUERIDO NO DEFINIDO
        // =====================================================

        if (aportes.valorAportesRequerido() == null) {

            String mensaje =
                    "No fue posible calcular los aportes requeridos "
                            + "para el desembolso. Verifique que la "
                            + "solicitud tenga un valor formalizado "
                            + "válido y un factor de reciprocidad "
                            + "de aportes mayor que cero.";

            bloqueos.add(
                    new BloqueoDesembolso(
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

        // =====================================================
        // COMPARAR APORTES
        // =====================================================

        boolean cumple =
                valorActual.compareTo(
                        valorRequerido
                ) >= 0;

        // =====================================================
        // ACTUALIZAR APORTES VIGENTES
        //
        // Se actualizan aunque sean insuficientes.
        //
        // La solicitud debe continuar en estado aprobado
        // y pendiente de desembolso.
        // =====================================================

        if (solicitudHabilitada) {

            Integer idUsuario =
                    usuarioSesionService.idUsuario();

            int actualizados =
                    repository.actualizarValidacionAportes(
                            idSolicitudCredito,
                            valorActual,
                            cumple,
                            idUsuario
                    );

            if (actualizados != 1) {

                String mensaje =
                        "No fue posible actualizar los "
                                + "aportes vigentes de la solicitud.";

                bloqueos.add(
                        new BloqueoDesembolso(
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

        // =====================================================
        // APORTES INSUFICIENTES
        // =====================================================

        if (!cumple) {

            String mensaje =
                    "Los aportes actuales son insuficientes "
                            + "para continuar con el desembolso. "
                            + "Aportes requeridos: $"
                            + valorRequerido.toPlainString()
                            + ". Aportes actuales: $"
                            + valorActual.toPlainString()
                            + ". Faltante: $"
                            + faltante.toPlainString()
                            + ".";

            bloqueos.add(
                    new BloqueoDesembolso(
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

        // =====================================================
        // APORTES CUMPLEN
        // =====================================================

        return new ResultadoAportes(
                aportes.idCuentaAportes(),
                valorActual,
                valorRequerido,
                BigDecimal.ZERO,
                true,
                "Los aportes cumplen el valor requerido "
                        + "para continuar con el desembolso."
        );
    }

    // =========================================================
    // VALIDACIONES AUXILIARES
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

        return tieneTexto(
                valor
        )
                ? valor.trim()
                : "No registrado";
    }

    // =========================================================
    // RESULTADO GENERAL
    // =========================================================

    public record ResultadoValidacionDesembolso(

            Integer idSolicitudCredito,

            String numeroSolicitud,

            Integer idAgencia,

            Integer idDatosPersonal,

            Integer idLineaCredito,

            boolean puedeContinuar,

            String mensajeGeneral,

            int cantidadBloqueos,

            List<BloqueoDesembolso> bloqueos,

            List<BloqueoMora> bloqueosMora,

            ResultadoAportes aportes

    ) {
    }

    // =========================================================
    // BLOQUEO GENERAL
    // =========================================================

    public record BloqueoDesembolso(

            String tipo,

            String mensaje

    ) {
    }

    // =========================================================
    // DETALLE DE OBLIGACIÓN EN MORA
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
}