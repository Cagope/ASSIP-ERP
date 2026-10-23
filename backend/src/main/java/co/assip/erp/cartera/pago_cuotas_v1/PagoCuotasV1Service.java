package co.assip.erp.cartera.pago_cuotas_v1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Consulta y liquida cuotas de cartera sin registrar pagos ni modificar saldos.
 * Separa los conceptos para facilitar su conciliación con el ERP anterior.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagoCuotasV1Service {

    private static final BigDecimal CERO = BigDecimal.ZERO;
    private static final BigDecimal DIVISOR_INTERESES = new BigDecimal("36000");
    private static final MathContext MC = MathContext.DECIMAL128;

    private final PagoCuotasV1Repository repository;

    public List<PagoCuotasV1Repository.CreditoResumen> listarCreditosPorAsociado(
            Integer idDatosPersonal) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException("Debe indicar el asociado.");
        }
        return repository.listarCreditosPorAsociado(idDatosPersonal);
    }

    /** Historial de pagos agrupado por comprobante, exclusivamente de consulta. */
    public List<PagoCuotasV1Repository.PagoRealizado> listarPagosRealizados(
            Integer idCarteraCredito) {
        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException("Debe indicar el crédito.");
        }
        return repository.listarPagosRealizados(idCarteraCredito);
    }

    public PagoCuotasV1Repository.CreditoLiquidacion consultarCredito(
            Integer idCarteraCredito) {
        if (idCarteraCredito == null || idCarteraCredito <= 0) {
            throw new IllegalArgumentException("Debe indicar el crédito.");
        }
        return repository.buscarCredito(idCarteraCredito)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el crédito " + idCarteraCredito + "."));
    }

    /**
     * Calcula los conceptos de mora con la misma secuencia del VB:
     * fecha de inicio -> días -> base de capital -> interés redondeado.
     *
     * La base de capital exigible se recibe del cálculo de cuotas; no se
     * presume que sea igual al saldo actual del pagaré.
     */
    public MoraCalculada calcularMora(
            PagoCuotasV1Repository.CreditoLiquidacion credito,
            LocalDate fechaPago,
            BigDecimal capitalExigibleAntesDeVencimiento) {

        validarFechaPago(fechaPago);
        if (credito == null) {
            throw new IllegalArgumentException("Debe indicar el crédito.");
        }
        BigDecimal base = positivo(capitalExigibleAntesDeVencimiento);
        LocalDate inicio = fechaInicioMora(credito, fechaPago);
        int dias = inicio == null ? 0 : diasVB(inicio, fechaPago);

        // VB no cobra mora por un único día de diferencia.
        if (dias <= 1 || base.signum() == 0) {
            return new MoraCalculada(inicio, 0, CERO, CERO, CERO);
        }

        BigDecimal tasa = tasaMoraVigente(fechaPago);
        BigDecimal interes = redondearVB(
                base.multiply(tasa, MC)
                        .multiply(BigDecimal.valueOf(dias), MC)
                        .divide(DIVISOR_INTERESES, MC));

        return new MoraCalculada(inicio, dias, tasa, base, interes);
    }

    /**
     * Determina la fecha de mora de acuerdo con CALCULAR_VALORES_CUOTA_FIJA
     * y CALCULAR_VALORES_VARIABLE del formulario VB.
     */
    public LocalDate fechaInicioMora(
            PagoCuotasV1Repository.CreditoLiquidacion credito,
            LocalDate fechaPago) {

        validarFechaPago(fechaPago);
        LocalDate proximaCapital = requerido(credito.proximaFechaCapital(),
                "proxima_fecha_capital");
        LocalDate ultimaMora = requerido(credito.ultimaFechaMora(),
                "ultima_fecha_mora");

        if (!proximaCapital.isBefore(fechaPago)) {
            return null;
        }

        if (esCuotaFija(credito)) {
            return proximaCapital.isAfter(ultimaMora) ? proximaCapital : ultimaMora;
        }

        // Rama variable: INTFH_C = intereses_pagados_hasta,
        // correspondencia indicada para la migración.
        if (!ultimaMora.isBefore(fechaPago)) {
            return null;
        }
        LocalDate interesesHasta = requerido(credito.interesesPagadosHasta(),
                "intereses_pagados_hasta");

        if (proximaCapital.isBefore(ultimaMora)) {
            return proximaCapital.isEqual(interesesHasta) ? ultimaMora : interesesHasta;
        }
        return interesesHasta.isAfter(ultimaMora) ? interesesHasta : proximaCapital;
    }

    /**
     * Interés corriente de cuota variable vencida, según VB.
     * No reemplaza el cálculo de cuota fija ni de intereses anticipados.
     */
    public InteresCorrienteCalculado calcularInteresVariableVencido(
            PagoCuotasV1Repository.CreditoLiquidacion credito,
            LocalDate fechaPago) {

        validarFechaPago(fechaPago);
        LocalDate desde = requerido(credito.ultimaFechaInteres(),
                "ultima_fecha_interes");
        int dias = diasVB(desde, fechaPago);
        if (dias < 0) {
            throw new IllegalArgumentException(
                    "La fecha de pago es anterior a la última fecha de intereses.");
        }
        BigDecimal saldo = positivo(credito.saldoActual());
        BigDecimal tasa = positivo(credito.tasaNominalAnual());
        BigDecimal interes = redondearInteresVariableVB(
                saldo.multiply(tasa, MC)
                        .multiply(BigDecimal.valueOf(dias), MC)
                        .divide(DIVISOR_INTERESES, MC));
        return new InteresCorrienteCalculado(desde, dias, tasa, interes);
    }

    /**
     * Consulta los datos financieros para la pantalla de liquidación.
     * El valor contractual de la cuota ya está persistido en cartera:
     * valor_primera_cuota y valor_cuota. No se recalcula con
     * CuotasFinancieras; el pago determinará solamente los conceptos
     * exigibles. La liquidación integral todavía requiere conciliar
     * las reglas de distribución del pago con el VB.
     */
    public DatosPrevios consultarDatosPrevios(Integer idCarteraCredito,
                                              LocalDate fechaPago) {
        validarFechaPago(fechaPago);
        var credito = consultarCredito(idCarteraCredito);
        if (positivo(credito.saldoActual()).signum() == 0) {
            throw new IllegalArgumentException("El crédito no tiene saldo pendiente.");
        }
        return new DatosPrevios(credito, fechaPago, tasaMoraVigente(fechaPago));
    }

    /**
     * Liquidación de cuotas exigibles para comparación con VB.
     * No registra pagos ni modifica fechas o saldos.
     *
     * periodoInteresMeses se suministra explícitamente porque el campo
     * periodo_codigo_interes es un código de catálogo, no necesariamente
     * el número de meses. Evita interpretar códigos como plazos.
     */
    public LiquidacionCuotas liquidar(Integer idCarteraCredito,
                                      LocalDate fechaPago,
                                      int periodoInteresMeses) {
        return liquidar(idCarteraCredito, fechaPago, periodoInteresMeses, null);
    }

    /**
     * Liquida las cuotas seleccionadas. Si cuotasACancelar es null,
     * conserva la selección automática basada en las fechas del crédito.
     * La consulta no registra movimientos ni actualiza saldos.
     */
    public LiquidacionCuotas liquidar(Integer idCarteraCredito,
                                      LocalDate fechaPago,
                                      int periodoInteresMeses,
                                      Integer cuotasACancelar) {
        validarFechaPago(fechaPago);
        if (periodoInteresMeses <= 0) {
            throw new IllegalArgumentException("El período de intereses debe ser mayor que cero.");
        }
        var c = consultarCredito(idCarteraCredito);
        BigDecimal saldo = positivo(c.saldoActual());
        if (saldo.signum() == 0) {
            throw new IllegalArgumentException("El crédito no tiene saldo pendiente.");
        }
        int amortizacion = enteroPositivo(c.amortizacionCapital(), "amortizacion_capital");
        int plazo = enteroPositivo(c.plazo(), "plazo");
        if (plazo % amortizacion != 0) {
            throw new IllegalArgumentException("El plazo no es divisible por la amortización de capital.");
        }
        int pagadas = c.alturaCuota() == null ? 0 : c.alturaCuota();
        int totalCuotas = plazo / amortizacion;
        if (pagadas < 0 || pagadas >= totalCuotas) {
            throw new IllegalArgumentException("La altura de cuota no corresponde a un crédito pendiente.");
        }
        LocalDate proximaCapital = requerido(c.proximaFechaCapital(), "proxima_fecha_capital");
        int diasDesdeVencimiento = diasVB(proximaCapital, fechaPago);
        int cantidad;
        if (esCuotaFija(c)) {
            cantidad = Math.max(1, Math.floorDiv(diasDesdeVencimiento, amortizacion * 30) + 1);
        } else {
            LocalDate referencia = "A".equalsIgnoreCase(c.tipoModalidadInteres())
                    ? requerido(c.ultimaFechaCapital(), "ultima_fecha_capital")
                    : proximaCapital;
            int dias = diasVB(referencia, fechaPago);
            cantidad = Math.max(1, Math.floorDiv(dias, periodoInteresMeses * 30) + 1);
        }
        cantidad = Math.min(cantidad, totalCuotas - pagadas);
        if (cuotasACancelar != null) {
            if (cuotasACancelar <= 0 || cuotasACancelar > totalCuotas - pagadas) {
                throw new IllegalArgumentException(
                        "Las cuotas a cancelar deben estar entre 1 y "
                                + (totalCuotas - pagadas) + ".");
            }
            cantidad = cuotasACancelar;
        }

        BigDecimal capital = CERO;
        BigDecimal intereses = CERO;
        BigDecimal saldoSimulado = saldo;
        BigDecimal tasaCorriente = positivo(c.tasaNominalAnual());
        boolean fija = esCuotaFija(c);

        if (fija) {
            BigDecimal cuota = positivo(c.valorCuota());
            BigDecimal factor = tasaCorriente
                    .multiply(BigDecimal.valueOf(periodoInteresMeses), MC)
                    .divide(new BigDecimal("1200"), MC);
            for (int i = 0; i < cantidad; i++) {
                BigDecimal interesCuota = redondearVB(saldoSimulado.multiply(factor, MC));
                BigDecimal capitalCuota = redondearVB(cuota.subtract(interesCuota));
                if (capitalCuota.signum() < 0) {
                    throw new IllegalStateException("La cuota fija es inferior a sus intereses.");
                }
                capitalCuota = capitalCuota.min(saldoSimulado);
                capital = capital.add(capitalCuota);
                intereses = intereses.add(interesCuota);
                saldoSimulado = saldoSimulado.subtract(capitalCuota);
                if (saldoSimulado.signum() == 0) break;
            }
        } else {
            // En cuota variable el valor almacenado es el capital periódico.
            // Se respeta el vencimiento de capital cuando los períodos difieren.
            for (int i = 0; i < cantidad; i++) {
                if (periodoInteresMeses == amortizacion ||
                        !proximaCapital.isAfter(fechaPago)) {
                    BigDecimal capitalCuota = positivo(pagadas + i == 0
                            ? c.valorPrimeraCuota() : c.valorCuota());
                    capitalCuota = capitalCuota.min(saldoSimulado);
                    capital = capital.add(capitalCuota);
                    saldoSimulado = saldoSimulado.subtract(capitalCuota);
                }
                if (saldoSimulado.signum() == 0) break;
            }
            // Interés variable: el VB calcula sobre el saldo inicial del pago.
            if ("A".equalsIgnoreCase(c.tipoModalidadInteres())) {
                LocalDate hasta = requerido(c.interesesPagadosHasta(), "intereses_pagados_hasta");
                int dias;
                if (!fechaPago.isBefore(requerido(c.fechaFinal(), "fecha_final"))) {
                    dias = Math.max(0, diasVB(hasta, fechaPago));
                } else if (fechaPago.isAfter(hasta)) {
                    int transcurridos = diasVB(hasta, fechaPago);
                    dias = Math.max(1, Math.floorDiv(transcurridos, periodoInteresMeses * 30))
                            * periodoInteresMeses * 30;
                } else {
                    dias = periodoInteresMeses * 30;
                }
                intereses = interesPorDias(saldo, tasaCorriente, dias);
            } else {
                LocalDate desde = requerido(c.ultimaFechaInteres(), "ultima_fecha_interes");
                int dias = diasVB(desde, fechaPago);
                if (dias < 0) {
                    throw new IllegalArgumentException("Pago anterior a la última fecha de intereses.");
                }
                intereses = interesPorDias(saldo, tasaCorriente, dias);
            }
        }

        // Como en VB, la base de mora se determina antes de forzar el
        // capital total al llegar a la fecha final del pagaré.
        MoraCalculada mora = calcularMora(c, fechaPago, capital);
        boolean vencimientoFinal = !fechaPago.isBefore(requerido(c.fechaFinal(), "fecha_final"));
        BigDecimal capitalExigible = vencimientoFinal ? saldo : capital;
        // El seguro ya fue causado por Seguros de Cartera; aquí solo se recauda.
        BigDecimal segurosPendientes = repository.consultarSaldoSeguros(c.idCarteraCredito());
        if (segurosPendientes == null) segurosPendientes = CERO;
        if (segurosPendientes.signum() < 0) {
            throw new IllegalStateException("Saldo negativo de seguros para el crédito "
                    + c.idCarteraCredito() + ".");
        }
        // Los alivios son intereses históricos: aquí solo se consulta su saldo
        // y se distribuye entre las cuotas de capital pendientes.
        BigDecimal saldoAlivios = repository.consultarSaldoAlivios(c.idCarteraCredito());
        if (saldoAlivios == null) saldoAlivios = CERO;
        if (saldoAlivios.signum() < 0) {
            throw new IllegalStateException("Saldo negativo de alivios para el crédito "
                    + c.idCarteraCredito() + ".");
        }
        BigDecimal cuotaAlivios = calcularCuotaAlivios(
                saldoAlivios, totalCuotas - pagadas);

        BigDecimal total = capitalExigible.add(intereses)
                .add(mora.interesesMora()).add(segurosPendientes)
                .add(cuotaAlivios);

        // Sugerencia informativa para cuota variable no vencida.
        // No modifica el capital exigible ni la liquidación validada.
        BigDecimal capitalAnticipadoSugerido = CERO;
        if (!fija && proximaCapital.isAfter(fechaPago)
                && capitalExigible.signum() == 0) {
            BigDecimal proximaCuota = positivo(pagadas == 0
                    ? c.valorPrimeraCuota() : c.valorCuota());
            capitalAnticipadoSugerido = proximaCuota.min(saldo);
        }
        BigDecimal valorPagoSugerido = total.add(capitalAnticipadoSugerido);

        return new LiquidacionCuotas(c.idCarteraCredito(), c.pagareCartera(),
                fechaPago, fija ? "FIJA" : "VARIABLE", cantidad,
                saldo, capitalExigible, intereses, mora, segurosPendientes,
                saldoAlivios, cuotaAlivios, total, vencimientoFinal,
                capitalAnticipadoSugerido, valorPagoSugerido);
    }

    /**
     * Distribuye el saldo de alivios entre las cuotas de capital restantes.
     * La cuota se cobra una sola vez por liquidación y nunca supera el saldo.
     */
    private static BigDecimal calcularCuotaAlivios(
            BigDecimal saldoAlivios, int cuotasRestantes) {
        if (saldoAlivios.signum() == 0) return CERO;
        if (cuotasRestantes <= 0) {
            throw new IllegalStateException(
                    "El crédito tiene alivios pendientes sin cuotas restantes.");
        }
        return saldoAlivios.divide(
                        BigDecimal.valueOf(cuotasRestantes), 0, RoundingMode.HALF_UP)
                .min(saldoAlivios);
    }

    /**
     * Simula la distribución del valor recibido sin modificar el crédito.
     *
     * Para cuota variable, primero cubre intereses, mora, seguros y alivios;
     * el remanente se abona a capital. Para cuota fija se conserva, en esta
     * primera versión, el mínimo equivalente a la liquidación completa.
     *
     * Un valor superior al liquidado puede representar un abono adicional a
     * capital, siempre que no exceda el saldo pendiente del pagaré.
     */
    public SimulacionPago simularPago(Integer idCarteraCredito,
                                      LocalDate fechaPago,
                                      int periodoInteresMeses,
                                      Integer cuotasACancelar,
                                      BigDecimal valorPago) {
        BigDecimal recibido = positivo(valorPago);
        if (recibido.signum() == 0) {
            throw new IllegalArgumentException("El valor del pago debe ser mayor que cero.");
        }

        LiquidacionCuotas liquidacion = liquidar(
                idCarteraCredito, fechaPago, periodoInteresMeses, cuotasACancelar);

        BigDecimal intereses = liquidacion.interesesCorrientes();
        BigDecimal mora = liquidacion.mora().interesesMora();
        BigDecimal seguros = liquidacion.segurosPendientes();
        BigDecimal alivios = liquidacion.cuotaAlivios();
        BigDecimal otrosConceptos = intereses.add(mora).add(seguros).add(alivios);
        boolean cuotaFija = "FIJA".equals(liquidacion.tipoCuota());
        BigDecimal minimo = cuotaFija
                ? liquidacion.totalLiquidacion()
                : otrosConceptos;
        BigDecimal maximo = otrosConceptos.add(liquidacion.saldoInicial());

        if (recibido.compareTo(minimo) < 0) {
            throw new IllegalArgumentException(
                    "El valor del pago es inferior al mínimo permitido: " + minimo + ".");
        }
        if (recibido.compareTo(maximo) > 0) {
            throw new IllegalArgumentException(
                    "El valor del pago supera el saldo de capital más los conceptos exigibles: "
                            + maximo + ".");
        }

        BigDecimal capitalAplicado = recibido.subtract(otrosConceptos);
        BigDecimal saldoCapitalFinal = liquidacion.saldoInicial().subtract(capitalAplicado);
        BigDecimal capitalLiquidado = liquidacion.capitalExigible();

        return new SimulacionPago(
                liquidacion, recibido, minimo, maximo,
                capitalAplicado, intereses, mora, seguros, alivios,
                capitalLiquidado.subtract(capitalAplicado),
                saldoCapitalFinal,
                capitalAplicado.compareTo(capitalLiquidado) > 0,
                capitalAplicado.compareTo(capitalLiquidado) < 0);
    }

    /** Resultado exclusivamente informativo; no actualiza saldos ni fechas. */
    public record SimulacionPago(
            LiquidacionCuotas liquidacion,
            BigDecimal valorPago,
            BigDecimal pagoMinimo,
            BigDecimal pagoMaximo,
            BigDecimal capitalAplicado,
            BigDecimal interesesCorrientesAplicados,
            BigDecimal interesesMoraAplicados,
            BigDecimal segurosAplicados,
            BigDecimal aliviosAplicados,
            BigDecimal diferenciaCapitalLiquidado,
            BigDecimal saldoCapitalProyectado,
            boolean abonoAdicionalCapital,
            boolean capitalInferiorAlLiquidado) {}

    private static int enteroPositivo(Integer valor, String nombre) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("Falta un valor válido de " + nombre + ".");
        }
        return valor;
    }

    private static BigDecimal interesPorDias(BigDecimal saldo, BigDecimal tasa, int dias) {
        BigDecimal bruto = saldo.multiply(tasa, MC)
                .multiply(BigDecimal.valueOf(dias), MC)
                .divide(DIVISOR_INTERESES, MC);
        return redondearInteresVariableVB(bruto);
    }

    public record LiquidacionCuotas(Integer idCarteraCredito,
                                    String pagareCartera,
                                    LocalDate fechaPago,
                                    String tipoCuota,
                                    int cuotasConsideradas,
                                    BigDecimal saldoInicial,
                                    BigDecimal capitalExigible,
                                    BigDecimal interesesCorrientes,
                                    MoraCalculada mora,
                                    BigDecimal segurosPendientes,
                                    BigDecimal saldoAlivios,
                                    BigDecimal cuotaAlivios,
                                    BigDecimal totalLiquidacion,
                                    boolean vencimientoFinal,
                                    BigDecimal capitalAnticipadoSugerido,
                                    BigDecimal valorPagoSugerido) {}

    private BigDecimal tasaMoraVigente(LocalDate fechaPago) {
        return repository.buscarTasaMoraVigente(fechaPago)
                .filter(tasa -> tasa.signum() > 0)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe tasa de mora vigente para " + fechaPago + "."));
    }

    /** Valor contractual registrado, sin recalcular la cuota. */
    public BigDecimal valorCuotaCapitalRegistrada(
            PagoCuotasV1Repository.CreditoLiquidacion credito,
            boolean primeraCuota) {
        if (credito == null) {
            throw new IllegalArgumentException("Debe indicar el crédito.");
        }
        BigDecimal valor = primeraCuota
                ? credito.valorPrimeraCuota()
                : credito.valorCuota();
        return positivo(valor);
    }

    private static boolean esCuotaFija(
            PagoCuotasV1Repository.CreditoLiquidacion credito) {
        // En el VB CUOTAF_C = "1" significa fija. El catálogo ERP
        // debe conservar esa equivalencia antes de habilitar liquidación.
        String tipo = credito.codigoTipoCuota();
        if ("1".equals(tipo)) return true;
        if ("2".equals(tipo)) return false;
        throw new IllegalArgumentException(
                "Tipo de cuota no reconocido para liquidación: " + tipo);
    }

    private static BigDecimal positivo(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Falta un valor financiero obligatorio.");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("El valor financiero no puede ser negativo.");
        }
        return valor;
    }

    private static LocalDate requerido(LocalDate fecha, String campo) {
        if (fecha == null) {
            throw new IllegalArgumentException("Falta la fecha " + campo + ".");
        }
        return fecha;
    }

    private static void validarFechaPago(LocalDate fechaPago) {
        if (fechaPago == null) {
            throw new IllegalArgumentException("Debe indicar la fecha de pago.");
        }
    }

    /**
     * Convención 30/360 usada por TablaAmortizacionService del ERP.
     * La equivalencia exacta con DIFFECHAS del VB debe verificarse
     * en las pruebas comparativas antes de registrar pagos.
     */
    private static int diasVB(LocalDate desde, LocalDate hasta) {
        int diaInicial = Math.min(desde.getDayOfMonth(), 30);
        int diaFinal = Math.min(hasta.getDayOfMonth(), 30);
        return Math.toIntExact(
                (hasta.getYear() - desde.getYear()) * 360L
                        + (hasta.getMonthValue() - desde.getMonthValue()) * 30L
                        + diaFinal - diaInicial);
    }

    /** VB Round utiliza redondeo bancario (mitad al par). */
    private static BigDecimal redondearVB(BigDecimal valor) {
        return valor.setScale(0, RoundingMode.HALF_EVEN);
    }

    /** En la rama de interés variable VB fuerza las mitades exactas hacia arriba. */
    private static BigDecimal redondearInteresVariableVB(BigDecimal valor) {
        return valor.setScale(0, RoundingMode.HALF_UP);
    }

    public record MoraCalculada(LocalDate fechaInicioMora,
                                int diasMora,
                                BigDecimal tasaNominalAnual,
                                BigDecimal baseMora,
                                BigDecimal interesesMora) {}

    public record InteresCorrienteCalculado(LocalDate fechaDesde,
                                            int diasInteres,
                                            BigDecimal tasaNominalAnual,
                                            BigDecimal interesesCorrientes) {}

    public record DatosPrevios(
            PagoCuotasV1Repository.CreditoLiquidacion credito,
            LocalDate fechaPago,
            BigDecimal tasaMoraVigente) {}
}
