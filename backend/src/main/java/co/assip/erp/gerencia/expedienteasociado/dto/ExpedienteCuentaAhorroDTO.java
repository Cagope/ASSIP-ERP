package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpedienteCuentaAhorroDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Long idCuentaAhorro;
    private Long idDatosPersonal;

    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Producto
    // =========================================================

    private Long idFormaAhorro;

    private String codigoFormaAhorro;
    private String nombreFormaAhorro;

    private String codigoTipoCaptacion;
    private String nombreTipoCaptacion;

    private String numeroCuenta;
    private String numeroLibreta;

    // =========================================================
    // Estado
    // =========================================================

    private String codigoEstadoCuenta;
    private String nombreEstadoCuenta;

    private Boolean activa;
    private Boolean bloqueada;
    private Boolean embargada;
    private Boolean cancelada;

    // =========================================================
    // Fechas y actividad
    // =========================================================

    private LocalDate fechaApertura;
    private LocalDate fechaUltimoMovimiento;

    private Integer diasSinMovimiento;

    // =========================================================
    // Saldos
    // =========================================================

    private BigDecimal saldoDisponible;
    private BigDecimal saldoCanje;
    private BigDecimal saldoTotal;
    private BigDecimal saldoPromedio;

    // =========================================================
    // Movimiento histórico consolidado
    // =========================================================

    private BigDecimal totalConsignaciones;
    private BigDecimal totalRetiros;
    private BigDecimal totalIntereses;

    // =========================================================
    // Movimiento del mes
    // =========================================================

    private Integer cantidadEntradasMes;
    private BigDecimal valorEntradasMes;

    private Integer cantidadSalidasMes;
    private BigDecimal valorSalidasMes;

    private Integer cantidadMovimientosMes;
    private BigDecimal valorMovimientosMes;

    // =========================================================
    // Movimiento del año
    // =========================================================

    private Integer cantidadMovimientosAno;
    private BigDecimal valorMovimientosAno;

    // =========================================================
    // Configuración
    // =========================================================

    private Boolean generaIntereses;
    private Boolean exentaGMF;
    private Boolean cuentaConjunta;
    private Boolean tieneBeneficiarios;
    private Boolean tieneApoderados;

    // =========================================================
    // Cheques
    // =========================================================

    private Integer cantidadChequesCanje;
    private BigDecimal valorChequesCanje;

    // =========================================================
    // Riesgo operativo
    // =========================================================

    private Boolean saldoNegativo;
    private Boolean movimientosInusuales;
    private Boolean cuentaInactiva;
    private Boolean requiereRevision;

    // =========================================================
    // Alertas
    // =========================================================

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

    public ExpedienteCuentaAhorroDTO() {

        inicializarBooleanos();
        inicializarValores();
        inicializarContadores();

        diasSinMovimiento = 0;
        nivelAlerta = "NORMAL";
    }

    // =========================================================
    // Evaluación general
    // =========================================================

    /**
     * Normaliza la información recibida y recalcula los valores
     * derivados y las alertas operativas de la cuenta.
     */
    public void evaluarEstadoCuenta() {

        normalizarDatos();

        calcularSaldoTotal();
        calcularConsolidadoMensual();

        saldoNegativo =
                saldoTotal.compareTo(BigDecimal.ZERO) < 0;

        reiniciarAlertas();

        evaluarAlertasCriticas();
        evaluarAlertasAdvertencia();
        evaluarAlertasInformativas();

        cantidadAlertas =
                alertasCriticas
                        + alertasAdvertencia
                        + alertasInformativas;

        /*
         * Las alertas informativas no obligan por sí solas
         * a revisar la cuenta.
         */
        requiereRevision =
                alertasCriticas > 0
                        || alertasAdvertencia > 0;

        determinarNivelAlerta();
    }

    // =========================================================
    // Cálculos
    // =========================================================

    /**
     * Calcula el saldo total de la cuenta.
     *
     * saldo total = saldo disponible + saldo en canje.
     */
    public BigDecimal calcularSaldoTotal() {

        saldoTotal =
                valorSeguro(saldoDisponible)
                        .add(valorSeguro(saldoCanje));

        return saldoTotal;
    }

    /**
     * Consolida las entradas y salidas del mes.
     *
     * El valor consolidado representa el volumen bruto operado,
     * no el movimiento neto.
     */
    public void calcularConsolidadoMensual() {

        cantidadMovimientosMes =
                enteroNoNegativo(cantidadEntradasMes)
                        + enteroNoNegativo(cantidadSalidasMes);

        valorMovimientosMes =
                valorSeguro(valorEntradasMes)
                        .add(valorSeguro(valorSalidasMes));
    }

    /**
     * Calcula el movimiento neto del mes.
     *
     * movimiento neto = entradas - salidas.
     */
    public BigDecimal calcularMovimientoNetoMes() {

        return valorSeguro(valorEntradasMes)
                .subtract(valorSeguro(valorSalidasMes));
    }

    /**
     * Calcula el movimiento neto histórico de la cuenta.
     *
     * movimiento neto histórico =
     * consignaciones + intereses - retiros.
     */
    public BigDecimal calcularMovimientoNetoHistorico() {

        return valorSeguro(totalConsignaciones)
                .add(valorSeguro(totalIntereses))
                .subtract(valorSeguro(totalRetiros));
    }

    // =========================================================
    // Alertas
    // =========================================================

    private void reiniciarAlertas() {

        cantidadAlertas = 0;

        alertasCriticas = 0;
        alertasAdvertencia = 0;
        alertasInformativas = 0;

        requiereRevision = Boolean.FALSE;
        nivelAlerta = "NORMAL";
    }

    private void evaluarAlertasCriticas() {

        if (Boolean.TRUE.equals(saldoNegativo)) {
            alertasCriticas++;
        }

        if (Boolean.TRUE.equals(bloqueada)) {
            alertasCriticas++;
        }
    }

    private void evaluarAlertasAdvertencia() {

        if (Boolean.TRUE.equals(embargada)) {
            alertasAdvertencia++;
        }

        if (Boolean.TRUE.equals(cuentaInactiva)
                && tieneSaldoDistintoDeCero()) {

            alertasAdvertencia++;
        }

        if (Boolean.TRUE.equals(movimientosInusuales)) {
            alertasAdvertencia++;
        }
    }

    private void evaluarAlertasInformativas() {

        if (Boolean.TRUE.equals(cuentaConjunta)) {
            alertasInformativas++;
        }

        if (Boolean.TRUE.equals(exentaGMF)) {
            alertasInformativas++;
        }

        if (Boolean.TRUE.equals(tieneBeneficiarios)) {
            alertasInformativas++;
        }

        if (Boolean.TRUE.equals(tieneApoderados)) {
            alertasInformativas++;
        }
    }

    private void determinarNivelAlerta() {

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
     * Indica si la cuenta presenta actividad dentro del número
     * de días indicado.
     */
    public boolean tieneActividadReciente(int dias) {

        if (dias < 0
                || fechaUltimoMovimiento == null
                || diasSinMovimiento == null) {

            return false;
        }

        return diasSinMovimiento <= dias;
    }

    /**
     * Determina si la cuenta tiene saldo positivo.
     */
    public boolean tieneSaldo() {

        return valorSeguro(saldoTotal)
                .compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Determina si la cuenta tiene saldo diferente de cero.
     */
    public boolean tieneSaldoDistintoDeCero() {

        return valorSeguro(saldoTotal)
                .compareTo(BigDecimal.ZERO) != 0;
    }

    /**
     * Determina si la cuenta puede recibir movimientos.
     *
     * Un embargo se maneja como alerta, pero no se utiliza aquí
     * como bloqueo automático porque depende del alcance real
     * de la medida registrada.
     */
    public boolean disponibleParaMovimientos() {

        return Boolean.TRUE.equals(activa)
                && !Boolean.TRUE.equals(bloqueada)
                && !Boolean.TRUE.equals(cancelada);
    }

    /**
     * Indica si existen entradas durante el mes.
     */
    public boolean tieneEntradasMes() {

        return enteroNoNegativo(cantidadEntradasMes) > 0
                || valorSeguro(valorEntradasMes)
                .compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Indica si existen salidas durante el mes.
     */
    public boolean tieneSalidasMes() {

        return enteroNoNegativo(cantidadSalidasMes) > 0
                || valorSeguro(valorSalidasMes)
                .compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Indica si la cuenta tiene cheques pendientes en canje.
     */
    public boolean tieneChequesEnCanje() {

        return enteroNoNegativo(cantidadChequesCanje) > 0
                || valorSeguro(valorChequesCanje)
                .compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Indica si la cuenta corresponde a aportes sociales.
     */
    public boolean esCuentaAportes() {

        return "1".equals(
                textoSeguro(codigoTipoCaptacion)
        );
    }

    /**
     * Indica si la cuenta corresponde a un producto de ahorro
     * diferente de aportes sociales.
     */
    public boolean esCuentaAhorros() {

        return !esCuentaAportes();
    }

    // =========================================================
    // Normalización
    // =========================================================

    private void normalizarDatos() {

        activa = booleanoSeguro(activa);
        bloqueada = booleanoSeguro(bloqueada);
        embargada = booleanoSeguro(embargada);
        cancelada = booleanoSeguro(cancelada);

        generaIntereses = booleanoSeguro(generaIntereses);
        exentaGMF = booleanoSeguro(exentaGMF);
        cuentaConjunta = booleanoSeguro(cuentaConjunta);
        tieneBeneficiarios = booleanoSeguro(tieneBeneficiarios);
        tieneApoderados = booleanoSeguro(tieneApoderados);

        movimientosInusuales =
                booleanoSeguro(movimientosInusuales);

        cuentaInactiva =
                booleanoSeguro(cuentaInactiva);

        saldoDisponible =
                valorSeguro(saldoDisponible);

        saldoCanje =
                valorSeguro(saldoCanje);

        saldoTotal =
                valorSeguro(saldoTotal);

        saldoPromedio =
                valorSeguro(saldoPromedio);

        totalConsignaciones =
                valorSeguro(totalConsignaciones);

        totalRetiros =
                valorSeguro(totalRetiros);

        totalIntereses =
                valorSeguro(totalIntereses);

        valorEntradasMes =
                valorSeguro(valorEntradasMes);

        valorSalidasMes =
                valorSeguro(valorSalidasMes);

        valorMovimientosMes =
                valorSeguro(valorMovimientosMes);

        valorMovimientosAno =
                valorSeguro(valorMovimientosAno);

        valorChequesCanje =
                valorSeguro(valorChequesCanje);

        diasSinMovimiento =
                enteroNoNegativo(diasSinMovimiento);

        cantidadEntradasMes =
                enteroNoNegativo(cantidadEntradasMes);

        cantidadSalidasMes =
                enteroNoNegativo(cantidadSalidasMes);

        cantidadMovimientosMes =
                enteroNoNegativo(cantidadMovimientosMes);

        cantidadMovimientosAno =
                enteroNoNegativo(cantidadMovimientosAno);

        cantidadChequesCanje =
                enteroNoNegativo(cantidadChequesCanje);
    }

    // =========================================================
    // Inicialización
    // =========================================================

    private void inicializarBooleanos() {

        activa = Boolean.FALSE;
        bloqueada = Boolean.FALSE;
        embargada = Boolean.FALSE;
        cancelada = Boolean.FALSE;

        generaIntereses = Boolean.FALSE;
        exentaGMF = Boolean.FALSE;
        cuentaConjunta = Boolean.FALSE;
        tieneBeneficiarios = Boolean.FALSE;
        tieneApoderados = Boolean.FALSE;

        saldoNegativo = Boolean.FALSE;
        movimientosInusuales = Boolean.FALSE;
        cuentaInactiva = Boolean.FALSE;
        requiereRevision = Boolean.FALSE;
    }

    private void inicializarValores() {

        saldoDisponible = BigDecimal.ZERO;
        saldoCanje = BigDecimal.ZERO;
        saldoTotal = BigDecimal.ZERO;
        saldoPromedio = BigDecimal.ZERO;

        totalConsignaciones = BigDecimal.ZERO;
        totalRetiros = BigDecimal.ZERO;
        totalIntereses = BigDecimal.ZERO;

        valorEntradasMes = BigDecimal.ZERO;
        valorSalidasMes = BigDecimal.ZERO;

        valorMovimientosMes = BigDecimal.ZERO;
        valorMovimientosAno = BigDecimal.ZERO;

        valorChequesCanje = BigDecimal.ZERO;
    }

    private void inicializarContadores() {

        cantidadEntradasMes = 0;
        cantidadSalidasMes = 0;

        cantidadMovimientosMes = 0;
        cantidadMovimientosAno = 0;

        cantidadChequesCanje = 0;

        cantidadAlertas = 0;
        alertasCriticas = 0;
        alertasAdvertencia = 0;
        alertasInformativas = 0;
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

    private Boolean booleanoSeguro(
            Boolean valor
    ) {

        return Boolean.TRUE.equals(valor);
    }

    private Integer enteroNoNegativo(
            Integer valor
    ) {

        if (valor == null || valor < 0) {
            return 0;
        }

        return valor;
    }

    private String textoSeguro(
            String valor
    ) {

        return valor == null
                ? ""
                : valor.trim();
    }
}