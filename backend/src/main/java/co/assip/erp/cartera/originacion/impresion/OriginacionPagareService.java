package co.assip.erp.cartera.originacion.impresion;

import co.assip.erp.cartera.originacion.impresion.OriginacionPagareRepository.FirmanteDatos;
import co.assip.erp.cartera.originacion.impresion.OriginacionPagareRepository.PagareDatos;

import co.assip.erp.shared.financiero.NumerosEnLetras;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class OriginacionPagareService {

    private static final int PROCESO_FORMALIZACION = 4;
    private static final int RESULTADO_APROBADA = 1;

    private final OriginacionPagareRepository repository;

    public OriginacionPagareService(
            OriginacionPagareRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // PREPARAR PAGARÉ DILIGENCIADO
    //
    // No genera un nuevo pagaré.
    // No modifica la solicitud.
    // No modifica las condiciones del crédito.
    // =========================================================

    public PagareImpresion obtenerPagareDiligenciado(
            Integer idSolicitudCredito
    ) {

        return prepararImpresion(
                idSolicitudCredito,
                TipoPagare.DILIGENCIADO
        );
    }


    // =========================================================
    // PREPARAR PAGARÉ EN BLANCO
    //
    // El PDF conserva:
    // - Pagaré en blanco.
    // - Carta de instrucciones.
    // - Identificación y contacto de los firmantes.
    // =========================================================

    public PagareImpresion obtenerPagareEnBlanco(
            Integer idSolicitudCredito
    ) {

        return prepararImpresion(
                idSolicitudCredito,
                TipoPagare.EN_BLANCO
        );
    }

    // =========================================================
    // PREPARACIÓN COMÚN
    // =========================================================

    private PagareImpresion prepararImpresion(
            Integer idSolicitudCredito,
            TipoPagare tipoPagare
    ) {

        validarIdSolicitud(idSolicitudCredito);

        PagareDatos pagare =
                repository.buscarPagare(
                        idSolicitudCredito
                ).orElseThrow(
                        () -> new IllegalStateException(
                                "La solicitud "
                                        + idSolicitudCredito
                                        + " no tiene un pagaré constituido "
                                        + "y disponible para impresión."
                        )
                );

        validarPagareConstituido(pagare);

        List<FirmanteDatos> firmantes =
                List.copyOf(
                        repository.listarFirmantes(
                                idSolicitudCredito
                        )
                );

        validarFirmantes(
                pagare,
                firmantes
        );

        if (tipoPagare == TipoPagare.DILIGENCIADO) {

            validarCondicionesFinancieras(
                    pagare
            );
        }

        return new PagareImpresion(
                tipoPagare,
                pagare,
                firmantes
        );
    }

    // =========================================================
    // VALIDAR SOLICITUD
    // =========================================================

    private void validarIdSolicitud(
            Integer idSolicitudCredito
    ) {

        if (idSolicitudCredito == null
                || idSolicitudCredito <= 0) {

            throw new IllegalArgumentException(
                    "La solicitud de crédito es obligatoria."
            );
        }
    }

    // =========================================================
    // VALIDAR PAGARÉ CONSTITUIDO
    //
    // Se permite reimprimir un pagaré existente.
    // El crédito no tiene que permanecer en estado P.
    // =========================================================

    private void validarPagareConstituido(
            PagareDatos pagare
    ) {

        if (pagare.idSolicitudProceso() == null
                || pagare.idSolicitudProceso()
                < PROCESO_FORMALIZACION) {

            throw new IllegalStateException(
                    "La solicitud todavía no se encuentra "
                            + "en una etapa que permita "
                            + "imprimir el pagaré."
            );
        }

        if (pagare.idSolicitudResultado() == null
                || pagare.idSolicitudResultado()
                != RESULTADO_APROBADA) {

            throw new IllegalStateException(
                    "La solicitud no tiene un resultado "
                            + "aprobado para imprimir el pagaré."
            );
        }

        if (pagare.idCarteraCredito() == null
                || pagare.idCarteraCredito() <= 0) {

            throw new IllegalStateException(
                    "La solicitud todavía no tiene "
                            + "un crédito constituido."
            );
        }

        if (pagare.pagareCartera() == null
                || pagare.pagareCartera().isBlank()) {

            throw new IllegalStateException(
                    "El crédito constituido no tiene "
                            + "número de pagaré."
            );
        }

        if (pagare.idDatosPersonal() == null
                || pagare.idDatosPersonal() <= 0) {

            throw new IllegalStateException(
                    "La solicitud no tiene identificado "
                            + "al deudor principal."
            );
        }
    }

    // =========================================================
    // VALIDAR FIRMANTES
    // =========================================================

    private void validarFirmantes(
            PagareDatos pagare,
            List<FirmanteDatos> firmantes
    ) {

        if (firmantes == null
                || firmantes.isEmpty()) {

            throw new IllegalStateException(
                    "El pagaré no tiene firmantes registrados."
            );
        }

        long cantidadPrincipales =
                firmantes.stream()
                        .filter(
                                firmante ->
                                        "PRINCIPAL".equalsIgnoreCase(
                                                firmante.tipoDeudor()
                                        )
                        )
                        .count();

        if (cantidadPrincipales != 1) {

            throw new IllegalStateException(
                    "El pagaré debe tener exactamente "
                            + "un deudor principal activo."
            );
        }

        Set<Integer> personasRegistradas =
                new HashSet<>();

        for (FirmanteDatos firmante : firmantes) {

            if (firmante.idDatosPersonal() == null
                    || firmante.idDatosPersonal() <= 0) {

                throw new IllegalStateException(
                        "Existe un firmante sin identificación "
                                + "en Hoja de Vida."
                );
            }

            if (firmante.nombreCompleto() == null
                    || firmante.nombreCompleto().isBlank()) {

                throw new IllegalStateException(
                        "El firmante "
                                + firmante.idDatosPersonal()
                                + " no tiene nombre completo."
                );
            }

            if (firmante.documento() == null
                    || firmante.documento().isBlank()) {

                throw new IllegalStateException(
                        "El firmante "
                                + firmante.nombreCompleto()
                                + " no tiene documento "
                                + "de identificación."
                );
            }

            if (!personasRegistradas.add(
                    firmante.idDatosPersonal()
            )) {

                throw new IllegalStateException(
                        "La persona "
                                + firmante.nombreCompleto()
                                + " aparece repetida "
                                + "entre los firmantes del pagaré."
                );
            }

            if ("PRINCIPAL".equalsIgnoreCase(
                    firmante.tipoDeudor()
            )) {

                if (!firmante.idDatosPersonal().equals(
                        pagare.idDatosPersonal()
                )) {

                    throw new IllegalStateException(
                            "El deudor principal del pagaré "
                                    + "no coincide con el titular "
                                    + "de la solicitud de crédito."
                    );
                }
            }
        }
    }

    // =========================================================
    // VALIDAR CONDICIONES FINANCIERAS
    //
    // Aplica únicamente al pagaré diligenciado.
    // =========================================================

    private void validarCondicionesFinancieras(
            PagareDatos pagare
    ) {

        if (pagare.valorFormalizado() == null
                || pagare.valorFormalizado()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalStateException(
                    "El pagaré no tiene un valor "
                            + "formalizado válido."
            );
        }

        if (pagare.plazoFormalizado() == null
                || pagare.plazoFormalizado() <= 0) {

            throw new IllegalStateException(
                    "El pagaré no tiene un plazo "
                            + "formalizado válido."
            );
        }

        if (pagare.amortizacionCapitalFormalizada() == null
                || pagare.amortizacionCapitalFormalizada() <= 0) {

            throw new IllegalStateException(
                    "El pagaré no tiene una periodicidad "
                            + "de amortización de capital válida."
            );
        }

        if (pagare.valorCuotaFormalizada() == null
                || pagare.valorCuotaFormalizada()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalStateException(
                    "El pagaré no tiene un valor "
                            + "de cuota formalizada válido."
            );
        }

        if (pagare.tasaNominalFormalizada() == null
                || pagare.tasaNominalFormalizada()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalStateException(
                    "El pagaré no tiene una tasa "
                            + "nominal formalizada válida."
            );
        }

        if (pagare.codigoFormaPagoFormalizada() == null
                || pagare.codigoFormaPagoFormalizada()
                .isBlank()) {

            throw new IllegalStateException(
                    "El pagaré no tiene definida "
                            + "la forma de pago."
            );
        }

        if (pagare.periodoCodigoInteresFormalizado() == null
                || pagare.periodoCodigoInteresFormalizado()
                .isBlank()) {

            throw new IllegalStateException(
                    "El pagaré no tiene definida "
                            + "la periodicidad de intereses."
            );
        }

        if (pagare.tipoModalidadInteresFormalizado() == null
                || pagare.tipoModalidadInteresFormalizado()
                .isBlank()) {

            throw new IllegalStateException(
                    "El pagaré no tiene definida "
                            + "la modalidad de intereses."
            );
        }
    }

    // =========================================================
    // TIPO DE DOCUMENTO
    // =========================================================

    public enum TipoPagare {

        DILIGENCIADO,

        EN_BLANCO
    }

    // =========================================================
    // DATOS PREPARADOS PARA IMPRESIÓN
    //
    // DILIGENCIADO:
    // - Impresión HTML desde Angular.
    //
    // EN_BLANCO:
    // - Pagaré en blanco.
    // - Carta de instrucciones.
    // =========================================================

    public record PagareImpresion(

            TipoPagare tipoPagare,

            PagareDatos pagare,

            List<FirmanteDatos> firmantes



    ) {

        // =====================================================
        // VALOR FORMALIZADO EN LETRAS
        // =====================================================

        @JsonProperty("valorFormalizadoEnLetras")
        public String valorFormalizadoEnLetras() {

            if (tipoPagare != TipoPagare.DILIGENCIADO) {
                return "";
            }

            return NumerosEnLetras.valorEnLetras(
                    pagare.valorFormalizado()
            );
        }

        // =====================================================
        // CONDICIONES DE PAGO
        //
        // Conserva la estructura contractual del pagaré
        // utilizada por el sistema anterior.
        //
        // Los valores proceden exclusivamente de las
        // condiciones definitivas del crédito constituido.
        // =====================================================

        @JsonProperty("condicionesPago")
        public String condicionesPago() {

            if (tipoPagare != TipoPagare.DILIGENCIADO) {
                return "";
            }

            int amortizacion =
                    pagare.amortizacionCapitalFormalizada();

            int cantidadPagos =
                    pagare.plazoFormalizado() / amortizacion;

            String periodicidadCapital =
                    switch (amortizacion) {
                        case 1 -> "MENSUAL(ES)";
                        case 2 -> "BIMESTRAL(ES)";
                        case 3 -> "TRIMESTRAL(ES)";
                        case 6 -> "SEMESTRAL(ES)";
                        case 12 -> "ANUAL(ES)";
                        default -> "CADA " + amortizacion + " MES(ES)";
                    };

            String periodicidadInteres =
                    periodicidadInteres(
                            pagare.periodoCodigoInteresFormalizado()
                    );

            String textoInteres;

            if ("1".equals(
                    pagare.codigoTipoCuotaFormalizada()
            )) {

                textoInteres =
                        "INCLUIDO INTERESES VENCIDOS.";

            } else {

                textoInteres =
                        "MAS INTERES "
                                + periodicidadInteres
                                + " VENCIDOS.";
            }

            return "en: "
                    + cantidadPagos
                    + " Pago(s) "
                    + periodicidadCapital
                    + " de $ "
                    + formatearValorEntero(
                    pagare.valorCuotaFormalizada()
            )
                    + " "
                    + textoInteres;
        }


        // =====================================================
        // PERIODICIDAD DE INTERESES PARA TEXTO DEL PAGARÉ
        // =====================================================

        private String periodicidadInteres(
                String codigo
        ) {

            if (codigo == null || codigo.isBlank()) {
                return "";
            }

            return switch (
                    codigo.trim()
                            .substring(0, 1)
                            .toUpperCase()
                    ) {
                case "Q" -> "QUINCENAL";
                case "M" -> "MENSUAL";
                case "B" -> "BIMESTRAL";
                case "T" -> "TRIMESTRAL";
                case "S" -> "SEMESTRAL";
                case "A" -> "ANUAL";
                default -> codigo.trim().toUpperCase();
            };
        }


        // =====================================================
        // FORMATO MONETARIO PARA TEXTO CONTRACTUAL
        // =====================================================

        private String formatearValorEntero(
                BigDecimal valor
        ) {

            return String.format(
                    java.util.Locale.US,
                    "%,.0f",
                    valor
            );
        }

        // =====================================================
        // DEUDOR PRINCIPAL
        // =====================================================

        public FirmanteDatos deudorPrincipal() {

            return firmantes.stream()
                    .filter(
                            firmante ->
                                    "PRINCIPAL".equalsIgnoreCase(
                                            firmante.tipoDeudor()
                                    )
                    )
                    .findFirst()
                    .orElseThrow(
                            () -> new IllegalStateException(
                                    "No existe deudor principal "
                                            + "para el pagaré."
                            )
                    );
        }

        // =====================================================
        // CODEUDORES
        // =====================================================

        public List<FirmanteDatos> codeudores() {

            return firmantes.stream()
                    .filter(
                            firmante ->
                                    "CODEUDOR".equalsIgnoreCase(
                                            firmante.tipoDeudor()
                                    )
                    )
                    .toList();
        }
    }
}