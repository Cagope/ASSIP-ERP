package co.assip.erp.cartera.prorrogas_v1;

import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Repository;
import co.assip.erp.cartera.pago_cuotas_v1.PagoCuotasV1Service;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Liquida prórrogas sin aplicar pagos ni modificar datos financieros. */
@Service
@RequiredArgsConstructor
public class ProrrogasV1LiquidacionService {

    private static final BigDecimal CERO = BigDecimal.ZERO;
    private static final BigDecimal DIVISOR = new BigDecimal("36000");
    private static final MathContext MC = MathContext.DECIMAL128;

    private final PagoCuotasV1Service pagoCuotasV1Service;
    private final PagoCuotasV1Repository pagoCuotasV1Repository;
    private final UsuarioSesionService usuarioSesionService;
    private final JdbcTemplate jdbc;

    /**
     * Liquida los conceptos exigibles para una prórroga.
     * El capital es opcional y se presenta inicialmente en cero.
     */
    @Transactional(readOnly = true)
    public LiquidacionProrroga liquidar(
            Integer idCredito,
            LocalDate fechaLiquidacion) {

        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un crédito válido.");
        }
        if (fechaLiquidacion == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha de liquidación.");
        }

        var credito = pagoCuotasV1Service.consultarCredito(idCredito);
        usuarioSesionService.validarAgencia(credito.idAgencia());

        if (!"A".equals(credito.codigoEstadoCartera())
                || credito.saldoActual() == null
                || credito.saldoActual().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El crédito debe estar activo y tener saldo pendiente.");
        }

        if (!"2".equals(credito.codigoTipoCuota())) {
            throw new IllegalArgumentException(
                    "Las prórrogas están habilitadas para créditos de cuota variable.");
        }

        if (existeProrrogaActiva(idCredito)) {
            throw new IllegalArgumentException(
                    "El crédito ya tiene una prórroga activa.");
        }

        var modalidad = consultarModalidad(idCredito);

        validarFechas(
                credito,
                fechaLiquidacion,
                modalidad.tipoModalidad());

        BigDecimal baseMora = calcularBaseMora(
                credito,
                fechaLiquidacion,
                modalidad.periodoMeses());

        BigDecimal intereses = validarImporte(
                calcularIntereses(
                        credito,
                        fechaLiquidacion,
                        modalidad.periodoMeses(),
                        modalidad.tipoModalidad()),
                "intereses corrientes");

        BigDecimal mora = validarImporte(
                pagoCuotasV1Service.calcularMora(
                                credito,
                                fechaLiquidacion,
                                baseMora)
                        .interesesMora(),
                "intereses de mora");

        BigDecimal seguros = validarImporte(
                pagoCuotasV1Repository.consultarSaldoSeguros(idCredito),
                "seguros");

        BigDecimal capital = CERO;
        BigDecimal aportes = CERO;
        BigDecimal fondoGarantia = CERO;
        BigDecimal otrosConceptos = CERO;

        BigDecimal total = intereses
                .add(mora)
                .add(capital)
                .add(seguros)
                .add(aportes)
                .add(fondoGarantia)
                .add(otrosConceptos);

