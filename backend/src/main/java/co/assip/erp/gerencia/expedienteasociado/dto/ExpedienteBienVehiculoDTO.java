package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ExpedienteBienVehiculoDTO {

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
    // Identificación del asociado
    // =========================================================

    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Bien general
    // =========================================================

    private Long idBien;

    private String codigoTipoBien;
    private String nombreTipoBien;

    private String descripcionGeneral;

    private BigDecimal valorComercial;
    private BigDecimal valorGravamen;
    private BigDecimal valorNeto;

    // =========================================================
    // Vehículo
    // =========================================================

    private Long idBienVehiculo;

    private Long idTipoVehiculo;

    private String codigoTipoVehiculo;
    private String nombreTipoVehiculo;

    private String placa;

    private String marca;
    private String linea;

    private Integer modelo;

    private String color;

    private String numeroMotor;
    private String numeroChasis;
    private String numeroSerie;

    // =========================================================
    // Gravamen
    // =========================================================

    private Long idTipoGravamen;

    private String codigoTipoGravamen;
    private String nombreTipoGravamen;

    private Boolean tieneGravamen;

    private BigDecimal porcentajeGravamen;

    // =========================================================
    // Seguro
    // =========================================================

    private Long idBienVehiculoSeguro;

    private String aseguradora;
    private String numeroPoliza;

    private BigDecimal valorAsegurado;

    private LocalDate fechaInicioSeguro;
    private LocalDate fechaVencimientoSeguro;

    private Integer diasParaVencimientoSeguro;

    private Boolean tieneSeguro;
    private Boolean seguroVigente;
    private Boolean seguroProximoVencer;
    private Boolean seguroVencido;

    private BigDecimal porcentajeCoberturaSeguro;

    // =========================================================
    // Garantía de cartera
    // =========================================================

    private Boolean vinculadoComoGarantia;

    private Integer cantidadCreditosGarantizados;

    private BigDecimal saldoCreditosGarantizados;

    private BigDecimal porcentajeCoberturaGarantia;

    private Boolean garantiaSuficiente;

    // =========================================================
    // Calidad de la información
    // =========================================================

    private Boolean informacionIdentificacionCompleta;
    private Boolean informacionSeguroCompleta;
    private Boolean informacionValoracionCompleta;

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

    public ExpedienteBienVehiculoDTO() {

        porcentajePropiedad = BigDecimal.valueOf(100);
        valorParticipacion = BigDecimal.ZERO;

        titularPrincipal = Boolean.FALSE;
        propiedadCompartida = Boolean.FALSE;
        cantidadPropietarios = 1;

        valorComercial = BigDecimal.ZERO;
        valorGravamen = BigDecimal.ZERO;
        valorNeto = BigDecimal.ZERO;

        tieneGravamen = Boolean.FALSE;
        porcentajeGravamen = BigDecimal.ZERO;

        valorAsegurado = BigDecimal.ZERO;

        diasParaVencimientoSeguro = 0;

        tieneSeguro = Boolean.FALSE;
        seguroVigente = Boolean.FALSE;
        seguroProximoVencer = Boolean.FALSE;
        seguroVencido = Boolean.FALSE;

        porcentajeCoberturaSeguro = BigDecimal.ZERO;

        vinculadoComoGarantia = Boolean.FALSE;

        cantidadCreditosGarantizados = 0;

        saldoCreditosGarantizados = BigDecimal.ZERO;

        porcentajeCoberturaGarantia = BigDecimal.ZERO;

        garantiaSuficiente = Boolean.FALSE;

        informacionIdentificacionCompleta = Boolean.FALSE;
        informacionSeguroCompleta = Boolean.FALSE;
        informacionValoracionCompleta = Boolean.FALSE;
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
    // Métodos auxiliares
    // =========================================================

    public BigDecimal calcularValorParticipacion() {

        valorParticipacion =
                valorSeguro(valorComercial)
                        .multiply(valorSeguro(porcentajePropiedad))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return valorParticipacion;
    }

    public BigDecimal calcularValorNeto() {

        valorNeto =
                valorSeguro(valorComercial)
                        .subtract(valorSeguro(valorGravamen));

        return valorNeto;
    }

    public void evaluarSeguro(int diasAdvertencia) {

        tieneSeguro =
                numeroPoliza != null
                        || fechaVencimientoSeguro != null;

        seguroVigente = false;
        seguroProximoVencer = false;
        seguroVencido = false;

        if (fechaVencimientoSeguro == null) {
            return;
        }

        long dias =
                ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        fechaVencimientoSeguro);

        diasParaVencimientoSeguro = (int) dias;

        if (dias < 0) {

            seguroVencido = true;

        } else if (dias <= diasAdvertencia) {

            seguroVigente = true;
            seguroProximoVencer = true;

        } else {

            seguroVigente = true;
        }

        calcularCoberturaSeguro();
    }

    public BigDecimal calcularCoberturaSeguro() {

        if (valorSeguro(valorComercial)
                .compareTo(BigDecimal.ZERO) <= 0) {

            porcentajeCoberturaSeguro = BigDecimal.ZERO;
            return porcentajeCoberturaSeguro;
        }

        porcentajeCoberturaSeguro =
                valorSeguro(valorAsegurado)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                valorSeguro(valorComercial),
                                2,
                                RoundingMode.HALF_UP);

        return porcentajeCoberturaSeguro;
    }

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

        return porcentajeCoberturaGarantia;
    }

    public int evaluarCompletitud() {

        informacionIdentificacionCompleta =
                placa != null
                        && marca != null
                        && linea != null
                        && modelo != null;

        informacionSeguroCompleta =
                !Boolean.TRUE.equals(tieneSeguro)
                        || (
                        aseguradora != null
                                && numeroPoliza != null
                                && fechaVencimientoSeguro != null
                );

        informacionValoracionCompleta =
                valorSeguro(valorComercial)
                        .compareTo(BigDecimal.ZERO) > 0;

        int total = 3;
        int completos = 0;

        if (informacionIdentificacionCompleta) completos++;
        if (informacionSeguroCompleta) completos++;
        if (informacionValoracionCompleta) completos++;

        porcentajeCompletitud =
                (completos * 100) / total;

        informacionCompleta =
                completos == total;

        return porcentajeCompletitud;
    }

    public void evaluarEstadoGeneral(int diasAdvertenciaSeguro) {

        calcularValorParticipacion();
        calcularValorNeto();

        evaluarSeguro(diasAdvertenciaSeguro);

        calcularCoberturaGarantia();

        evaluarCompletitud();

        alertasCriticas = 0;
        alertasAdvertencia = 0;
        alertasInformativas = 0;

        if (seguroVencido
                && Boolean.TRUE.equals(vinculadoComoGarantia)) {

            alertasCriticas++;
        }

        if (seguroProximoVencer) {

            alertasAdvertencia++;
        }

        if (!informacionCompleta) {

            alertasAdvertencia++;
        }

        if (Boolean.TRUE.equals(tieneGravamen)) {

            alertasInformativas++;
        }

        if (Boolean.TRUE.equals(propiedadCompartida)) {

            alertasInformativas++;
        }

        if (Boolean.TRUE.equals(vinculadoComoGarantia)) {

            alertasInformativas++;
        }

        cantidadAlertas =
                alertasCriticas
                        + alertasAdvertencia
                        + alertasInformativas;

        requiereRevision =
                alertasCriticas > 0
                        || alertasAdvertencia > 0;

        requiereActualizacion =
                seguroVencido
                        || !informacionCompleta;

        if (alertasCriticas > 0) {

            nivelAlerta = "CRITICA";
            resumenAlertas =
                    "El vehículo presenta novedades críticas.";

        } else if (alertasAdvertencia > 0) {

            nivelAlerta = "ADVERTENCIA";
            resumenAlertas =
                    "El vehículo requiere revisión.";

        } else if (alertasInformativas > 0) {

            nivelAlerta = "INFORMATIVA";
            resumenAlertas =
                    "El vehículo presenta información relevante.";

        } else {

            nivelAlerta = "NORMAL";
            resumenAlertas =
                    "El vehículo no presenta novedades.";
        }
    }

    private BigDecimal valorSeguro(BigDecimal valor) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

}