package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ExpedienteBienInversionDTO {

    // =========================================================
    // Relación persona ↔ bien
    // =========================================================
    private Long idBienPersona;
    private Long idDatosPersonal;

    private BigDecimal porcentajePropiedad;
    private BigDecimal valorParticipacion;

    private Boolean titularPrincipal;
    private Boolean propiedadCompartida;
    private Integer cantidadPropietarios;

    // =========================================================
    // Identificación asociado
    // =========================================================
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Bien general
    // =========================================================
    private Long idBien;

    private Long idTipoBien;
    private String codigoTipoBien;
    private String nombreTipoBien;

    private String descripcionGeneral;

    private BigDecimal valorComercial;
    private BigDecimal valorGravamen;
    private BigDecimal valorNeto;

    // =========================================================
    // Inversión
    // =========================================================
    private Long idBienInversion;

    private Long idTipoInversion;
    private String codigoTipoInversion;
    private String nombreTipoInversion;

    private String entidad;

    private String numeroTitulo;

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;

    private BigDecimal valorNominal;

    private BigDecimal tasa;

    // =========================================================
    // Indicadores financieros
    // =========================================================
    private BigDecimal rentabilidadEsperada;
    private BigDecimal rendimientoAcumulado;

    private Integer diasVigencia;
    private Integer diasParaVencimiento;

    private Boolean vigente;
    private Boolean proximoVencer;
    private Boolean vencido;

    // =========================================================
    // Gravamen
    // =========================================================
    private Long idTipoGravamen;

    private String codigoTipoGravamen;
    private String nombreTipoGravamen;

    private Boolean tieneGravamen;

    private BigDecimal porcentajeGravamen;

    // =========================================================
    // Garantía
    // =========================================================
    private Boolean vinculadoComoGarantia;

    private Integer cantidadCreditosGarantizados;

    private BigDecimal saldoCreditosGarantizados;

    private BigDecimal valorDisponibleGarantia;

    private BigDecimal porcentajeCoberturaGarantia;

    private Boolean garantiaSuficiente;
    private Boolean garantiaSobreutilizada;

    // =========================================================
    // Calidad información
    // =========================================================
    private Boolean informacionGeneralCompleta;
    private Boolean informacionFinancieraCompleta;
    private Boolean informacionFechasCompleta;

    private Boolean informacionCompleta;

    private Integer porcentajeCompletitud;

    // =========================================================
    // Estado
    // =========================================================
    private Boolean activo;

    private Boolean requiereRevision;
    private Boolean requiereActualizacion;

    // =========================================================
    // Alertas
    // =========================================================
    private Integer cantidadAlertas;

    private Integer alertasCriticas;
    private Integer alertasAdvertencia;
    private Integer alertasInformativas;

    private String nivelAlerta;

    private String resumenAlertas;

    // =========================================================
    // Observaciones
    // =========================================================
    private String observaciones;

    // =========================================================
    // Constructor
    // =========================================================
    public ExpedienteBienInversionDTO() {

        porcentajePropiedad = BigDecimal.valueOf(100);
        valorParticipacion = BigDecimal.ZERO;

        titularPrincipal = Boolean.FALSE;
        propiedadCompartida = Boolean.FALSE;
        cantidadPropietarios = 1;

        valorComercial = BigDecimal.ZERO;
        valorGravamen = BigDecimal.ZERO;
        valorNeto = BigDecimal.ZERO;

        valorNominal = BigDecimal.ZERO;
        tasa = BigDecimal.ZERO;

        rentabilidadEsperada = BigDecimal.ZERO;
        rendimientoAcumulado = BigDecimal.ZERO;

        vigente = Boolean.FALSE;
        proximoVencer = Boolean.FALSE;
        vencido = Boolean.FALSE;

        tieneGravamen = Boolean.FALSE;
        porcentajeGravamen = BigDecimal.ZERO;

        vinculadoComoGarantia = Boolean.FALSE;
        cantidadCreditosGarantizados = 0;

        saldoCreditosGarantizados = BigDecimal.ZERO;
        valorDisponibleGarantia = BigDecimal.ZERO;
        porcentajeCoberturaGarantia = BigDecimal.ZERO;

        garantiaSuficiente = Boolean.FALSE;
        garantiaSobreutilizada = Boolean.FALSE;

        informacionGeneralCompleta = Boolean.FALSE;
        informacionFinancieraCompleta = Boolean.FALSE;
        informacionFechasCompleta = Boolean.FALSE;
        informacionCompleta = Boolean.FALSE;

        porcentajeCompletitud = 0;

        activo = Boolean.TRUE;

        requiereRevision = Boolean.FALSE;
        requiereActualizacion = Boolean.FALSE;

        cantidadAlertas = 0;
        alertasCriticas = 0;
        alertasAdvertencia = 0;
        alertasInformativas = 0;
    }

    // =========================================================
    // Cálculos patrimoniales
    // =========================================================

    public BigDecimal calcularValorParticipacion() {

        valorParticipacion =
                valorSeguro(valorComercial)
                        .multiply(valorSeguro(porcentajePropiedad))
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP);

        return valorParticipacion;
    }

    public BigDecimal calcularValorNeto() {

        valorNeto =
                valorSeguro(valorComercial)
                        .subtract(valorSeguro(valorGravamen));

        return valorNeto;
    }

    public BigDecimal calcularRentabilidadEsperada() {

        if (fechaEmision == null
                || fechaVencimiento == null
                || tasa == null) {

            rentabilidadEsperada = BigDecimal.ZERO;
            return rentabilidadEsperada;
        }

        long dias =
                ChronoUnit.DAYS.between(
                        fechaEmision,
                        fechaVencimiento);

        rentabilidadEsperada =
                valorSeguro(valorNominal)
                        .multiply(tasa)
                        .multiply(BigDecimal.valueOf(dias))
                        .divide(
                                BigDecimal.valueOf(36500),
                                2,
                                RoundingMode.HALF_UP);

        return rentabilidadEsperada;
    }

    // =========================================================
    // Vigencia
    // =========================================================

    public void evaluarVigencia(int diasAdvertencia) {

        vigente = false;
        proximoVencer = false;
        vencido = false;

        if (fechaVencimiento == null) {
            return;
        }

        diasParaVencimiento =
                (int) ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        fechaVencimiento);

        if (diasParaVencimiento < 0) {

            vencido = true;

        } else if (diasParaVencimiento <= diasAdvertencia) {

            vigente = true;
            proximoVencer = true;

        } else {

            vigente = true;
        }
    }

    // =========================================================
    // Garantías
    // =========================================================

    public BigDecimal calcularCoberturaGarantia() {

        if (valorSeguro(saldoCreditosGarantizados)
                .compareTo(BigDecimal.ZERO) <= 0) {

            porcentajeCoberturaGarantia = BigDecimal.ZERO;
            garantiaSuficiente = true;

            return porcentajeCoberturaGarantia;
        }

        porcentajeCoberturaGarantia =
                valorSeguro(valorNeto)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                saldoCreditosGarantizados,
                                2,
                                RoundingMode.HALF_UP);

        garantiaSuficiente =
                valorSeguro(valorNeto)
                        .compareTo(saldoCreditosGarantizados) >= 0;

        garantiaSobreutilizada =
                !garantiaSuficiente;

        valorDisponibleGarantia =
                valorSeguro(valorNeto)
                        .subtract(
                                valorSeguro(
                                        saldoCreditosGarantizados));

        return porcentajeCoberturaGarantia;
    }

    // =========================================================
    // Calidad información
    // =========================================================

    public int evaluarCompletitud() {

        informacionGeneralCompleta =
                entidad != null
                        && numeroTitulo != null;

        informacionFinancieraCompleta =
                valorSeguro(valorNominal)
                        .compareTo(BigDecimal.ZERO) > 0
                        && tasa != null;

        informacionFechasCompleta =
                fechaEmision != null
                        && fechaVencimiento != null;

        int completas = 0;

        if (informacionGeneralCompleta) completas++;
        if (informacionFinancieraCompleta) completas++;
        if (informacionFechasCompleta) completas++;

        porcentajeCompletitud =
                completas * 100 / 3;

        informacionCompleta =
                completas == 3;

        return porcentajeCompletitud;
    }

    // =========================================================
    // Estado general
    // =========================================================

    public void evaluarEstadoGeneral(
            int diasAdvertenciaVencimiento) {

        calcularValorParticipacion();
        calcularValorNeto();
        calcularRentabilidadEsperada();
        calcularCoberturaGarantia();

        evaluarVigencia(
                diasAdvertenciaVencimiento);

        evaluarCompletitud();

        alertasCriticas = 0;
        alertasAdvertencia = 0;
        alertasInformativas = 0;

        if (vencido
                && vinculadoComoGarantia) {

            alertasCriticas++;
        }

        if (garantiaSobreutilizada) {

            alertasCriticas++;
        }

        if (proximoVencer) {

            alertasAdvertencia++;
        }

        if (!informacionCompleta) {

            alertasAdvertencia++;
        }

        if (tieneGravamen) {

            alertasInformativas++;
        }

        if (vinculadoComoGarantia) {

            alertasInformativas++;
        }

        cantidadAlertas =
                alertasCriticas
                        + alertasAdvertencia
                        + alertasInformativas;

        requiereRevision =
                cantidadAlertas > 0;

        requiereActualizacion =
                vencido
                        || !informacionCompleta;

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

    private BigDecimal valorSeguro(BigDecimal valor) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

}