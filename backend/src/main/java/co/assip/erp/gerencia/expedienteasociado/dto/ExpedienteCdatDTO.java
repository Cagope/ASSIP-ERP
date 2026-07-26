package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ExpedienteCdatDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Long idCdat;
    private Long idDatosPersonal;

    private String documento;
    private String nombreCompleto;

    // =========================================================
    // CDAT
    // =========================================================

    private String numeroCdat;

    private String codigoEstado;
    private String nombreEstado;

    private Boolean activo;
    private Boolean cancelado;
    private Boolean vencido;

    // =========================================================
    // Fechas
    // =========================================================

    private LocalDate fechaConstitucion;
    private LocalDate fechaVencimiento;
    private LocalDate fechaCancelacion;

    private LocalDate fechaUltimoMovimiento;
    private LocalDate fechaUltimoPagoIntereses;

    private Integer plazoDias;
    private Integer diasTranscurridos;
    private Integer diasParaVencimiento;

    // =========================================================
    // Capital
    // =========================================================

    private BigDecimal capitalInicial;

    /**
     * Capital constituido históricamente para este certificado.
     *
     * Actualmente se obtiene del movimiento 001. Cuando el extracto
     * no tenga dicho movimiento, el repository usa capitalInicial
     * como respaldo.
     */
    private BigDecimal capitalHistoricoInvertido;

    /**
     * Capital actualmente vigente.
     *
     * Es cero para certificados cancelados o sin saldo.
     */
    private BigDecimal capitalVigente;

    private BigDecimal saldoCapital;

    /**
     * Se conserva por compatibilidad con los indicadores generales
     * del expediente. Actualmente corresponde al saldo de capital.
     */
    private BigDecimal saldoTotal;

    // =========================================================
    // Rendimiento histórico
    // =========================================================

    /**
     * Pagos históricos de intereses.
     *
     * Movimiento 055: PAGO INTERESES A DEPÓSITOS.
     */
    private BigDecimal interesesLiquidados;

    /**
     * Retención histórica practicada.
     *
     * Movimiento 056: RETENCIÓN EN LA FUENTE.
     */
    private BigDecimal retencionFuente;

    /**
     * Rendimiento histórico neto:
     *
     * interesesLiquidados - retencionFuente.
     */
    private BigDecimal rendimientoHistorico;

    private Integer cantidadPagosIntereses;
    private Integer cantidadMovimientos;

    // =========================================================
    // Tasas
    // =========================================================

    private BigDecimal tasaEA;
    private BigDecimal tasaNominal;

    // =========================================================
    // Titularidad
    // =========================================================

    private Boolean conjunto;
    private Integer numeroTitulares;

    // =========================================================
    // Alertas operativas
    // =========================================================

    private Boolean proximoVencer;

    private Integer cantidadAlertas;

    private Integer alertasCriticas;
    private Integer alertasAdvertencia;
    private Integer alertasInformativas;

    private String nivelAlerta;

    // =========================================================
    // Observaciones
    // =========================================================

    private String observaciones;

    // =========================================================
    // Constructor
    // =========================================================

    public ExpedienteCdatDTO() {

        activo = Boolean.FALSE;
        cancelado = Boolean.FALSE;
        vencido = Boolean.FALSE;
        proximoVencer = Boolean.FALSE;

        plazoDias = 0;
        diasTranscurridos = 0;
        diasParaVencimiento = 0;

        capitalInicial = BigDecimal.ZERO;
        capitalHistoricoInvertido = BigDecimal.ZERO;
        capitalVigente = BigDecimal.ZERO;

        saldoCapital = BigDecimal.ZERO;
        saldoTotal = BigDecimal.ZERO;

        interesesLiquidados = BigDecimal.ZERO;
        retencionFuente = BigDecimal.ZERO;
        rendimientoHistorico = BigDecimal.ZERO;

        cantidadPagosIntereses = 0;
        cantidadMovimientos = 0;

        tasaEA = BigDecimal.ZERO;
        tasaNominal = BigDecimal.ZERO;

        conjunto = Boolean.FALSE;
        numeroTitulares = 1;

        cantidadAlertas = 0;
        alertasCriticas = 0;
        alertasAdvertencia = 0;
        alertasInformativas = 0;

        nivelAlerta = "NORMAL";
    }

    // =========================================================
    // Evaluación general
    // =========================================================

    /**
     * Normaliza valores y evalúa las alertas operativas del CDAT.
     *
     * Un certificado cancelado nunca debe quedar marcado como
     * vencido o próximo a vencer.
     */
    public void evaluarEstado() {

        normalizarValores();
        calcularDiasVencimiento();
        normalizarEstadoOperativo();
        evaluarAlertas();
    }

    // =========================================================
    // Normalización de valores
    // =========================================================

    private void normalizarValores() {

        capitalInicial =
                valorSeguro(capitalInicial);

        capitalHistoricoInvertido =
                valorSeguro(capitalHistoricoInvertido);

        capitalVigente =
                valorSeguro(capitalVigente);

        saldoCapital =
                valorSeguro(saldoCapital);

        saldoTotal =
                valorSeguro(saldoTotal);

        interesesLiquidados =
                valorSeguro(interesesLiquidados);

        retencionFuente =
                valorSeguro(retencionFuente);

        tasaEA =
                valorSeguro(tasaEA);

        tasaNominal =
                valorSeguro(tasaNominal);

        plazoDias =
                enteroSeguro(plazoDias);

        diasTranscurridos =
                enteroSeguro(diasTranscurridos);

        cantidadPagosIntereses =
                enteroSeguro(cantidadPagosIntereses);

        cantidadMovimientos =
                enteroSeguro(cantidadMovimientos);

        numeroTitulares =
                numeroTitulares == null || numeroTitulares <= 0
                        ? 1
                        : numeroTitulares;

        rendimientoHistorico =
                interesesLiquidados.subtract(retencionFuente);

        /*
         * Protección temporal:
         * si el repository no entrega saldoTotal, usamos saldoCapital.
         */
        if (saldoTotal.compareTo(BigDecimal.ZERO) == 0
                && saldoCapital.compareTo(BigDecimal.ZERO) != 0) {

            saldoTotal = saldoCapital;
        }

        /*
         * Protección temporal para extractos históricos incompletos.
         */
        if (capitalHistoricoInvertido.compareTo(BigDecimal.ZERO) == 0
                && capitalInicial.compareTo(BigDecimal.ZERO) > 0) {

            capitalHistoricoInvertido = capitalInicial;
        }
    }

    // =========================================================
    // Fechas y estado operativo
    // =========================================================

    /**
     * Calcula los días restantes o vencidos respecto a la fecha actual.
     *
     * Un valor negativo significa que la fecha de vencimiento ya pasó.
     */
    public void calcularDiasVencimiento() {

        if (fechaVencimiento == null) {

            diasParaVencimiento = 0;
            return;
        }

        long dias =
                ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        fechaVencimiento
                );

        diasParaVencimiento =
                convertirEnteroSeguro(dias);
    }

    private void normalizarEstadoOperativo() {

        boolean estaCancelado =
                Boolean.TRUE.equals(cancelado)
                        || fechaCancelacion != null
                        || saldoCapital.compareTo(BigDecimal.ZERO) <= 0;

        cancelado =
                estaCancelado;

        if (estaCancelado) {

            activo = Boolean.FALSE;
            vencido = Boolean.FALSE;
            proximoVencer = Boolean.FALSE;
            capitalVigente = BigDecimal.ZERO;

            return;
        }

        boolean estaActivo =
                Boolean.TRUE.equals(activo)
                        && saldoCapital.compareTo(BigDecimal.ZERO) > 0;

        activo =
                estaActivo;

        boolean fechaYaVencida =
                fechaVencimiento != null
                        && fechaVencimiento.isBefore(LocalDate.now());

        boolean fechaProxima =
                fechaVencimiento != null
                        && !fechaVencimiento.isBefore(LocalDate.now())
                        && !fechaVencimiento.isAfter(
                        LocalDate.now().plusDays(30)
                );

        vencido =
                estaActivo && fechaYaVencida;

        proximoVencer =
                estaActivo && fechaProxima;

        capitalVigente =
                estaActivo
                        ? saldoCapital
                        : BigDecimal.ZERO;
    }

    // =========================================================
    // Alertas
    // =========================================================

    private void evaluarAlertas() {

        alertasCriticas = 0;
        alertasAdvertencia = 0;
        alertasInformativas = 0;

        if (Boolean.TRUE.equals(vencido)) {
            alertasCriticas++;
        }

        if (Boolean.TRUE.equals(proximoVencer)) {
            alertasAdvertencia++;
        }

        cantidadAlertas =
                alertasCriticas
                        + alertasAdvertencia
                        + alertasInformativas;

        if (alertasCriticas > 0) {

            nivelAlerta = "CRITICA";

        } else if (alertasAdvertencia > 0) {

            nivelAlerta = "ADVERTENCIA";

        } else if (alertasInformativas > 0) {

            nivelAlerta = "INFORMATIVA";

        } else {

            nivelAlerta = "NORMAL";
        }
    }

    // =========================================================
    // Consultas auxiliares
    // =========================================================

    /**
     * Indica si el certificado está activo y aún no ha vencido.
     */
    public boolean estaVigente() {

        return Boolean.TRUE.equals(activo)
                && !Boolean.TRUE.equals(cancelado)
                && !Boolean.TRUE.equals(vencido);
    }

    /**
     * Determina si el certificado conserva saldo de capital.
     */
    public boolean tieneSaldo() {

        return valorSeguro(saldoCapital)
                .compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Indica si el certificado ya fue cancelado.
     */
    public boolean estaCancelado() {

        return Boolean.TRUE.equals(cancelado);
    }

    /**
     * Calcula el porcentaje de ejecución del plazo.
     */
    public BigDecimal calcularPorcentajePlazo() {

        if (plazoDias == null
                || plazoDias <= 0
                || diasTranscurridos == null
                || diasTranscurridos <= 0) {

            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(diasTranscurridos)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(plazoDias),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    /**
     * Rentabilidad histórica neta respecto al capital histórico.
     *
     * Es un indicador informativo, no representa una tasa efectiva
     * anual ni sustituye la tasa pactada del certificado.
     */
    public BigDecimal calcularRentabilidadHistorica() {

        if (capitalHistoricoInvertido == null
                || capitalHistoricoInvertido.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return BigDecimal.ZERO;
        }

        return valorSeguro(rendimientoHistorico)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        capitalHistoricoInvertido,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    // =========================================================
    // Utilidades
    // =========================================================

    private BigDecimal valorSeguro(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private Integer enteroSeguro(
            Integer valor
    ) {

        return valor == null
                ? 0
                : valor;
    }

    private Integer convertirEnteroSeguro(
            long valor
    ) {

        if (valor > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        if (valor < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return (int) valor;
    }
}