        return new LiquidacionProrroga(
                idCredito,
                fechaLiquidacion,
                modalidad.periodoMeses(),
                modalidad.tipoModalidad(),
                credito.saldoActual(),
                intereses,
                mora,
                capital,
                seguros,
                aportes,
                fondoGarantia,
                otrosConceptos,
                total);
    }

    /**
     * Consulta la modalidad contractual registrada en el catálogo.
     * No deduce la modalidad a partir de las fechas del crédito.
     */
    private ModalidadInteres consultarModalidad(Integer idCredito) {

        String sql = """
                SELECT mi.periodo_meses, mi.tipo_modalidad
                FROM cartera.carteras_creditos cc
                JOIN cartera.modalidades_intereses mi
                  ON mi.periodo_codigo = cc.periodo_codigo_interes
                 AND mi.tipo_modalidad = cc.tipo_modalidad_interes
                WHERE cc.id_cartera_credito = ?
                  AND mi.activo = TRUE
                """;

        return jdbc.query(sql, rs -> {
            if (!rs.next()) {
                throw new IllegalStateException(
                        "El crédito no tiene una modalidad de intereses activa.");
            }

            int meses = rs.getInt("periodo_meses");
            String tipo = rs.getString("tipo_modalidad");

            if (meses <= 0 || (!"A".equals(tipo) && !"V".equals(tipo))) {
                throw new IllegalStateException(
                        "La modalidad contractual de intereses es inválida.");
            }

            return new ModalidadInteres(meses, tipo);
        }, idCredito);
    }

    /** Verifica que no exista otra prórroga activa para el crédito. */
    private boolean existeProrrogaActiva(Integer idCredito) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM cartera.creditos_prorrogas
                    WHERE id_cartera_credito = ?
                      AND estado_prorroga = 'A'
                )
                """;

        return Boolean.TRUE.equals(
                jdbc.queryForObject(sql, Boolean.class, idCredito));
    }

    /**
     * Calcula los intereses corrientes según modalidad contractual.
     * Aplica convención comercial 30/360.
     */
    private BigDecimal calcularIntereses(
            PagoCuotasV1Repository.CreditoLiquidacion credito,
            LocalDate fechaLiquidacion,
            int periodoMeses,
            String modalidad) {

        if ("V".equals(modalidad)) {
            return pagoCuotasV1Service.calcularInteresVariableVencido(
                    credito, fechaLiquidacion).interesesCorrientes();
        }

        BigDecimal saldo = valorRequerido(
                credito.saldoActual(), "saldo actual");

        BigDecimal tasa = valorRequerido(
                credito.tasaNominalAnual(), "tasa nominal anual");

        int dias;

        if ("A".equals(modalidad)) {

            LocalDate pagadosHasta = fechaRequerida(
                    credito.interesesPagadosHasta(),
                    "intereses pagados hasta");

            LocalDate fechaFinal = fechaRequerida(
                    credito.fechaFinal(),
                    "fecha final del crédito");

            int diasPeriodo = Math.multiplyExact(periodoMeses, 30);

            if (!fechaLiquidacion.isBefore(fechaFinal)) {

                dias = Math.max(
                        0,
                        dias360(pagadosHasta, fechaLiquidacion));

            } else if (fechaLiquidacion.isAfter(pagadosHasta)) {

                int diasPendientes = dias360(
                        pagadosHasta,
                        fechaLiquidacion);

                int periodos = Math.max(
                        1,
                        Math.floorDiv(diasPendientes, diasPeriodo));

                dias = Math.multiplyExact(
                        periodos,
                        diasPeriodo);

            } else {

                dias = diasPeriodo;
            }

        } else {
            throw new IllegalArgumentException(
                    "Modalidad de intereses no reconocida.");
        }

        BigDecimal resultado = saldo
                .multiply(tasa, MC)
                .multiply(BigDecimal.valueOf(dias), MC)
                .divide(DIVISOR, MC);

        return resultado.setScale(0, RoundingMode.HALF_UP);
    }

    /** Determina una cuota contractual como base de mora, sin recaudar capital. */
    private BigDecimal calcularBaseMora(
            PagoCuotasV1Repository.CreditoLiquidacion credito,
            LocalDate fechaLiquidacion,
            int periodoInteresMeses) {

        LocalDate proximaCapital = fechaRequerida(
                credito.proximaFechaCapital(), "próxima fecha de capital");
        Integer amortizacion = credito.amortizacionCapital();
        if (amortizacion == null || amortizacion <= 0) {
            throw new IllegalStateException(
                    "El crédito no tiene una periodicidad de capital válida.");
        }
        if (!proximaCapital.isBefore(fechaLiquidacion)
                && amortizacion != periodoInteresMeses) {
            return CERO;
        }

        int altura = credito.alturaCuota() == null ? 0 : credito.alturaCuota();
        BigDecimal cuota = pagoCuotasV1Service.valorCuotaCapitalRegistrada(
                credito, altura == 0);
        return cuota.min(valorRequerido(credito.saldoActual(), "saldo actual"));
    }

    /**
     * Verifica las fechas de liquidación según la modalidad.
     * Impide liquidaciones anteriores a los registros contractuales.
     */
    private void validarFechas(
            PagoCuotasV1Repository.CreditoLiquidacion credito,
            LocalDate fechaLiquidacion,
            String modalidad) {

        LocalDate referencia;

        if ("V".equals(modalidad)) {
            referencia = fechaRequerida(
                    credito.ultimaFechaInteres(),
                    "última fecha de intereses");
        } else {
            referencia = fechaRequerida(
                    credito.ultimaFechaCapital(),
                    "última fecha de capital");
        }

        if (fechaLiquidacion.isBefore(referencia)) {
            throw new IllegalArgumentException(
                    "La fecha de liquidación es anterior "
                            + "a la fecha contractual de referencia.");
        }
    }

    /** Calcula días comerciales con la convención 30/360 de Pago de Cuotas. */
    private static int dias360(
            LocalDate desde,
            LocalDate hasta) {

        int diaDesde = Math.min(desde.getDayOfMonth(), 30);
        int diaHasta = Math.min(hasta.getDayOfMonth(), 30);

        return Math.toIntExact(
                (hasta.getYear() - desde.getYear()) * 360L
                        + (hasta.getMonthValue()
                        - desde.getMonthValue()) * 30L
                        + diaHasta
                        - diaDesde);
    }

    /** Exige que el importe exista y no sea negativo. */
    private static BigDecimal valorRequerido(
            BigDecimal valor,
            String concepto) {

        if (valor == null || valor.signum() < 0) {
            throw new IllegalStateException(
                    "Valor inválido para " + concepto + ".");
        }

        return valor;
    }

    /** Exige que la fecha contractual esté registrada. */
    private static LocalDate fechaRequerida(
            LocalDate fecha,
            String concepto) {

        if (fecha == null) {
            throw new IllegalStateException(
                    "No está registrada la fecha de " + concepto + ".");
        }

        return fecha;
    }

    /** Valida importes en pesos, admitiendo valores iguales a cero. */
    private static BigDecimal validarImporte(
            BigDecimal valor,
            String concepto) {

        if (valor == null
                || valor.signum() < 0
                || valor.stripTrailingZeros().scale() > 0) {
            throw new IllegalStateException(
                    "Importe inválido para " + concepto + ".");
        }

        return valor;
    }

    private record ModalidadInteres(
            int periodoMeses,
            String tipoModalidad) {
    }

    /** Resultado financiero de la liquidación de prórroga. */
    public record LiquidacionProrroga(
            Integer idCarteraCredito,
            LocalDate fechaLiquidacion,
            Integer periodoInteresMeses,
            String tipoModalidadInteres,
            BigDecimal saldoCapital,
            BigDecimal valorInteresCorriente,
            BigDecimal valorInteresMora,
            BigDecimal valorCapital,
            BigDecimal valorSeguro,
            BigDecimal valorAportes,
            BigDecimal valorFondoGarantia,
            BigDecimal valorOtrosConceptos,
            BigDecimal valorTotalSugerido) {
    }
}
