package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ExpedienteBienMaquinariaDTO {

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
    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Bien general: hoja_vida.bienes
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
    // Maquinaria: hoja_vida.bienes_maquinaria
    // =========================================================
    private Long idBienMaquinaria;

    private Long idTipoMaquinaria;
    private String codigoTipoMaquinaria;
    private String nombreTipoMaquinaria;

    private String marca;
    private String modelo;
    private String serie;

    private String descripcionTecnica;

    // =========================================================
    // Gravamen principal
    // Regla del proyecto: un solo tipo de gravamen por bien.
    // =========================================================
    private Long idTipoGravamen;
    private String codigoTipoGravamen;
    private String nombreTipoGravamen;

    private Boolean tieneGravamen;

    private BigDecimal porcentajeGravamen;

    private Boolean gravamenSuperaValorComercial;
    private Boolean valorNetoNegativo;

    // =========================================================
    // Seguro vigente o más reciente
    // =========================================================
    private Long idBienMaquinariaSeguro;

    private String aseguradora;
    private String numeroPoliza;

    private BigDecimal valorAsegurado;

    private LocalDate fechaInicioSeguro;
    private LocalDate fechaVencimientoSeguro;

    private String estadoSeguro;
    private String observacionesSeguro;

    private Integer diasParaVencimientoSeguro;

    private Boolean tieneSeguro;
    private Boolean seguroVigente;
    private Boolean seguroProximoVencer;
    private Boolean seguroVencido;

    private BigDecimal porcentajeCoberturaSeguro;
    private Boolean seguroCubreValorComercial;

    // =========================================================
    // Relación futura como garantía de cartera
    // =========================================================
    private Boolean vinculadoComoGarantia;
    private Integer cantidadCreditosGarantizados;

    private BigDecimal saldoCreditosGarantizados;
    private BigDecimal valorDisponibleGarantia;
    private BigDecimal porcentajeCoberturaGarantia;

    private Boolean garantiaSuficiente;
    private Boolean garantiaSobreutilizada;

    // =========================================================
    // Calidad de la información
    // =========================================================
    private Boolean informacionIdentificacionCompleta;
    private Boolean informacionTecnicaCompleta;
    private Boolean informacionValoracionCompleta;
    private Boolean informacionSeguroCompleta;

    private Boolean informacionCompleta;
    private Integer porcentajeCompletitud;

    // =========================================================
    // Estado general
    // =========================================================
    private String estadoBien;
    private Boolean activo;

    private Boolean tieneValorComercial;
    private Boolean requiereActualizacion;
    private Boolean requiereRevision;

    // =========================================================
    // Alertas gerenciales
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
    // Auditoría
    // =========================================================
    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;

    // =========================================================
    // Constructor
    // =========================================================
    public ExpedienteBienMaquinariaDTO() {

        this.porcentajePropiedad = BigDecimal.valueOf(100);
        this.valorParticipacion = BigDecimal.ZERO;

        this.titularPrincipal = Boolean.FALSE;
        this.propiedadCompartida = Boolean.FALSE;
        this.cantidadPropietarios = 1;

        this.valorComercial = BigDecimal.ZERO;
        this.valorGravamen = BigDecimal.ZERO;
        this.valorNeto = BigDecimal.ZERO;

        this.tieneGravamen = Boolean.FALSE;
        this.porcentajeGravamen = BigDecimal.ZERO;

        this.gravamenSuperaValorComercial = Boolean.FALSE;
        this.valorNetoNegativo = Boolean.FALSE;

        this.valorAsegurado = BigDecimal.ZERO;
        this.diasParaVencimientoSeguro = 0;

        this.tieneSeguro = Boolean.FALSE;
        this.seguroVigente = Boolean.FALSE;
        this.seguroProximoVencer = Boolean.FALSE;
        this.seguroVencido = Boolean.FALSE;

        this.porcentajeCoberturaSeguro = BigDecimal.ZERO;
        this.seguroCubreValorComercial = Boolean.FALSE;

        this.vinculadoComoGarantia = Boolean.FALSE;
        this.cantidadCreditosGarantizados = 0;

        this.saldoCreditosGarantizados = BigDecimal.ZERO;
        this.valorDisponibleGarantia = BigDecimal.ZERO;
        this.porcentajeCoberturaGarantia = BigDecimal.ZERO;

        this.garantiaSuficiente = Boolean.FALSE;
        this.garantiaSobreutilizada = Boolean.FALSE;

        this.informacionIdentificacionCompleta = Boolean.FALSE;
        this.informacionTecnicaCompleta = Boolean.FALSE;
        this.informacionValoracionCompleta = Boolean.FALSE;
        this.informacionSeguroCompleta = Boolean.FALSE;

        this.informacionCompleta = Boolean.FALSE;
        this.porcentajeCompletitud = 0;

        this.activo = Boolean.TRUE;
        this.tieneValorComercial = Boolean.FALSE;

        this.requiereActualizacion = Boolean.FALSE;
        this.requiereRevision = Boolean.FALSE;

        this.cantidadAlertas = 0;
        this.alertasCriticas = 0;
        this.alertasAdvertencia = 0;
        this.alertasInformativas = 0;
    }

    // =========================================================
    // Cálculos patrimoniales
    // =========================================================

    /**
     * Calcula el valor de la participación del asociado sobre
     * el valor comercial total de la maquinaria.
     */
    public BigDecimal calcularValorParticipacion() {

        this.valorParticipacion =
                valorSeguro(valorComercial)
                        .multiply(valorSeguro(porcentajePropiedad))
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );

        return this.valorParticipacion;
    }

    /**
     * Calcula el valor neto después de descontar el gravamen.
     */
    public BigDecimal calcularValorNeto() {

        this.valorNeto =
                valorSeguro(valorComercial)
                        .subtract(valorSeguro(valorGravamen));

        this.valorNetoNegativo =
                this.valorNeto.compareTo(BigDecimal.ZERO) < 0;

        this.gravamenSuperaValorComercial =
                valorSeguro(valorGravamen)
                        .compareTo(valorSeguro(valorComercial)) > 0;

        return this.valorNeto;
    }

    /**
     * Calcula qué porcentaje representa el gravamen frente al
     * valor comercial de la maquinaria.
     */
    public BigDecimal calcularPorcentajeGravamen() {

        BigDecimal comercial =
                valorSeguro(valorComercial);

        if (comercial.compareTo(BigDecimal.ZERO) <= 0) {

            this.porcentajeGravamen = BigDecimal.ZERO;
            return this.porcentajeGravamen;
        }

        this.porcentajeGravamen =
                valorSeguro(valorGravamen)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                comercial,
                                2,
                                RoundingMode.HALF_UP
                        );

        return this.porcentajeGravamen;
    }

    // =========================================================
    // Titularidad
    // =========================================================

    /**
     * Evalúa si la maquinaria es de propiedad compartida y si
     * el asociado es titular principal.
     */
    public void evaluarTitularidad() {

        BigDecimal porcentaje =
                valorSeguro(porcentajePropiedad);

        this.propiedadCompartida =
                porcentaje.compareTo(BigDecimal.valueOf(100)) < 0
                        || (
                        cantidadPropietarios != null
                                && cantidadPropietarios > 1
                );

        this.titularPrincipal =
                porcentaje.compareTo(BigDecimal.valueOf(50)) >= 0;
    }

    // =========================================================
    // Seguro
    // =========================================================

    /**
     * Evalúa la existencia y vigencia del seguro.
     */
    public void evaluarSeguro(int diasAdvertencia) {

        this.tieneSeguro =
                tieneTexto(aseguradora)
                        || tieneTexto(numeroPoliza)
                        || fechaInicioSeguro != null
                        || fechaVencimientoSeguro != null
                        || valorSeguro(valorAsegurado)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.seguroVigente = Boolean.FALSE;
        this.seguroProximoVencer = Boolean.FALSE;
        this.seguroVencido = Boolean.FALSE;

        if (fechaVencimientoSeguro == null) {

            this.diasParaVencimientoSeguro = 0;
            calcularCoberturaSeguro();
            return;
        }

        long dias =
                ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        fechaVencimientoSeguro
                );

        this.diasParaVencimientoSeguro =
                convertirEnteroSeguro(dias);

        int advertencia =
                Math.max(diasAdvertencia, 0);

        if (dias < 0) {

            this.seguroVencido = Boolean.TRUE;

        } else if (dias <= advertencia) {

            this.seguroVigente = Boolean.TRUE;
            this.seguroProximoVencer = Boolean.TRUE;

        } else {

            this.seguroVigente = Boolean.TRUE;
        }

        calcularCoberturaSeguro();
    }

    /**
     * Calcula el porcentaje asegurado frente al valor comercial.
     */
    public BigDecimal calcularCoberturaSeguro() {

        BigDecimal comercial =
                valorSeguro(valorComercial);

        if (comercial.compareTo(BigDecimal.ZERO) <= 0) {

            this.porcentajeCoberturaSeguro = BigDecimal.ZERO;
            this.seguroCubreValorComercial = Boolean.FALSE;

            return this.porcentajeCoberturaSeguro;
        }

        this.porcentajeCoberturaSeguro =
                valorSeguro(valorAsegurado)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                comercial,
                                2,
                                RoundingMode.HALF_UP
                        );

        this.seguroCubreValorComercial =
                valorSeguro(valorAsegurado)
                        .compareTo(comercial) >= 0;

        return this.porcentajeCoberturaSeguro;
    }

    // =========================================================
    // Garantías
    // =========================================================

    /**
     * Calcula la cobertura disponible frente a los créditos
     * garantizados por la maquinaria.
     */
    public BigDecimal calcularCoberturaGarantia() {

        BigDecimal saldo =
                valorSeguro(saldoCreditosGarantizados);

        BigDecimal disponible =
                valorSeguro(valorNeto)
                        .max(BigDecimal.ZERO);

        this.valorDisponibleGarantia =
                disponible.subtract(saldo);

        this.garantiaSobreutilizada =
                this.valorDisponibleGarantia
                        .compareTo(BigDecimal.ZERO) < 0;

        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {

            this.porcentajeCoberturaGarantia = BigDecimal.ZERO;
            this.garantiaSuficiente = Boolean.TRUE;

            return this.porcentajeCoberturaGarantia;
        }

        this.porcentajeCoberturaGarantia =
                disponible
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                saldo,
                                2,
                                RoundingMode.HALF_UP
                        );

        this.garantiaSuficiente =
                disponible.compareTo(saldo) >= 0;

        return this.porcentajeCoberturaGarantia;
    }

    // =========================================================
    // Calidad de información
    // =========================================================

    /**
     * Evalúa la completitud de la información relevante.
     */
    public int evaluarCompletitud() {

        this.informacionIdentificacionCompleta =
                idTipoMaquinaria != null
                        && tieneTexto(marca)
                        && tieneTexto(modelo)
                        && tieneTexto(serie);

        this.informacionTecnicaCompleta =
                tieneTexto(descripcionTecnica);

        this.informacionValoracionCompleta =
                valorSeguro(valorComercial)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.informacionSeguroCompleta =
                !Boolean.TRUE.equals(tieneSeguro)
                        || (
                        tieneTexto(aseguradora)
                                && tieneTexto(numeroPoliza)
                                && fechaVencimientoSeguro != null
                                && valorSeguro(valorAsegurado)
                                .compareTo(BigDecimal.ZERO) > 0
                );

        int totalValidaciones = 4;
        int validacionesCompletas = 0;

        if (Boolean.TRUE.equals(
                informacionIdentificacionCompleta)) {

            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionTecnicaCompleta)) {

            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionValoracionCompleta)) {

            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionSeguroCompleta)) {

            validacionesCompletas++;
        }

        this.porcentajeCompletitud =
                (validacionesCompletas * 100)
                        / totalValidaciones;

        this.informacionCompleta =
                validacionesCompletas == totalValidaciones;

        return this.porcentajeCompletitud;
    }

    // =========================================================
    // Estado general y alertas
    // =========================================================

    /**
     * Consolida indicadores y alertas gerenciales.
     */
    public void evaluarEstadoGeneral(
            int diasAdvertenciaSeguro
    ) {

        calcularValorParticipacion();
        calcularValorNeto();
        calcularPorcentajeGravamen();

        evaluarTitularidad();
        evaluarSeguro(diasAdvertenciaSeguro);
        calcularCoberturaGarantia();
        evaluarCompletitud();

        this.tieneValorComercial =
                valorSeguro(valorComercial)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.tieneGravamen =
                idTipoGravamen != null
                        && valorSeguro(valorGravamen)
                        .compareTo(BigDecimal.ZERO) > 0;

        int criticas = 0;
        int advertencias = 0;
        int informativas = 0;

        // -----------------------------------------------------
        // Alertas críticas
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(seguroVencido)
                && Boolean.TRUE.equals(vinculadoComoGarantia)) {

            criticas++;
        }

        if (Boolean.TRUE.equals(
                gravamenSuperaValorComercial)) {

            criticas++;
        }

        if (Boolean.TRUE.equals(
                garantiaSobreutilizada)) {

            criticas++;
        }

        // -----------------------------------------------------
        // Advertencias
        // -----------------------------------------------------
        if (!Boolean.TRUE.equals(tieneValorComercial)) {

            advertencias++;
        }

        if (Boolean.TRUE.equals(seguroVencido)
                && !Boolean.TRUE.equals(vinculadoComoGarantia)) {

            advertencias++;
        }

        if (Boolean.TRUE.equals(seguroProximoVencer)) {

            advertencias++;
        }

        if (Boolean.TRUE.equals(vinculadoComoGarantia)
                && !Boolean.TRUE.equals(tieneSeguro)) {

            advertencias++;
        }

        if (Boolean.TRUE.equals(vinculadoComoGarantia)
                && Boolean.TRUE.equals(tieneSeguro)
                && !Boolean.TRUE.equals(
                seguroCubreValorComercial)) {

            advertencias++;
        }

        if (Boolean.TRUE.equals(vinculadoComoGarantia)
                && !Boolean.TRUE.equals(
                garantiaSuficiente)) {

            advertencias++;
        }

        if (!Boolean.TRUE.equals(informacionCompleta)) {

            advertencias++;
        }

        // -----------------------------------------------------
        // Informativas
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(propiedadCompartida)) {

            informativas++;
        }

        if (Boolean.TRUE.equals(tieneGravamen)) {

            informativas++;
        }

        if (Boolean.TRUE.equals(vinculadoComoGarantia)) {

            informativas++;
        }

        this.alertasCriticas = criticas;
        this.alertasAdvertencia = advertencias;
        this.alertasInformativas = informativas;

        this.cantidadAlertas =
                criticas
                        + advertencias
                        + informativas;

        this.requiereActualizacion =
                Boolean.TRUE.equals(seguroVencido)
                        || !Boolean.TRUE.equals(
                        informacionCompleta);

        this.requiereRevision =
                criticas > 0
                        || advertencias > 0;

        if (criticas > 0) {

            this.nivelAlerta = "CRITICA";
            this.resumenAlertas =
                    "La maquinaria presenta situaciones de atención prioritaria.";

        } else if (advertencias > 0) {

            this.nivelAlerta = "ADVERTENCIA";
            this.resumenAlertas =
                    "La maquinaria requiere revisión o actualización.";

        } else if (informativas > 0) {

            this.nivelAlerta = "INFORMATIVA";
            this.resumenAlertas =
                    "La maquinaria presenta condiciones relevantes para consulta.";

        } else {

            this.nivelAlerta = "NORMAL";
            this.resumenAlertas =
                    "La maquinaria no presenta novedades gerenciales.";
        }
    }

    // =========================================================
    // Métodos privados
    // =========================================================

    private BigDecimal valorSeguro(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private boolean tieneTexto(
            String valor
    ) {

        return valor != null
                && !valor.isBlank();
    }

    private int convertirEnteroSeguro(
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