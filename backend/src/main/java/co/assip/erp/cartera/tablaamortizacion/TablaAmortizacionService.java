package co.assip.erp.shared.financiero.tablaamortizacion;

import co.assip.erp.shared.financiero.CuotasFinancieras;
import co.assip.erp.shared.financiero.dto.CuotaVariableResultado;
import co.assip.erp.shared.financiero.tablaamortizacion.dto.TablaAmortizacionDetalleDTO;
import co.assip.erp.shared.financiero.tablaamortizacion.dto.TablaAmortizacionRequestDTO;
import co.assip.erp.shared.financiero.tablaamortizacion.dto.TablaAmortizacionResponseDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class TablaAmortizacionService {

    private static final BigDecimal CERO =
            BigDecimal.ZERO.setScale(0, RoundingMode.HALF_UP);

    private static final BigDecimal CIEN =
            BigDecimal.valueOf(100);

    private static final BigDecimal TRESCIENTOS_SESENTA =
            BigDecimal.valueOf(360);

    // =========================================================
    // PROCESO PRINCIPAL
    // =========================================================

    public TablaAmortizacionResponseDTO calcular(
            TablaAmortizacionRequestDTO request
    ) {
        validarRequest(request);

        BigDecimal valorCredito =
                dinero(request.getValorCredito());

        BigDecimal tasaColocacion =
                request.getTasaColocacion();

        int plazoMeses =
                request.getPlazoMeses();

        int amortizacionCapitalMeses =
                request.getAmortizacionCapitalMeses();

        int periodoInteresMeses =
                request.getPeriodoInteresMeses();

        String codigoTipoCuota =
                request.getCodigoTipoCuota().trim();

        String tipoModalidadInteres =
                request.getTipoModalidadInteres().trim();

        BigDecimal porcentajeSeguroCredito =
                porcentaje(
                        request.getPorcentajeSeguroCredito()
                );

        BigDecimal porcentajeSeguroEntidad =
                porcentaje(
                        request.getPorcentajeSeguroEntidad()
                );

        // =====================================================
        // CUOTA FINANCIERA / CAPITAL VARIABLE
        // =====================================================

        BigDecimal valorCuotaProyectada;

        BigDecimal capitalRegular = null;
        BigDecimal capitalPrimeraCuota = null;

        if ("1".equals(codigoTipoCuota)) {

            if (amortizacionCapitalMeses
                    != periodoInteresMeses) {

                throw new IllegalArgumentException(
                        "Para cuota fija, la periodicidad de intereses "
                                + "debe ser igual a la amortización de capital."
                );
            }

            valorCuotaProyectada =
                    dinero(
                            CuotasFinancieras.cuotaFija(
                                    periodoInteresMeses,
                                    plazoMeses,
                                    tasaColocacion,
                                    valorCredito
                            )
                    );

        } else if ("2".equals(codigoTipoCuota)
                || "3".equals(codigoTipoCuota)) {

            int numeroCuotasCapital =
                    plazoMeses
                            / amortizacionCapitalMeses;

            if (numeroCuotasCapital <= 0) {
                throw new IllegalArgumentException(
                        "El número de cuotas de capital debe ser mayor que cero."
                );
            }

            CuotaVariableResultado resultadoVariable =
                    CuotasFinancieras.cuotaVariable(
                            valorCredito,
                            numeroCuotasCapital
                    );

            capitalRegular =
                    dinero(
                            resultadoVariable.valorCuotaRegular()
                    );

            capitalPrimeraCuota =
                    dinero(
                            resultadoVariable.valorPrimeraCuota()
                    );

            valorCuotaProyectada =
                    capitalRegular;

        } else {
            throw new IllegalArgumentException(
                    "El tipo de cuota "
                            + codigoTipoCuota
                            + " no está soportado."
            );
        }

        // =====================================================
        // GENERAR TABLA
        // =====================================================

        List<TablaAmortizacionDetalleDTO> detalle =
                generarDetalle(
                        request,
                        valorCredito,
                        tasaColocacion,
                        codigoTipoCuota,
                        tipoModalidadInteres,
                        porcentajeSeguroCredito,
                        porcentajeSeguroEntidad,
                        valorCuotaProyectada,
                        capitalRegular,
                        capitalPrimeraCuota
                );

        // =====================================================
        // TOTALES
        // =====================================================

        BigDecimal totalCapital = CERO;
        BigDecimal totalIntereses = CERO;
        BigDecimal totalSeguros = CERO;
        BigDecimal totalPagado = CERO;

        for (TablaAmortizacionDetalleDTO fila : detalle) {

            totalCapital =
                    totalCapital.add(
                            fila.getAbonoCapital()
                    );

            totalIntereses =
                    totalIntereses.add(
                            fila.getValorIntereses()
                    );

            totalSeguros =
                    totalSeguros.add(
                            fila.getValorSeguros()
                    );

            totalPagado =
                    totalPagado.add(
                            fila.getValorCuota()
                    );
        }

        return TablaAmortizacionResponseDTO.builder()
                .valorCredito(valorCredito)
                .tasaColocacion(tasaColocacion)
                .plazoMeses(plazoMeses)
                .amortizacionCapitalMeses(
                        amortizacionCapitalMeses
                )
                .periodoInteresMeses(
                        periodoInteresMeses
                )
                .tipoModalidadInteres(
                        tipoModalidadInteres
                )
                .codigoTipoCuota(
                        codigoTipoCuota
                )
                .fechaDesembolso(
                        request.getFechaDesembolso()
                )
                .fechaPrimeraCuotaCapital(
                        request.getFechaPrimeraCuotaCapital()
                )
                .fechaPrimeraCuotaInteres(
                        request.getFechaPrimeraCuotaInteres()
                )
                .cantidadCuotas(detalle.size())
                .valorCuotaProyectada(
                        valorCuotaProyectada
                )
                .totalCapital(
                        dinero(totalCapital)
                )
                .totalIntereses(
                        dinero(totalIntereses)
                )
                .totalSeguros(
                        dinero(totalSeguros)
                )
                .totalPagado(
                        dinero(totalPagado)
                )
                .detalle(detalle)
                .build();
    }

    // =========================================================
    // GENERAR DETALLE
    // =========================================================

    private List<TablaAmortizacionDetalleDTO> generarDetalle(
            TablaAmortizacionRequestDTO request,
            BigDecimal valorCredito,
            BigDecimal tasaColocacion,
            String codigoTipoCuota,
            String tipoModalidadInteres,
            BigDecimal porcentajeSeguroCredito,
            BigDecimal porcentajeSeguroEntidad,
            BigDecimal valorCuotaProyectada,
            BigDecimal capitalRegular,
            BigDecimal capitalPrimeraCuota
    ) {

        List<TablaAmortizacionDetalleDTO> detalle =
                new ArrayList<>();

        BigDecimal saldo =
                valorCredito;

        LocalDate fechaDesembolso =
                request.getFechaDesembolso();

        LocalDate fechaInteres =
                request.getFechaPrimeraCuotaInteres();

        LocalDate fechaCapital =
                request.getFechaPrimeraCuotaCapital();

        LocalDate fechaFinal =
                fechaDesembolso.plusMonths(
                        request.getPlazoMeses()
                );

        boolean primeraCuotaCapital = true;

        int numeroCuota = 0;

        LocalDate fechaInteresAnterior =
                fechaDesembolso;

        while (!fechaInteres.isAfter(fechaFinal)
                && saldo.signum() > 0) {

            numeroCuota++;

            BigDecimal saldoInicial =
                    saldo;

            // =================================================
            // DÍAS DE INTERÉS
            // =================================================

            long diasInteres =
                    calcularDias360(
                            fechaInteresAnterior,
                            fechaInteres
                    );

            if (diasInteres <= 0) {
                throw new IllegalStateException(
                        "La cantidad de días para calcular intereses "
                                + "debe ser mayor que cero."
                );
            }

            // =================================================
            // CAPITAL QUE VENCE EN ESTA FECHA
            // =================================================

            boolean venceCapital =
                    !fechaInteres.isBefore(fechaCapital);

            BigDecimal capital = CERO;

            if (venceCapital) {

                if ("1".equals(codigoTipoCuota)) {

                    /*
                     * Para cuota fija el capital se determina
                     * después de calcular el interés.
                     */

                } else {

                    if (primeraCuotaCapital) {
                        capital =
                                capitalPrimeraCuota;
                        primeraCuotaCapital = false;
                    } else {
                        capital =
                                capitalRegular;
                    }

                    if (capital.compareTo(saldoInicial) > 0) {
                        capital =
                                saldoInicial;
                    }
                }
            }

            // =================================================
            // SEGURO
            //
            // Se calcula sobre el saldo antes del abono de
            // capital, igual que el proceso histórico.
            // =================================================

            BigDecimal seguro =
                    calcularSeguro(
                            saldoInicial,
                            porcentajeSeguroCredito,
                            porcentajeSeguroEntidad
                    );

            // =================================================
            // INTERÉS
            // =================================================

            BigDecimal interes;
            BigDecimal saldoBaseInteres;

            if (esAnticipada(tipoModalidadInteres)
                    && venceCapital
                    && !"1".equals(codigoTipoCuota)) {

                /*
                 * Modalidad anticipada:
                 * primero se descuenta capital y después se
                 * calcula el interés.
                 */
                saldoBaseInteres =
                        dinero(
                                saldoInicial.subtract(capital)
                        );

            } else {

                /*
                 * Modalidad vencida:
                 * interés sobre el saldo antes del capital.
                 */
                saldoBaseInteres =
                        saldoInicial;
            }

            interes =
                    calcularInteres(
                            saldoBaseInteres,
                            tasaColocacion,
                            diasInteres
                    );

            // =================================================
            // CUOTA FIJA
            // =================================================

            if ("1".equals(codigoTipoCuota)) {

                BigDecimal cuotaFinanciera =
                        valorCuotaProyectada;

                capital =
                        dinero(
                                cuotaFinanciera.subtract(interes)
                        );

                if (capital.signum() <= 0) {
                    throw new IllegalStateException(
                            "La cuota fija no alcanza a cubrir "
                                    + "los intereses del período."
                    );
                }

                if (capital.compareTo(saldoInicial) > 0
                        || fechaInteres.equals(fechaFinal)) {

                    capital =
                            saldoInicial;
                }
            }

            // =================================================
            // ACTUALIZAR SALDO
            // =================================================

            saldo =
                    dinero(
                            saldoInicial.subtract(capital)
                    );

            if (saldo.signum() < 0) {
                saldo = CERO;
            }

            // =================================================
            // VALOR TOTAL CUOTA
            // =================================================

            BigDecimal valorCuota =
                    dinero(
                            capital
                                    .add(interes)
                                    .add(seguro)
                    );

            detalle.add(
                    TablaAmortizacionDetalleDTO.builder()
                            .numeroCuota(numeroCuota)
                            .fechaPago(fechaInteres)
                            .abonoCapital(
                                    dinero(capital)
                            )
                            .valorIntereses(
                                    dinero(interes)
                            )
                            .valorSeguros(
                                    dinero(seguro)
                            )
                            .saldoCredito(
                                    dinero(saldo)
                            )
                            .valorCuota(
                                    valorCuota
                            )
                            .build()
            );

            // =================================================
            // SIGUIENTE FECHA DE CAPITAL
            // =================================================

            if (venceCapital) {

                fechaCapital =
                        fechaCapital.plusMonths(
                                request.getAmortizacionCapitalMeses()
                        );
            }

            // =================================================
            // SIGUIENTE FECHA DE INTERÉS
            // =================================================

            fechaInteresAnterior =
                    fechaInteres;

            fechaInteres =
                    fechaInteres.plusMonths(
                            request.getPeriodoInteresMeses()
                    );
        }

        if (saldo.signum() != 0) {
            throw new IllegalStateException(
                    "La tabla de amortización terminó con un saldo pendiente de "
                            + saldo + ". Revise plazo, amortización de capital "
                            + "y periodicidad de intereses."
            );
        }

        return detalle;
    }

    // =========================================================
    // INTERÉS
    // =========================================================

    private BigDecimal calcularInteres(
            BigDecimal saldo,
            BigDecimal tasaColocacion,
            long dias
    ) {

        if (saldo.signum() <= 0) {
            return CERO;
        }

        return dinero(
                saldo
                        .multiply(tasaColocacion)
                        .multiply(
                                BigDecimal.valueOf(dias)
                        )
                        .divide(
                                TRESCIENTOS_SESENTA
                                        .multiply(CIEN),
                                12,
                                RoundingMode.HALF_UP
                        )
        );
    }

    // =========================================================
    // SEGURO
    // =========================================================

    private BigDecimal calcularSeguro(
            BigDecimal saldo,
            BigDecimal porcentajeSeguroCredito,
            BigDecimal porcentajeSeguroEntidad
    ) {

        if (saldo.signum() <= 0
                || porcentajeSeguroCredito.signum() <= 0
                || porcentajeSeguroEntidad.signum() <= 0) {

            return CERO;
        }

        BigDecimal baseSeguro =
                dinero(
                        saldo
                                .multiply(
                                        porcentajeSeguroCredito
                                )
                                .divide(
                                        CIEN,
                                        12,
                                        RoundingMode.HALF_UP
                                )
                );

        return dinero(
                baseSeguro
                        .multiply(
                                porcentajeSeguroEntidad
                        )
                        .divide(
                                CIEN,
                                12,
                                RoundingMode.HALF_UP
                        )
        );
    }

    // =========================================================
    // MODALIDAD
    // =========================================================

    private boolean esAnticipada(
            String tipoModalidadInteres
    ) {
        if (tipoModalidadInteres == null) {
            return false;
        }

        String modalidad =
                tipoModalidadInteres.trim()
                        .toUpperCase();

        /*
         * Compatibilidad con el comportamiento histórico:
         * el segundo carácter identifica "A" para anticipada.
         */
        return modalidad.length() >= 2
                && modalidad.charAt(1) == 'A';
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarRequest(
            TablaAmortizacionRequestDTO request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los parámetros de la tabla de amortización son obligatorios."
            );
        }

        if (request.getValorCredito() == null
                || request.getValorCredito().signum() <= 0) {

            throw new IllegalArgumentException(
                    "El valor del crédito debe ser mayor que cero."
            );
        }

        if (request.getTasaColocacion() == null
                || request.getTasaColocacion().signum() <= 0) {

            throw new IllegalArgumentException(
                    "La tasa de colocación debe ser mayor que cero."
            );
        }

        if (request.getPlazoMeses() == null
                || request.getPlazoMeses() <= 0) {

            throw new IllegalArgumentException(
                    "El plazo debe ser mayor que cero."
            );
        }

        if (request.getAmortizacionCapitalMeses() == null
                || request.getAmortizacionCapitalMeses() <= 0) {

            throw new IllegalArgumentException(
                    "La amortización de capital debe ser mayor que cero."
            );
        }

        if (request.getPeriodoInteresMeses() == null
                || request.getPeriodoInteresMeses() <= 0) {

            throw new IllegalArgumentException(
                    "La periodicidad de intereses debe ser mayor que cero."
            );
        }

        if (request.getCodigoTipoCuota() == null
                || request.getCodigoTipoCuota().isBlank()) {

            throw new IllegalArgumentException(
                    "El tipo de cuota es obligatorio."
            );
        }

        if (request.getTipoModalidadInteres() == null
                || request.getTipoModalidadInteres().isBlank()) {

            throw new IllegalArgumentException(
                    "La modalidad de intereses es obligatoria."
            );
        }

        if (request.getFechaDesembolso() == null) {
            throw new IllegalArgumentException(
                    "La fecha de desembolso es obligatoria."
            );
        }

        if (request.getFechaPrimeraCuotaCapital() == null) {
            throw new IllegalArgumentException(
                    "La fecha de primera cuota de capital es obligatoria."
            );
        }

        if (request.getFechaPrimeraCuotaInteres() == null) {
            throw new IllegalArgumentException(
                    "La fecha de primera cuota de intereses es obligatoria."
            );
        }

        if (!request.getFechaPrimeraCuotaCapital()
                .isAfter(request.getFechaDesembolso())) {

            throw new IllegalArgumentException(
                    "La primera cuota de capital debe ser posterior "
                            + "a la fecha de desembolso."
            );
        }

        if (!request.getFechaPrimeraCuotaInteres()
                .isAfter(request.getFechaDesembolso())) {

            throw new IllegalArgumentException(
                    "La primera cuota de intereses debe ser posterior "
                            + "a la fecha de desembolso."
            );
        }

        if (request.getPlazoMeses()
                % request.getAmortizacionCapitalMeses() != 0) {

            throw new IllegalArgumentException(
                    "El plazo debe ser divisible exactamente "
                            + "por la amortización de capital."
            );
        }

        if (request.getPlazoMeses()
                % request.getPeriodoInteresMeses() != 0) {

            throw new IllegalArgumentException(
                    "El plazo debe ser divisible exactamente "
                            + "por la periodicidad de intereses."
            );
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private BigDecimal porcentaje(
            BigDecimal valor
    ) {
        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private BigDecimal dinero(
            BigDecimal valor
    ) {
        if (valor == null) {
            return CERO;
        }

        return valor.setScale(
                0,
                RoundingMode.HALF_UP
        );
    }

    private long calcularDias360(
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {

        int diaInicial =
                Math.min(
                        fechaInicial.getDayOfMonth(),
                        30
                );

        int diaFinal =
                Math.min(
                        fechaFinal.getDayOfMonth(),
                        30
                );

        return (fechaFinal.getYear()
                - fechaInicial.getYear()) * 360L
                + (fechaFinal.getMonthValue()
                - fechaInicial.getMonthValue()) * 30L
                + (diaFinal - diaInicial);
    }
}